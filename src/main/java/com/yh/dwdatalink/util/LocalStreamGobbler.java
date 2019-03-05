package com.yh.dwdatalink.util;

import com.yh.dwdatalink.dataxlauncher.DataXLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by zhou1 on 2019/2/28.
 */
public class LocalStreamGobbler extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(LocalStreamGobbler.class);
    public static final String stdout = "stdout";
    public static final String stderr = "stderr";

    InputStream is;
    String type;

    public LocalStreamGobbler(InputStream is, String type) {
        this.is = is;
        this.type = type;

    }

    public void run() {
        Thread.currentThread().setName(type);
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null)
                logger.info(line);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        logger.info("ended");
    }
}
