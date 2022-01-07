package com.jo.spectrumtracking.twilio.chat.messages;

public interface ChatMessage {

  String getMessageBody();

  String getAuthor();

  String getDateCreated();
}
