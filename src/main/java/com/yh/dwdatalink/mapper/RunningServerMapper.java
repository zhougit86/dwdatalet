package com.yh.dwdatalink.mapper;

import com.yh.dwdatalink.domain.RunningServer;
import com.yh.dwdatalink.domain.RunningServerWithBLOBs;

public interface RunningServerMapper {
    int deleteByPrimaryKey(Long id);

    int insert(RunningServerWithBLOBs record);

    int insertSelective(RunningServerWithBLOBs record);

    RunningServerWithBLOBs selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(RunningServerWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(RunningServerWithBLOBs record);

    int updateByPrimaryKey(RunningServer record);
}