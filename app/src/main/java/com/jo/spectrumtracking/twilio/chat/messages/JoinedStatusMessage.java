package com.jo.spectrumtracking.twilio.chat.messages;

public class JoinedStatusMessage extends StatusMessage {


  public JoinedStatusMessage(String author) {
    super(author);
  }

  @Override
  public String getMessageBody() {
    return this.getAuthor() + " joined the channel";
  }
}
