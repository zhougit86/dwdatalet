package com.yh.dwdatalink.configuration.util;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zhou1 on 2019/3/12.
 */
public class ZKlient {
    private static final Logger logger = LoggerFactory.getLogger(ZKlient.class);

    /** session超时时间 */
    static final int sessionTimeout = 5000;
    final String connectString;
    final String jobDataPath;

    private ZkClient zkClient;

    public ZKlient(String jobType, String zkAddr) throws Exception{
        logger.info("connecting to the zk {} ",zkAddr);
        connectString = zkAddr;
        zkClient = new ZkClient(new ZkConnection(connectString), sessionTimeout);


        jobDataPath = String.format("/job/%s",jobType);
        if(!zkClient.exists(jobDataPath)){
            zkClient.createPersistent(jobDataPath,true);
        }
        logger.info("connecting to the zk {} successfully",zkAddr);
    }

    public boolean checkJobConfExists(final String groupName,final String jobName){
        final String jobPath = String.format("%s/%s/%s",jobDataPath,groupName,jobName);
        return zkClient.exists(jobPath);
    }

    public void setNodeValue(final String groupName,final String jobName,final String jobValue){
        final String jobPath = String.format("%s/%s/%s",jobDataPath,groupName,jobName);
        if(!checkJobConfExists(groupName,jobName)){
            zkClient.createPersistent(jobPath,true);
        }
        zkClient.writeData(jobPath,jobValue);
        logger.info("write zk node {} successfully",jobPath);
    }

    public void deleteNode(final String groupName,final String jobName){
        final String jobPath = String.format("%s/%s/%s",jobDataPath,groupName,jobName);
        zkClient.delete(jobPath);
        logger.info("delete zk node {} successfully",jobPath);
    }

    public String getNode(final String groupName,final String jobName){
        final String jobPath = String.format("%s/%s/%s",jobDataPath,groupName,jobName);
        String value = zkClient.readData(jobPath);
        logger.info("read zk node {} finished, if null: {}",jobPath,value==null?"nullValue":"Something");
        return value;
    }

    public void release(){

        logger.info("releasing zookeeper");
        zkClient.close();
    }



}
