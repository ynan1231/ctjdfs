package com.caitaojun.ctjdfs.client;

import com.alibaba.fastjson.JSON;
import com.caitaojun.ctjdfs.model.DownloadFile;
import com.caitaojun.ctjdfs.model.StorageFileInfo;
import com.caitaojun.ctjdfs.model.StorageFileInfoNode;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by caitaojun on 2019/1/4 0004.
 */
public class CtjDfsClient {
    private static String trackerAddress;
    private static int trackerPort;
    //初始化连接配置
    public static void init(String trackerAddress,int trackerPort){
        CtjDfsClient.trackerAddress = trackerAddress;
        CtjDfsClient.trackerPort = trackerPort;
    }

    //上传文件 返回文件id
    public static String upload(File file, Map<String,String> metaData) throws Exception {
        //"2#1#c7637c97eed3bde8b64dd321c917aa89#20971520000#dog.jpg#{'username':'ctj','age':12}"
        String md5Hex = DigestUtils.md5Hex(new FileInputStream(file));
        long length = file.length();
        String fileName = file.getName();
        String metaDataStr = JSON.toJSONString(metaData);
        String dataStr1 = "2#1#"+md5Hex+"#"+length+"#"+fileName+"#"+metaDataStr;
        Socket connectTrackerSocket = new Socket(trackerAddress,trackerPort);
        OutputStream outputStream = connectTrackerSocket.getOutputStream();
        outputStream.write(dataStr1.getBytes());
        connectTrackerSocket.shutdownOutput();
        InputStream inputStream = connectTrackerSocket.getInputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        StringBuffer sb = new StringBuffer();
        while ((len=inputStream.read(buffer))!=-1){
            sb.append(new String(buffer,0,len));
        }
        connectTrackerSocket.shutdownInput();
        StorageFileInfo fileInfo = JSON.parseObject(sb.toString(), StorageFileInfo.class);
        connectTrackerSocket.close();
        //获取fileinfo中的storage进行分块上传文件
        String fileId = fileInfo.getId();
        LinkedList<StorageFileInfoNode> storageNodes = fileInfo.getStorageNodes();
        int total = storageNodes.size();
        int count = 0;
        for (StorageFileInfoNode storageNode : storageNodes) {
            String address = storageNode.getAddress();//127.0.0.01:2689
            String scopeSize = storageNode.getScope();//0-1689x
            String[] ipAndPort = address.split(":");
            String[] beginAndEnd = scopeSize.split("-");
            String ip = ipAndPort[0];
            int port = Integer.valueOf(ipAndPort[1]);
            Long begin = Long.valueOf(beginAndEnd[0]);
            Long end = Long.valueOf(beginAndEnd[1]);
            Socket connectStorageSocket = new Socket(ip,port);
            OutputStream os = connectStorageSocket.getOutputStream();
            if (uploadFileHandle(begin,end,file,md5Hex,os)){
                count++;
            }
            connectStorageSocket.close();
        }
        if (total==count){
            return fileId;
        }
        return null;
    }

    public static boolean delete(String fileId) throws IOException {
        //根据文件id删除
        Socket connectTrackerSocket = new Socket(trackerAddress,trackerPort);
        OutputStream outputStream = connectTrackerSocket.getOutputStream();
        String dataStr1 = "2#3#"+fileId;
        outputStream.write(dataStr1.getBytes());
        connectTrackerSocket.shutdownOutput();
        return true;
    }

    //下载文件 返回流
    public static DownloadFile download(String fileId) throws Exception {
        Socket connectTrackerSocket = new Socket(trackerAddress,trackerPort);
        OutputStream outputStream = connectTrackerSocket.getOutputStream();
        String dataStr1 = "2#2#"+fileId;
        outputStream.write(dataStr1.getBytes());
        connectTrackerSocket.shutdownOutput();
        InputStream inputStream = connectTrackerSocket.getInputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        StringBuffer sb = new StringBuffer();
        while ((len=inputStream.read(buffer))!=-1){
            sb.append(new String(buffer,0,len));
        }
        connectTrackerSocket.shutdownInput();
        StorageFileInfo fileInfo = JSON.parseObject(sb.toString(), StorageFileInfo.class);
        connectTrackerSocket.close();
        //执行下载
        LinkedList<StorageFileInfoNode> storageNodes = fileInfo.getStorageNodes();
        File tempFile = File.createTempFile(fileId, null);
        RandomAccessFile randomAccessFile = new RandomAccessFile(tempFile,"rw");
        for (StorageFileInfoNode storageNode : storageNodes) {
            String address = storageNode.getAddress();//127.0.0.01:2689
            String scopeSize = storageNode.getScope();//0-1689x
            //dc7637c97eed3bde8b64dd321c917aa891_25829120-146800640
            String fileName = fileId+"_"+scopeSize;
            String[] ipAndPort = address.split(":");
            String[] beginAndEnd = scopeSize.split("-");
            Long begin = Long.valueOf(beginAndEnd[0]);
//            Long end = Long.valueOf(beginAndEnd[1]);
            String ip = ipAndPort[0];
            int port = Integer.valueOf(ipAndPort[1]);
            Socket connectStorageSocket = new Socket(ip,port);
            OutputStream outputStream1 = connectStorageSocket.getOutputStream();
            outputStream1.write(("d"+fileName).getBytes());
//            connectStorageSocket.shutdownOutput();
            InputStream inputStream1 = connectStorageSocket.getInputStream();
            randomAccessFile.seek(begin);
            byte[] buff = new byte[1024];
            int leng = 0;
            while ((leng=inputStream1.read(buff))!=-1){
                randomAccessFile.write(buff,0,leng);
            }
        }
        DownloadFile downloadFile = new DownloadFile();
        downloadFile.setStorageFileInfo(fileInfo);
        downloadFile.setFileInputStream(new FileInputStream(tempFile));
        tempFile.deleteOnExit();
        return downloadFile;
    }

    private static boolean uploadFileHandle(Long begin, Long end, File file, String md5Hex, OutputStream outputStream) throws Exception {
        //写头 uc7637c97eed3bde8b64dd321c917aa891_25829120-146800640#xxxxx
        String head = "u"+md5Hex+"_"+begin+"-"+end+"#";
        outputStream.write(head.getBytes());
        RandomAccessFile randomAccessFile = new RandomAccessFile(file,"r");
//        System.out.println(randomAccessFile.length()+":"+randomAccessFile.getFilePointer());
        randomAccessFile.seek(begin);
//        System.out.println(randomAccessFile.length()+":"+randomAccessFile.getFilePointer());
        int length = 0;
        byte[] buffer = new byte[1024];
        while ((length=randomAccessFile.read(buffer))!=-1){
            if (randomAccessFile.getFilePointer()>end){
                //超过了
                Long size = randomAccessFile.getFilePointer()-end;
                int size_ = size.intValue();
                outputStream.write(buffer,0,length-size_);
                break;
            }else {
                //没有超过
                outputStream.write(buffer,0,length);
            }
        }
        return true;
    }


}
