package com.example.newsfeed;

public class Source {
    private String id;
    private String name;
    private String feedUrl;
    private String link;
    private String photo;
    public Source(String name, String feedUrl, String link, String photo) {
        this.name = name;
        this.feedUrl = feedUrl;
        this.link = link;
        this.photo = photo;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getFeedUrl() {
        return feedUrl;
    }

    public String getLink() {
        return link;
    }

    public String getPhoto() {
        return photo;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFeedUrl(String feedUrl) {
        this.feedUrl = feedUrl;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}
