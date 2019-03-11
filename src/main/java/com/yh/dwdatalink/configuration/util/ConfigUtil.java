package com.yh.dwdatalink.configuration.util;

import com.yh.dwdatalink.configuration.DataXException;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.HttpGet;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static com.yh.dwdatalink.configuration.Configuration.configErr;

/**
 * Created by zhou1 on 2019/3/11.
 */
public class ConfigUtil {

    public static String getJobContent(String jobResource) {
        String jobContent;

        boolean isJobResourceFromHttp = jobResource.trim().toLowerCase().startsWith("http");


        if (isJobResourceFromHttp) {
            //设置httpclient的 HTTP_TIMEOUT_INMILLIONSECONDS
//            Configuration coreConfig = ConfigParser.parseCoreConfig(CoreConstant.DATAX_CONF_PATH);
            int httpTimeOutInMillionSeconds = 5000;
            HttpClientUtil.setHttpTimeoutInMillionSeconds(httpTimeOutInMillionSeconds);

            HttpClientUtil httpClientUtil = new HttpClientUtil();
            try {
                URL url = new URL(jobResource);
                HttpGet httpGet = HttpClientUtil.getGetRequest();
                httpGet.setURI(url.toURI());

                jobContent = httpClientUtil.executeAndGetWithFailedRetry(httpGet, 1, 1000l);
            } catch (Exception e) {
                throw DataXException.asDataXException(configErr, "获取作业配置信息失败:" + jobResource, e);
            }finally {
                httpClientUtil.destroy();
                httpClientUtil.asyncExecutor.shutdown();
            }
        } else {
            // jobResource 是本地文件绝对路径
            try {
                jobContent = FileUtils.readFileToString(new File(jobResource));
            } catch (IOException e) {
                throw DataXException.asDataXException(configErr, "获取作业配置信息失败:" + jobResource, e);
            }
        }

        if (jobContent == null) {
            throw DataXException.asDataXException(configErr, "获取作业配置信息失败:" + jobResource);
        }
        return jobContent;
    }
}
