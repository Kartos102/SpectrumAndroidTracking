package com.jo.spectrumtracking.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.fragment.ShareFragment;
import com.jo.spectrumtracking.model.Resp_SharedList;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SharedUserListRecyclerViewAdapter extends RecyclerView.Adapter<SharedUserListRecyclerViewAdapter.ViewHolder> {


    private List<Resp_SharedList> itemList;
    private int itemLayoutResID;
    public Fragment fragment;

    public SharedUserListRecyclerViewAdapter(Fragment fragment, List<Resp_SharedList> itemList, int itemLayoutResID) {
        this.fragment = fragment;
        this.itemList = itemList;
        this.itemLayoutResID = itemLayoutResID;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View viewHolder = LayoutInflater.from(parent.getContext()).inflate(itemLayoutResID, parent, false);

        return new SharedUserListRecyclerViewAdapter.ViewHolder(viewHolder);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Resp_SharedList item = itemList.get(position);

        holder.txt_email.setText(item.getEmail());

        holder.check_email.setChecked(item.getChecked());

        holder.check_email.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ShareFragment shareFragment = (ShareFragment) fragment;
            shareFragment.onCheckSharedUserList(position, isChecked);
        });
        holder.itemView.setTag(item);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.check_email)
        CheckBox check_email;

        @BindView(R.id.txt_email)
        TextView txt_email;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}