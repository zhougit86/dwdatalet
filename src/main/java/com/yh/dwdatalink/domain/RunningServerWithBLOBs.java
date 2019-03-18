package com.yh.dwdatalink.domain;

public class RunningServerWithBLOBs extends RunningServer {
    private String serviceInfo;

    private String runInfo;

    public String getServiceInfo() {
        return serviceInfo;
    }

    public void setServiceInfo(String serviceInfo) {
        this.serviceInfo = serviceInfo == null ? null : serviceInfo.trim();
    }

    public String getRunInfo() {
        return runInfo;
    }

    public void setRunInfo(String runInfo) {
        this.runInfo = runInfo == null ? null : runInfo.trim();
    }
}