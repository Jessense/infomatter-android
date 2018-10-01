package com.example.newsfeed;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

public class Entry {
    private String title;
    private String link;
    private String time;
    private String source_id;
    private String source_name;
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

    public String getSourceName() {
        return source_name;
    }
    //    public String getSource() {
//        return source;
//    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String geLocalPubTime() {
        Date now = new Date();
        LocalDateTime localDate = now.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        int cur_year  = localDate.getYear();
        int cur_month = localDate.getMonthValue();
        int cur_day   = localDate.getDayOfMonth();
        int cur_hour = localDate.getHour();


        DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_DATE_TIME;
        TemporalAccessor accessor = timeFormatter.parse(time);
        Date pubDate = Date.from(Instant.from(accessor));
        LocalDateTime localPubDate = pubDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        int pub_year  = localPubDate.plusHours(8).getYear();
        int pub_month = localPubDate.plusHours(8).getMonthValue();
        int pub_day   = localPubDate.plusHours(8).getDayOfMonth();
        int pub_hour = localPubDate.plusHours(8).getHour();
        int pub_minute = localPubDate.getMinute();

        if (cur_year == pub_year && cur_month == pub_month && cur_day == pub_day) {
            return Integer.toString(pub_hour) + ":" + Integer.toString(pub_minute);
        } else if(cur_year == pub_year){
            return Integer.toString(pub_month) + "-" + Integer.toString(pub_day) +" " + Integer.toString(pub_hour) + ":" + Integer.toString(pub_minute);
        } else {
            return Integer.toString(pub_year) + " " + Integer.toString(pub_month) + "-" + Integer.toString(pub_day);
        }


    }

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

    public void setSourceName(String source_name) {
        this.source_name = source_name;
    }
}
