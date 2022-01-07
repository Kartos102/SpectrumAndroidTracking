package com.jo.spectrumtracking.adapter;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.activity.MainActivity;
import com.jo.spectrumtracking.api.ApiClient;
import com.jo.spectrumtracking.api.ApiInterface;
import com.jo.spectrumtracking.fragment.GeofenceFragment;
import com.jo.spectrumtracking.fragment.MonitorFragment;
import com.jo.spectrumtracking.fragment.SetShareDeviceFragment;
import com.jo.spectrumtracking.fragment.ShareFragment;
import com.jo.spectrumtracking.global.GlobalConstant;
import com.jo.spectrumtracking.global.Utils;
import com.jo.spectrumtracking.model.Resp_ImageUrl;
import com.jo.spectrumtracking.model.Resp_Tracker;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

/**
 * Created by JO on 3/17/2018.
 */

public class AssetListMultiSelectRecyclerViewAdapter extends RecyclerView.Adapter<AssetListMultiSelectRecyclerViewAdapter.ViewHolder> {

    private List<Resp_Tracker> itemList;
    private int itemLayoutResID;
    private Fragment fragment;
    private String getAddresskey = "9pZmjAAHOLwLLmeDe54Y8epAGWrv53Fm8YhPgmI9";

    public AssetListMultiSelectRecyclerViewAdapter(Fragment fragment, List<Resp_Tracker> itemList, int itemLayoutResID) {
        this.itemList = itemList;
        this.itemLayoutResID = itemLayoutResID;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View viewHolder = LayoutInflater.from(parent.getContext()).inflate(itemLayoutResID, parent, false);
        int height = parent.getMeasuredHeight() / 4;
        viewHolder.setMinimumHeight(25);

        return new AssetListMultiSelectRecyclerViewAdapter.ViewHolder(viewHolder);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        if (itemList.size() <= position) return;

        final Resp_Tracker item = itemList.get(position);

        if (fragment instanceof GeofenceFragment) {
            GeofenceFragment geofenceFragment = (GeofenceFragment) fragment;
            // geofenceFragment.onRightPanelAssetListCheckChanged(position, true);
            // item.isSelected=true;
            holder.checkReplayRightOptions.setChecked(item.getSelected());
        } else holder.checkReplayRightOptions.setChecked(item.getSelected());

        //holder.txtVehicleName.setText(item.getName() + "(" + item.getDriverName() + ")");
        holder.txtVehicleName.setText(item.getDriverName() );


        holder.checkReplayRightOptions.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (fragment instanceof MonitorFragment) {
                MonitorFragment monitorFragment = (MonitorFragment) fragment;
                monitorFragment.onBottomTrackerListCheckChanged(position, isChecked);
            } else if (fragment instanceof GeofenceFragment) {
                GeofenceFragment geofenceFragment = (GeofenceFragment) fragment;
                geofenceFragment.onBottomTrackerListCheckChanged(position, isChecked);
            }
        });

        final ImageView btn_option = holder.btn_option;
        holder.btn_option.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(fragment.getContext(), btn_option);
            popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

            SharedPreferences preferences = fragment.getContext().getSharedPreferences("spectrum", Context.MODE_PRIVATE);
            String myId = preferences.getString("username", "");
            if (item.getSpectrumId().equals(myId) || !item.getTrackerModel().toLowerCase().equals("phone")) {
                popup.getMenu().removeItem(R.id.btn_chatting);
            }

            popup.setOnMenuItemClickListener(item1 -> {
                if (item1.getTitle().equals(getApplicationContext().getResources().getString(R.string.edit_vehicle_info))) {
                    if (fragment instanceof MonitorFragment) {
                        MonitorFragment monitorFragment = (MonitorFragment) fragment;
                        monitorFragment.onUpdateTracker(position);
                    }
                } else if (item1.getTitle().equals(getApplicationContext().getResources().getString(R.string.get_there))) {
                    if (fragment instanceof MonitorFragment) {
                        MonitorFragment monitorFragment = (MonitorFragment) fragment;
                        monitorFragment.onGetRoute(position);
                    }
                } else if (item1.getTitle().equals(getApplicationContext().getResources().getString(R.string.share_your_location))) {
                    if (fragment instanceof MonitorFragment) {
                        MainActivity mainActivity = (MainActivity) fragment.getActivity();
                        ShareFragment fragment = ShareFragment.newInstance();
                        mainActivity.pushFragment(fragment);
                    }
                } else {
                    if (fragment instanceof MonitorFragment) {
//                        MainActivity mainActivity = (MainActivity)fragment.getActivity();
                        MonitorFragment monitorFragment = (MonitorFragment) fragment;
                        monitorFragment.onChatRoom(position);
                    }
                }
                // Toast.makeText(fragment.getContext(),"You Clicked : " + item.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            });
            popup.show();
        });

        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(getPixel(8));
        double speed = item.getSpeedInMph();
        if (item.getACCStatus() != 0 && item.getSpeedInMph() != 0) {
            shape.setColor(Color.rgb(0, 200, 83));
        } else if (item.getACCStatus() != 0 && item.getSpeedInMph() == 0) {
            shape.setColor(Color.rgb(186, 69, 240));
            speed = 0;
        } else {
            shape.setColor(Color.rgb(244, 31, 27));
            speed = 0;
        }
        holder.status.setBackground(shape);

        double metricScale = 1.60934;
        String distUnit = " kmh ";

