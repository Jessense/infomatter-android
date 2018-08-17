package com.example.newsfeed;

public class Entry {
    private String title;
    private String link;
    private String time;
    private String source_id;
    private String photo;

    public Entry(String title, String link, String time) {
        this.title = title;
        this.link = link;
    }
    public String getTitle() {
        return title;
    }
    public String getLink() {
        return link;
    }
    public String getTime() {
        return time;
    }
    public String getSourceId() {
        return source_id;
    }
    public String getPhoto() {
        return photo;
    }

//    public String getSource() {
//        return source;
//    }

    public void setTitle(String title) {
        this.title = title;
    }
    public void setLink(String link) {
        this.link = link;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public void setSourceId(String source_id) {
        this.source_id = source_id;
    }
    public void setPhoto(String photo) {
        this.photo = photo;
    }

}
