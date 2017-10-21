package com.cjastram.mysql2firebase.client;

import com.cjastram.mysql2firebase.config.FirebaseConfig;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class FirebaseClient {


    private static Logger logger = LoggerFactory.getLogger(FirebaseMySQLClient.class);


    public FirebaseClient(FirebaseConfig config) {
        checkNotNull(config, "Configuration for FirebaseClient can't be null");
        this.config = config;
    }

    void initializeFirebaseApp() throws IOException {

        logger.info("load config file and initialize FirebaseApp");
        try (FileInputStream serviceAccount =
                     new FileInputStream(config.getAdminsdkConfigPath())) {

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredential(FirebaseCredentials.fromCertificate(serviceAccount))
                    .setDatabaseAuthVariableOverride(config.getDatabasAuthVariableOverrides())
                    .setDatabaseUrl(config.getFirebaseDatabaseUrl())
                    .build();

            FirebaseApp.initializeApp(options);
        }
    }

    final int run() {
        int result = 0;
        try {
            initializeFirebaseApp();
            setupClient();
            runOnce();
            waitForShutdown();
            return 0;
        } catch (Throwable e) {
            logger.error("unexpected error", e);
            result = -1;
        }
        return result;
    }

    protected void runOnce() {
        // do nothing
    }

    abstract void setupClient();

    protected void waitForShutdown() {
        logger.info("Press ENTER to shutdown ...");
        try {
            int i = System.in.read();
        } catch (IOException e) {
            // do nothing
        }
    }


    private FirebaseConfig config;
}
