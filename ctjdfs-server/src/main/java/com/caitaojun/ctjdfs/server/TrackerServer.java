package com.caitaojun.ctjdfs.server;

import com.alibaba.fastjson.JSON;
import com.caitaojun.ctjdfs.model.StorageFileInfo;
import com.caitaojun.ctjdfs.model.StorageFileInfoNode;
import com.caitaojun.ctjdfs.model.StorageServerModel;
import com.caitaojun.ctjdfs.system.SystemConstant;
import com.caitaojun.ctjdfs.utils.H2DbHelper;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by caitaojun on 2019/1/2 0002.
 * √接收storage的心跳请求   1#127.0.0.1:2689
 * 接收client的请求
 *      √上传  2#1#hash值#size#name#metadata
 *      √获取  2#2#hash值
 *      √删除  2#3#hash值
 */
public class TrackerServer {

    private static void init(String trackerDataPath) throws SQLException {
        File file = new File(trackerDataPath);
        if (!file.exists()){
            file.mkdirs();
        }
        initDb();
    }

    private static void initDb() throws SQLException {
        //初始化数据库
        H2DbHelper.createTable();
    }

    public static void handle(Integer dfsport, String trackerDataPath) throws Exception {
        init(trackerDataPath);
        System.out.println("TrackerServer server running port："+dfsport);
        ServerSocket serverSocket = new ServerSocket(dfsport);
        String storageBlockSizeStr = SystemConstant.storageBlockSize;
        Integer storageBlockSize = Integer.valueOf(storageBlockSizeStr);
        while (true){
            Socket socket = serverSocket.accept();
            if (socket.isConnected()){
                System.out.println("connect server...");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        InputStream inputStream = null;
                        try {
                            //获取文件hash值，文件大小,分配storage的ip和端口
                            inputStream = socket.getInputStream();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                            String line = reader.readLine();
                            socket.shutdownInput();
                            if (StringUtils.isNotBlank(line)){
                                if (line.startsWith("1#")){
                                    //接收storage的心跳
                                    String ipAndPortStr = line.split("#")[1];
                                    String[] ipAndPort = ipAndPortStr.split(":");
                                    StorageServerModel storageServerModel = new StorageServerModel();
                                    storageServerModel.setIp(ipAndPort[0]);
                                    storageServerModel.setPort(Integer.valueOf(ipAndPort[1]));
                                    storageServerModel.setLastTimeMillis(System.currentTimeMillis());
                                    OnLineStorageServers.onLines.put(ipAndPortStr,storageServerModel);
                                }else if (line.startsWith("2#1")){
                                    //上传文件    2#1#hash值#size#name#metadata
                                    String hashAndSizeAndNameAndMetaData = line.substring("2#1#".length());//hash值#size#name#metadata
                                    String[] datas = hashAndSizeAndNameAndMetaData.split("#");

                                    //先检查这个上传的是否已经存在，如果存在就先删除掉
                                    StorageFileInfo info = H2DbHelper.findData(datas[0]);
                                    if (null!=info){
                                        //删除 storage中的文件 以及 数据库中的记录
                                        H2DbHelper.deleteData(info.getId());
                                        LinkedList<StorageFileInfoNode> storageNodes = info.getStorageNodes();
                                        for (StorageFileInfoNode storageNode : storageNodes) {
                                            String address = storageNode.getAddress();//127.0.0.01:2689
                                            String scopeSize = storageNode.getScope();//0-1689x
                                            String[] ipAndPort = address.split(":");
                                            String ip = ipAndPort[0];
                                            int port = Integer.valueOf(ipAndPort[1]);
                                            Socket connectStorageSocket = new Socket(ip,port);
                                            OutputStream os = connectStorageSocket.getOutputStream();
                                            os.write(("x"+info.getId()+"_"+scopeSize).getBytes());
                                            connectStorageSocket.close();
                                        }
                                    }

                                    StorageFileInfo fileInfo = new StorageFileInfo();
                                    fileInfo.setId(datas[0]);
                                    fileInfo.setSize(Long.valueOf(datas[1]));
                                    fileInfo.setName(datas[2]);
                                    fileInfo.setMetadate(JSON.parseObject(datas[3],Map.class));

                                    //文件size  字节（byte）  （字节---kb   1024）
                                    //每20mb切割
                                    String[] ips = OnLineStorageServers.onLines.keySet().toArray(new String[]{});
                                    Long size = Long.valueOf(datas[1]);
                                    if (size>(storageBlockSize*1024*1024)){
                                        Double val = (size*1.0/(storageBlockSize*1024*1024));
                                        val = Math.ceil(val);
                                        Long count = val.longValue();
                                        for (Long i = 0L; i < count; i++) {
                                            String ip = loadBalance(Arrays.asList(ips));
                                            if (i==(count-1)){
                                                StorageFileInfoNode node = new StorageFileInfoNode();
                                                node.setAddress(ip);
                                                node.setScope(i*storageBlockSize*1024*1024+"-"+size);
                                                fileInfo.getStorageNodes().add(node);
                                            }else{
                                                StorageFileInfoNode node = new StorageFileInfoNode();
                                                node.setAddress(ip);
                                                node.setScope(i*storageBlockSize*1024*1024+"-"+(i+1)*storageBlockSize*1024*1024);
                                                fileInfo.getStorageNodes().add(node);
                                            }
                                        }
                                    }else{
                                        //取一台storage
                                        String ip = loadBalance(Arrays.asList(ips));
                                        StorageFileInfoNode node = new StorageFileInfoNode();
                                        node.setAddress(ip);
                                        node.setScope(0+"-"+size);
                                        fileInfo.getStorageNodes().add(node);
                                    }
//                                    OnLineStorageServers.storages.put(datas[0],fileInfo);
                                    H2DbHelper.insertData(fileInfo);
                                    //返回
                                    OutputStream outputStream = socket.getOutputStream();
                                    String fileInfoJsonStr = JSON.toJSONString(fileInfo);
                                    outputStream.write(fileInfoJsonStr.getBytes());
                                    socket.shutdownOutput();
                                }else if (line.startsWith("2#2")){
                                    //获取文件
                                    String hash = line.substring("2#1#".length());
//                                    StorageFileInfo fileInfo = OnLineStorageServers.storages.get(hash);
                                    StorageFileInfo fileInfo = H2DbHelper.findData(hash);
                                    String fileInfoJsonStr = JSON.toJSONString(fileInfo);
                                    OutputStream outputStream = socket.getOutputStream();
                                    outputStream.write(fileInfoJsonStr.getBytes());
                                    socket.shutdownOutput();
                                }else if (line.startsWith("2#3")){
                                    String hash = line.substring("2#3#".length());
                                    //先检查这个上传的是否已经存在，如果存在就先删除掉
                                    StorageFileInfo info = H2DbHelper.findData(hash);
                                    if (null!=info){
                                        //删除 storage中的文件 以及 数据库中的记录
                                        H2DbHelper.deleteData(info.getId());
                                        LinkedList<StorageFileInfoNode> storageNodes = info.getStorageNodes();
                                        for (StorageFileInfoNode storageNode : storageNodes) {
                                            String address = storageNode.getAddress();//127.0.0.01:2689
                                            String scopeSize = storageNode.getScope();//0-1689x
                                            String[] ipAndPort = address.split(":");
                                            String ip = ipAndPort[0];
                                            int port = Integer.valueOf(ipAndPort[1]);
                                            Socket connectStorageSocket = new Socket(ip,port);
                                            OutputStream os = connectStorageSocket.getOutputStream();
                                            os.write(("x"+info.getId()+"_"+scopeSize).getBytes());
                                            connectStorageSocket.close();
                                        }
                                    }
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }finally {
                            try {
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }
        }
    }

    public static int index = 0;
    //负载均衡获取storage服务器
    public static String loadBalance(List<String> ips){
//        System.out.println(ips.size()+"----"+index);
        if (index>=ips.size()){
            index = 0;
        }
        String ip = ips.get(index);
        index++;
        return ip;
    }

    public static void main(String[] args) {
//        System.out.println("啊啊啊".getBytes().length);
//        String ss = "2#1#asdfasdfasddf:334489";
//        System.out.println(ss.substring("2#1#".length()));
        Long size = 45644l;
        System.out.println(size*1.0/20);
        System.out.println(Math.ceil(4.1));
        Double dd = 4.4;
        dd = Math.ceil(dd);
//        System.out.println(dd.longValue());
    }
}
