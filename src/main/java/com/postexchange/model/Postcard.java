package com.postexchange.model;

public class Postcard {
    private int postcardID;
    private String timeSent;
    private String timeReceived;
    private int userIDSent;
    private int userIDReceived;
    private String postcardImage;
    private String postcardMessage;

    // Constructors
    public Postcard() {
    }

    public Postcard(int postcardID, String timeSent, String timeReceived, int userIDSent, int userIDReceived, String postcardImage, String postcardMessage) {
        this.postcardID = postcardID;
        this.timeSent = timeSent;
        this.timeReceived = timeReceived;
        this.userIDSent = userIDSent;
        this.userIDReceived = userIDReceived;
        this.postcardImage = postcardImage;
        this.postcardMessage = postcardMessage;
    }

    // Getters and setters
    public int getPostcardID() {
        return postcardID;
    }

    public void setPostcardID(int postcardID) {
        this.postcardID = postcardID;
    }

    public String getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(String timeSent) {
        this.timeSent = timeSent;
    }

    public String getTimeReceived() {
        return timeReceived;
    }

    public void setTimeReceived(String timeReceived) {
        this.timeReceived = timeReceived;
    }

    public int getUserIDSent() {
        return userIDSent;
    }

    public void setUserIDSent(int userIDSent) {
        this.userIDSent = userIDSent;
    }

    public int getUserIDReceived() {
        return userIDReceived;
    }

    public void setUserIDReceived(int userIDReceived) {
        this.userIDReceived = userIDReceived;
    }

    public String getPostcardImage() {
        return postcardImage;
    }

    public void setPostcardImage(String postcardImage) {
        this.postcardImage = postcardImage;
    }

    public String getPostcardMessage() {
        return postcardMessage;
    }

    public void setPostcardMessage(String postcardMessage) {
        this.postcardMessage = postcardMessage;
    }
}
