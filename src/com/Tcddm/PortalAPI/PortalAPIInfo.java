package com.Tcddm.PortalAPI;

/**
 * API版本信息
 */
public class PortalAPIInfo {
    private final String version="1.0.0";
    private final String author="Tcddm";

    /**
     * 获取版本
     * @return 版本
     */
    public String getVersion() {
        return version;
    }

    /**
     * 获取作者
     * @return 作者
     */
    public String getAuthor() {
        return author;
    }

    /**
     * 获取API信息
     * @return API信息
     */
    public String toString() {
        return "PortalAPIInfo{" +
                "version='" + version + '\'' +
                ", author='" + author + '\'' +
                '}';
    }
}
