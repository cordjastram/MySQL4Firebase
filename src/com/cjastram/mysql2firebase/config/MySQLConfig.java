package com.cjastram.mysql2firebase.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class MySQLConfig {

    private static final String SERVER_NAME = "serverName";
    private static final String DATABASE = "database";
    private static final String USER_NAME = "username";
    private static final String PASSWORD = "password";
    private static final String CONFIG_FILE = "./config/mysql.config.xml";

    private static MySQLConfig config = new MySQLConfig();

    private Properties properties;

    public static MySQLConfig getInstance() {
        return MySQLConfig.config;
    }

    private MySQLConfig() {
        properties = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            properties.loadFromXML(fis);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * @return the server name from the XML config file
     */
    private String getServerName() {
        return properties.getProperty(SERVER_NAME);
    }

    /**
     * @return the database name from the XML config file
     */
    private String getDatabase() {
        return properties.getProperty(DATABASE);
    }

    /**
     * @return the user name from the XML config file
     */
    public String getUsername() {
        return properties.getProperty(USER_NAME);
    }

    /**
     * @return the password from the XML config file
     */
    public String getPassword() {
        return properties.getProperty(PASSWORD);
    }

    /**
     * @return the URL for the database
     */
    public String getURL() {
        return String.format("jdbc:mysql://%s/%s?generateSimpleParameterMetadata=true", config.getServerName(), config.getDatabase());
    }
}
