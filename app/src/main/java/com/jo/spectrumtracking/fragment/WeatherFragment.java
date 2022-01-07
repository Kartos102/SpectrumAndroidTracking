package com.jo.spectrumtracking.fragment;

//0be1fd5d65ce6c964b3962a4649d4670

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.activity.MainActivity;
import com.jo.spectrumtracking.adapter.WeatherRecyclerviewAdapter;
import com.jo.spectrumtracking.api.ApiClient;
import com.jo.spectrumtracking.api.ApiInterface;
import com.jo.spectrumtracking.model.Resp_Weather;
import com.jo.spectrumtracking.model.Weather;
import com.jo.spectrumtracking.tracking.GPSTracker;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WeatherFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WeatherFragment extends Fragment {
    private GPSTracker gpsTracker;
/*

    @BindView(R.id.txt_date)
    TextView txt_date;

    @BindView(R.id.img_icon)
    ImageView img_icon;

    @BindView(R.id.txt_temp)
    TextView txt_temp;

    @BindView(R.id.txt_humidity)
    TextView txt_humidity;

    @BindView(R.id.txt_wind)
    TextView txt_wind;

    @BindView(R.id.txt_pressue)
    TextView txt_pressue;

    @BindView(R.id.txt_state)
    TextView txt_state;

*/

    @BindView(R.id.current_weather_recyclerview)
    RecyclerView current_weather_recyclerview;

    @BindView(R.id.weather_recyclerview)
    RecyclerView weather_recyclerview;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    // TODO: Rename and change types of parameters


    public WeatherFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WeatherFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WeatherFragment newInstance() {
        WeatherFragment fragment = new WeatherFragment();

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
        View rootView = inflater.inflate(R.layout.fragment_weather, container, false);
        ButterKnife.bind(this, rootView);
        initWeather(rootView);
        return rootView;
    }

    private void initWeather(View rootView) {
        if (gpsTracker == null) {
            gpsTracker = new GPSTracker(this.getActivity().getApplicationContext());
            gpsTracker.getLocation();
        }
        gpsTracker.getLocation();
        ApiInterface apiInterface = ApiClient.getWeatherClient(this.getContext()).create(ApiInterface.class);
        //38.2297498,-85.5672221
        apiInterface.getWeather(gpsTracker.getLatitude(), gpsTracker.getLongitude(), "0be1fd5d65ce6c964b3962a4649d4670").enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();

                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setLongSerializationPolicy(LongSerializationPolicy.STRING);

                Gson gson = gsonBuilder.create();
                if (code == 200) {
                    // success
                    ResponseBody responseBody = response.body();
                    JSONObject object = null;
                    try {
                        object = new JSONObject(responseBody.string());
                        Resp_Weather resp_weather = gson.fromJson(object.toString(), Resp_Weather.class);

                        if (resp_weather.getCod().equals("200")) {
                            TimeZone defaultTimeZone = TimeZone.getDefault();

                            List<Weather> weatherList = resp_weather.getList();
                            List<Weather> currentWeatherList = new ArrayList<Weather>();
                            weatherList.get(0).setDt_txt("");
                            currentWeatherList.add(weatherList.get(0));
                            WeatherRecyclerviewAdapter currentRecyclerviewAdapter = new WeatherRecyclerviewAdapter(WeatherFragment.this, currentWeatherList, R.layout.recyclerview_current_weather_row);
                            current_weather_recyclerview.setAdapter(currentRecyclerviewAdapter);
                            LinearLayoutManager verticalLayoutManager = new LinearLayoutManager(WeatherFragment.this.getContext(), LinearLayoutManager.VERTICAL, false);

                            current_weather_recyclerview.setLayoutManager(verticalLayoutManager);
                            current_weather_recyclerview.setItemAnimator(new DefaultItemAnimator());


                            SimpleDateFormat lv_parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            lv_parser.setTimeZone(TimeZone.getTimeZone("UTC"));

                            SimpleDateFormat lv_formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            lv_formatter.setTimeZone(defaultTimeZone);

                            List<Weather> weatherSubList = weatherList.subList(1, 5);
                            for (Weather weather : weatherSubList) {
                                try {
                                    Date lv_localDate = lv_parser.parse(weather.getDt_txt());
                                    weather.setDt_txt(lv_formatter.format(lv_localDate));
                                } catch (Exception e) {

                                }

                            }

                            //  List<Weather> weatherSubList=weatherList.subList(2,6);
                            //weatherSubList.get(0).dt_txt="Current Weather";
                            WeatherRecyclerviewAdapter adapter = new WeatherRecyclerviewAdapter(WeatherFragment.this, weatherSubList, R.layout.recyclerview_weather_row);
                            LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(WeatherFragment.this.getActivity(), LinearLayoutManager.HORIZONTAL, false);
                            weather_recyclerview.setLayoutManager(horizontalLayoutManager);
                            weather_recyclerview.addItemDecoration(new DividerItemDecoration(WeatherFragment.this.getActivity(), LinearLayoutManager.HORIZONTAL));
                            weather_recyclerview.setAdapter(adapter);
                            weather_recyclerview.setItemAnimator(new DefaultItemAnimator());
                        }


                        System.out.println(object);
                    } catch (Exception ex) {
                        ex.printStackTrace();

                    }
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });


    }

    @OnClick(R.id.btn_forward_monitor)
    public void OnForwardButtonClick() {
        MainActivity mainActivity = (MainActivity) this.getActivity();
        mainActivity.showMonitorFragment();
    }


}
