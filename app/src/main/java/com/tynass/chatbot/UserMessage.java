package com.tynass.chatbot;

public class UserMessage extends Message {

    public UserMessage() {
    }

    public UserMessage(String message, User sender, String createdAt) {
        super(message,sender,createdAt);
    }

    @Override
    public int getType() {
        return Message.USER_MESSAGE;
    }
}
