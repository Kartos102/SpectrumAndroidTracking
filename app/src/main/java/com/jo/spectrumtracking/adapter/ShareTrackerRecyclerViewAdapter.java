package com.jo.spectrumtracking.adapter;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.fragment.ShareFragment;
import com.jo.spectrumtracking.global.Utils;
import com.jo.spectrumtracking.model.Resp_Share;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ShareTrackerRecyclerViewAdapter extends RecyclerView.Adapter<ShareTrackerRecyclerViewAdapter.ViewHolder> {

    public static final int REQUEST_CODE_CONTACT = 1;
    public static final int PERMISSIONS_REQUEST_READ_CONTACTS = 2;

    private List<Resp_Share> itemList;
    private int itemLayoutResID;
    public Fragment fragment;
    public int importContactIndex = -1;
    public String importedContactEmail = "";

    public ShareTrackerRecyclerViewAdapter(Fragment fragment, List<Resp_Share> itemList, int itemLayoutResID) {
        this.fragment = fragment;
        this.itemList = itemList;
        this.itemLayoutResID = itemLayoutResID;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View viewHolder = LayoutInflater.from(parent.getContext()).inflate(itemLayoutResID, parent, false);

        return new ShareTrackerRecyclerViewAdapter.ViewHolder(viewHolder);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Resp_Share item = itemList.get(position);

        holder.plateNumber.setText(item.getPlateNumber());
        holder.spinnerLabel.setText(item.getSpinner_label());

        if (importContactIndex == position) {
            holder.share_email.setText(importedContactEmail);
            importedContactEmail = "";
            importContactIndex = -1;
        }

        holder.sharedUserList.setOnClickListener(v -> {
            ShareFragment shareFragment = (ShareFragment) fragment;
            shareFragment.getShareUsers(item, position);
        });

        holder.btn_share.setOnClickListener(v -> {
            String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
            if (holder.share_email.getText().toString().isEmpty()) {
                Utils.showShortToast(fragment.getContext(), fragment.getString(R.string.please_enter_email), true);
            } else {
                if (holder.share_email.getText().toString().trim().matches(emailPattern)) {
                    InputMethodManager imm = (InputMethodManager) fragment.getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    View view = fragment.getActivity().getCurrentFocus();
                    if (view == null) {
                        view = new View(fragment.getActivity());
                    }
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                    ShareFragment shareFragment = (ShareFragment) fragment;
                    shareFragment.shareTracker(item.getReport_id(), holder.share_email.getText().toString());
                } else {
                    Utils.showShortToast(fragment.getContext(), fragment.getContext().getResources().getString(R.string.invalid_email_address), true);
                }
            }

        });

        holder.btn_unshare.setOnClickListener(v -> {
            if (item.getSpinner_label().equals(fragment.getString(R.string.select_users))) {
                Utils.showShortToast(fragment.getContext(), fragment.getString(R.string.please_select_user_for_unshare), true);
                return;
            }
            String[] myarray = item.getSpinner_label().split(",");

            ShareFragment shareFragment = (ShareFragment) fragment;
            shareFragment.unShareTracker(item.getReport_id(), myarray);
        });

        holder.ivContact.setOnClickListener(v -> {
            if (requestContactPermission()) {
                importContactIndex = position;
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Email.CONTENT_URI);
                fragment.startActivityForResult(intent, REQUEST_CODE_CONTACT);
            }
        });

        holder.itemView.setTag(item);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.label_plateNumber)
        TextView plateNumber;

        @BindView(R.id.spinner_label)
        TextView spinnerLabel;

        @BindView(R.id.share_email)
        EditText share_email;

        @BindView(R.id.btn_share)
        Button btn_share;

        @BindView(R.id.btn_unshare)
        Button btn_unshare;

        @BindView(R.id.shareUserSpinner)
        LinearLayout sharedUserList;

        @BindView(R.id.ivContact)
        ImageView ivContact;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public boolean requestContactPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Activity activity = fragment.getActivity();
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
}