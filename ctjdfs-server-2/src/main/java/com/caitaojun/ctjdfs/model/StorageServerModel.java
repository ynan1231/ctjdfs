package com.caitaojun.ctjdfs.model;

/**
 * Created by caitaojun on 2019/1/2 0002.
 */
public class StorageServerModel {
    private String ip;
    private Integer port;
    private Long lastTimeMillis;

    public StorageServerModel() {
    }

    public StorageServerModel(String ip, Integer port, Long lastTimeMillis) {
        this.ip = ip;
        this.port = port;
        this.lastTimeMillis = lastTimeMillis;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Long getLastTimeMillis() {
        return lastTimeMillis;
    }

    public void setLastTimeMillis(Long lastTimeMillis) {
        this.lastTimeMillis = lastTimeMillis;
    }
}
