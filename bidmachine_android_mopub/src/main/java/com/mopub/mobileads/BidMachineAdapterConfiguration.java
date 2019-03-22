package com.mopub.mobileads;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.mopub.common.BaseAdapterConfiguration;
import com.mopub.common.OnNetworkInitializationFinishedListener;
import com.mopub.common.logging.MoPubLog;

import java.util.Map;

import io.bidmachine.BidMachine;

public class BidMachineAdapterConfiguration extends BaseAdapterConfiguration {

    private static final String ADAPTER_NAME = BidMachineAdapterConfiguration.class.getSimpleName();
    private static final String NETWORK_VERSION = BidMachine.VERSION;
    private static final String ADAPTER_VERSION = NETWORK_VERSION + ".0";
    private static final String MOPUB_NETWORK_NAME = "BidMachine";
    private static final String LOGGING_ENABLED = "logging_enabled";
    private static final String TEST_MODE = "test_mode";

    @NonNull
    @Override
    public String getAdapterVersion() {
        return ADAPTER_VERSION;
    }

    @Nullable
    @Override
    public String getBiddingToken(@NonNull Context context) {
        return null;
    }

    @NonNull
    @Override
    public String getMoPubNetworkName() {
        return MOPUB_NETWORK_NAME;
    }

    @NonNull
    @Override
    public String getNetworkSdkVersion() {
        return NETWORK_VERSION;
    }

    @Override
    public void initializeNetwork(@NonNull Context context,
                                  @Nullable Map<String, String> configuration,
                                  @NonNull OnNetworkInitializationFinishedListener listener) {
        if (configuration != null) {
            String loggingEnabled = configuration.get(LOGGING_ENABLED);
            if (!TextUtils.isEmpty(loggingEnabled)) {
                BidMachine.setLoggingEnabled(Boolean.parseBoolean(loggingEnabled));
            }
            String testMode = configuration.get(TEST_MODE);
            if (!TextUtils.isEmpty(testMode)) {
                BidMachine.setTestMode(Boolean.parseBoolean(testMode));
            }
            if (BidMachineUtils.initialize(context, configuration)) {
                listener.onNetworkInitializationFinished(
                        BidMachineAdapterConfiguration.class,
                        MoPubErrorCode.ADAPTER_INITIALIZATION_SUCCESS
                );
                return;
            }
        }

        MoPubLog.log(MoPubLog.AdapterLogEvent.CUSTOM,
                ADAPTER_NAME,
                "seller_id not found! BidMachine not initialized");
        listener.onNetworkInitializationFinished(
                BidMachineAdapterConfiguration.class,
                MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR
        );
    }
}
