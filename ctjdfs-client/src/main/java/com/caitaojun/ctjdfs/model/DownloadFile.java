package com.caitaojun.ctjdfs.model;

import java.io.FileInputStream;

/**
 * Created by caitaojun on 2019/1/4 0004.
 */
public class DownloadFile {
    private StorageFileInfo storageFileInfo;
    private FileInputStream fileInputStream;

    public StorageFileInfo getStorageFileInfo() {
        return storageFileInfo;
    }

    public void setStorageFileInfo(StorageFileInfo storageFileInfo) {
        this.storageFileInfo = storageFileInfo;
    }

    public FileInputStream getFileInputStream() {
        return fileInputStream;
    }

    public void setFileInputStream(FileInputStream fileInputStream) {
        this.fileInputStream = fileInputStream;
    }
}
