package com.jo.spectrumtracking.api;

import androidx.annotation.NonNull;

public interface AddressCallbak {

    void onSuccess(@NonNull String value);

    void onError(@NonNull Throwable throwable);

}