//        if (item.getCountry() != null) {
//            metricScale = (item.getCountry().equals("United States") || item.getCountry().equals("US") || item.getCountry().length() == 0) ? 1 : 1.60934;
//            distUnit = (item.getCountry().equals("United States") || item.getCountry().equals("US") || item.getCountry().length() == 0) ? " mph " : " kmh ";
//        }
        metricScale = Utils.getDistanceUnit().equals("miles") ? 1 : 1.60934;
        distUnit = Utils.getDistanceUnit().equals("miles") ? " mph " : " kmh ";


        holder.txtSpeed.setText(String.format("%.0f", speed * metricScale) + distUnit);
        if (item.getLatLngDateTime() != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd HH:mm");
            holder.txtLastUpdate.setText("Last Update at " + simpleDateFormat.format(item.getLatLngDateTime()));
        } else {
            holder.txtLastUpdate.setText("");
        }
        holder.txtLastUpdate.setSelected(true);
        holder.txtVehicleName.setSelected(true);

        if (item.isPhotoStatus()) {
            getImageUrl("driver_" + item.getAssetId() + ".jpg", item.get_id(), holder.driver_image, true);
        } else {
            holder.driver_image.setImageResource(R.drawable.driver_empty);
        }

        holder.driver_image.setOnClickListener(v -> {
            ((MainActivity)(fragment.getActivity())).pickImage(item.getAssetId(), item.get_id());
        });
        if (GlobalConstant.allAddress.containsKey(item.get_id()) && item.getChangeFlag()==false) {
            holder.txtAddress.setText("Near " + GlobalConstant.allAddress.get(item.get_id()));
        } else getAddress(item.getLat(), item.getLng(), getAddresskey, holder.txtAddress, item);
        holder.itemView.setTag(item);
    }

    private int getPixel(int dp) {
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, fragment.getContext().getResources().getDisplayMetrics());
        return height;
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.check_replay_right_options)
        CheckBox checkReplayRightOptions;

        @BindView(R.id.txt_vehicle_name)
        TextView txtVehicleName;


        @BindView(R.id.last_update)
        TextView txtLastUpdate;

        @BindView(R.id.address)
        TextView txtAddress;

        @BindView(R.id.txt_speed)
        TextView txtSpeed;

        @BindView(R.id.driver_image)
        CircleImageView driver_image;

        @BindView(R.id.btn_option)
        ImageView btn_option;

        @BindView(R.id.status)
        ImageView status;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }

    private void getImageUrl(final String filename, String trackerId, final CircleImageView userImage, final Boolean changeFlag) {
        Log.d("getImageUrl fileName", filename);
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("driver_images", Context.MODE_PRIVATE);
        File myImageFile = new File(directory, filename);

//        if (myImageFile.exists() && !changeFlag) {
        if (myImageFile.exists()) {
            //Utils.showShortToast(fragment.getContext(), GlobalConstant.photoUploadTrackerId);
            if (!GlobalConstant.photoUploadTrackerId.equals(filename))
                Picasso.get().load(myImageFile).into(userImage);
            else {
                Picasso.get().load(myImageFile).memoryPolicy(MemoryPolicy.NO_CACHE).into(userImage);
                GlobalConstant.photoUploadTrackerId = "";
            }
        } else {

            if (!Utils.isNetworkConnected(fragment.getContext())) {
                return;
            }

            ApiInterface apiInterface = ApiClient.getClient(fragment.getContext()).create(ApiInterface.class);

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
                            Date date = new Date();
                            GlobalConstant.driverImageMap.put(trackerId, respImageUrl.getUrl());
                            Picasso.get().load(respImageUrl.getUrl()).memoryPolicy(MemoryPolicy.NO_CACHE).placeholder(R.drawable.driver_empty).into(userImage);
                        } else {
//                            Utils.showShortToast(fragment.getContext(), respImageUrl.getUrl(), true);

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    t.printStackTrace();
                    Utils.showShortToast(fragment.getContext(), fragment.getString(R.string.weak_cell_signal), true);
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

    private void getAddress(double lat, double lng, String key, final TextView txtAddress, final Resp_Tracker item) {
        ApiInterface apiInterface = ApiClient.getAddressClient(fragment.getContext()).create(ApiInterface.class);

        if (lat >= 180 || lat <= -180 || lng >= 90 || lng <= -90) {
            return;
        }

        apiInterface.getAddress(lat, lng, key).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    ResponseBody responseBody = response.body();
                    JSONObject object = null;
                    try {
                        object = new JSONObject(responseBody.string());
                        JSONArray result = object.getJSONArray("results");
                        JSONObject real_address = (JSONObject) result.get(0);
                        txtAddress.setText("Near " + real_address.getString("address"));
                        GlobalConstant.allAddress.put(item.get_id(), real_address.getString("address"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Utils.showShortToast(fragment.getContext(), fragment.getString(R.string.weak_cell_signal), true);
            }
        });

    }
}
