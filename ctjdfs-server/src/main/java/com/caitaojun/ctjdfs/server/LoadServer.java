package com.caitaojun.ctjdfs.server;

import com.caitaojun.ctjdfs.system.SystemConstant;
import org.apache.commons.lang3.StringUtils;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * Created by caitaojun on 2019/1/2 0002.
 */
public class LoadServer {
    public static void main(String[] args) throws Exception {
        //1.获取服务角色
        String dfsrole = System.getProperty("dfsRole", SystemConstant.serverRole);
        //2.获取端口
        String dfsportStr = System.getProperty("dfsPort", SystemConstant.serverPort);
        Integer dfsport = Integer.valueOf(dfsportStr);
        //3.tracker地址
        String trackerAddress = System.getProperty("dfsTrackerAddress",SystemConstant.trackerAddress);
        //4.心跳间隔（秒）
        String heartBeatInterval = System.getProperty("dfsHeartBeatInterval", SystemConstant.heartbeatInterval);
        //5.storage地址
        String storageAddress = System.getProperty("dfsStorageAddress",SystemConstant.storageAddress);
        //6.storage文件存储地址
        String storageFilePath = System.getProperty("dfsStorageFilePath",SystemConstant.storageFilePath);
        //7.tracker数据文件存储地址
        String trackerDataPath = System.getProperty("dfsTrackerDataPath",SystemConstant.trackerDataPath);
        //8.通过外置配置文件加载
        String configFile = System.getProperty("dfsConfigFile");
        //9.storage文件存储块大小
        String storageBlockSize = System.getProperty("dfsBlockSize",SystemConstant.storageBlockSize);

//        System.out.println("configFile:"+configFile);
        if (StringUtils.isNotBlank(configFile)){
            Properties properties = new Properties();
            FileInputStream inputstream = new FileInputStream(configFile);
            properties.load(inputstream);
            dfsrole = properties.getProperty("ctjdfs.role");
            dfsport = Integer.valueOf(properties.getProperty("ctjdfs.port"));
            trackerAddress = properties.getProperty("ctjdfs.tracker");
            heartBeatInterval = properties.getProperty("ctjdfs.heartbeat.interval");
            storageAddress = properties.getProperty("ctjdfs.storage");
            storageFilePath = properties.getProperty("ctjdfs.storage.file.path");
            trackerDataPath = properties.getProperty("ctjdfs.tracker.data.path");
            storageBlockSize = properties.getProperty("ctjdfs.storage.block.size");
        }

//        System.out.println(dfsrole);

        SystemConstant.serverRole = dfsrole;
        SystemConstant.serverPort = dfsportStr;
        SystemConstant.trackerAddress =trackerAddress;
        SystemConstant.heartbeatInterval = heartBeatInterval;
        SystemConstant.storageAddress = storageAddress;
        SystemConstant.storageFilePath = storageFilePath;
        SystemConstant.trackerDataPath = trackerDataPath;
        SystemConstant.storageBlockSize = storageBlockSize;

        System.out.println("Welcome Use CtjDfs");
        if ("tracker".equals(dfsrole)){
            TrackerServer.handle(dfsport,trackerDataPath);
        }else if ("storage".equals(dfsrole)){
            StorageServer.handle(dfsport,trackerAddress,heartBeatInterval,storageAddress,storageFilePath);
        }
    }
}
