package com.yh.dwdatalink.service;

import org.springframework.stereotype.Service;

/**
 * Created by zhou1 on 2019/3/5.
 */
@Service
public class ProcessService {
    Process proc;

    public void setProc(Process p){
        this.proc = p;
    }

    public Process getProc(){
        return this.proc;
    }

    public void killProc() throws Exception{
        if (this.proc != null && this.proc.isAlive()){
            this.proc.destroy();
        }
        throw new Exception("no proc running now");
    }
}
