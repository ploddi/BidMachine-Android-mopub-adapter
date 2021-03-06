package com.mopub.mobileads;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mopub.common.LifecycleListener;
import com.mopub.common.MoPubReward;
import com.mopub.common.logging.MoPubLog;

import java.util.Map;
import java.util.UUID;

import io.bidmachine.rewarded.RewardedAd;
import io.bidmachine.rewarded.RewardedListener;
import io.bidmachine.rewarded.RewardedRequest;
import io.bidmachine.utils.BMError;

public class BidMachineRewardedVideo extends CustomEventRewardedVideo {

    private static final String ADAPTER_NAME = BidMachineRewardedVideo.class.getSimpleName();

    private RewardedAd rewardedAd;
    private String adUnitId = "";

    @Nullable
    @Override
    protected LifecycleListener getLifecycleListener() {
        return null;
    }

    @Override
    protected boolean checkAndInitializeSdk(@NonNull Activity launcherActivity,
                                            @NonNull Map<String, Object> localExtras,
                                            @NonNull Map<String, String> serverExtras) throws Exception {
        return BidMachineUtils.prepareBidMachine(
                launcherActivity,
                BidMachineUtils.getFusedMap(serverExtras, localExtras));
    }

    @Override
    protected void loadWithSdkInitialized(@NonNull Activity activity,
                                          @NonNull Map<String, Object> localExtras,
                                          @NonNull Map<String, String> serverExtras) throws Exception {
        adUnitId = UUID.randomUUID().toString();
        Map<String, Object> fusedMap = BidMachineUtils.getFusedMap(serverExtras, localExtras);
        BidMachineUtils.prepareBidMachine(activity, fusedMap);
        RewardedRequest rewardedRequest = new RewardedRequest.Builder()
                .setTargetingParams(BidMachineUtils.findTargetingParams(fusedMap))
                .setPriceFloorParams(BidMachineUtils.findPriceFloorParams(fusedMap))
                .build();

        rewardedAd = new RewardedAd(activity);
        rewardedAd.setListener(new BidMachineAdListener());
        rewardedAd.load(rewardedRequest);

        MoPubLog.log(
                MoPubLog.AdapterLogEvent.LOAD_ATTEMPTED,
                ADAPTER_NAME);
    }

    @NonNull
    @Override
    protected String getAdNetworkId() {
        return adUnitId;
    }

    @Override
    protected boolean hasVideoAvailable() {
        return rewardedAd != null && rewardedAd.canShow();
    }

    @Override
    protected boolean isReady() {
        return rewardedAd != null && rewardedAd.canShow();
    }

    @Override
    protected void showVideo() {
        MoPubLog.log(
                MoPubLog.AdapterLogEvent.SHOW_ATTEMPTED,
                ADAPTER_NAME);
        if (rewardedAd != null && rewardedAd.canShow()) {
            rewardedAd.show();
        } else {
            MoPubLog.log(
                    MoPubLog.AdapterLogEvent.SHOW_FAILED,
                    ADAPTER_NAME,
                    MoPubErrorCode.NETWORK_NO_FILL.getIntCode(),
                    MoPubErrorCode.NETWORK_NO_FILL);
            MoPubRewardedVideoManager.onRewardedVideoPlaybackError(
                    BidMachineRewardedVideo.class,
                    getAdNetworkId(),
                    MoPubErrorCode.NETWORK_NO_FILL);
        }
    }

    @Override
    protected void onInvalidate() {
        if (rewardedAd != null) {
            rewardedAd.setListener(null);
            rewardedAd.destroy();
            rewardedAd = null;
        }
    }

    private class BidMachineAdListener implements RewardedListener {

        @Override
        public void onAdShowFailed(@NonNull RewardedAd rewardedAd, @NonNull BMError bmError) {
            MoPubLog.log(
                    MoPubLog.AdapterLogEvent.SHOW_FAILED,
                    ADAPTER_NAME);
            MoPubRewardedVideoManager.onRewardedVideoPlaybackError(
                    BidMachineRewardedVideo.class,
                    getAdNetworkId(),
                    MoPubErrorCode.VIDEO_PLAYBACK_ERROR);
        }

        @Override
        public void onAdClosed(@NonNull RewardedAd rewardedAd, boolean b) {
            MoPubLog.log(
                    MoPubLog.AdapterLogEvent.DID_DISAPPEAR,
                    ADAPTER_NAME);
            MoPubRewardedVideoManager.onRewardedVideoClosed(
                    BidMachineRewardedVideo.class,
                    getAdNetworkId());
        }

        @Override
        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
            MoPubLog.log(
                    MoPubLog.AdapterLogEvent.LOAD_SUCCESS,
                    ADAPTER_NAME);
            MoPubRewardedVideoManager.onRewardedVideoLoadSuccess(
                    BidMachineRewardedVideo.class,
                    getAdNetworkId());
        }

        @Override
        public void onAdLoadFailed(@NonNull RewardedAd rewardedAd, @NonNull BMError bmError) {
            MoPubErrorCode moPubErrorCode = BidMachineUtils.transformToMoPubErrorCode(bmError);
            MoPubLog.log(
                    MoPubLog.AdapterLogEvent.LOAD_FAILED,
                    ADAPTER_NAME,
                    moPubErrorCode.getIntCode(),
                    moPubErrorCode);
            MoPubRewardedVideoManager.onRewardedVideoLoadFailure(
                    BidMachineRewardedVideo.class,
                    getAdNetworkId(),
                    moPubErrorCode);
        }

        @Override
        public void onAdShown(@NonNull RewardedAd rewardedAd) {
            MoPubLog.log(
                    MoPubLog.AdapterLogEvent.SHOW_SUCCESS,
                    ADAPTER_NAME);
            MoPubRewardedVideoManager.onRewardedVideoStarted(
                    BidMachineRewardedVideo.class,
                    getAdNetworkId());
        }

        @Override
        public void onAdImpression(@NonNull RewardedAd rewardedAd) {

        }

        @Override
        public void onAdClicked(@NonNull RewardedAd rewardedAd) {
            MoPubLog.log(
                    MoPubLog.AdapterLogEvent.CLICKED,
                    ADAPTER_NAME);
            MoPubRewardedVideoManager.onRewardedVideoClicked(
                    BidMachineRewardedVideo.class,
                    getAdNetworkId());
        }

        @Override
        public void onAdExpired(@NonNull RewardedAd rewardedAd) {
            MoPubLog.log(
                    MoPubLog.AdapterLogEvent.CUSTOM,
                    ADAPTER_NAME,
                    "Ad was expired");
            MoPubRewardedVideoManager.onRewardedVideoLoadFailure(
                    BidMachineRewardedVideo.class,
                    getAdNetworkId(),
                    MoPubErrorCode.EXPIRED);
        }

        @Override
        public void onAdRewarded(@NonNull RewardedAd rewardedAd) {
            MoPubReward moPubReward = MoPubReward.success(
                    MoPubReward.NO_REWARD_LABEL,
                    MoPubReward.DEFAULT_REWARD_AMOUNT);
            MoPubLog.log(
                    MoPubLog.AdapterLogEvent.SHOULD_REWARD,
                    ADAPTER_NAME,
                    moPubReward.getAmount(),
                    moPubReward.getLabel());
            MoPubRewardedVideoManager.onRewardedVideoCompleted(
                    BidMachineRewardedVideo.class,
                    getAdNetworkId(),
                    moPubReward);
        }
    }

}
