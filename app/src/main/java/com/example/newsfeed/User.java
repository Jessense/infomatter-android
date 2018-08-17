package com.example.newsfeed;

public class User {
    private String name;
    private String password;
    private String photo;
    private String id;
    private Boolean logined;

    public User() {
        this.logined = false;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getId() {
        return id;
    }

    public String getPhoto() {
        return photo;
    }

    public Boolean getLogined() {
        return logined;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLogined(Boolean logined) {
        this.logined = logined;
    }
}
