package com.yh.dwdatalink.service;

import com.yh.dwdatalink.domain.RunningServerWithBLOBs;
import com.yh.dwdatalink.mapper.RunningServerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by zhou1 on 2019/3/18.
 */
@Service
public class RunServerSvc {
    @Autowired
    RunningServerMapper runningServerMapper;

    public void createRunServer(RunningServerWithBLOBs runServer){
        runningServerMapper.insertSelective(runServer);
    }
}
