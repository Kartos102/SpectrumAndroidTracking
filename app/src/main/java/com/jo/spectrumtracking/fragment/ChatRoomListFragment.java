package com.jo.spectrumtracking.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
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
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.twilio.chat.Channel;
import com.twilio.chat.StatusListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

public class ChatRoomListFragment extends Fragment {

    public static final int REQUEST_CODE_CONTACT = 1;
    public static final int PERMISSIONS_REQUEST_READ_CONTACTS = 2;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private ArrayList<Resp_Tracker> trackerList = new ArrayList<>();
    private MyAdapter adapter = new MyAdapter();

    private EditText etDlgInvitationEmail;

    public ChatRoomListFragment() {

    }

    // TODO: Rename and change types and number of parameters
    public static ChatRoomListFragment newInstance(ArrayList<Resp_Tracker> trackerList) {

        SharedPreferences preferences = MainActivity.get().getSharedPreferences("spectrum", Context.MODE_PRIVATE);
        String myId = preferences.getString("username", "");

        ChatRoomListFragment fragment = new ChatRoomListFragment();
        ArrayList<Resp_Tracker> _trackerList = new ArrayList<>();
        for (Resp_Tracker tracker : trackerList) {
            if (!tracker.getSpectrumId().equals(myId) && tracker.getTrackerModel().toLowerCase().equals("phone")) {
                _trackerList.add(tracker);
            }
        }

        fragment.trackerList = _trackerList;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_chat_room_list, container, false);;
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);
    }

    @OnClick(R.id.back)
    public void onBack() {
        MainActivity.get().onMonitor();
    }

    @OnClick(R.id.btn_invite)
    public void onInvite() {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_invite_to_chat);
        //dialog.setCancelable(false);
        final EditText shareEmail = dialog.findViewById(R.id.edt_invite_chat_share_email);
        etDlgInvitationEmail = shareEmail;

        ImageView contactImageView = dialog.findViewById(R.id.iv_invite_chat_contact);
        Button btnInvite = dialog.findViewById(R.id.btn_dlg_invite_chat_ok);
        Button btnCancel = dialog.findViewById(R.id.btn_dlg_invite_chat_cancel);

