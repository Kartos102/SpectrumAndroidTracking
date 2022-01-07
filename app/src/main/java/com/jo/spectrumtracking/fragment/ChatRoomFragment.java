package com.jo.spectrumtracking.fragment;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.activity.MainActivity;
import com.jo.spectrumtracking.api.ApiClient;
import com.jo.spectrumtracking.api.ApiInterface;
import com.jo.spectrumtracking.global.GlobalConstant;
import com.jo.spectrumtracking.global.Utils;
import com.jo.spectrumtracking.model.Resp_ImageUrl;
import com.jo.spectrumtracking.model.Resp_Tracker;
import com.jo.spectrumtracking.twilio.chat.channels.ChannelManager;
import com.jo.spectrumtracking.twilio.chat.messages.ChatMessage;
import com.jo.spectrumtracking.twilio.chat.messages.DateFormatter;
import com.jo.spectrumtracking.twilio.chat.messages.UserMessage;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.twilio.chat.CallbackListener;
import com.twilio.chat.Channel;
import com.twilio.chat.ChannelListener;
import com.twilio.chat.ErrorInfo;
import com.twilio.chat.Member;
import com.twilio.chat.Message;
import com.twilio.chat.Messages;
import com.twilio.chat.StatusListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

public class ChatRoomFragment extends Fragment implements ChannelListener {

