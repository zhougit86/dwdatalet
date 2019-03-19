package com.yh.dwdatalink.dataxlauncher;

import com.alibaba.fastjson.JSON;
import com.yh.dwdatalink.configuration.util.ConfigUtil;
import com.yh.dwdatalink.configuration.util.FileUtil;
import com.yh.dwdatalink.configuration.util.JobStatus;
import com.yh.dwdatalink.configuration.util.ZKlient;
import com.yh.dwdatalink.domain.RunningServer;
import com.yh.dwdatalink.domain.RunningServerWithBLOBs;
import com.yh.dwdatalink.mapper.RunningServerMapper;
import com.yh.dwdatalink.service.ProcessService;
import com.yh.dwdatalink.service.Suicider;
import com.yh.dwdatalink.util.LocalStreamGobbler;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.Calendar;
import java.util.Map;

import static com.yh.dwdatalink.configuration.util.JobStatus.*;

/**
 * Created by zhou1 on 2019/3/5.
 */
@Component
@Order(value = 1)
public class DataXLauncher implements ApplicationRunner {
    public static final String podType = "datax";

    public static final String jobNameConst = "DATAX_JOB";
    public static final String registryUrlConst = "DATAX_URL";
    public static final String jobGroupConst = "DATAX_GRP";

    final String jobConfigFileName = "job.json";
    final String dataxPath = "/usr/bin/datax";

    private static final Logger logger = LoggerFactory.getLogger(DataXLauncher.class);

    @Autowired
    ProcessService procSvc;
    @Autowired
    Suicider suicider;
    @Autowired
    RunningServerMapper runningServerMapper;

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {


//        ExecutorService execService = Executors.newSingleThreadExecutor();

//        String theJobDescJson = "{\"job\":{\"setting\":{\"speed\":{\"byte\":10485760},\"errorLimit\":{\"record\":0,\"percentage\":0.02}},\"content\":[{\"reader\":{\"name\":\"streamreader\",\"parameter\":{\"column\":[{\"value\":\"DataX\",\"type\":\"string\"},{\"value\":19990604,\"type\":\"long\"},{\"value\":\"1989-06-05 00:00:00\",\"type\":\"date\"},{\"value\":true,\"type\":\"bool\"},{\"value\":\"test\",\"type\":\"bytes\"}],\"sliceRecordCount\":100000}},\"writer\":{\"name\":\"streamwriter\",\"parameter\":{\"print\":false,\"encoding\":\"UTF-8\"}}}]}}";
//        System.err.println(theJobDescJson);

        InetAddress address = InetAddress.getLocalHost();
        String localIp = address.getHostAddress();
        logger.info("my ip is {}",localIp);




        Map<String, String> envVar = System.getenv();
        suicider.currentJobName = envVar.get(jobNameConst);
        suicider.currentJobGroup = envVar.get(jobGroupConst);
        final String currentRegistUrl = envVar.get(registryUrlConst);

        suicider.podIp = envVar.get("POD_IP");
        suicider.podName = envVar.get("POD_NAME");
        logger.info("get variable from system,the job name:{}, to ip:{}",
                suicider.currentJobName,currentRegistUrl);
        if(suicider.currentJobName ==null || currentRegistUrl==null
                || suicider.currentJobGroup ==null){
            System.exit(1);
        }

        RunningServerWithBLOBs rs = new RunningServerWithBLOBs();
        rs.setTaskCode(suicider.currentJobName);
        rs.setServiceIp(suicider.podIp);
        rs.setServicePod(suicider.podName);
        rs.setRunState(jobStatusReady);
        rs.setRunBegin(Calendar.getInstance().getTime());
//        rs.setServiceInfo(String.format("%s--%s",Calendar.getInstance().getTime(),"enter into ready state"));

        runningServerMapper.insertSelective(rs);
        suicider.podState = rs;

        int retryTimes = 0;
        while(suicider.client == null && retryTimes<5){
            try{
                suicider.client = new ZKlient(podType,currentRegistUrl);
            }catch (Exception e){
                e.printStackTrace();
            }
            retryTimes++;
        }

        if (suicider.client == null){
            suicider.suicide(1,jobStatusError);
        }
        String conf =  suicider.client.getNode(suicider.currentJobGroup,suicider.currentJobName);

        logger.info("get job config from zk:\n {}",conf);

        suicider.client.setNodeStatus(suicider.currentJobGroup
                ,suicider.currentJobName
                ,new JobStatus(jobStatusReady,localIp));


//        CloseableHttpClient client = HttpClients.createDefault();
//        final String compelteUrl = currentRegistUrl + currentJobName;
//        HttpPost request = new HttpPost(compelteUrl);
//
//        CloseableHttpResponse response =null;

//        response = client.execute(request);

//        String conf =  ConfigUtil.getJobContent("http://127.0.0.1:8085/config/oxox");
//        String conf =  ConfigUtil.getJobContent(registryUrlConst + jobConfigFileName);

        String jobPath = String.format("%s/job/", dataxPath);
        jobPath+=jobConfigFileName;
//        System.err.println(conf);
        try{
            FileUtil.writeFile(jobPath,conf);
        }catch (Exception e){
            e.printStackTrace();
        }


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

        StringBuilder sb = new StringBuilder();
        sb.append(startJobJvmMetrics);
        sb.append(startJobSystemMetrics);
        sb.append(startJobEntryClass);
        sb.append(startJobinputArgs);
        sb.append(jobConfigFileName);
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

        suicider.client.setNodeStatus(suicider.currentJobGroup
                ,suicider.currentJobName
                ,new JobStatus(jobStatusRunning,localIp));

        rs.setRunState(jobStatusRunning);
//        rs.appendServiceInfo(String.format("%s--%s",Calendar.getInstance().getTime(),"enter into running state"));
        runningServerMapper.updateByPrimaryKeySelective(rs);

        int exitVal = p.waitFor();
        while (errGobbler.isAlive() && outputGobbler.isAlive()){
            Thread.sleep(1*1000L);
        }


        suicider.suicide(1,exitVal==0?jobStatusFinish:jobStatusError);
    }

}
