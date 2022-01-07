package com.jo.spectrumtracking.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.global.GlobalConstant;
import com.jo.spectrumtracking.global.Utils;
import com.jo.spectrumtracking.model.ShippingAddressHolder;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ShippingAddressActivity extends AppCompatActivity {

    Toolbar toolbar;

    @BindView(R.id.sp_state)
    Spinner spState;

    @BindView(R.id.edit_name)
    EditText editName;

    @BindView(R.id.edit_email)
    EditText editEmail;

    @BindView(R.id.edit_street_address)
    EditText editStreetAddress;

    @BindView(R.id.edit_city)
    EditText editCity;

    @BindView(R.id.edit_zip_code)
    EditText editZipCode;

    @BindView(R.id.btn_make_payment)
    Button btn_make_payment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipping_address);

        ButterKnife.bind(this);

        setToolbar();

        setStateSpinner();

    }

    private void setStateSpinner() {
        String[] states = {
                "Alabama",
                "Alaska",
                "Arizona",
                "Arkansas",
                "California",
                "Colorado",
                "Connecticut",
                "Delaware",
                "District Of Columbia",
                "Florida",
                "Georgia",
                "Hawaii",
                "Idaho",
                "Illinois",
                "Indiana",
                "Iowa",
                "Kansas",
                "Kentucky",
                "Louisiana",
                "Maine",
                "Maryland",
                "Massachusetts",
                "Michigan",
                "Minnesota",
                "Mississippi",
                "Missouri",
                "Montana",
                "Nebraska",
                "Nevada",
                "New Hampshire",
                "New Jersey",
                "New Mexico",
                "New York",
                "North Carolina",
                "North Dakota",
                "Ohio",
                "Oklahoma",
                "Oregon",
                "Pennsylvania",
                "Rhode Island",
                "South Carolina",
                "South Dakota",
                "Tennessee",
                "Texas",
                "Utah",
                "Vermont",
                "Virginia",
                "Washington",
                "West Virginia",
                "Wisconsin",
                "Wyoming"
        };

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Arrays.asList(states));
        spState.setAdapter(arrayAdapter);
        spState.setSelection(0);
    }

    private void setToolbar() {

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @OnClick(R.id.btn_make_payment)
    public void makePaymentClick() {
        btn_make_payment.setEnabled(false);
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String streetAddress = editStreetAddress.getText().toString().trim();
        String city = editCity.getText().toString().trim();
        String zipCode = editZipCode.getText().toString().trim();
        String state = spState.getSelectedItem().toString().trim();

        if ("".equals(name)) {
            Utils.showShortToast(this, getResources().getString(R.string.please_enter_username), true);
            return;
        }
        if ("".equals(email)) {
            Utils.showShortToast(this, getResources().getString(R.string.please_enter_password), true);
            return;
        }
        if ("".equals(streetAddress)) {
            Utils.showShortToast(this, getResources().getString(R.string.please_enter_street_address), true);
            return;
        }
        if ("".equals(city)) {
            Utils.showShortToast(this, getResources().getString(R.string.please_enter_city), true);
            return;
        }
        if ("".equals(zipCode)) {
            Utils.showShortToast(this, getResources().getString(R.string.please_enter_zip_code), true);
            return;
        }
        if ("".equals(state)) {
            Utils.showShortToast(this, getResources().getString(R.string.please_enter_state), true);
            return;
        }

        Intent intent = new Intent(this, CheckoutActivity.class);
        intent.putExtra("from", "ShippingAddressActivity");

        GlobalConstant.shippingAddress = new ShippingAddressHolder(name, email, streetAddress, city, zipCode, state);

        this.startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}


