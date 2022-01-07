package com.jo.spectrumtracking.global;

import android.app.ProgressDialog;

import com.jo.spectrumtracking.model.Landmark;
import com.jo.spectrumtracking.model.OrderService;
import com.jo.spectrumtracking.model.OrderTracker;
import com.jo.spectrumtracking.model.Resp_Tracker;
import com.jo.spectrumtracking.model.ShippingAddressHolder;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.maps.Style;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by BlueSky on 2017-11-18.
 */

public class GlobalConstant {

    public static final String WEB_API_BASE_URL = "https://api.spectrumtracking.com/v1/";
    public static final String WEB_API_FOR_FIREBASE = "http://52.15.165.124/";
    public static final String WEATHER_API_BASE_URL = "https://api.openweathermap.org";
    public static final String ADDRESS_API_BASE_URL = "https://api.nettoolkit.com/";
    public static final String WEATHER_IMAGE_BASE_URL ="http://openweathermap.org/img/w/";
    public static final String MAP_BOX_ACCESS_TOKEN = "sk.eyJ1IjoieW9uZ3NoZW5nbGlhbiIsImEiOiJjam93dmx0aGoyMXkzM3BybnYzY2MzcjRoIn0.FjG6XWvZuM14iwHTZENTDQ";

    public static final String MAP_BOX_STYLE_URL = "https://osm.spectrumtracking.com/styles/ciw6czz2n00242kmg6hw20box/style.json";
    public static final String MAP_BOX_SATELLITE_URL = Style.SATELLITE_STREETS;


    public static final String WEB_API_IMAGE_BASE_URL = "https://app.spectrumtracking.com/";
    public static boolean cannotEnableGPS=false;

    // public static final String TWILIO_ACCESS_TOKEN_SERVICE_URL = "https://vanilla-iguana-5471.twil.io/";
    public static final String TWILIO_ACCESS_TOKEN_SERVICE_URL = "https://camel-mink-6453.twil.io/";
    public static final String TWILIO_API_KEY = "SK402de71b428f7381fefcb48515ff8fbe";
    public static final String TWILIO_API_SECRET = "ZAOQeLkLLukf7GMNSnLMfZrk7Vp9PSTW";
    public static final String TWILIO_DEFAULT_REALM = "us1";
    public static final String TWILIO_DEFAULT_TTL = "3000";

    static ProgressDialog progressDialog;

    public static String X_CSRF_TOKEN = "";
    public static Double metricScale = 1.0;
    public static Map<String, String> allAddress = new LinkedHashMap<>();
    public static boolean upload_state = false;
    public static Point user_point;
    public static List<String> selectedTrackerIds = new ArrayList<>();
    public static List<Resp_Tracker> AllTrackerList = new ArrayList<>();
    public static Double volumeMetricScale = 1.0;
    public static Map<String, String> alerts = null;
    public static List<Landmark> landmarks = new ArrayList<>();
    public static String email = "";
    public static String photoUploadTrackerId = "";
    public static JSONObject app_user = new JSONObject();
    public static List<Resp_Tracker> sharedTrackerList = new ArrayList<>();
    public static List<OrderService> orderServiceItemList = new ArrayList<>();; // order service fragment data to be used on checkout activity
    public static ShippingAddressHolder shippingAddress = null;; // order tracker -> shipping address holder
    public static List<OrderTracker> orderTrackerList = new ArrayList<>();; // order tracker list to be used on checkout activity
    public static HashMap<String, String> driverImageMap = new HashMap<>();

