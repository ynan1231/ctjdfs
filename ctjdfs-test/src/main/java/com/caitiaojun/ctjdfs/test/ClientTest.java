package com.caitiaojun.ctjdfs.test;

import com.caitaojun.ctjdfs.client.CtjDfsClient;
import com.caitaojun.ctjdfs.model.DownloadFile;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ClientTest {
    public static void main(String[] args) throws Exception {
        CtjDfsClient.init("192.168.68.29", 2688);
        String filePath = "F:\\jar\\tianya-fw-1.7.26.5.jar";
        File file = new File(filePath);
        Map<String, String> metaData = new HashMap<>();
        String result = CtjDfsClient.upload(file, metaData);
        System.out.println(result);

    }
    @Test
    public void download() {
        String fileId = "690ba85c9df9213b20e692118b74ebd1";
        try {
            CtjDfsClient.init("192.168.68.29", 2688);
            DownloadFile download = CtjDfsClient.download(fileId);
            File file = new File("F:\\jar\\tianya-fw-1.7.26.5.copy.jar");
            FileUtils.copyInputStreamToFile(download.getFileInputStream(),file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void delete() {
        String fileId = "690ba85c9df9213b20e692118b74ebd1";
        try {
            CtjDfsClient.init("192.168.68.29", 2688);
            boolean delete = CtjDfsClient.delete(fileId);
            System.out.println(delete);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
