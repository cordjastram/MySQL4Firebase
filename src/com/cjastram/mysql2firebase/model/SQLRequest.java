package com.cjastram.mysql2firebase.model;

import java.util.ArrayList;
import java.util.List;

public class SQLRequest {

    public String dbStatementName;

    public String message;

    public boolean executionFailed;

    public List<String> result = new ArrayList<>();

    public List<Parameter> parameter = new ArrayList<>();

    @Override
    public String toString() {
        return "SQLRequest{" +
                "  dbStatementName='" + dbStatementName + '\'' +
                ", message='" + message + '\'' +
                ", executionFailed=" + executionFailed +
                ", result=" + result +
                ", parameter=" + parameter +
                '}';
    }
}
