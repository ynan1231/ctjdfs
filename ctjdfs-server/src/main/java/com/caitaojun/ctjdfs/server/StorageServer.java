package com.caitaojun.ctjdfs.server;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by caitaojun on 2019/1/2 0002.
 * 上传文件  u===>117
 *      uc7637c97eed3bde8b64dd321c917aa891_25829120-146800640#xxxxx
 * 下载文件  d===>100
 *      dc7637c97eed3bde8b64dd321c917aa891_25829120-146800640
 * 删除  xc7637c97eed3bde8b64dd321c917aa891_25829120-146800640
 */
public class StorageServer {

    private static void init(String storageFilePath){
        File file = new File(storageFilePath);
        if (!file.exists()){
            file.mkdirs();
        }
    }

    //心跳发送
    private static void sendHeartBeat(String trackerAddress, String heartBeatInterval, String storageAddress){
        String[] ipAndPort = trackerAddress.split(":");
        Integer interval = Integer.valueOf(heartBeatInterval);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Socket socket = null;
                try {
                    socket = new Socket(ipAndPort[0],Integer.valueOf(ipAndPort[1]));
                    OutputStream outputStream = socket.getOutputStream();
                    outputStream.write(("1#"+storageAddress).getBytes());
                    socket.shutdownOutput();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        },1000,interval*1000);
    }

    public static void handle(Integer dfsport, String trackerAddress, String heartBeatInterval, String storageAddress, String storageFilePath) throws Exception {
        init(storageFilePath);
        System.out.println("StorageServer server running port："+dfsport);
        sendHeartBeat(trackerAddress,heartBeatInterval,storageAddress);
        ServerSocket serverSocket = new ServerSocket(dfsport);
        while (true){
            Socket socket = serverSocket.accept();
            if (socket.isConnected()){
                System.out.println("connect server...");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        InputStream inputStream = null;
                        try {
                            inputStream = socket.getInputStream();
//                            int read = inputStream.read();
//                            inputStream.reset();
//                            inputStream.skip(1);
                            BufferedInputStream bi = new BufferedInputStream(inputStream);
                            bi.mark(100);
                            byte[] buffer = new byte[100];
                            bi.read(buffer,0,100);
                            String head = new String(buffer);
//                            System.out.println(head);
                            char firstchar = head.charAt(0);
                            if ('u'==firstchar){
                                //上传
                                //获取文件id
                                int index = head.indexOf("#");
                                System.out.println(index);
                                String fileName = head.substring(1,index);
                                bi.reset();
                                bi.skip(index+1);

                                FileUtils.copyInputStreamToFile(bi,new File(storageFilePath+File.separatorChar+fileName));
//                                socket.shutdownInput();
                            }else if ('d'==firstchar){
                                //下载
                                String fileName = head.substring(1);
                                fileName = fileName.trim();
                                File file = new File(storageFilePath+File.separatorChar+fileName);
                                if (file.exists()){
                                    OutputStream outputStream = socket.getOutputStream();
                                    DataOutputStream datao = new DataOutputStream(outputStream);
                                    FileUtils.copyFile(file,datao);
                                    socket.shutdownOutput();
                                }
                                socket.shutdownInput();
                            }else if ('x'==firstchar){
                                String fileName = head.substring(1);
                                fileName = fileName.trim();
                                File file = new File(storageFilePath+File.separatorChar+fileName);
                                if (file.exists()){
                                    file.delete();
                                }
                                socket.shutdownInput();
                            }
//                            System.out.println(head.indexOf("#"));
//                            bi.skip(1);


//                            FileUtils.copyInputStreamToFile(bi,new File("c:/ctjaaaa.jpg"));


//                            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//                            String line = reader.readLine();
//                            System.out.println(line);
//                            PrintStream pp = new PrintStream(socket.getOutputStream());
//                            pp.write("hello world".getBytes());
//                            if ("exit".equals(line)){
//                                socket.close();
//                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
//                            try {
//                                if (inputStream!=null){
//                                    inputStream.close();
//                                }
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
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
}