//        contactImageView.setVisibility(View.VISIBLE);

        btnInvite.setOnClickListener(v -> {
            inviteToChat(shareEmail.getText().toString());
            etDlgInvitationEmail = null;
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> {
            etDlgInvitationEmail = null;
            dialog.dismiss();
        });

        contactImageView.setOnClickListener(v -> {
            if (requestContactPermission()) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Email.CONTENT_URI);
                startActivityForResult(intent, REQUEST_CODE_CONTACT);
            }
        });

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(dialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.show();
        dialog.getWindow().setAttributes(lWindowParams);
    }

    public void onChatRoom(int position) {
        if (position >= ChannelManager.getInstance().getPrivateChannels().size()) {
            return;
        }

        MainActivity mainActivity = (MainActivity)getActivity();
//        ChatRoomFragment fragment = ChatRoomFragment.newInstance(assetList.get(position), true);
        String channelName = ChannelManager.getInstance().getPrivateChannels().get(position).getUniqueName();
        ChatRoomFragment fragment = ChatRoomFragment.newInstance(channelName, true);
        mainActivity.pushFragment(fragment);
    }

    public void updateList() {
        adapter.notifyDataSetChanged();
    }

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        class MyViewHolder extends RecyclerView.ViewHolder {

            CircleImageView imgProfile;
            TextView txtUserName;
            TextView txtUnReadCount;
            LinearLayout invitationButtons;
            Button btnAccept;
            Button btnReject;

            public MyViewHolder(@NonNull View view) {
                super(view);
                this.imgProfile = view.findViewById(R.id.driver_image);
                this.txtUserName = view.findViewById(R.id.txt_user_name);
                this.txtUnReadCount = view.findViewById(R.id.txt_unread_count);
                this.invitationButtons = view.findViewById(R.id.ll_invitation_buttons);
                this.btnAccept = view.findViewById(R.id.btn_accept);
                this.btnReject = view.findViewById(R.id.btn_reject);
            }
        }
        @NonNull
        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_chat_room_list, viewGroup, false);
            return new MyAdapter.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyAdapter.MyViewHolder holder, int position) {
            Channel channel = ChannelManager.getInstance().getPrivateChannels().get(position);
            String channelName = channel.getUniqueName();
            String partnerId = ChannelManager.getInstance().getPartnerName(channelName);

            Resp_Tracker tracker = null;

            for (Resp_Tracker _tracker : trackerList) {
                if (_tracker.getSpectrumId().equalsIgnoreCase(partnerId)) {
                    tracker = _tracker;
                    break;
                }
            }

            if (tracker != null) {
                holder.txtUserName.setText(tracker.getDriverName());
                if (tracker.isPhotoStatus()) {
                    if (tracker.get_id().equals(GlobalConstant.photoUploadTrackerId)) {
                        getImageUrl("driver_" + tracker.getAssetId() + ".jpg", holder.imgProfile, true);
                    } else getImageUrl("driver_" + tracker.getAssetId() + ".jpg", holder.imgProfile, false);
                } else {
                    holder.imgProfile.setImageResource(R.drawable.driver_empty);
                }
            } else {
                holder.txtUserName.setText(partnerId);
                holder.imgProfile.setImageResource(R.drawable.driver_empty);
            }

            if (channel.getStatus() == Channel.ChannelStatus.JOINED) {
                holder.invitationButtons.setVisibility(View.GONE);
                Long unreadCount = ChannelManager.getInstance().unreadCountMap.get(channelName);

                if (unreadCount == null || unreadCount == 0L) {
                    holder.txtUnReadCount.setVisibility(View.GONE);
                } else {
                    holder.txtUnReadCount.setVisibility(View.VISIBLE);
                    holder.txtUnReadCount.setText(String.format("%d", unreadCount.intValue()));
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onChatRoom(position);
                    }
                });
            } else if (channel.getStatus() == Channel.ChannelStatus.INVITED) {
                holder.invitationButtons.setVisibility(View.VISIBLE);
                holder.txtUnReadCount.setVisibility(View.GONE);
                holder.btnAccept.setOnClickListener( v -> {
                    channel.join(new StatusListener() {
                        @Override
                        public void onSuccess() {
                            onChatRoom(position);
                        }
                    });
                });
                holder.btnReject.setOnClickListener( v -> {
                    ChannelManager.getInstance().declineInvitation(channel);
                });
            }
        }

        @Override
        public int getItemCount() {
            return ChannelManager.getInstance().getPrivateChannels().size();
//            return assetList.size();
        }

        private void getImageUrl(final String filename, final CircleImageView userImage, final Boolean changeFlag) {
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

                if (!Utils.isNetworkConnected(ChatRoomListFragment.this.getContext())) {
                    return;
                }

                ApiInterface apiInterface = ApiClient.getClient(ChatRoomListFragment.this.getContext()).create(ApiInterface.class);

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
                                //  Picasso.get().load(respImageUrl.url + "&" + date.getTime()).into(userImage);
                            } else {
//                                Utils.showShortToast(ChatRoomListFragment.this.getContext(), respImageUrl.getUrl(), true);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        t.printStackTrace();
                        Utils.showShortToast(ChatRoomListFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
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
    }

    public void inviteToChat(String email) {
        ApiInterface apiInterface = ApiClient.getClient(this.getContext()).create(ApiInterface.class);
        HashMap<String, Object> body = new HashMap<>();
        body.put("email", email);
        apiInterface.inviteJoinChat(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                String channelName = ChannelManager.getInstance().getChannelName(email);
                ChannelManager.getInstance().joinOrCreatePrivateChannelWithName(channelName, email, null);
                Utils.showShortToast(ChatRoomListFragment.this.getContext(), getString(R.string.successfully_invited_to_join_chat), false);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Utils.showShortToast(ChatRoomListFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
            }
        });
    }

    public boolean requestContactPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Activity activity = getActivity();
            if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{android.Manifest.permission.READ_CONTACTS},
                        PERMISSIONS_REQUEST_READ_CONTACTS);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ChatRoomListFragment.REQUEST_CODE_CONTACT && data != null) {
            String email = "";
            Uri uri = data.getData();
            Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
                email = cursor.getString(index);
            }
            cursor.close();

            if (etDlgInvitationEmail != null) {
                etDlgInvitationEmail.setText(email);
            }
        }
    }
}