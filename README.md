# BidMachine-Android-mopub-adapter
BidMachine Android adapter for MoPub mediation

## Examples:

#### Initialize: [Sample](example/src/main/java/io/bidmachine/examples/BidMachineMoPubActivity.java#L110)
#### Load Banner: [Sample](example/src/main/java/io/bidmachine/examples/BidMachineMoPubActivity.java#L170)
#### Load Interstitial: [Sample](example/src/main/java/io/bidmachine/examples/BidMachineMoPubActivity.java#L222)
#### Load Rewarded Video: [Sample](example/src/main/java/io/bidmachine/examples/BidMachineMoPubActivity.java#L262)


Local SDK configuration sample:
<
//Prepare priceFloors for BidMachine
JSONArray jsonArray = new JSONArray();
try {
    jsonArray.put(new JSONObject().put("id1", 300.006));
    jsonArray.put(new JSONObject().put("id2", 1000));
    jsonArray.put(302.006);
    jsonArray.put(1002);
} catch (Exception e) {
    e.printStackTrace();
}

//Prepare configuration map for BidMachineAdapterConfiguration
Map<String, String> configuration = new HashMap<>();
configuration.put("seller_id", "1");
configuration.put("coppa", "true");
configuration.put("logging_enabled", "true");
configuration.put("test_mode", "true");
configuration.put("banner_width", "320");
configuration.put("userId", "user123");
configuration.put("gender", "F");
configuration.put("yob", "2000");
configuration.put("keywords", "Keyword_1,Keyword_2,Keyword_3,Keyword_4");
configuration.put("country", "Russia");
configuration.put("city", "Kirov");
configuration.put("zip", "610000");
configuration.put("sturl", "https://store_url.com");
configuration.put("paid", "true");
configuration.put("bcat", "IAB-1,IAB-3,IAB-5");
configuration.put("badv", "https://domain_1.com,https://domain_2.org");
configuration.put("bapps", "com.test.application_1,com.test.application_2,com.test.application_3");
configuration.put("priceFloors", jsonArray.toString());

//Prepare SdkConfiguration for initialize MoPub with BidMachineAdapterConfiguration
SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(AD_UNIT_ID)
        .withAdditionalNetwork(BidMachineAdapterConfiguration.class.getName())
        .withMediatedNetworkConfiguration(
                BidMachineAdapterConfiguration.class.getName(),
                configuration)
        .build();
>


Server Banner configuration sample:
<
{
    "seller_id": "1",
    "coppa": "true",
    "logging_enabled": "true",
    "test_mode": "true",
    "banner_width": "320",
    "userId": "user123",
    "gender": "F",
    "yob": "2000",
    "keywords": "Keyword_1,Keyword_2,Keyword_3,Keyword_4",
    "country": "Russia",
    "city": "Kirov",
    "zip": "610000",
    "sturl": "https://store_url.com",
    "paid": "true",
    "bcat": "IAB-1,IAB-3,IAB-5",
    "badv": "https://domain_1.com,https://domain_2.org",
    "bapps": "com.test.application_1,com.test.application_2,com.test.application_3",
    "priceFloors": [{
            "id_1": 300.06
        }, {
            "id_2": 1000
        },
        302.006,
        1002
    ]
}
>

Local Banner configuration sample:
<
//Prepare priceFloors for BidMachine
JSONArray jsonArray = new JSONArray();
try {
    jsonArray.put(new JSONObject().put("id1", 300.006));
    jsonArray.put(new JSONObject().put("id2", 1000));
    jsonArray.put(302.006);
    jsonArray.put(1002);
} catch (Exception e) {
    e.printStackTrace();
}

//Prepare localExtras for MoPubView
Map<String, String> localExtras = new HashMap<>();
localExtras.put("seller_id", "1");
localExtras.put("coppa", "true");
localExtras.put("logging_enabled", "true");
localExtras.put("test_mode", "true");
localExtras.put("banner_width", "320");
localExtras.put("userId", "user123");
localExtras.put("gender", "F");
localExtras.put("yob", "2000");
localExtras.put("keywords", "Keyword_1,Keyword_2,Keyword_3,Keyword_4");
localExtras.put("country", "Russia");
localExtras.put("city", "Kirov");
localExtras.put("zip", "610000");
localExtras.put("sturl", "https://store_url.com");
localExtras.put("paid", "true");
localExtras.put("bcat", "IAB-1,IAB-3,IAB-5");
localExtras.put("badv", "https://domain_1.com,https://domain_2.org");
localExtras.put("bapps", "com.test.application_1,com.test.application_2,com.test.application_3");
localExtras.put("priceFloors", jsonArray.toString());

