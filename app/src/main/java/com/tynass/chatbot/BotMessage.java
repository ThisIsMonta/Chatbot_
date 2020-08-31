package com.tynass.chatbot;

public class BotMessage extends Message{

    public BotMessage() {
    }

    public BotMessage(String message, User sender, String createdAt) {
        super(message,sender,createdAt);
    }

    @Override
    public int getType() {
        return Message.BOT_MESSAGE;
    }

}
