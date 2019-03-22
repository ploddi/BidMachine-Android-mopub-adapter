package com.mopub.mobileads;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.mopub.common.MoPub;
import com.mopub.common.logging.MoPubLog;
import com.mopub.common.privacy.PersonalInfoManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

import io.bidmachine.BidMachine;
import io.bidmachine.PriceFloorParams;
import io.bidmachine.TargetingParams;
import io.bidmachine.utils.BMError;
import io.bidmachine.utils.Gender;

class BidMachineUtils {

    private static final String SELLER_ID = "seller_id";
    private static final String COPPA = "coppa";
    private static String sellerId;

    static void storeSellerId(@NonNull Map<String, String> configuration) {
        sellerId = configuration.get(SELLER_ID);
    }

    //TODO добавить метод BidMachine.isInitialized()
    static boolean initialize(Context context, Map<String, String> serverExtras, Map<String, Object> localExtras) {
        String coppa = serverExtras.get(COPPA);
        if (TextUtils.isEmpty(coppa)) {
            coppa = (String) localExtras.get(COPPA);
        }
        if (!TextUtils.isEmpty(coppa)) {
            BidMachine.setCoppa(Boolean.parseBoolean(coppa));
        }

        String sellerId = serverExtras.get(SELLER_ID);
        if (TextUtils.isEmpty(sellerId)) {
            sellerId = (String) localExtras.get(SELLER_ID);
        }
        if (TextUtils.isEmpty(sellerId)) {
            sellerId = BidMachineUtils.sellerId;
        }
        if (!TextUtils.isEmpty(sellerId)) {
            BidMachineUtils.updateGDPR();
            BidMachine.initialize(context, sellerId);
            return true;
        } else {
            MoPubLog.log(
                    MoPubLog.AdapterLogEvent.CUSTOM,
                    BidMachineUtils.class.getSimpleName(),
                    "seller_id not found anywhere (serverExtras, localExtras, configuration). BidMachine not initialized");
        }
        return false;
    }

    static MoPubErrorCode transformToMoPubErrorCode(@NonNull BMError bmError) {
        if (bmError == BMError.NoContent
                || bmError == BMError.NotLoaded
                || bmError == BMError.Server
                || bmError == BMError.Connection) {
            return MoPubErrorCode.NO_FILL;
        } else if (bmError == BMError.TimeoutError) {
            return MoPubErrorCode.NETWORK_TIMEOUT;
        } else if (bmError == BMError.IncorrectAdUnit) {
            return MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR;
        } else if (bmError == BMError.Internal) {
            return MoPubErrorCode.INTERNAL_ERROR;
        } else if (bmError == BMError.AlreadyShown
                || bmError == BMError.Destroyed
                || bmError == BMError.NotInitialized
                || bmError == BMError.Expired) {
            return MoPubErrorCode.NETWORK_INVALID_STATE;
        } else {
            return MoPubErrorCode.UNSPECIFIED;
        }
    }

    static void updateGDPR() {
        PersonalInfoManager personalInfoManager = MoPub.getPersonalInformationManager();
        if (personalInfoManager != null) {
            BidMachine.setSubjectToGDPR(personalInfoManager.gdprApplies());
            BidMachine.setConsentConfig(
                    personalInfoManager.canCollectPersonalInformation(),
                    "");
        }
    }

