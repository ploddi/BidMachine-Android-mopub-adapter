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

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import io.bidmachine.BidMachine;
import io.bidmachine.PriceFloorParams;
import io.bidmachine.TargetingParams;
import io.bidmachine.utils.BMError;
import io.bidmachine.utils.Gender;

class BidMachineUtils {

    private static final String SELLER_ID = "seller_id";
    private static final String COPPA = "coppa";
    private static final String LOGGING_ENABLED = "logging_enabled";
    private static final String TEST_MODE = "test_mode";
    private static Map<String, String> configuration;
    private static boolean isInitialized = false;

    static void storeConfiguration(@NonNull Map<String, String> configuration) {
        BidMachineUtils.configuration = configuration;
    }

    /**
     * @param extras - map where are seller_id, coppa, logging_enabled, test_mode
     * @return was initialize or not
     */
    static <T> boolean prepareBidMachine(Context context, @NonNull Map<String, T> extras) {
        Boolean loggingEnabled = parseBoolean(extras.get(LOGGING_ENABLED));
        if (loggingEnabled != null) {
            BidMachine.setLoggingEnabled(loggingEnabled);
        }
        Boolean testMode = parseBoolean(extras.get(TEST_MODE));
        if (testMode != null) {
            BidMachine.setTestMode(testMode);
        }
        Boolean coppa = parseBoolean(extras.get(COPPA));
        if (coppa != null) {
            BidMachine.setCoppa(coppa);
        }
        BidMachineUtils.updateGDPR();
        if (!isInitialized) {
            String sellerId = parseString(extras.get(SELLER_ID));
            if (!TextUtils.isEmpty(sellerId)) {
                BidMachine.initialize(context, sellerId);
                isInitialized = true;
                return true;
            } else {
                MoPubLog.log(
                        MoPubLog.AdapterLogEvent.CUSTOM,
                        BidMachineUtils.class.getSimpleName(),
                        "seller_id not found anywhere (serverExtras, localExtras, configuration). BidMachine not initialized");
                return false;
            }
        }
        return true;
    }

    /**
     * Transform BidMachine error to MoPub error
     *
     * @param bmError - BidMachine error object
     * @return MoPub error object
     */
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

    /**
     * Prepare fused map from serverExtras, localExtras and configuration
     *
     * @param serverExtras - map from server
     * @param localExtras  - map from local, set with setLocalExtras
     * @return fused map which must be contains serverExtras, localExtras and configuration
     */
    static Map<String, Object> getFusedMap(Map<String, String> serverExtras, Map<String, Object> localExtras) {
        Map<String, Object> fusedExtras = new HashMap<>();
        putMap(fusedExtras, configuration);
        putMap(fusedExtras, localExtras);
        putMap(fusedExtras, serverExtras);
        return fusedExtras;
    }

    /**
     * +----------+--------------------------------------------------------------------------------+------------+
     * |   Key    |                                   Definition                                   | Value type |
     * +----------+--------------------------------------------------------------------------------+------------+
     * | userId   | Vendor-specific ID for the user                                                | String     |
     * | gender   | Gender, one of following: "F", "M", "O"                                        | String     |
     * | yob      | Year of birth as a 4-digit integer (e.g - 1990)                                | String     |
     * | keywords | List of keywords, interests, or intents (separated by comma)                   | String     |
     * | country  | Country of the user's home base (i.e., not necessarily their current location) | String     |
     * | city     | City of the user's home base (i.e., not necessarily their current location)    | String     |
     * | zip      | Zip of the user's home base (i.e., not necessarily their current location)     | String     |
     * | sturl    | App store URL for an installed app; for IQG 2.1 compliance                     | String     |
     * | paid     | Determines, if it is a free or paid version of the app                         | String     |
     * | bcat     | Block list of content categories using IDs (separated by comma)                | String     |
     * | badv     | Block list of advertisers by their domains (separated by comma)                | String     |
     * | bapps    | Block list of apps where ads are disallowed (separated by comma)               | String     |
     * +----------+--------------------------------------------------------------------------------+------------+
     * <p>
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
     *
     * @param extras - map where are the necessary parameters for targeting
     * @return TargetingParams with targeting from extras
     */
    static TargetingParams findTargetingParams(@NonNull Map<String, Object> extras) {
        TargetingParams targetingParams = new TargetingParams();
        String userId = parseString(extras.get("userId"));
        if (userId != null) {
            targetingParams.setUserId(userId);
        }
        Gender gender = parseGender(extras.get("gender"));
        if (gender != null) {
            targetingParams.setGender(gender);
        }
        int birthdayYear = parseInteger(extras.get("yob"));
        if (birthdayYear > -1) {
            targetingParams.setBirthdayYear(birthdayYear);
        }
        String keywords = parseString(extras.get("keywords"));
        if (keywords != null) {
            targetingParams.setKeywords(splitString(keywords));
        }
        String country = parseString(extras.get("country"));
        if (country != null) {
            targetingParams.setCountry(country);
        }
        String city = parseString(extras.get("city"));
        if (city != null) {
            targetingParams.setCity(city);
        }
        String zip = parseString(extras.get("zip"));
        if (zip != null) {
            targetingParams.setZip(zip);
        }
        String sturl = parseString(extras.get("sturl"));
        if (sturl != null) {
            targetingParams.setStoreUrl(sturl);
        }
        Boolean paid = parseBoolean(extras.get("paid"));
        if (paid != null) {
            targetingParams.setPaid(paid);
        }
        String bcat = parseString(extras.get("bcat"));
        if (bcat != null) {
            for (String value : splitString(bcat)) {
                targetingParams.addBlockedAdvertiserIABCategory(value);
            }
        }
        String badv = parseString(extras.get("badv"));
        if (badv != null) {
            for (String value : splitString(badv)) {
                targetingParams.addBlockedAdvertiserDomain(value);
            }
        }
        String bapps = parseString(extras.get("bapps"));
        if (bapps != null) {
            for (String value : splitString(bapps)) {
                targetingParams.addBlockedApplication(value);
            }
        }
        return targetingParams;
    }

