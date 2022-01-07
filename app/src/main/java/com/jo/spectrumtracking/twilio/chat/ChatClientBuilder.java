package com.jo.spectrumtracking.twilio.chat;

import android.content.Context;

import com.jo.spectrumtracking.global.GlobalConstant;
import com.jo.spectrumtracking.twilio.chat.listeners.TaskCompletionListener;
import com.twilio.chat.CallbackListener;
import com.twilio.chat.ChatClient;
import com.twilio.chat.ErrorInfo;

public class ChatClientBuilder extends CallbackListener<ChatClient> {

  private Context context;
  private TaskCompletionListener<ChatClient, String> buildListener;

  public ChatClientBuilder(Context context) {
    this.context = context;
  }

  public void build(String token, final TaskCompletionListener<ChatClient, String> listener) {
    ChatClient.Properties props =
        new ChatClient.Properties.Builder()
                .setRegion(GlobalConstant.TWILIO_DEFAULT_REALM)
                .setDeferCertificateTrustToPlatform(false)
                .createProperties();

    this.buildListener = listener;
    ChatClient.create(context.getApplicationContext(),
        token,
        props,
        this);
  }


  @Override
  public void onSuccess(ChatClient chatClient) {
    this.buildListener.onSuccess(chatClient);
  }

  @Override
  public void onError(ErrorInfo errorInfo) {
    this.buildListener.onError(errorInfo.getMessage());
  }
}
