package com.jo.spectrumtracking.api;

import com.jo.spectrumtracking.model.Landmark;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.List;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiInterface {

    @POST("auth/login")
    Call<ResponseBody> authLogin(
            @Body HashMap<String, Object> body);

    @POST("auth/logout")
    Call<ResponseBody> authLogout();

    @POST("auth/register")
    Call<ResponseBody> authRegister(
            @Body HashMap<String, Object> body);

    @POST("auth/verify")
    Call<ResponseBody> authVerify(
            @Body HashMap<String, Object> body);

    @POST("auth/reset-password")
    Call<ResponseBody> resetPassword(
            @Body HashMap<String, Object> body);

    @POST("auth/resend-password-reset")
    Call<ResponseBody> authResendPasswordReset(
            @Body HashMap<String, Object> body);

    @POST("users/submitToken")
    Call<ResponseBody> postFirebaseToken(@Body HashMap<String, Object> body);

    @POST("users/cleanMessage")
    Call<ResponseBody> cleanMessage(
            @Body HashMap<String, Object> body);

    @GET("assets")
    Call<ResponseBody> assets();

    @POST("trackers/getAllTrackersWeb")
    Call<ResponseBody> getAllTrackersWeb(
            @Header("X-CSRFToken") String xCSRFToken
    );


    @GET("trackers/{id}")
    Call<ResponseBody> trackers_id(
            @Path("id") String trackerId);

    @GET("users/{id}")
    Call<ResponseBody> users_id(
            @Path("id") String userId);

    @GET("trackers/getTrackerBySpectrumId/{id}")
    Call<ResponseBody> getTrackerBySpectrumId(
            @Path("id") String spectrumId);


    @GET("trackers/getTrackerModelBySpectrumId/{id}")
    Call<ResponseBody> getTrackerModelBySpectrumId(
            @Path("id") String spectrumId);

    @POST("trackers/modify")
    Call<ResponseBody> modify_tracker(
            @Body HashMap<String, Object> body
    );

    @POST("trackers/registerPhoneTracker")
    Call<ResponseBody> registerPhoneTracker(
            @Body HashMap<String, Object> body
    );

    @POST("users/registerPhoneTracker")
    Call<ResponseBody> setPhoneTrackerFlag(
            @Body HashMap<String, Object> body
    );

    @POST("assets/createPhoneAsset")
    Call<ResponseBody> createPhoneAsset(
            @Body HashMap<String, Object> body
    );

    @POST("trackers/unShareTrakcer")
    Call<ResponseBody> unShareTrakcer(
            @Body HashMap<String, Object> body
    );

    @POST("trackers/shareTrakcer")
    Call<ResponseBody> shareTrakcer(
            @Body HashMap<String, Object> body
    );

    @POST("api-inerface/inviteJoinChat")
    Call<ResponseBody> inviteJoinChat(
            @Body HashMap<String, Object> body
    );

    @POST("trackers/getShareUsers")
    Call<ResponseBody> getShareUsers(
            @Body HashMap<String, Object> body
    );

    @POST("users/setShareFlag")
    Call<ResponseBody> setShareFlag(
            @Body HashMap<String, Object> body
    );

    @POST("trackers/getTrackersByTrackerIds")
    Call<ResponseBody> getAllTrackers(
            @Body HashMap<String, Object> body
    );

    @POST("trackers/getTrackersSummary")
    Call<ResponseBody> getTrackersSummary(
            @Body HashMap<String, Object> body
    );

    @GET("trackers/getCheckOutPlans")
    Call<ResponseBody> getCheckOutPlans();

    @POST("asset-logs")
    Call<ResponseBody> postUserLocation(
            @Header("X-SpectrumTracking-TrackerEndpointKey") String apiKey,
            @Body HashMap<String, Object> body
    );

    @POST("users/getUserFromId")
    Call<ResponseBody> getUserInfo(
            @Header("X-CSRFToken") String xCSRFToken
    );

    @GET("assets/{id}/tracker")
    Call<ResponseBody> assets_tracker(
            @Path("id") String trackerId
    );


    @GET("assets/{id}/logs")
    Call<ResponseBody> assets_logs(
            @Path("id") String trackerId,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate,
            @Query("animation") Boolean animation);

    @GET("asset-logs/tripInfo")
    Call<ResponseBody> tripInfo(
            @Query("reportingId") String reportId,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate,
            @Query("tripDuration") Boolean tripDuration,
            @Query("alertReport") Boolean alertReport,
            @Query("stateMileage") Boolean stateMileage,
            @Query("pinbypinReport") Boolean pinbypinReport);

    @GET("triplog/{id}/logs")
    Call<ResponseBody> trip_logs(
            @Path("id") String reportingId,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate);

    @GET("triplog/tripLogSummary")
    Call<ResponseBody> trip_log_summary(
            @Query("reportingId") String reportingId,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate,
            @Query("metricScale") Double metricScale,
            @Query("volumeMetricScale") Double volumeMetricScale);

//    @GET("triplog/tripLogSummary")
//    Call<ResponseBody> trip_logs(
//            @Query("reportingId") String reportingId,
//            @Query("startDate") String startDate,
//            @Query("endDate") String endDate);

    @GET("alertlog/{id}/logs")
    Call<ResponseBody> event_logs(
            @Path("id") String reportingId,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate);

    @POST("https://app.spectrumtracking.com/php/route.php")
    Call<ResponseBody> tokenizeCreditCard(
            @Body HashMap<String, Object> body);

    @GET("auth")
    Call<ResponseBody> doAuth();

    @PUT("users/{id}")
    Call<ResponseBody> updateCreditCardInfoSecondary(
            @Path("id") String userId,
            @Query("token_type") String tokenType,
            @Query("token_number") String tokenNumber,
            @Query("cardholder_name") String cardholderName,
            @Query("card_type") String cardType,
            @Query("exp_date") String expDate);


    @PUT("assets/{id}")
    Call<ResponseBody> updateAsset(
            @Path("id") String userId,
            @Body HashMap<String, Object> body
    );

    @PUT("users/updateAppReviewReminder/{email}")
    Call<ResponseBody> addReviewNumber(
            @Path("email") String email
    );

    @POST("trackers/register")
    Call<ResponseBody> trackerRegister(
            @Header("X-CSRFToken") String xCSRFToken,
            @Body HashMap<String, Object> body);

    @POST("orders/generateToken")
    Call<ResponseBody> generateToken(
            @Body HashMap<String, Object> body);

    @POST("assets")
    Call<ResponseBody> createAssets(
            @Body HashMap<String, Object> body);

    @POST("orders/payment")
    Call<ResponseBody> ordersPayment(
            @Body HashMap<String, Object> body);

    @POST("trackers/setGeoFence")
    Call<ResponseBody> setGeoFence(
            @Body HashMap<String, Object> body);

    @POST("users/landmark")
    Call<ResponseBody> addLandmark(
            @Header("X-CSRFToken") String xCSRFToken,
            @Body List<Landmark> body
    );

    @POST("trackers/modify")
    Call<ResponseBody> modify(
            @Header("X-CSRFToken") String xCSRFToken,
            @Body HashMap<String, Object> body);

    @POST("trackers/getImageUrl")
    Call<ResponseBody> getImageUrl(
            @Body HashMap<String, Object> body);

    @POST("trackers/imageUpload")
    Call<ResponseBody> imageUpload(
            @Body RequestBody body);


    @GET("trackers/byAssetName/{id}")
    Call<ResponseBody> alarm(
            @Path("id") String trackerId);

    @GET("api-interface/coord2AddressMapbox/{latLng}")
    Call<ResponseBody> coord2AddressMapbox(
            @Path("latLng") String latLng);

    @GET("api-interface/coord2AddressMapboxAll/{latLng}")
    Call<ResponseBody> coord2AddressMapboxAll(
            @Path("latLng") String latLng);

    @GET("api-interface/coord2AddressMapbox/{latLng}")
    Call<ResponseBody> coord2Address(
            @Path("latLng") String latLng);


    @GET("data/2.5/forecast")
    Call<ResponseBody> getWeather(
            @Query("lat") Double lat,
            @Query("lon") Double lon,
            @Query("APPID") String AppID
    );

    @GET("v1/geo/reverse-geocodes")
    Call<ResponseBody> getAddress(
            @Query("latitude") Double lat,
            @Query("longitude") Double lon,
            @Query("key") String AppID
    );

    @GET("chat-token")
    Call<ResponseBody> getTwilioAccessToken(@Query("identity") String identity,
                                            @Query("realm") String realm,
                                            @Query("ttl") String ttl);

}