    @BindView(R.id.editMessage)
    EditText editMessage;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.imgProfile)
    CircleImageView imgProfile;
    @BindView(R.id.txtUserName)
    TextView txtUserName;

    private Boolean isFromChatRoomList = false;

    private Channel channel = null;
    private String channelName = "";
    Resp_Tracker selectedTracker = null;

    Messages messagesObject;
    List<ChatMessage> messages = new ArrayList<>();
    MyAdapter adapter = new MyAdapter();

    public ChatRoomFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ChatRoomFragment newInstance(Resp_Tracker tracker, Boolean isFromChatRoomList) {
        ChatRoomFragment fragment = new ChatRoomFragment();
        fragment.selectedTracker = tracker;
        fragment.isFromChatRoomList = isFromChatRoomList;
        return fragment;
    }

    public static ChatRoomFragment newInstance(String channelName, Boolean isFromChatRoomList) {
        ChatRoomFragment fragment = new ChatRoomFragment();

        fragment.channelName = channelName;
        String partnerName = ChannelManager.getInstance().getPartnerName(channelName);

        for (Resp_Tracker tracker : GlobalConstant.AllTrackerList) {
            if (partnerName.equalsIgnoreCase(tracker.getSpectrumId())) {
                fragment.selectedTracker = tracker;
                break;
            }
        }

        fragment.isFromChatRoomList = isFromChatRoomList;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String partnerId = "";

        if (selectedTracker != null) {
            partnerId = selectedTracker.getSpectrumId();
            channelName = ChannelManager.getInstance().getChannelName(partnerId);
        } else if (!channelName.isEmpty()) {
            partnerId = ChannelManager.getInstance().getPartnerId(channelName);
        }

        if (channelName.isEmpty() || partnerId == null || partnerId.isEmpty()) {
            return;
        }

        ChannelManager.getInstance().joinOrCreatePrivateChannelWithName(channelName, partnerId, new StatusListener() {
            @Override
            public void onSuccess() {
                channel = ChannelManager.getInstance().getPrivateChannelWithName(channelName);
                channel.addListener(ChatRoomFragment.this);
                if (channel.getStatus() == Channel.ChannelStatus.JOINED) {
                    loadMessages();
                } else {
                    channel.join(new StatusListener() {
                        @Override
                        public void onSuccess() {
                            loadMessages();
                        }
                    });
                }
            }

            @Override
            public void onError(ErrorInfo errorInfo) {
                super.onError(errorInfo);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_chat_room, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        if (selectedTracker != null) {
            txtUserName.setText(selectedTracker.getDriverName());

            if (selectedTracker.isPhotoStatus()) {
                if (selectedTracker.get_id().equals(GlobalConstant.photoUploadTrackerId)) {
                    getImageUrl("driver_" + selectedTracker.getAssetId() + ".jpg", imgProfile, true);
                } else getImageUrl("driver_" + selectedTracker.getAssetId() + ".jpg", imgProfile, false);
            } else {
                imgProfile.setImageResource(R.drawable.driver_empty);
            }
        } else {
            String partnerName = ChannelManager.getInstance().getPartnerName(channelName);
            txtUserName.setText(partnerName);
            imgProfile.setImageResource(R.drawable.driver_empty);
        }

        recyclerView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            int offsetY = oldBottom - bottom;
            if ( bottom < oldBottom) {
                recyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.scrollBy(0, offsetY);
                    }
                }, 100);
            }
        });
    }

    public void loadMessages() {
        this.messagesObject = this.channel.getMessages();
        this.channel.getMessagesCount(new CallbackListener<Long>() {
            @Override
            public void onSuccess(Long messageCount) {
                if (messagesObject != null && messageCount != null) {
                    messagesObject.getLastMessages(messageCount.intValue(), new CallbackListener<List<Message>>() {
                        @Override
                        public void onSuccess(List<Message> messageList) {
                            messages = convertTwilioMessages(messageList);
                            adapter.notifyDataSetChanged();
                            scrollToBottom();
                        }
                    });

                    messagesObject.setAllMessagesConsumedWithResult(null);
                    ChannelManager.getInstance().unreadCountMap.put(channelName, 0L);
                }
            }
        });
    }

    private List<ChatMessage> convertTwilioMessages(List<Message> messages) {
        List<ChatMessage> chatMessages = new ArrayList<>();
        for (Message message : messages) {
            chatMessages.add(new UserMessage(message));
        }
        return chatMessages;
    }

    private void sendMessage() {
        String messageText = editMessage.getText().toString();
        if (messageText.length() == 0) {
            return;
        }

        Message.Options messageOptions = Message.options().withBody(messageText);
        this.messagesObject.sendMessage(messageOptions, new CallbackListener<Message>() {
            @Override
            public void onSuccess(Message message) {
            ChannelManager.getInstance().unreadCountMap.put(channelName, 0L);
            messagesObject.setAllMessagesConsumedWithResult(null);
            }
        });
        editMessage.setText("");
    }

    @OnClick(R.id.btnSend)
    public void onSend() {
        sendMessage();
    }

    @OnClick(R.id.back)
    public void onBack() {
//        if (this.isFromChatRoomList) {
//            ChatRoomListFragment fragment = ChatRoomListFragment.newInstance(new ArrayList<>(GlobalConstant.AllTrackerList));
//            MainActivity.get().replaceOverlayFragment(fragment);
//        } else {
//            MainActivity.get().hideOverlayFragment();
//        }
        MainActivity.get().popFragment();
        MainActivity.get().updatedUnreadCount();
    }

    private void getImageUrl(final String filename, final ImageView userImage, final Boolean changeFlag) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("driver_images", Context.MODE_PRIVATE);
        File myImageFile = new File(directory, filename);

        if (myImageFile.exists() && !changeFlag) {
            //Utils.showShortToast(fragment.getContext(), GlobalConstant.photoUploadTrackerId);
            if (!GlobalConstant.photoUploadTrackerId.equals(filename))
                Picasso.get().load(myImageFile).into(userImage);
            else {
                Picasso.get().load(myImageFile).memoryPolicy(MemoryPolicy.NO_CACHE).into(userImage);
                GlobalConstant.photoUploadTrackerId = "";
            }
        } else {
            if (!Utils.isNetworkConnected(this.getContext())) {
                return;
            }

            ApiInterface apiInterface = ApiClient.getClient(this.getContext()).create(ApiInterface.class);

            HashMap<String, Object> body = new HashMap<>();

            body.put("name", filename);

            apiInterface.getImageUrl(body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    ResponseBody responseBody = response.body();
                    try {
                        String bodyString = responseBody.string();
                        Log.d("getimageurl", bodyString);
                        Gson gson = new Gson();
                        Resp_ImageUrl respImageUrl = gson.fromJson(bodyString, Resp_ImageUrl.class);
                        if (respImageUrl.isSuccess() == true) {
                            //Utils.showShortToast(fragment.getContext(), respImageUrl.url);
                            Date date = new Date();
                            Picasso.get().load(respImageUrl.getUrl() + "&" + date.getTime()).into(picassoImageTarget(getApplicationContext(), "driver_images", filename));
                            ContextWrapper cw = new ContextWrapper(getApplicationContext());
                            File directory = cw.getDir("driver_images", Context.MODE_PRIVATE);
                            File myImageFile = new File(directory, filename);
                            Picasso.get().load(myImageFile).memoryPolicy(MemoryPolicy.NO_CACHE).into(userImage);
                            GlobalConstant.photoUploadTrackerId = filename;
                        } else {
//                            Utils.showShortToast(ChatRoomFragment.this.getContext(), respImageUrl.getUrl());

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    t.printStackTrace();
                    Utils.showShortToast(ChatRoomFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
                }
            });
        }

    }

    private Target picassoImageTarget(Context context, final String imageDir, final String imageName) {
        Log.d("picassoImageTarget", " picassoImageTarget");
        ContextWrapper cw = new ContextWrapper(context);
        final File directory = cw.getDir(imageDir, Context.MODE_PRIVATE); // path to /data/data/yourapp/app_imageDir
        return new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(() -> {
                    final File myImageFile = new File(directory, imageName); // Create image file
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(myImageFile);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.d("image", "image saved to >>>" + myImageFile.getAbsolutePath());

                }).start();
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                if (placeHolderDrawable != null) {
                }
            }
        };
    }

    private void scrollToBottom() {
        if (messages.size() == 0) {
            return;
        }

        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                recyclerView.smoothScrollToPosition(messages.size() - 1);
            }
        }, 100);
    }

    @Override
    public void onMessageAdded(Message message) {
        messages.add(new UserMessage(message));
        messagesObject.setAllMessagesConsumedWithResult(null);
        adapter.notifyDataSetChanged();
        scrollToBottom();
    }

    @Override
    public void onMessageUpdated(Message message, Message.UpdateReason updateReason) {

    }

    @Override
    public void onMessageDeleted(Message message) {

    }

    @Override
    public void onMemberAdded(Member member) {

    }

    @Override
    public void onMemberUpdated(Member member, Member.UpdateReason updateReason) {

    }

    @Override
    public void onMemberDeleted(Member member) {

    }

    @Override
    public void onTypingStarted(Channel channel, Member member) {

    }

    @Override
    public void onTypingEnded(Channel channel, Member member) {

    }

    @Override
    public void onSynchronizationChanged(Channel channel) {

    }

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        class MyViewHolder extends RecyclerView.ViewHolder {

            LinearLayout layoutHolder, layoutLeft, layoutRight;
            TextView txtLeftDate, txtLeftMessage, txtRightDate, txtRightMessage;

            public MyViewHolder(@NonNull View view) {
                super(view);
                this.layoutHolder = view.findViewById(R.id.layoutHolder);
                this.layoutLeft = view.findViewById(R.id.layoutLeft);
                this.layoutRight = view.findViewById(R.id.layoutRight);
                this.txtLeftDate = view.findViewById(R.id.txtLeftDate);
                this.txtLeftMessage = view.findViewById(R.id.txtLeftMessage);
                this.txtRightDate = view.findViewById(R.id.txtRightDate);
                this.txtRightMessage = view.findViewById(R.id.txtRightMessage);
            }
        }
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_chat_message, viewGroup, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
            ChatMessage message = messages.get(position);

            SharedPreferences preferences = MainActivity.get().getSharedPreferences("spectrum", Context.MODE_PRIVATE);
            String myId = preferences.getString("username", "");

            if (!message.getAuthor().equals(myId)) {
                holder.layoutLeft.setVisibility(View.VISIBLE);
                holder.layoutRight.setVisibility(View.GONE);
                if (TextUtils.isEmpty(message.getDateCreated())) {
                    holder.txtLeftDate.setVisibility(View.GONE);
                } else {
                    holder.txtLeftDate.setVisibility(View.VISIBLE);

                    String partnerName = "";
                    if (selectedTracker != null) {
                        partnerName = selectedTracker.getDriverName();
                    } else {
                        partnerName = ChannelManager.getInstance().getPartnerName(channelName);
                    }
                    String userNameAndDate = partnerName + ", " + DateFormatter.getFormattedDateFromISOString(message.getDateCreated());
                    holder.txtLeftDate.setText(userNameAndDate);
                }

                holder.txtLeftMessage.setVisibility(View.VISIBLE);
                holder.txtLeftMessage.setText(message.getMessageBody());
            } else {
                holder.layoutLeft.setVisibility(View.GONE);
                holder.layoutRight.setVisibility(View.VISIBLE);

                if (TextUtils.isEmpty(message.getDateCreated())) {
                    holder.txtRightDate.setVisibility(View.GONE);
                } else {
                    holder.txtRightDate.setVisibility(View.VISIBLE);
                    holder.txtRightDate.setText(DateFormatter.getFormattedDateFromISOString(message.getDateCreated()));
                }

                holder.txtRightMessage.setVisibility(View.VISIBLE);
                holder.txtRightMessage.setText(message.getMessageBody());
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }
    }
}