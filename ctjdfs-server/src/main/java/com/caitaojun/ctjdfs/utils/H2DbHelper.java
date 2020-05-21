package com.caitaojun.ctjdfs.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.caitaojun.ctjdfs.model.StorageFileInfo;
import com.caitaojun.ctjdfs.model.StorageFileInfoNode;
import com.caitaojun.ctjdfs.system.SystemConstant;

import java.io.File;
import java.sql.*;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by caitaojun on 2019/1/4 0004.
 */
public class H2DbHelper {
    private static Connection connection;
    static {
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection("jdbc:h2:file:"+ SystemConstant.trackerDataPath+ File.separatorChar+"ctjdfs", "sa", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createConnection() throws SQLException {
        if (connection.isClosed()){
            connection =DriverManager.getConnection("jdbc:h2:file:"+ SystemConstant.trackerDataPath+ File.separatorChar+"ctjdfs", "sa", "");
        }
    }


    public static void createTable() throws SQLException {
        try {
            createConnection();
            Statement statement = connection.createStatement();
            String sql = "create table t_file(id VARCHAR (32) PRIMARY  KEY,name VARCHAR(50),size int(15),metadate VARCHAR(300), storageNodes VARCHAR(5000) )";
            int i = statement.executeUpdate(sql);
            statement.close();
            connection.close();
        } catch (SQLException e) {
            if (!e.getMessage().contains("already exists")){
                e.printStackTrace();
            }
        }
    }

    public static void insertData(StorageFileInfo storageFileInfo) throws SQLException {
        createConnection();
        String sql = "insert into t_file(id,name,size,metadate,storageNodes) values(?,?,?,?,?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1,storageFileInfo.getId());
        preparedStatement.setString(2,storageFileInfo.getName());
        preparedStatement.setLong(3,storageFileInfo.getSize());
        String metaDataStr = JSON.toJSONString(storageFileInfo.getMetadate());
        preparedStatement.setString(4,metaDataStr);
        String storageNodesStr = JSON.toJSONString(storageFileInfo.getStorageNodes());
        preparedStatement.setString(5,storageNodesStr);
        preparedStatement.executeUpdate();
        preparedStatement.close();
        connection.close();
    }

    public static void deleteData(String fileId) throws SQLException {
        createConnection();
        String sql = "delete from t_file WHERE id=?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1,fileId);
        preparedStatement.executeUpdate();
        preparedStatement.close();
        connection.close();
    }

    public static StorageFileInfo findData(String fileId) throws SQLException {
        createConnection();
        String sql = "select * from t_file where id=?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1,fileId);
        ResultSet resultSet = preparedStatement.executeQuery();
        StorageFileInfo fileInfo = new StorageFileInfo();
        while (resultSet.next()){
            String id = resultSet.getString("id");
            String name = resultSet.getString("name");
            long size = resultSet.getLong("size");
            String metadateStr = resultSet.getString("metadate");
            Map metadate = JSON.parseObject(metadateStr, Map.class);
            String storageNodesStr = resultSet.getString("storageNodes");
            LinkedList<StorageFileInfoNode> storageFileInfoNodes = JSON.parseObject(storageNodesStr, new TypeReference<LinkedList<StorageFileInfoNode>>() {
            });
            fileInfo.setId(id);
            fileInfo.setName(name);
            fileInfo.setSize(size);
            fileInfo.setMetadate(metadate);
            fileInfo.setStorageNodes(storageFileInfoNodes);
        }
        preparedStatement.close();
        connection.close();
        return fileInfo;
    }

    public static void main(String[] args) throws SQLException {
        createTable();

//        StorageFileInfo fileInfo = new StorageFileInfo();
//        fileInfo.setId(UUID.randomUUID().toString().replace("-",""));
//        fileInfo.setName("dog.jpg");
//        fileInfo.setSize(156897L);
//        Map<String, String> metaData = new HashMap<>();
//        metaData.put("author","caitaojun");
//        fileInfo.setMetadate(metaData);
//        LinkedList<StorageFileInfoNode> nodes = new LinkedList<>();
//        StorageFileInfoNode node1 = new StorageFileInfoNode();
//        node1.setAddress("127.0.0.1:5656");
//        node1.setScope("0-123");
//        StorageFileInfoNode node2 = new StorageFileInfoNode();
//        node2.setAddress("127.0.0.1:5657");
//        node2.setScope("123-456");
//        nodes.add(node1);
//        nodes.add(node2);
//        fileInfo.setStorageNodes(nodes);
//        insertData(fileInfo);

//        String fileId = "a5dd77607cae4eb1887f4e4a8af30fe0";
//        StorageFileInfo fileInfo = findData(fileId);
//        System.out.println(fileInfo);

    }


}