    /**
     * Map<String, String> extraData = new HashMap<>();
     * extraData.put("userId", "user123");
     * extraData.put("gender", Gender.Female.getOrtbValue());
     * extraData.put("yob", "2000");
     * extraData.put("keywords", "Keyword_1,Keyword_2,Keyword_3,Keyword_4");
     * extraData.put("country", "Russia");
     * extraData.put("city", "Kirov");
     * extraData.put("zip", "610000");
     * extraData.put("sturl", "https://store_url.com");
     * extraData.put("paid", "true");
     * extraData.put("bcat", "IAB-1,IAB-3,IAB-5");
     * extraData.put("badv", "https://domain_1.com,https://domain_2.org");
     * extraData.put("bapps", "application_1,application_2,application_3");
     */
    static TargetingParams findTargetingParams(@Nullable Map<String, Object> extras) {
        if (extras == null) {
            return null;
        }

        String userId = (String) extras.get("userId");
        String gender = (String) extras.get("gender");
        String birthdayYear = (String) extras.get("yob");
        String keywords = (String) extras.get("keywords");
        String country = (String) extras.get("country");
        String city = (String) extras.get("city");
        String zip = (String) extras.get("zip");
        String sturl = (String) extras.get("sturl");
        String paid = (String) extras.get("paid");
        String bcat = (String) extras.get("bcat");
        String badv = (String) extras.get("badv");
        String bapps = (String) extras.get("bapps");

        TargetingParams targetingParams = new TargetingParams();
        targetingParams.setUserId(userId);
        targetingParams.setGender(parseGender(gender));
        targetingParams.setBirthdayYear(parseInt(birthdayYear));
        targetingParams.setKeywords(splitString(keywords));
        targetingParams.setCountry(country);
        targetingParams.setCity(city);
        targetingParams.setZip(zip);
        targetingParams.setStoreUrl(sturl);
        targetingParams.setPaid(Boolean.parseBoolean(paid));
        for (String value : splitString(bcat)) {
            targetingParams.addBlockedAdvertiserIABCategory(value);
        }
        for (String value : splitString(badv)) {
            targetingParams.addBlockedAdvertiserDomain(value);
        }
        for (String value : splitString(bapps)) {
            targetingParams.addBlockedApplication(value);
        }
        return targetingParams;
    }

    static <T> PriceFloorParams findPriceFloorParams(@Nullable Map<String, T> extras) {
        if (extras == null) {
            return null;
        }

//        JSONArray jsonArray = new JSONArray();
//        try {
//            jsonArray.put(new JSONObject().put("id1", "1"));
//            jsonArray.put(new JSONObject().put("id1", "1.2"));
//            jsonArray.put(new JSONObject().put("id2", "20,04"));
//            jsonArray.put(new JSONObject().put("id3", 300.006));
//            jsonArray.put(new JSONObject().put("id4", 1000));
//            jsonArray.put("1");
//            jsonArray.put("1.2");
//            jsonArray.put("20,04");
//            jsonArray.put(300.006);
//            jsonArray.put(1000);
//        } catch (Exception ignore) {
//
//        }
//        Map<String, String> extraData = new HashMap<>();
//        extraData.put("priceFloors", jsonArray.toString());

        String priceFloors = (String) extras.get("priceFloors");
        return createPriceFloorParams(priceFloors);
    }

    private static Gender parseGender(String ortbValue) {
        if (Gender.Female.getOrtbValue().equals(ortbValue)) {
            return Gender.Female;
        } else if (Gender.Male.getOrtbValue().equals(ortbValue)) {
            return Gender.Male;
        } else {
            return Gender.Omitted;
        }
    }

    private static Integer parseInt(String value) {
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }

    private static String[] splitString(String value) {
        if (TextUtils.isEmpty(value)) {
            return new String[0];
        }
        try {
            return value.split(",");
        } catch (Exception e) {
            return new String[0];
        }
    }

    private static PriceFloorParams createPriceFloorParams(String jsonArrayString) {
        PriceFloorParams priceFloorParams = new PriceFloorParams();
        if (TextUtils.isEmpty(jsonArrayString)) {
            return priceFloorParams;
        }

        try {
            JSONArray jsonArray = new JSONArray(jsonArrayString);
            for (int i = 0; i < jsonArray.length(); i++) {
                Object object = jsonArray.opt(i);
                if (object instanceof Double) {
                    priceFloorParams.addPriceFloor((Double) object);
                } else if (object instanceof JSONObject) {
                    JSONObject jsonObject = (JSONObject) object;
                    String id = jsonObject.optString("id");
                    double price = jsonObject.optDouble("price", -1);
                    if (!TextUtils.isEmpty(id) && price >= 0) {
                        priceFloorParams.addPriceFloor(id, price);
                    }
                }
            }
        } catch (Exception e) {
            return new PriceFloorParams();
        }

        return priceFloorParams;
    }

}
