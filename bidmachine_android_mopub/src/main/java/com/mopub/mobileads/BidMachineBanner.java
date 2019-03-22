package com.mopub.mobileads;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mopub.common.logging.MoPubLog;
import com.mopub.common.util.Views;

import java.util.Map;

import io.bidmachine.PriceFloorParams;
import io.bidmachine.TargetingParams;
import io.bidmachine.banner.BannerListener;
import io.bidmachine.banner.BannerRequest;
import io.bidmachine.banner.BannerSize;
import io.bidmachine.banner.BannerView;
import io.bidmachine.utils.BMError;

public class BidMachineBanner extends CustomEventBanner {

    private static final String ADAPTER_NAME = BidMachineBanner.class.getSimpleName();
    private static final String BANNER_WIDTH = "banner_width";

    private CustomEventBannerListener customBannerListener;
    private BannerView bannerView;

    @Override
    protected void loadBanner(Context context,
                              CustomEventBannerListener customEventBannerListener,
                              Map<String, Object> localExtras,
                              Map<String, String> serverExtras) {
        setAutomaticImpressionAndClickTracking(false);
        customBannerListener = customEventBannerListener;

        BannerSize bannerSize = findBannerSize(serverExtras);
        if (bannerSize == null) {
            bannerSize = findBannerSize(localExtras);
        }
        if (bannerSize == null) {
            MoPubLog.log(MoPubLog.AdapterLogEvent.CUSTOM,
                    ADAPTER_NAME,
                    "Unsupported banner size");
            MoPubLog.log(MoPubLog.AdapterLogEvent.LOAD_FAILED,
                    ADAPTER_NAME,
                    MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR.getIntCode(),
                    MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            if (customBannerListener != null) {
                customBannerListener.onBannerFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            }
            return;
        }

        BidMachineUtils.initialize(context, serverExtras);
        BidMachineUtils.updateGDPR();
        BannerRequest.Builder bannerRequestBuilder = new BannerRequest.Builder()
                .setSize(bannerSize);
        TargetingParams targetingParams = BidMachineUtils.findTargetingParams(localExtras);
        if (targetingParams != null) {
            bannerRequestBuilder.setTargetingParams(targetingParams);
        }
        PriceFloorParams priceFloorParams = BidMachineUtils.findPriceFloorParams(localExtras);
        if (priceFloorParams != null) {
            bannerRequestBuilder.setPriceFloorParams(priceFloorParams);
        }

        bannerView = new BannerView(context);
        bannerView.setListener(new BidMachineAdListener());
        bannerView.load(bannerRequestBuilder.build());

        MoPubLog.log(
                MoPubLog.AdapterLogEvent.LOAD_ATTEMPTED,
                ADAPTER_NAME,
                ", with size: ",
                bannerSize);
    }

    @Override
    protected void onInvalidate() {
        if (bannerView != null) {
            Views.removeFromParent(bannerView);
            bannerView.setListener(null);
            bannerView.destroy();
            bannerView = null;
        }
        customBannerListener = null;
    }

    private <T> BannerSize findBannerSize(@Nullable Map<String, T> extras) {
        if (extras == null) {
            return null;
        }

        int width = 0;
        try {
            Object value = extras.get(BANNER_WIDTH);
            if (value instanceof Integer) {
                width = (int) value;
            } else if (value instanceof String) {
                width = Integer.parseInt((String) value);
            }
        } catch (Exception e) {
            return null;
        }

        switch (width) {
            case 300:
                return BannerSize.Size_300_250;
            case 320:
                return BannerSize.Size_320_50;
            case 728:
                return BannerSize.Size_728_90;
            default:
                return null;
        }
    }

    private class BidMachineAdListener implements BannerListener {

        @Override
        public void onAdLoaded(@NonNull BannerView bannerView) {
            MoPubLog.log(
                    MoPubLog.AdapterLogEvent.LOAD_SUCCESS,
                    ADAPTER_NAME);
            MoPubLog.log(
                    MoPubLog.AdapterLogEvent.SHOW_ATTEMPTED,
                    ADAPTER_NAME);
            MoPubLog.log(
                    MoPubLog.AdapterLogEvent.SHOW_SUCCESS,
                    ADAPTER_NAME);
            if (customBannerListener != null) {
                customBannerListener.onBannerLoaded(bannerView);
            }
        }

        @Override
        public void onAdLoadFailed(@NonNull BannerView bannerView, @NonNull BMError bmError) {
            MoPubErrorCode moPubErrorCode = BidMachineUtils.transformToMoPubErrorCode(bmError);
            MoPubLog.log(
                    MoPubLog.AdapterLogEvent.LOAD_FAILED,
                    ADAPTER_NAME,
                    moPubErrorCode.getIntCode(),
                    moPubErrorCode);
            if (customBannerListener != null) {
                customBannerListener.onBannerFailed(moPubErrorCode);
            }
        }

        @Override
        public void onAdShown(@NonNull BannerView bannerView) {
            MoPubLog.log(
                    MoPubLog.AdapterLogEvent.SHOW_SUCCESS,
                    ADAPTER_NAME);
        }

        @Override
        public void onAdImpression(@NonNull BannerView bannerView) {
            if (customBannerListener != null) {
                customBannerListener.onBannerImpression();
            }
        }

        @Override
        public void onAdClicked(@NonNull BannerView bannerView) {
            MoPubLog.log(
                    MoPubLog.AdapterLogEvent.CLICKED,
                    ADAPTER_NAME);
            if (customBannerListener != null) {
                customBannerListener.onBannerClicked();
            }
        }

        @Override
        public void onAdExpired(@NonNull BannerView bannerView) {
            MoPubLog.log(
                    MoPubLog.AdapterLogEvent.CUSTOM,
                    ADAPTER_NAME,
                    "Ad was expired");
            if (customBannerListener != null) {
                customBannerListener.onBannerFailed(MoPubErrorCode.EXPIRED);
            }
        }

    }

}
