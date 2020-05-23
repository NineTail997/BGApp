package com.example.bgapp;

public class Messages {
    private String from, to, message, messageID, type, time, date;

    public Messages() {

    }

    public Messages(String from, String to, String message, String messageID, String type, String time, String date) {
        this.from = from;
        this.to = to;
        this.message = message;
        this.messageID = messageID;
        this.type = type;
        this.time = time;
        this.date = date;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getType() { return type; }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