    public static String[] countries = {"United States", "Afghanistan", "Åland Islands", "Albania", "Algeria", "American Samoa", "Andorra", "Angola", "Anguilla",
            "Antarctica", "Antigua and Barbuda", "Argentina", "Armenia", "Aruba", "Australia", "Austria", "Azerbaijan", "Bahamas", "Bahrain", "Bangladesh",
            "Barbados", "Belarus", "Belgium", "Belize", "Benin", "Bermuda", "Bhutan", "Bolivia", "Bosnia and Herzegovina", "Botswana", "Bouvet Island", "Brazil",
            "British Indian Ocean Territory", "Brunei Darussalam", "Bulgaria", "Burkina Faso", "Burundi", "Cambodia", "Cameroon", "Canada", "Cape Verde", "Cayman Islands",
            "Central African Republic", "Chad", "Chile", "China", "Christmas Island", "Cocos (Keeling) Islands", "Colombia", "Comoros", "Congo",
            "Congo, The Democratic Republic of The", "Cook Islands", "Costa Rica", "Cote D'ivoire", "Croatia", "Cuba", "Cyprus", "Czech Republic", "Denmark", "Djibouti",
            "Dominica", "Dominican Republic", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea", "Estonia", "Ethiopia", "Falkland Islands (Malvinas)",
            "Faroe Islands", "Fiji", "Finland", "France", "French Guiana", "French Polynesia", "French Southern Territories", "Gabon", "Gambia", "Georgia", "Germany",
            "Ghana", "Gibraltar", "Greece", "Greenland", "Grenada", "Guadeloupe", "Guam", "Guatemala", "Guernsey", "Guinea", "Guinea-bissau", "Guyana", "Haiti",
            "Heard Island and Mcdonald Islands", "Holy See (Vatican City State)", "Honduras", "Hong Kong", "Hungary", "Iceland", "India", "Indonesia", "Iran, Islamic Republic of",
            "Iraq", "Ireland", "Isle of Man", "Israel", "Italy", "Jamaica", "Japan", "Jersey", "Jordan", "Kazakhstan", "Kenya", "Kiribati",
            "Korea, Democratic People's Republic of", "Korea, Republic of", "Kuwait", "Kyrgyzstan", "Lao People's Democratic Republic", "Latvia", "Lebanon", "Lesotho",
            "Liberia", "Libyan Arab Jamahiriya", "Liechtenstein", "Lithuania", "Luxembourg", "Macao", "Macedonia, The Former Yugoslav Republic of", "Madagascar",
            "Malawi", "Malaysia", "Maldives", "Mali", "Malta", "Marshall Islands", "Martinique", "Mauritania", "Mauritius", "Mayotte", "Mexico",
            "Micronesia, Federated States of", "Moldova, Republic of", "Monaco", "Mongolia", "Montenegro", "Montserrat", "Morocco", "Mozambique", "Myanmar", "Namibia",
            "Nauru", "Nepal", "Netherlands", "Netherlands Antilles", "New Caledonia", "New Zealand", "Nicaragua", "Niger", "Nigeria", "Niue", "Norfolk Island",
            "Northern Mariana Islands", "Norway", "Oman", "Pakistan", "Palau", "Palestinian Territory, Occupied", "Panama", "Papua New Guinea", "Paraguay", "Peru",
            "Philippines", "Pitcairn", "Poland", "Portugal", "Puerto Rico", "Qatar", "Reunion", "Romania", "Russian Federation", "Rwanda", "Saint Helena",
            "Saint Kitts and Nevis", "Saint Lucia", "Saint Pierre and Miquelon", "Saint Vincent and The Grenadines", "Samoa", "San Marino", "Sao Tome and Principe",
            "Saudi Arabia", "Senegal", "Serbia", "Seychelles", "Sierra Leone", "Singapore", "Slovakia", "Slovenia", "Solomon Islands", "Somalia", "South Africa",
            "South Georgia and The South Sandwich Islands", "Spain", "Sri Lanka", "Sudan", "Suriname", "Svalbard and Jan Mayen", "Swaziland", "Sweden", "Switzerland",
            "Syrian Arab Republic", "Taiwan, Province of China", "Tajikistan", "Tanzania, United Republic of", "Thailand", "Timor-leste", "Togo", "Tokelau", "Tonga",
            "Trinidad and Tobago", "Tunisia", "Turkey", "Turkmenistan", "Turks and Caicos Islands", "Tuvalu", "Uganda", "Ukraine", "United Arab Emirates", "United Kingdom",
            "United States Minor Outlying Islands", "Uruguay", "Uzbekistan", "Vanuatu", "Venezuela", "Viet Nam", "Virgin Islands, British", "Virgin Islands, U.S.",
            "Wallis and Futuna", "Western Sahara", "Yemen", "Zambia", "Zimbabwe"};
}
