package com.jo.spectrumtracking.twilio.chat.messages;

public class LeftStatusMessage extends StatusMessage {


  public LeftStatusMessage(String author) {
    super(author);
  }


  @Override
  public String getMessageBody() {
    return this.getAuthor() + " left the channel";
  }


}
