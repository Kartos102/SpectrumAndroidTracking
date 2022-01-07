package com.jo.spectrumtracking.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jo.gps.spectrumtracking.R;

import butterknife.ButterKnife;

public class OrderTrackerFragment extends Fragment {
/*

    @BindView(R.id.rv_order_tracker)
    RecyclerView rvOrderTracker;

    @BindView(R.id.txt_sub_total)
    TextView txtSubTotal;

    @BindView(R.id.txt_shipping)
    TextView txtShipping;

    @BindView(R.id.txt_taxes)
    TextView txtTaxes;

    @BindView(R.id.txt_total)
    TextView txtTotal;

    OrderTrackerRecyclerViewAdapter adapter;
*/

    public OrderTrackerFragment() {
        // Required empty public constructor
    }

    public static OrderTrackerFragment newInstance() {
        OrderTrackerFragment fragment = new OrderTrackerFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_order_tracker, container, false);

        ButterKnife.bind(this, rootView);


        return rootView;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(getString(R.string.order_tracker));

        WebView webView = view.getRootView().findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("https://spectrumtracking.com/Trackers.html");

     /*   getActivity().setTitle("Order Tracker");

        List<OrderTracker> items = new ArrayList<>();

        OrderTracker item;
        item = new OrderTracker();
        item.name = "Spectrum Smart 3G";
        item.description = "Real-time tracking + Track driving behavior + Track fuel economy";
        item.image = "/images/3g-obd-gps-tracker.jpg";
        item.price = 34.0;
        item.count = 0;
        items.add(item);


        item = new OrderTracker();
        item.name = "Spectrum Smart 4G";
        item.description = "Real-time tracking + Track driving behavior + Track fuel economy + With Wifi hotspot";
        item.image = "/images/4g-obd-tracker-5.jpg";
        item.price = 106.0;
        item.count = 0;
        items.add(item);

        adapter = new OrderTrackerRecyclerViewAdapter(this, items, R.layout.recyclerview_row_order_tracker);

        rvOrderTracker.setAdapter(adapter);
        rvOrderTracker.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        rvOrderTracker.setItemAnimator(new DefaultItemAnimator());

        this.updateFooter(items);*/

    }
/*
    @OnClick(R.id.btn_order_tracker_checkout)
    public void onCheckoutClick() {
//        Utils.showShortToast(this.getContext(), "Checkout ?");

        GlobalConstant.orderTrackerList = adapter.getItemList();
        this.startActivity(new Intent(this.getContext(), ShippingAddressActivity.class));
    }

    public void updateFooter(List<OrderTracker> itemList) {
        double subTotal = 0;
        for(OrderTracker tracker: itemList) {
            subTotal += tracker.price * tracker.count;
        }

        double upTax = 1;
        double downTax = 0;
        double shipping;
        double defaultShipping = 0;
        shipping = (subTotal > 0 && subTotal < (100 / upTax)) ? defaultShipping : 0;

        txtSubTotal.setText("$"+String.format("%.2f", subTotal));
        txtTaxes.setText("$"+String.format("%.2f", subTotal * downTax));
        txtTotal.setText("$"+String.format("%.2f", subTotal * upTax + shipping));
        txtShipping.setText("$"+String.format("%.2f", shipping));
    }*/
}
