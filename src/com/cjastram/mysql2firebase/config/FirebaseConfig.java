package com.cjastram.mysql2firebase.config;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class FirebaseConfig {

    public FirebaseConfig(String uid) {
        checkNotNull(uid, "userid should not be null");
        // auth.put("uid", uid);
    }

    public String getAdminsdkConfigPath() {
        return "./config/mysql-4daa0-firebase-adminsdk.json";
    }

    public String getFirebaseDatabaseUrl() {
        return "https://mysql-4daa0.firebaseio.com";
    }

    private HashMap<String, Object> auth = new HashMap<String, Object>();

    public Map<String, Object> getDatabasAuthVariableOverrides() {
        return auth;
    }
}
