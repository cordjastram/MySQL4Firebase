package com.cjastram.mysql4firebase.model;

import java.util.ArrayList;
import java.util.List;

public class DbStatement {

    public static final String QUERY = "QUERY";

    public static final String CALLABLE = "CALLABLE";

    public final static String STATEMENT = "STATEMENT";

    public static final String PLAIN_SQL = "PLAIN_SQL";

    public String name;

    public String statement;

    public String type;

    public List<Parameter> parameterList = new ArrayList<>();

}
