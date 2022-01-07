package com.jo.spectrumtracking.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.global.GlobalConstant;
import com.jo.spectrumtracking.model.OrderTracker;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by JO on 3/17/2018.
 */

public class OrderTrackerRecyclerViewAdapter extends RecyclerView.Adapter<OrderTrackerRecyclerViewAdapter.ViewHolder> {

    private List<OrderTracker> itemList;
    private int itemLayoutResID;
    private Fragment parentFragment;

    public List<OrderTracker> getItemList() {
        return itemList;
    }

    public OrderTrackerRecyclerViewAdapter(Fragment parentFragment, List<OrderTracker> itemList, int itemLayoutResID) {
        this.itemList = itemList;
        this.itemLayoutResID = itemLayoutResID;
        this.parentFragment = parentFragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View viewHolder = LayoutInflater.from(parent.getContext()).inflate(itemLayoutResID, parent, false);

        return new ViewHolder(viewHolder);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final OrderTracker item = itemList.get(position);

        holder.txtTitle.setText(item.getName());
        holder.txtSubtitle.setText(item.getDescription());
        Picasso.get().load(GlobalConstant.WEB_API_IMAGE_BASE_URL + item.getImage()).into(holder.imageOrderTracker);
        holder.txtPriceTotal.setText(String.format("$ %.2f", item.getPrice() * item.getCount()));
        holder.txtCount.setText(String.valueOf(item.getCount()));

        /*
        holder.btnCountMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(itemList.get(position).count <= 0) {
                    return;
                }
                itemList.get(position).count -= 1;
                holder.txtPriceTotal.setText(String.format("$ %.2f", itemList.get(position).price * itemList.get(position).count));
                holder.txtCount.setText(String.valueOf(itemList.get(position).count));

                if(OrderTrackerRecyclerViewAdapter.this.parentFragment instanceof  OrderTrackerFragment) {
                    ((OrderTrackerFragment)OrderTrackerRecyclerViewAdapter.this.parentFragment).updateFooter(itemList);
                }

            }
        });

        holder.btnCountPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemList.get(position).count += 1;
                holder.txtPriceTotal.setText(String.format("$ %.2f", itemList.get(position).price * itemList.get(position).count));
                holder.txtCount.setText(String.valueOf(itemList.get(position).count));

                if(OrderTrackerRecyclerViewAdapter.this.parentFragment instanceof  OrderTrackerFragment) {
                    ((OrderTrackerFragment)OrderTrackerRecyclerViewAdapter.this.parentFragment).updateFooter(itemList);
                }
            }
        });

        holder.btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemList.remove(position);
                OrderTrackerRecyclerViewAdapter.this.notifyDataSetChanged();

                if(OrderTrackerRecyclerViewAdapter.this.parentFragment instanceof  OrderTrackerFragment) {
                    ((OrderTrackerFragment)OrderTrackerRecyclerViewAdapter.this.parentFragment).updateFooter(itemList);
                }

            }
        });
        */

        holder.itemView.setTag(item);

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image_order_tracker)
        ImageView imageOrderTracker;

        @BindView(R.id.txt_title)
        TextView txtTitle;

        @BindView(R.id.txt_subtitle)
        TextView txtSubtitle;


        @BindView(R.id.txt_price_total)
        TextView txtPriceTotal;

        @BindView(R.id.txt_count)
        TextView txtCount;

        @BindView(R.id.btn_count_minus)
        ImageButton btnCountMinus;

        @BindView(R.id.btn_count_plus)
        ImageButton btnCountPlus;

        @BindView(R.id.btn_remove)
        Button btnRemove;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
