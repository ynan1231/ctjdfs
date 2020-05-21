package com.caitaojun.ctjdfs.model;

import java.util.LinkedList;
import java.util.Map;

/**
 * Created by caitaojun on 2019/1/3 0003.
 */
public class StorageFileInfo {
    private String id;
    private String name;
    private Long size;
    private Map<String,String> metadate;
    private LinkedList<StorageFileInfoNode> storageNodes = new LinkedList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Map<String,String> getMetadate() {
        return metadate;
    }

    public void setMetadate(Map<String,String> metadate) {
        this.metadate = metadate;
    }

    public LinkedList<StorageFileInfoNode> getStorageNodes() {
        return storageNodes;
    }

    public void setStorageNodes(LinkedList<StorageFileInfoNode> storageNodes) {
        this.storageNodes = storageNodes;
    }

    @Override
    public String toString() {
        return "StorageFileInfo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", size=" + size +
                ", metadate=" + metadate +
                ", storageNodes=" + storageNodes +
                '}';
    }
}
