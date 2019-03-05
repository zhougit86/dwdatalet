package com.yh.dwdatalink.dataxlauncher;

import com.yh.dwdatalink.service.ProcessService;
import com.yh.dwdatalink.util.LocalStreamGobbler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by zhou1 on 2019/3/5.
 */
@Component
@Order(value = 1)
public class DataXLauncher implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataXLauncher.class);

    @Autowired
    ProcessService procSvc;

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        ExecutorService execService = Executors.newSingleThreadExecutor();

        String dataxPath = "/usr/bin/datax";

        String startJobJvmMetrics =
                "java -server -Xms1g -Xmx1g " +
                        "-XX:+HeapDumpOnOutOfMemoryError " +
                        String.format("-XX:HeapDumpPath=%s/log ", dataxPath);
        String startJobEntryClass =
                "com.alibaba.datax.core.Engine ";

        String startJobSystemMetrics = String.format("-Dloglevel=info -Dfile.encoding=UTF-8 " +
                "-Dlogback.statusListenerClass=ch.qos.logback.core.status.NopStatusListener " +
                " -Ddatax.home=%s " +
                " -classpath %s/lib/*  " +
                "-Dlog.file.name=ob_mysql2stream_json ", dataxPath, dataxPath);
        String startJobinputArgs = String.format("-mode standalone -jobid -1" +
                " -job %s/job/", dataxPath);

        String jobName = "job.json";

        StringBuilder sb = new StringBuilder();
        sb.append(startJobJvmMetrics);
        sb.append(startJobSystemMetrics);
        sb.append(startJobEntryClass);
        sb.append(startJobinputArgs);
        sb.append(jobName);
        String startString = sb.toString();

//        System.err.println(startString);

        Future<Integer> runFuture = execService.submit(new dataXCaller(startString));
        System.err.println(runFuture.get());
    }

    class dataXCaller implements Callable<Integer>{
        String jobCmdLine;

        public dataXCaller(String cmd){
            this.jobCmdLine = cmd;
        }

        public Integer call() throws Exception {
            Process p = Runtime.getRuntime().exec(jobCmdLine);
            procSvc.setProc(p);

            LocalStreamGobbler outputGobbler = new
                    LocalStreamGobbler(p.getInputStream(), LocalStreamGobbler.stdout);

            LocalStreamGobbler errGobbler = new
                    LocalStreamGobbler(p.getErrorStream(), LocalStreamGobbler.stderr);
            errGobbler.start();
            outputGobbler.start();
            int exitVal = p.waitFor();

            return exitVal;
        }
    }
}
