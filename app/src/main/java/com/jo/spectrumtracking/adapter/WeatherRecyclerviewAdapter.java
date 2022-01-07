package com.jo.spectrumtracking.adapter;

import android.graphics.Color;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.fragment.WeatherFragment;
import com.jo.spectrumtracking.global.GlobalConstant;
import com.jo.spectrumtracking.model.Weather;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WeatherRecyclerviewAdapter extends RecyclerView.Adapter<WeatherRecyclerviewAdapter.ViewHolder> {


    private int itemLayoutResID;
    private WeatherFragment fragment;
    private List<Weather> weatherList;
    private ViewGroup parent;

    public ViewHolder getHolder() {
        return holder;
    }

    private WeatherRecyclerviewAdapter.ViewHolder holder;


    public WeatherRecyclerviewAdapter(WeatherFragment fragment, List<Weather> weatherList, int itemLayoutResID) {
        this.weatherList = weatherList;
        this.itemLayoutResID = itemLayoutResID;
        this.fragment = fragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View viewHolder = LayoutInflater.from(parent.getContext()).inflate(itemLayoutResID, parent, false);
        // viewHolder.getLayoutParams().width=(int)parent.getLayoutParams().width/4;
        holder = new WeatherRecyclerviewAdapter.ViewHolder(viewHolder);
        this.parent = parent;

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {


        Weather weather = weatherList.get(position);

        if (!weather.getDt_txt().equals("")) {
            String subString = weather.getDt_txt().substring(5);
            holder.txt_date.setText(subString.substring(0, subString.length() - 3).replace("-", "/").replace(" ", " | "));
        } else
            holder.txt_date.setText(weather.getDt_txt());


        String icon = weather.getWeather().get(0).icon + ".png";
        Picasso.get().load(GlobalConstant.WEATHER_IMAGE_BASE_URL + icon).into(holder.img_icon);
        String value = String.valueOf((int) ((weather.getMain().temp - 273) * 9 / 5 + 32));
        holder.txt_temp.setText(value + (char) 0x00B0 + "F");
        holder.txt_wind.setText(weather.getWind().speed + "m/s");
        holder.txt_state.setText(weather.getWeather().get(0).description);
        holder.txt_humidity.setText(weather.getMain().humidity + "%");
        holder.txt_pressue.setText(weather.getMain().pressure + "hpa");
        View view = (View) parent.getParent();
        Display display = fragment.getActivity().getWindowManager().getDefaultDisplay();

        holder.itemView.getLayoutParams().width = display.getWidth() / getItemCount();

        if (weatherList.size() == 1) {
            holder.txt_temp.setTextColor(Color.WHITE);
            holder.txt_wind.setTextColor(Color.WHITE);
            holder.txt_state.setTextColor(Color.WHITE);
            holder.txt_humidity.setTextColor(Color.WHITE);
            holder.txt_pressue.setTextColor(Color.WHITE);

        }

    }

    @Override
    public int getItemCount() {
        return weatherList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.txt_date)
        TextView txt_date;
        @BindView(R.id.img_icon)
        ImageView img_icon;
        @BindView(R.id.txt_temp)
        TextView txt_temp;
        @BindView(R.id.txt_wind)
        TextView txt_wind;
        @BindView(R.id.txt_humidity)
        TextView txt_humidity;
        @BindView(R.id.txt_pressue)
        TextView txt_pressue;
        @BindView(R.id.txt_state)
        TextView txt_state;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }

}

