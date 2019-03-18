package com.yh.dwdatalink.configuration.util;


import com.alibaba.fastjson.JSONObject;

/**
 * Created by zhou1 on 2019/3/15.
 */
public class JobStatus {

    public static final String jobStatusReady = "READY";
    public static final String jobStatusRunning = "RUNNING";
    public static final String jobStatusFinish = "FINISH";
    public static final String jobStatusKilled = "KILLED";
    public static final String jobStatusError = "ERROR";

    public String jobPhase;
    public String podIp;

    public JobStatus(){}

    public JobStatus(String phase, String ip){
        this.jobPhase = phase;
        this.podIp = ip;
    }

    public String getJobPhase() {
        return jobPhase;
    }

    public void setJobPhase(String jobPhase) {
        this.jobPhase = jobPhase;
    }

    public String getPodIp() {
        return podIp;
    }

    public void setPodIp(String podIp) {
        this.podIp = podIp;
    }

    public String toString(){
        return JSONObject.toJSONString(this);
    }
}
