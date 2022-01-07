package com.jo.spectrumtracking.global;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.activity.MainActivity;
import com.jo.spectrumtracking.model.Resp_Tracker;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class Utils {

    public static final int NETWORK_STATUS_NOT_CONNECTED = 0, NETWORK_STAUS_WIFI = 1, NETWORK_STATUS_MOBILE = 2;

    public static void showShortToast(final Context context, final int res_id, Boolean isFailed) {
//        Toast.makeText(context, res_id, Toast.LENGTH_SHORT).show();
        int alertType = isFailed ? SweetAlertDialog.ERROR_TYPE : SweetAlertDialog.SUCCESS_TYPE;
        Utils.showSweetAlert(context, null, context.getResources().getString(res_id), null, null, alertType, null);
    }

    public static void log(String log) {
        Log.e("LOG from sandy", log);
    }

    public static void showShortToast(final Context context, final String msg, Boolean isFailed) {
        if(context!=null) {
//           Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            int alertType = isFailed ? SweetAlertDialog.ERROR_TYPE : SweetAlertDialog.SUCCESS_TYPE;
            Utils.showSweetAlert(context, null, msg, null, null, alertType, null);
        }
    }

    public static void showProgress(final Context context) {
        if (GlobalConstant.progressDialog != null) {
            if (GlobalConstant.progressDialog.getOwnerActivity() != null) {
                if (GlobalConstant.progressDialog.getOwnerActivity().isDestroyed()) {
                    return;
                }
                if (GlobalConstant.progressDialog != null && GlobalConstant.progressDialog.isShowing()) {
                    GlobalConstant.progressDialog.dismiss();
                }
            }
        }
//        GlobalConstant.progressDialog = new ProgressDialog(context, R.style.CustomProgressDialogTheme);
//        GlobalConstant.progressDialog.setCancelable(false);
////        GlobalConstant.progressDialog.setMessage(context.getString(R.string.please_wait));
//        GlobalConstant.progressDialog.show();

        GlobalConstant.progressDialog = ProgressDialog.show(context, null,null);
        ProgressBar spinner = new android.widget.ProgressBar(context, null,android.R.attr.progressBarStyle);
        spinner.getIndeterminateDrawable().setColorFilter(context.getResources().getColor(R.color.main), android.graphics.PorterDuff.Mode.SRC_IN);
        GlobalConstant.progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        GlobalConstant.progressDialog.setContentView(spinner);
        GlobalConstant.progressDialog.setCancelable(false);
        GlobalConstant.progressDialog.show();
    }

    public static void hideProgress() {
        if (GlobalConstant.progressDialog == null) {
            return;
        }
        if (GlobalConstant.progressDialog.getOwnerActivity() != null) {
            if (Build.VERSION.SDK_INT < 17) {
                if (GlobalConstant.progressDialog.getOwnerActivity().isFinishing()) return;
            } else {
                if (GlobalConstant.progressDialog.getOwnerActivity().isDestroyed()) return;
            }
        }

        if (GlobalConstant.progressDialog.isShowing()) {
            GlobalConstant.progressDialog.dismiss();
            GlobalConstant.progressDialog = null;
        }
    }

    public static int TYPE_WIFI = 1;
    public static int TYPE_MOBILE = 2;
    public static int TYPE_NOT_CONNECTED = 0;

    public static boolean isNetworkConnected(Context context) {
        if (context == null) return false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //showShortToast(context,"Server connection error");

        return cm.getActiveNetworkInfo() != null;
    }

    public static int getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return TYPE_WIFI;

            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return TYPE_MOBILE;
        }
        return TYPE_NOT_CONNECTED;
    }

    public static int getConnectivityStatusString(Context context) {
        int conn = getConnectivityStatus(context);
        int status = 0;
        if (conn == TYPE_WIFI) {
            status = NETWORK_STAUS_WIFI;
        } else if (conn == TYPE_MOBILE) {
            status = NETWORK_STATUS_MOBILE;
        } else if (conn == TYPE_NOT_CONNECTED) {
            status = NETWORK_STATUS_NOT_CONNECTED;
        }
        return status;
    }

    public static void setMapUtilityToolsInReplay(final MapboxMap mapboxMap, View rootView) {
        SeekBar seek_zoom = rootView.findViewById(R.id.seek_zoom_replay);
        //.setProgress(9);
        //seek_zoom.setProgress((int)mapboxMap.getCameraPosition().zoom);

        seek_zoom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressValue = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressValue = progress;
                CameraPosition cameraPosition = mapboxMap.getCameraPosition();
                CameraPosition new_cameraPosition = new CameraPosition.Builder().target(cameraPosition.target).zoom((double) progressValue).bearing(0).tilt(0).build();
                mapboxMap.setCameraPosition(new_cameraPosition);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public static void setMapUtilityToolsInGeofence(final MapboxMap mapboxMap, View rootView) {
        SeekBar seek_zoom = rootView.findViewById(R.id.seek_zoom_geofence);
        //seek_zoom.setProgress(9);
        //seek_zoom.setProgress((int)mapboxMap.getCameraPosition().zoom);

        seek_zoom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressValue = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressValue = progress;
                CameraPosition cameraPosition = mapboxMap.getCameraPosition();
                CameraPosition new_cameraPosition = new CameraPosition.Builder().target(cameraPosition.target).zoom((double) progressValue).bearing(0).tilt(0).build();
                mapboxMap.setCameraPosition(new_cameraPosition);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public static String trucatLabelString(String name, int toIndex) {
        if (name.length() - 1 > toIndex) {
            name = name.substring(0, toIndex);
            name += "...";
        }

        return name;
    }

    static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_locaction_updates";

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    public static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    public static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }

    /**
     * Returns the {@code location} object as a human readable string.
     * @param location  The {@link Location}.
     */
    static String getLocationText(Location location) {
        return location == null ? "Unknown location" :
                "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
    }

    /*static String getLocationTitle(Context context) {
        return context.getString(R.string.location_updated,
                DateFormat.getDateTimeInstance().format(new Date()));
    }*/

    /***
     * Resize bitmap image
     * @param image
     * @param maxSize
     * @return
     */
    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public static int getPixel(int dp, Resources res) {
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
        return height;
    }

    public static String getDistanceUnit() {
        SharedPreferences preferences = MainActivity.get().getSharedPreferences("spectrum", Context.MODE_PRIVATE);
        String unit = preferences.getString("distance_unit", "miles");
        return unit;
    }

    public static void showSweetAlert(Context context, String title, String message, String confirmButton, String cancelButton, int alertType, OnSweetAlertListener listener) {
        SweetAlertDialog alert = new SweetAlertDialog(context, alertType);
        if (title != null) {
            alert.setTitleText(title);
        }
        if (message != null) {
            alert.setContentText(message);
        }
        if (confirmButton != null) {
            alert.setConfirmButton(confirmButton, sweetAlertDialog -> {
                if (listener != null) {
                    listener.onConfirm();
                }
                sweetAlertDialog.dismissWithAnimation();
            });
        }
        if (cancelButton != null) {
            alert.setCancelButton(cancelButton, sweetAlertDialog -> {
                if (listener != null) {
                    listener.onCancel();
                }
                sweetAlertDialog.dismissWithAnimation();
            });
        }

        alert.show();
    }

    public interface OnSweetAlertListener {
        void onConfirm();
        void onCancel();
    }
}




