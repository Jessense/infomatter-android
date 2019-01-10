package com.example.newsfeed;


public class Config {
    private String scheme = "http";
    private String host = "188.131.178.76"; //主机ip
    private Integer port = 3000; //后端程序运行的端口
    private Integer pub_rsshub_port = 1200;


    public String getScheme() {
        return scheme;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public Integer getPub_rsshub_port() {
        return pub_rsshub_port;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setPub_rsshub_port(Integer pub_rsshub_port) {
        this.pub_rsshub_port = pub_rsshub_port;
    }
}
