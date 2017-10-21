package com.cjastram.mysql4firebase.client;


import com.cjastram.mysql4firebase.model.DbStatement;
import com.cjastram.mysql4firebase.model.Parameter;
import com.cjastram.mysql4firebase.model.SQLRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by cjastram on 18.07.2017.
 */

public class MySqlProcessor {

    public static Connection connect() throws ClassNotFoundException, SQLException, IOException {
        MySQLConfig config = MySQLConfig.getInstance();
        return DriverManager.getConnection(config.getURL(), config.getUsername(), config.getPassword());
    }

    static void executeStatement(Map<String, DbStatement> preparedStatementMap, SQLRequest requestToProcess, Connection connection, DatabaseReference dbRef) throws SQLException {

        requestToProcess.message = "Start executing prepared statement ...";
        dbRef.setValue(requestToProcess);

        DbStatement statementTemplate = preparedStatementMap.get(requestToProcess.dbStatementName);

        if (statementTemplate == null) {
            requestToProcess.executionFailed = true;
            requestToProcess.message = "Template does not exist: " + requestToProcess.dbStatementName;
        } else {
            requestToProcess.message = "Start executing " + DbStatement.QUERY + " ...";
            dbRef.setValue(requestToProcess);
            switch (statementTemplate.type) {
                case DbStatement.QUERY:
                    handleQuery(statementTemplate, requestToProcess, connection);
                    break;
                case DbStatement.STATEMENT:
                    handleUpdateStatement(statementTemplate, requestToProcess, connection);
                    break;
                case DbStatement.CALLABLE:
                    handleStoredProcedure(statementTemplate, requestToProcess, connection);
                    break;
                case DbStatement.PLAIN_SQL:
                    handlePlainSQL(requestToProcess, connection);
                    break;
                default:
                    throw new IllegalArgumentException("invalid template type: " + statementTemplate.type);
            }
        }
    }

    static private void handleQuery(DbStatement template, SQLRequest request, Connection connection) throws SQLException {

        try (PreparedStatement pstmt = connection.prepareStatement(template.statement)) {
            mapInParameter(request, pstmt);
            ResultSet rs = pstmt.executeQuery();
            mapResultSet(rs, request);
        }
    }

    static public void handlePlainSQL(SQLRequest request, Connection connection) throws SQLException {

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(request.parameter.get(0).value);
            mapResultSet(stmt.getResultSet(), request);
        } catch (SQLException e) {
            // Just a syntax, error everything is ok
            request.message = "Syntax error: " + e.getMessage();
            request.executionFailed = true;
        }
    }

    static private void mapInParameter(SQLRequest template, PreparedStatement stmt) throws SQLException {

        for (Parameter param : template.parameter) {
            if (param.isIn) {
                switch (param.type) {
                    case Types.CHAR:
                    case Types.VARCHAR:
                        stmt.setString(param.position, param.value);
                        break;
                    case Types.INTEGER:
                        int pos = param.position;
                        int value = Integer.parseInt(param.value);
                        stmt.setInt(pos, value);
                        break;
                    default:
                        throw new SQLException("Invalid parameter type");
                }
            }
        }

    }

    static private void registerOutParameter(SQLRequest template, CallableStatement stmt) throws SQLException {

        for (Parameter param : template.parameter) {
            if (param.isOut) {
                stmt.registerOutParameter(param.position, param.type);
            }
        }

    }

    static private void mapOutParameter(CallableStatement stmt, SQLRequest template) throws SQLException {
        for (Parameter param : template.parameter) {
            if (param.isOut) {
                int pos = param.position;
                String value = stmt.getString(pos);
                param.value = value;
            }
        }
    }

    static private void mapResultSet(ResultSet rs, SQLRequest template) throws SQLException {
        if (rs != null) {
            ResultSetMetaData rsm = rs.getMetaData();
            int columnsCount = rsm.getColumnCount() + 1;
            List<String> result = new ArrayList<>();
            while (rs.next()) {
                JsonArray ja = new JsonArray();
                for (int col = 1; col < columnsCount; col++) {
                    String strCol = rs.getString(col);
                    ja.add(new JsonPrimitive(strCol == null ? "null" : strCol));
                }
                result.add(ja.toString());
            }
            template.result = result;
            template.executionFailed = false;
        }
    }

    static private void handleStoredProcedure(DbStatement template, SQLRequest request, Connection connection) throws SQLException {

        try (CallableStatement statement = connection.prepareCall(template.statement)) {
            mapInParameter(request, statement);
            registerOutParameter(request, statement);
            statement.execute();
            mapOutParameter(statement, request);
            mapResultSet(statement.getResultSet(), request);
        }
    }

    static private int handleUpdateStatement(DbStatement template, SQLRequest request, Connection connection) throws SQLException {
        int rowsAffected = -1;

        try (PreparedStatement cstmt = connection.prepareStatement(template.statement)) {
            mapInParameter(request, cstmt);
            rowsAffected = cstmt.executeUpdate();
        }
        return rowsAffected;
    }


}

class MySQLConfig {
    private static final String SERVER_NAME = "serverName";
    private static final String DATABASE = "database";
    private static final String USER_NAME = "username";
    private static final String PASSWORD = "password";
    private static final String CONFIG_FILE = "./config/mysql.config.xml";

    private static MySQLConfig config = new MySQLConfig();

    private Properties properties;

    static MySQLConfig getInstance() {
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
    String getUsername() {
        return properties.getProperty(USER_NAME);
    }

    /**
     * @return the password from the XML config file
     */
    String getPassword() {
        return properties.getProperty(PASSWORD);
    }

    /**
     * @return the URL for the database
     */
    String getURL() {
        return String.format("jdbc:mysql://%s/%s?generateSimpleParameterMetadata=true", config.getServerName(), config.getDatabase());
    }
}