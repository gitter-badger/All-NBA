package com.gmail.jorgegilcavazos.ballislife.features.model;

public class GameThreadSummary {

    private String id;
    private String title;
    private long created_utc;
    private int num_comments;

    public GameThreadSummary() {
    }

    public GameThreadSummary(String id, String title, long created_utc, int num_comments) {
        this.id = id;
        this.title = title;
        this.created_utc = created_utc;
        this.num_comments = num_comments;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getCreated_utc() {
        return created_utc;
    }

    public void setCreated_utc(long created_utc) {
        this.created_utc = created_utc;
    }

    public String toString() {
        return "Id: " + id + " , " + title + " - " + created_utc;
    }

    public int getNum_comments() {
        return num_comments;
    }

    public void setNum_comments(int num_comments) {
        this.num_comments = num_comments;
    }
}
