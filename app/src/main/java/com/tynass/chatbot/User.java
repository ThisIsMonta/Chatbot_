package com.tynass.chatbot;

public class User {
    String username;
    String userPhotoURL;

    public User() {
    }

    public User(String username, String userPhotoURL) {
        this.username = username;
        this.userPhotoURL = userPhotoURL;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhotoURL() {
        return this.userPhotoURL;
    }

    public void setPhotoURL(String userPhotoURL) {
        this.userPhotoURL = userPhotoURL;
    }

}
