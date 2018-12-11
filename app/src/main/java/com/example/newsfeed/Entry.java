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
    private String content;

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

    public String getContent() {
        return content;
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
        String cur_year  = String.format("%04d", localDate.getYear());
        String cur_month = String.format("%02d", localDate.getMonthValue());
        String cur_day   = String.format("%02d", localDate.getDayOfMonth());
        String cur_hour = String.format("%02d", localDate.getHour());


        DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_DATE_TIME;
        TemporalAccessor accessor = timeFormatter.parse(time);
        Date pubDate = Date.from(Instant.from(accessor));
        LocalDateTime localPubDate = pubDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        String pub_year;
        String pub_month;
        String pub_day;
        String pub_hour;
        String pub_minute = String.format("%02d", localPubDate.getMinute());
        if (localPubDate.plusHours(8).isAfter(localDate)) {
            pub_year  = String.format("%04d", localPubDate.getYear());
            pub_month = String.format("%02d", localPubDate.getMonthValue());
            pub_day   = String.format("%02d", localPubDate.getDayOfMonth());
            pub_hour = String.format("%02d", localPubDate.getHour());
        } else {
            pub_year  = String.format("%04d", localPubDate.plusHours(8).getYear());
            pub_month = String.format("%02d", localPubDate.plusHours(8).getMonthValue());
            pub_day   = String.format("%02d", localPubDate.plusHours(8).getDayOfMonth());
            pub_hour = String.format("%02d", localPubDate.plusHours(8).getHour());
        }



        if (cur_year.equals(pub_year) && cur_month.equals(pub_month) && cur_day.equals(pub_day)) {
            return pub_hour + ":" + pub_minute;
        } else if(cur_year.equals(pub_year)){
            return pub_month + "-" + pub_day + " " + pub_hour + ":" + pub_minute;
        } else {
            return pub_year + " " + pub_month + "-" + pub_day;
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

    public void setContent(String content) {
        this.content = content;
    }
}
