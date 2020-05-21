package com.caitaojun.ctjdfs.server;

import com.caitaojun.ctjdfs.model.StorageFileInfo;
import com.caitaojun.ctjdfs.model.StorageServerModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by caitaojun on 2019/1/2 0002.
 */
public class OnLineStorageServers {
    //key 127.0.0.1:2689   记录心跳的storage服务列表
    public static HashMap<String,StorageServerModel> onLines = new HashMap<>();

    public static HashMap<String,StorageFileInfo> storages = new HashMap<>();
}
