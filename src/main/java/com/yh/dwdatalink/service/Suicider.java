package com.yh.dwdatalink.service;

import com.yh.dwdatalink.DwdataletApplication;
import com.yh.dwdatalink.configuration.util.ZKlient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Suicider {
    private static final Logger logger = LoggerFactory.getLogger(Suicider.class);
    public static ZKlient client;


    public void suicide(long seconds){
        Thread suicideThread = new SuicideThread(seconds);
        suicideThread.start();

    }

    class SuicideThread extends Thread{
        private final Logger logger = LoggerFactory.getLogger(SuicideThread.class);
        private long secondsToDie;

        protected SuicideThread(long seconds){
            this.secondsToDie = seconds;
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
                client.release();
            }catch (Exception e){
                logger.info("release error {}",e.getMessage());
            }
            DwdataletApplication.context.close();
        }

    }
}