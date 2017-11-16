package com.cjastram.mysql2firebase.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

public class FirebaseConfig {

    private static final String CONFIG_FILE = "./config/firebase.config.xml";


    private FirebaseConfig(String uid) {
        checkNotNull(uid, "userid should not be null");
        auth.put("uid", uid);
        properties = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            properties.loadFromXML(fis);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static FirebaseConfig getInstance(String uid) {
        return new FirebaseConfig(uid);
    }

    private Properties properties;

    public String getAdminsdkConfigPath() {
        return properties.get("admin_sdk_json_file").toString();
    }

    public String getFirebaseDatabaseUrl() {
        return properties.get("firebase_db_path").toString();
    }

    private HashMap<String, Object> auth = new HashMap<String, Object>();

    public Map<String, Object> getDatabasAuthVariableOverrides() {
        return auth;
    }
}
