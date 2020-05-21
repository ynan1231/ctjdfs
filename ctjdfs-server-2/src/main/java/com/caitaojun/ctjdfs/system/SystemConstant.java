package com.caitaojun.ctjdfs.system;

import org.apache.commons.lang3.StringUtils;

import java.util.ResourceBundle;

/**
 * Created by caitaojun on 2019/1/2 0002.
 */
public class SystemConstant {
    //服务角色
    public static String serverRole = null;
    //服务端口
    public static String serverPort = null;
    //tracker服务地址
    public static String trackerAddress = null;
    //storage向tracker发送心跳的间隔(秒)
    public static String heartbeatInterval = null;
    //storage服务地址
    public static String storageAddress = null;
    //tracker数据文件存储路径（数据库文件）
    public static String trackerDataPath = null;
    //storage文件存储路径
    public static String storageFilePath = null;
    //storage文件块存储大小
    public static String storageBlockSize = null;
    static {
        ResourceBundle rs = ResourceBundle.getBundle("server");
        serverRole = rs.getString("ctjdfs.role");
        serverPort = rs.getString("ctjdfs.port");
        trackerAddress = rs.getString("ctjdfs.tracker");
        heartbeatInterval = rs.getString("ctjdfs.heartbeat.interval");
        storageAddress = rs.getString("ctjdfs.storage");
        trackerDataPath = rs.getString("ctjdfs.tracker.data.path");
        storageFilePath = rs.getString("ctjdfs.storage.file.path");
        storageBlockSize = rs.getString("ctjdfs.storage.block.size");
    }
}
