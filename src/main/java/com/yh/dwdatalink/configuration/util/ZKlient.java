package com.yh.dwdatalink.configuration.util;

import com.alibaba.fastjson.JSON;
import org.I0Itec.zkclient.IZkDataListener;
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
    public static final String statusNodeString = "status";

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

    public void setNodeValue(final String groupName,final String jobName,final Object jobValue){
        final String jobPath = String.format("%s/%s/%s",jobDataPath,groupName,jobName);
        if(!checkJobConfExists(groupName,jobName)){
            zkClient.createPersistent(jobPath,true);
        }
        zkClient.writeData(jobPath,jobValue);
        logger.info("write zk node {} successfully",jobPath);
    }

    public void setNodeStatus(String groupName, String jobName, JobStatus status){
        final String jobStatusPath =
                String.format("%s/%s/%s/%s",jobDataPath,groupName,jobName,statusNodeString);
        if(!zkClient.exists(jobStatusPath)){
            zkClient.createPersistent(jobStatusPath,true);
        }
        zkClient.writeData(jobStatusPath, JSON.toJSON(status));
    }

    public void subscribeNodeStatus(String groupName, String jobName, IZkDataListener zkListener){
        final String jobStatusPath =
                String.format("%s/%s/%s/%s",jobDataPath,groupName,jobName,statusNodeString);
        if(!zkClient.exists(jobStatusPath)){
            zkClient.createPersistent(jobStatusPath,true);
        }
        zkClient.subscribeDataChanges(jobStatusPath, zkListener);
    }

    public void deleteNode(final String groupName,final String jobName){
        final String jobPath = String.format("%s/%s/%s",jobDataPath,groupName,jobName);
        final String jobStatusPath =
                String.format("%s/%s/%s/%s",jobDataPath,groupName,jobName,statusNodeString);
        zkClient.delete(jobStatusPath);
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
