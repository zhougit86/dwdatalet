package com.yh.dwdatalink.service;

import com.yh.dwdatalink.DwdataletApplication;
import com.yh.dwdatalink.configuration.util.JobStatus;
import com.yh.dwdatalink.configuration.util.ZKlient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.yh.dwdatalink.configuration.util.JobStatus.jobStatusRunning;

@Component
public class Suicider {
    private static final Logger logger = LoggerFactory.getLogger(Suicider.class);
    public static ZKlient client;

    public static String currentJobName;
    public static String currentJobGroup;

    public static String podIp;
    public static String podName;


    public void suicide(long seconds, String cause){
        Thread suicideThread = new SuicideThread(seconds,cause);
        suicideThread.start();

    }

    class SuicideThread extends Thread{
        private final Logger logger = LoggerFactory.getLogger(SuicideThread.class);
        private long secondsToDie;
        private String exitCause;

        protected SuicideThread(long seconds, String cause){
            this.secondsToDie = seconds;
            this.exitCause = cause;
        }

        @Override
        public void run() {
            Thread.currentThread().setName("exitThread");
            try {
                Thread.sleep(secondsToDie*1000L);
            }
            catch (InterruptedException ex) {
                logger.error("the suicide count down got some problem: {}, but will be ignored",
                        ex.getMessage());
            }
            try{
                client.setNodeStatus(currentJobGroup
                        ,currentJobName
                        ,new JobStatus(exitCause,"null"));
                client.release();
            }catch (Exception e){
                logger.info("release error {}",e.getMessage());
            }
            DwdataletApplication.context.close();
        }

    }
}