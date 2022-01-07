package com.jo.spectrumtracking.twilio.chat.accesstoken;

import android.content.Context;
import android.content.SharedPreferences;

import com.jo.spectrumtracking.api.ApiClient;
import com.jo.spectrumtracking.api.ApiInterface;
import com.jo.spectrumtracking.global.GlobalConstant;
import com.jo.spectrumtracking.twilio.chat.listeners.TaskCompletionListener;
import org.json.JSONObject;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccessTokenFetcher {

  private Context context;

  public AccessTokenFetcher(Context context) {
    this.context = context;
  }

  public void fetch(final TaskCompletionListener<String, String> listener) {
    ApiInterface apiInterface = ApiClient.getTwilioAccessTokenClient(this.context).create(ApiInterface.class);

    SharedPreferences preferences = context.getSharedPreferences("spectrum", Context.MODE_PRIVATE);
    String email = preferences.getString("username", "");

    if (email.isEmpty()) {
      return;
    }

    apiInterface.getTwilioAccessToken(email, GlobalConstant.TWILIO_DEFAULT_REALM, GlobalConstant.TWILIO_DEFAULT_TTL).enqueue(new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        int code = response.code();
        if (code == 200) {
          ResponseBody responseBody = response.body();
          JSONObject object = null;
          try {
            object = new JSONObject(responseBody.string());
            String token = object.getString("token");
            listener.onSuccess(token);
          } catch (Exception e) {
            e.printStackTrace();
            listener.onError("Failed to parse token JSON response");
          }
        } else {
          listener.onError("Failed to fetch token");
        }
      }

      @Override
      public void onFailure(Call<ResponseBody> call, Throwable t) {
        listener.onError("Failed to fetch token");
      }
    });
  }
}