//Create new MoPubView instance and load
moPubView = new MoPubView(this);
moPubView.setLayoutParams(new ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT));
moPubView.setLocalExtras(localExtras);
moPubView.setAutorefreshEnabled(false);
moPubView.setAdUnitId(BANNER_KEY);
moPubView.setBannerAdListener(new BannerViewListener());
moPubView.loadAd();
>

Server Interstitial configuration sample:
<
{
    "seller_id": "1",
    "coppa": "true",
    "logging_enabled": "true",
    "test_mode": "true",
    "ad_content_type": "All",
    "userId": "user123",
    "gender": "F",
    "yob": "2000",
    "keywords": "Keyword_1,Keyword_2,Keyword_3,Keyword_4",
    "country": "Russia",
    "city": "Kirov",
    "zip": "610000",
    "sturl": "https://store_url.com",
    "paid": "true",
    "bcat": "IAB-1,IAB-3,IAB-5",
    "badv": "https://domain_1.com,https://domain_2.org",
    "bapps": "com.test.application_1,com.test.application_2,com.test.application_3",
    "priceFloors": [{
            "id_1": 300.06
        }, {
            "id_2": 1000
        },
        302.006,
        1002
    ]
}
>

Local Interstitial configuration sample:
<
//Prepare priceFloors for BidMachine
JSONArray jsonArray = new JSONArray();
try {
    jsonArray.put(new JSONObject().put("id1", 300.006));
    jsonArray.put(new JSONObject().put("id2", 1000));
    jsonArray.put(302.006);
    jsonArray.put(1002);
} catch (Exception e) {
    e.printStackTrace();
}

//Prepare localExtras for MoPubInterstitial
Map<String, String> localExtras = new HashMap<>();
localExtras.put("seller_id", "1");
localExtras.put("coppa", "true");
localExtras.put("logging_enabled", "true");
localExtras.put("test_mode", "true");
localExtras.put("banner_width", "320");
localExtras.put("userId", "user123");
localExtras.put("gender", "F");
localExtras.put("yob", "2000");
localExtras.put("keywords", "Keyword_1,Keyword_2,Keyword_3,Keyword_4");
localExtras.put("country", "Russia");
localExtras.put("city", "Kirov");
localExtras.put("zip", "610000");
localExtras.put("sturl", "https://store_url.com");
localExtras.put("paid", "true");
localExtras.put("bcat", "IAB-1,IAB-3,IAB-5");
localExtras.put("badv", "https://domain_1.com,https://domain_2.org");
localExtras.put("bapps", "com.test.application_1,com.test.application_2,com.test.application_3");
localExtras.put("priceFloors", jsonArray.toString());

//Create new MoPubInterstitial instance and load
moPubInterstitial = new MoPubInterstitial(this, INTERSTITIAL_KEY);
moPubInterstitial.setLocalExtras(localExtras);
moPubInterstitial.setInterstitialAdListener(new InterstitialListener());
moPubInterstitial.load();
>

Server RewardedVideo configuration sample:
<
{
    "seller_id": "1",
    "coppa": "true",
    "logging_enabled": "true",
    "test_mode": "true",
    "userId": "user123",
    "gender": "F",
    "yob": "2000",
    "keywords": "Keyword_1,Keyword_2,Keyword_3,Keyword_4",
    "country": "Russia",
    "city": "Kirov",
    "zip": "610000",
    "sturl": "https://store_url.com",
    "paid": "true",
    "bcat": "IAB-1,IAB-3,IAB-5",
    "badv": "https://domain_1.com,https://domain_2.org",
    "bapps": "com.test.application_1,com.test.application_2,com.test.application_3",
    "priceFloors": [{
            "id_1": 300.06
        }, {
            "id_2": 1000
        },
        302.006,
        1002
    ]
}
>