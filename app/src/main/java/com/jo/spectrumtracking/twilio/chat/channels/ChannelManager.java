package com.jo.spectrumtracking.twilio.chat.channels;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.jo.spectrumtracking.activity.MainActivity;
import com.jo.spectrumtracking.twilio.chat.ChatClientManager;
import com.jo.spectrumtracking.twilio.chat.accesstoken.AccessTokenFetcher;
import com.jo.spectrumtracking.twilio.chat.listeners.TaskCompletionListener;
import com.twilio.chat.CallbackListener;
import com.twilio.chat.Channel;
import com.twilio.chat.Channel.ChannelType;
import com.twilio.chat.ChannelDescriptor;
import com.twilio.chat.Channels;
import com.twilio.chat.ChatClient;
import com.twilio.chat.ChatClientListener;
import com.twilio.chat.ErrorInfo;
import com.twilio.chat.Members;
import com.twilio.chat.Paginator;
import com.twilio.chat.StatusListener;
import com.twilio.chat.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ChannelManager implements ChatClientListener {
  private static ChannelManager sharedManager = new ChannelManager();
  private ChatClientManager chatClientManager;
  private ChannelExtractor channelExtractor;
  private ArrayList<Channel> channels = new ArrayList<>();
  public HashMap<String, Long> unreadCountMap = new HashMap<>();
  private Channels privateChannelsObject;
  private ChatClientListener listener;
  private Handler handler;
  private Boolean isRefreshingChannels = false;

  private ChannelManager() {
    this.chatClientManager = MainActivity.get().getChatClientManager();
    this.channelExtractor = new ChannelExtractor();
    this.listener = this;
    handler = setupListenerHandler();
  }

  public static ChannelManager getInstance() {
    return sharedManager;
  }

  public ArrayList<Channel> getPrivateChannels() {
    return channels;
  }

  public void leaveChannelWithHandler(Channel channel, StatusListener handler) {
    channel.leave(handler);
  }

  public void deleteChannelWithHandler(Channel channel, StatusListener handler) {
    channel.destroy(handler);
  }

  public void populateUserChannels(final LoadChannelListener listener) {
    if (this.chatClientManager == null || this.isRefreshingChannels) {
      return;
    }
    this.isRefreshingChannels = true;

    handler.post(new Runnable() {
      @Override
      public void run() {
        privateChannelsObject = chatClientManager.getChatClient().getChannels();

        privateChannelsObject.getUserChannelsList(new CallbackListener<Paginator<ChannelDescriptor>>() {
          @Override
          public void onSuccess(Paginator<ChannelDescriptor> channelDescriptorPaginator) {
            extractUserChannelsFromPaginatorAndPopulate(channelDescriptorPaginator, listener);
          }
        });
        chatClientManager.addClientListener(ChannelManager.this);
      }
    });
  }

  private void extractUserChannelsFromPaginatorAndPopulate(final Paginator<ChannelDescriptor> channelsPaginator,
                                                           final LoadChannelListener listener) {
    channels.clear();
    channelExtractor.extractAndSortFromChannelDescriptor(channelsPaginator,
    new TaskCompletionListener<List<Channel>, String>() {
      @Override
      public void onSuccess(List<Channel> channels) {
        for (Channel channel : channels) {
          if (channel.getStatus() == Channel.ChannelStatus.JOINED || channel.getStatus() == Channel.ChannelStatus.INVITED) {
            ChannelManager.this.channels.add(channel);
          }
        }
        Collections.sort(ChannelManager.this.channels, new CustomChannelComparator());
        ChannelManager.this.isRefreshingChannels = false;
        listener.onChannelsFinishedLoading(channels);
      }

      @Override
      public void onError(String errorText) {

      }
    });
  }

  public String getChannelName(String partnerId) {
    SharedPreferences preferences = MainActivity.get().getSharedPreferences("spectrum", Context.MODE_PRIVATE);
    String myId = preferences.getString("username", "");

    String name = "";
    if (myId.compareTo(partnerId) <= 0) {
      name = myId + "__" + partnerId;
    } else {
      name = partnerId + "__" + myId;
    }

    return name;
  }

  public String getPartnerName(String channelName) {
    SharedPreferences preferences = MainActivity.get().getSharedPreferences("spectrum", Context.MODE_PRIVATE);
    String myId = preferences.getString("username", "");

    String[] userNames = channelName.split("__");

    if (userNames.length < 2) {
      return "unknown";
    } else {
      if (myId.equalsIgnoreCase(userNames[0])) {
        return userNames[1];
      } else if (myId.equalsIgnoreCase(userNames[1])) {
        return userNames[0];
      } else {
        return "unknown";
      }
    }
  }

  public String getPartnerId(String channelName) {
    String[] users = channelName.split("__");

    if (users.length < 2) {
      return null;
    } else {
      SharedPreferences preferences = MainActivity.get().getSharedPreferences("spectrum", Context.MODE_PRIVATE);
      String myId = preferences.getString("username", "");

      if (users[0].equals(myId)) {
        return users[1];
      } else {
        return users[0];
      }
    }
  }

  public void joinOrCreatePrivateChannelWithName(String name, String partnerId, final StatusListener listener) {
    Channel channel = this.getPrivateChannelWithName(name);
    if (channel != null) {
      joinPrivateChannelWithName(name, partnerId, listener);
    } else {
      createPrivateChannelWithName(name, partnerId, listener);
    }
  }

  public void createPrivateChannelWithName(String name, String partnerId, final StatusListener listener) {
    if (privateChannelsObject == null) {
      privateChannelsObject = chatClientManager.getChatClient().getChannels();
    }

    this.privateChannelsObject
        .channelBuilder()
        .withFriendlyName(name)
        .withUniqueName(name)
        .withType(ChannelType.PRIVATE)
        .build(new CallbackListener<Channel>() {
          @Override
          public void onSuccess(Channel channel) {
            channels.add(channel);
            joinPrivateChannelWithName(name, partnerId, listener);
//            channel.getMembers().inviteByIdentity(partnerId, null);
          }

          @Override
          public void onError(ErrorInfo errorInfo) {
            if (listener != null) {
              listener.onError(errorInfo);
            }
          }
        });
  }

  private void getUnreadCount(Channel channel) {
    channel.getUnconsumedMessagesCount(new CallbackListener<Long>() {
      @Override
      public void onSuccess(Long unreadCount) {
        if (unreadCount == null) {
          channel.getMessagesCount(new CallbackListener<Long>() {
            @Override
            public void onSuccess(Long messageCount) {
              if (messageCount == null) {
                unreadCountMap.put(channel.getUniqueName(), 0L);
              } else {
                unreadCountMap.put(channel.getUniqueName(), messageCount);
              }
              MainActivity.get().updatedUnreadCount();
            }
          });
        } else {
          unreadCountMap.put(channel.getUniqueName(), unreadCount);
          MainActivity.get().updatedUnreadCount();
        }
      }
    });
  }

  public void joinPrivateChannelWithName(String name, String partnerId, final StatusListener listener) {
    Channel channel = this.getPrivateChannelWithName(name);
    if (channel != null && channel.getStatus() == Channel.ChannelStatus.JOINED) {
      if (listener != null) {
        listener.onSuccess();
      }

      Members members = channel.getMembers();
      if (members != null) {
        channel.getMembers().inviteByIdentity(partnerId, null);
      }
      getUnreadCount(channel);
      return;
    }
    channel.join(new StatusListener() {
      @Override
      public void onSuccess() {
        channel.getMembers().inviteByIdentity(partnerId, null);
        getUnreadCount(channel);
        if (listener != null) {
          listener.onSuccess();
        }
      }

      @Override
      public void onError(ErrorInfo errorInfo) {
        if (listener != null) {
          listener.onError(errorInfo);
        }
      }
    });
  }

  public void declineInvitation(Channel channel) {
    channel.declineInvitation(new StatusListener() {
      @Override
      public void onSuccess() {
        int index = -1;
        for (int i = 0; i < channels.size(); i++) {
          if (channels.get(i).getUniqueName().equals(channel.getUniqueName())) {
            index = i;
            break;
          }
        }

        if (index >= 0) {
          channels.remove(index);
        }

        MainActivity.get().checkChatInvitation();
      }
    });
  }

  public void setChannelListener(ChatClientListener listener) {
    this.listener = listener;
  }

  private String getStringResource(int id) {
    Resources resources = MainActivity.get().getResources();
    return resources.getString(id);
  }

  public Channel getPrivateChannelWithName(String name) {
    for (Channel channel : channels) {
      if (channel.getUniqueName().equals(name)) {
        return channel;
      }
    }

    return null;
  }

  @Override
  public void onChannelAdded(Channel channel) {
    if (channel.getStatus() == Channel.ChannelStatus.JOINED || channel.getStatus() == Channel.ChannelStatus.INVITED) {
      channels.add(channel);
    }

    if (listener != null && listener != this) {
      listener.onChannelAdded(channel);
    }
    MainActivity.get().checkChatInvitation();
  }

  @Override
  public void onChannelUpdated(Channel channel, Channel.UpdateReason updateReason) {
    getUnreadCount(channel);

    int index = -1;
    for (int i = 0; i < channels.size(); i++) {
      if (channels.get(i).getUniqueName().equals(channel.getUniqueName())) {
        index = i;
        break;
      }
    }

    if (index >= 0) {
      if (channel.getStatus() == Channel.ChannelStatus.JOINED || channel.getStatus() == Channel.ChannelStatus.INVITED) {
        channels.set(index, channel);
      } else {
        channels.remove(index);
      }
    }

    MainActivity.get().checkChatInvitation();
    if (listener != null && listener != this) {
      listener.onChannelUpdated(channel, updateReason);
    }
  }

  @Override
  public void onChannelDeleted(Channel channel) {
    int index = -1;
    for (int i = 0; i < channels.size(); i++) {
      if (channels.get(i).getUniqueName().equals(channel.getUniqueName())) {
        index = i;
        break;
      }
    }

    if (index >= 0) {
      channels.remove(index);
    }

    if (listener != null && listener != this) {
      listener.onChannelDeleted(channel);
    }
    MainActivity.get().checkChatInvitation();
  }

  @Override
  public void onChannelSynchronizationChange(Channel channel) {
    if (listener != null && listener != this) {
      listener.onChannelSynchronizationChange(channel);
    }
  }

  @Override
  public void onError(ErrorInfo errorInfo) {
    if (listener != null && listener != this) {
      listener.onError(errorInfo);
    }
  }

  @Override
  public void onClientSynchronization(ChatClient.SynchronizationStatus synchronizationStatus) {
    Log.d("aa", "aa");
  }

  @Override
  public void onChannelJoined(Channel channel) {
    Log.d("aa", "aa");
  }

  @Override
  public void onChannelInvited(Channel channel) {
    int index = -1;
    for (int i = 0; i < channels.size(); i++) {
      if (channels.get(i).getUniqueName().equals(channel.getUniqueName())) {
        index = i;
        break;
      }
    }

    if (index >= 0) {
      if (channel.getStatus() == Channel.ChannelStatus.JOINED || channel.getStatus() == Channel.ChannelStatus.INVITED) {
        channels.set(index, channel);
      } else {
        channels.add(channel);
      }
    }

    MainActivity.get().checkChatInvitation();
    Log.d("aa", "aa");
  }

  @Override
  public void onUserUpdated(User user, User.UpdateReason updateReason) {
    if (listener != null && listener != this) {
      listener.onUserUpdated(user, updateReason);
    }
  }

  @Override
  public void onUserSubscribed(User user) {

  }

  @Override
  public void onUserUnsubscribed(User user) {

  }

  @Override
  public void onNewMessageNotification(String s, String s1, long l) {
    Log.d("aa", "aa");
  }

  @Override
  public void onAddedToChannelNotification(String s) {
    Log.d("aa", "aa");
  }

  @Override
  public void onInvitedToChannelNotification(String s) {
    Log.d("aa", "aa");
  }

  @Override
  public void onRemovedFromChannelNotification(String s) {

  }

  @Override
  public void onNotificationSubscribed() {

  }

  @Override
  public void onNotificationFailed(ErrorInfo errorInfo) {

  }

  @Override
  public void onConnectionStateChange(ChatClient.ConnectionState connectionState) {

  }

  @Override
  public void onTokenExpired() {
    refreshAccessToken();
  }

  @Override
  public void onTokenAboutToExpire() {
    refreshAccessToken();
  }

  private void refreshAccessToken() {
    AccessTokenFetcher accessTokenFetcher = chatClientManager.getAccessTokenFetcher();
    accessTokenFetcher.fetch(new TaskCompletionListener<String, String>() {
      @Override
      public void onSuccess(String token) {
        ChannelManager.this.chatClientManager.getChatClient().updateToken(token, new StatusListener() {
          @Override
          public void onSuccess() {

          }
        });
      }

      @Override
      public void onError(String message) {

      }
    });
  }

  private Handler setupListenerHandler() {
    Looper looper;
    Handler handler;
    if ((looper = Looper.myLooper()) != null) {
      handler = new Handler(looper);
    } else if ((looper = Looper.getMainLooper()) != null) {
      handler = new Handler(looper);
    } else {
      throw new IllegalArgumentException("Channel Listener must have a Looper.");
    }
    return handler;
  }
}