    /**
     * +-------------+---------------------+---------------------+
     * |     Key     |     Definition      |     Value type      |
     * +-------------+---------------------+---------------------+
     * | priceFloors | List of price floor | JSONArray in String |
     * +-------------+---------------------+---------------------+
     * <p>
     * JSONArray jsonArray = new JSONArray();
     * jsonArray.put(new JSONObject().put("id1", 300.006));
     * jsonArray.put(new JSONObject().put("id2", 1000));
     * jsonArray.put(302.006);
     * jsonArray.put(1002);
     * <p>
     * Map<String, String> extraData = new HashMap<>();
     * extraData.put("priceFloors", jsonArray.toString());
     *
     * @param extras - map where are the necessary parameters for price floor
     * @return PriceFloorParams with price floors from extras
     */
    static <T> PriceFloorParams findPriceFloorParams(@NonNull Map<String, T> extras) {
        String priceFloors = parseString(extras.get("priceFloors"));
        return createPriceFloorParams(priceFloors);
    }

    /**
     * Update GDPR state
     */
    private static void updateGDPR() {
        PersonalInfoManager personalInfoManager = MoPub.getPersonalInformationManager();
        if (personalInfoManager != null) {
            BidMachine.setSubjectToGDPR(personalInfoManager.gdprApplies());
            BidMachine.setConsentConfig(
                    personalInfoManager.canCollectPersonalInformation(),
                    "");
        }
    }

    private static void putMap(Map<String, Object> fusedMap, Map<String, ?> map) {
        if (fusedMap == null || map == null) {
            return;
        }
        try {
            fusedMap.putAll(map);
        } catch (Exception ignore) {

        }
    }

    private static Boolean parseBoolean(Object object) {
        if (object instanceof Boolean) {
            return (Boolean) object;
        } else if (object instanceof String) {
            return Boolean.parseBoolean((String) object);
        } else {
            return null;
        }
    }

    private static String parseString(Object object) {
        if (object instanceof String) {
            return (String) object;
        } else {
            return null;
        }
    }

    static int parseInteger(Object object) {
        if (object instanceof Integer) {
            return (int) object;
        } else if (object instanceof Double) {
            return ((Double) object).intValue();
        } else if (object instanceof String) {
            try {
                return Integer.parseInt((String) object);
            } catch (Exception e) {
                return -1;
            }
        } else {
            return -1;
        }
    }

    private static Gender parseGender(Object object) {
        String ortbValue = parseString(object);
        if (ortbValue == null) {
            return null;
        }
        if (Gender.Female.getOrtbValue().equals(ortbValue)) {
            return Gender.Female;
        } else if (Gender.Male.getOrtbValue().equals(ortbValue)) {
            return Gender.Male;
        } else {
            return Gender.Omitted;
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

    private static PriceFloorParams createPriceFloorParams(@Nullable String jsonArrayString) {
        PriceFloorParams priceFloorParams = new PriceFloorParams();
        if (TextUtils.isEmpty(jsonArrayString)) {
            return priceFloorParams;
        }

        try {
            JSONArray jsonArray = new JSONArray(jsonArrayString);
            for (int i = 0; i < jsonArray.length(); i++) {
                Object object = jsonArray.opt(i);
                if (object instanceof JSONObject) {
                    JSONObject jsonObject = (JSONObject) object;
                    Iterator<String> iterator = jsonObject.keys();
                    while (iterator.hasNext()) {
                        String id = iterator.next();
                        double price = parsePrice(jsonObject.opt(id));
                        if (!TextUtils.isEmpty(id) && price > -1) {
                            priceFloorParams.addPriceFloor(id, price);
                        }
                    }
                } else {
                    double price = parsePrice(object);
                    if (price > -1) {
                        priceFloorParams.addPriceFloor(price);
                    }
                }
            }
        } catch (Exception e) {
            return new PriceFloorParams();
        }

        return priceFloorParams;
    }

    private static double parsePrice(Object object) {
        if (object instanceof Double) {
            return (double) object;
        } else if (object instanceof Integer) {
            return ((Integer) object).doubleValue();
        } else if (object instanceof String) {
            return convertToPrice((String) object);
        }
        return -1;
    }

    private static double convertToPrice(String value) {
        if (!TextUtils.isEmpty(value)) {
            try {
                if (value.lastIndexOf('.') > value.lastIndexOf(',')) {
                    return NumberFormat.getInstance(Locale.TAIWAN).parse(value).doubleValue();
                } else {
                    return NumberFormat.getInstance().parse(value).doubleValue();
                }
            } catch (Exception e) {
                return -1;
            }
        }
        return -1;
    }

}
