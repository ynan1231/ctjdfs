package com.caitaojun.ctjdfs.model;

/**
 * Created by caitaojun on 2019/1/3 0003.
 */
public class StorageFileInfoNode {
    private String address;
    private String scope;//0-20

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
