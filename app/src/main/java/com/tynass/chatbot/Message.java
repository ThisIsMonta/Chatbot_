package com.tynass.chatbot;

public abstract class Message {

    public static final int USER_MESSAGE = 0;
    public static final int BOT_MESSAGE = 1;

    String message;
    User sender;
    String createdAt;

    public Message() {
    }

    public Message(String message, User sender, String createdAt) {
        this.message = message;
        this.sender = sender;
        this.createdAt = createdAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public String getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public abstract int getType();
}
