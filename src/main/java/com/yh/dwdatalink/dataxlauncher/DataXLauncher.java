package com.yh.dwdatalink.dataxlauncher;

import com.yh.dwdatalink.service.ProcessService;
import com.yh.dwdatalink.service.Suicider;
import com.yh.dwdatalink.util.LocalStreamGobbler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.Map;
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
    public static final String jobName = "DATAX_JOB";
    public static final String registryUrl = "DATAX_URL";

    private static final Logger logger = LoggerFactory.getLogger(DataXLauncher.class);

    @Autowired
    ProcessService procSvc;
    @Autowired
    Suicider suicider;

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
//        ExecutorService execService = Executors.newSingleThreadExecutor();

        String theJobDescJson = "{\"job\":{\"setting\":{\"speed\":{\"byte\":10485760},\"errorLimit\":{\"record\":0,\"percentage\":0.02}},\"content\":[{\"reader\":{\"name\":\"streamreader\",\"parameter\":{\"column\":[{\"value\":\"DataX\",\"type\":\"string\"},{\"value\":19990604,\"type\":\"long\"},{\"value\":\"1989-06-05 00:00:00\",\"type\":\"date\"},{\"value\":true,\"type\":\"bool\"},{\"value\":\"test\",\"type\":\"bytes\"}],\"sliceRecordCount\":100000}},\"writer\":{\"name\":\"streamwriter\",\"parameter\":{\"print\":false,\"encoding\":\"UTF-8\"}}}]}}";
        System.err.println(theJobDescJson);

        InetAddress address = InetAddress.getLocalHost();
        String localIp = address.getHostAddress();
        logger.error(localIp);


        Map<String, String> envVar = System.getenv();
        final String currentJobName = envVar.get(jobName);
        final String currentRegistUrl = envVar.get(registryUrl);
        logger.info("get variable from system,the job name:{}, to ip:{}",
                currentJobName,currentRegistUrl);
        if(currentJobName ==null || currentRegistUrl==null){
            System.exit(1);
        }

//        CloseableHttpClient client = HttpClients.createDefault();
//        final String compelteUrl = currentRegistUrl + currentJobName;
//        HttpPost request = new HttpPost(compelteUrl);
//
//        CloseableHttpResponse response =null;

//        response = client.execute(request);



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

//        Future<Integer> runFuture = execService.submit(new dataXCaller("sleep 10"));
//        System.err.println(runFuture.get());

        Process p = Runtime.getRuntime().exec(startString);
        procSvc.setProc(p);

        LocalStreamGobbler outputGobbler = new
                LocalStreamGobbler(p.getInputStream(), LocalStreamGobbler.stdout);

        LocalStreamGobbler errGobbler = new
                LocalStreamGobbler(p.getErrorStream(), LocalStreamGobbler.stderr);
        errGobbler.start();
        outputGobbler.start();


        int exitVal = p.waitFor();
        while (errGobbler.isAlive() && outputGobbler.isAlive()){
            Thread.sleep(1*1000L);
        }


        suicider.suicide(1);
    }

}
