package com.jo.spectrumtracking.twilio.chat.messages;


public class StatusMessage implements ChatMessage {
  private String author = "";

  public StatusMessage(String author) {
    this.author = author;
  }

  @Override
  public String getAuthor() {
    return author;
  }

  @Override
  public String getDateCreated() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getMessageBody() {
    throw new UnsupportedOperationException();
  }
}
