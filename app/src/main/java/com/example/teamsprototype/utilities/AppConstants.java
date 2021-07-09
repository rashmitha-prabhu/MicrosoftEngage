package com.example.teamsprototype.utilities;

import java.util.HashMap;

public class AppConstants {
//    Constants used throughout the app

    public static final String KEY_COLLECTION = "Users";
    public static final String NAME = "name";
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    public static final String USER_ID = "uid";

    public static final String PREFERENCE = "teamsCallPreference";
    public static final String SIGNED_IN = "isSignedIn";

    public static final String TOKENS = "agora_tokens";
    public static final String FCM_TOKEN = "fcm_token";

    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT = "Content-Type";

    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_ID = "registration_ids";
    public static final String REMOTE_MSG_INVITER_TOKEN = "inviterToken";

    public static final String REMOTE_MSG_RESPONSE = "response";
    public static final String TYPE = "type";

    public static final String INVITE = "invitation";
    public static final String ACCEPT = "accept";
    public static final String REJECT = "reject";
    public static final String CANCEL = "cancel";

    public static HashMap<String, String> getRemoteMessageHeaders(){
        HashMap<String, String> headers = new HashMap<>();
        headers.put(
                AppConstants.REMOTE_MSG_AUTHORIZATION,
                "key=AAAA213OA0Y:APA91bGMnSOzhkHTmKSUaeRDaBzZOc8xGFfa3yA6Tm0b4b-iepaHDXg5tGX15xlhRZNjjsFBjokrfXH9z_nBFq527BBDMV9tFRF-d6SzMi3a4CFvlcenEPg0Tlw9RMvXyOk_hgjF6V-t"
        );
        headers.put(AppConstants.REMOTE_MSG_CONTENT, "application/json");
        return headers;
    }
}
