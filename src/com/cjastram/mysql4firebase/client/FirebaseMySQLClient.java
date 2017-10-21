package com.cjastram.mysql4firebase.client;

import com.cjastram.mysql4firebase.config.FirebaseConfig;
import com.cjastram.mysql4firebase.model.DbStatement;
import com.cjastram.mysql4firebase.model.Parameter;
import com.cjastram.mysql4firebase.model.QueueItem;
import com.cjastram.mysql4firebase.model.SQLRequest;
import com.google.firebase.database.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseMySQLClient extends FirebaseClient {

    private static Logger logger = LoggerFactory.getLogger(FirebaseMySQLClient.class);


    static Map<String, DbStatement> dbStatements = new HashMap<>();

    public static void main(String[] args) {
        int rc = new FirebaseMySQLClient().run();
        logger.info(String.format("return code = %d", rc));
        System.exit(rc);
    }

    public FirebaseMySQLClient() {
        super(new FirebaseConfig("mysql-service"));
    }


    @Override
    void setupClient() {

        createDbStatements();

        readDbStatements();

        createDatabaseListeners();
    }


    private void createDbStatements() {
        List<DbStatement> dbStatements = new ArrayList<>();

        DbStatement dbStatement = new DbStatement();
        dbStatement.name = "select_customers";
        dbStatement.type = DbStatement.QUERY;
        dbStatement.statement = "select * from customers where city = ? limit ?";
        dbStatement.parameterList.add(Parameter.inParameter(1, Types.VARCHAR, "London"));
        dbStatement.parameterList.add(Parameter.inParameter(2, Types.DECIMAL, "2"));
        dbStatements.add(dbStatement);

        dbStatement = new DbStatement();
        dbStatement.name = "plain_sql";
        dbStatement.type = DbStatement.PLAIN_SQL;
        dbStatement.statement = "";
        dbStatement.parameterList.add(Parameter.inParameter(1, Types.VARCHAR, "select count(*) from customers"));
        dbStatements.add(dbStatement);

        dbStatement = new DbStatement();
        dbStatement.name = "stored_procedure";
        dbStatement.type = DbStatement.CALLABLE;
        dbStatement.statement = "{call sp_employees_cursor( ? )}";
        dbStatement.parameterList.add(Parameter.inParameter(1, Types.VARCHAR, "London"));
        dbStatements.add(dbStatement);


        DatabaseReference dbRefStatements = FirebaseDatabase.getInstance().getReference("db_statement");

        for (DbStatement dbs : dbStatements) {

            dbRefStatements.child(dbs.name).setValue(dbs, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError error, DatabaseReference ref) {
                    if (error == null) {
                        logger.info("Saved");
                    } else {
                        logger.error(error.getMessage());
                    }
                }
            });

        }
    }

    private void readDbStatements() {
        GetObjectListFromFirebaseHelper<DbStatement> helper = new GetObjectListFromFirebaseHelper<>(DbStatement.class);
        FirebaseDatabase.getInstance().getReference("db_statement").addListenerForSingleValueEvent(helper);

        List<DbStatement> statements = helper.getResult();

        for (DbStatement dbs : statements) {
            FirebaseMySQLClient.dbStatements.put(dbs.name, dbs);
        }
    }

    private void createDatabaseListeners() {
        logger.info("create database listeners ");
        FirebaseDatabase.getInstance().getReference("queue").addChildEventListener(new QueueListener());
    }

}

class QueueValueEventListener implements ValueEventListener {
    private static Logger logger = LoggerFactory.getLogger(QueueListener.class);

    @Override
    public void onDataChange(DataSnapshot snapshot) {

        Iterable<DataSnapshot> iterable = snapshot.getChildren();

        logger.info("OnChildAdded: " + snapshot.getChildrenCount() + " Childs");

        for (DataSnapshot ds : iterable) {

            QueueItem queueItem = ds.getValue(QueueItem.class);

            logger.info("OnChildAdded: " + queueItem.toString());

            DatabaseReference dbQueueItem = FirebaseDatabase.getInstance().getReference(queueItem.dbRef);

            if (!queueItem.isProcessed) {

                final DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference(queueItem.dbPathToProcess);

                dbReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        SQLRequest request = snapshot.getValue(SQLRequest.class);

                        logger.info("Process request: " + request.toString());

                        try {
                            logger.info("Connect");
                            request.message = "Try to connect ...";
                            dbReference.setValue(request);
                            Connection con = MySqlProcessor.connect();
                            MySqlProcessor.executeStatement(FirebaseMySQLClient.dbStatements, request, con, snapshot.getRef());
                            snapshot.getRef().setValue(request);
                            logger.info("Result: " + request.toString());
                            if (!request.executionFailed) {
                                request.message = "Success!";
                            }
                        } catch (Throwable e) {
                            request.message = e.getMessage();
                            request.executionFailed = true;
                            logger.error(e.getMessage(), e);
                        } finally {
                            dbReference.setValue(request);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        logger.info("onCancelled" + error.getMessage());
                    }
                });
            }

            //dbQueueItem.removeValue();
            logger.info(queueItem.toString());
        }
    }


    @Override
    public void onCancelled(DatabaseError error) {
        logger.error(error.getMessage());
    }
}


class QueueListener implements ChildEventListener {
    private static Logger logger = LoggerFactory.getLogger(QueueListener.class);

    @Override
    public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
        QueueItem queueItem = snapshot.getValue(QueueItem.class);

        logger.info("OnChildAdded: " + queueItem.toString());

        final DatabaseReference dbQueueItem = snapshot.getRef();

        if (!queueItem.isProcessed) {

            final DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference(queueItem.dbPathToProcess);

            dbReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    SQLRequest request = snapshot.getValue(SQLRequest.class);

                    logger.info("Process request: " + request.toString());

                    try {
                        request.message = "Try to connect ...";
                        dbReference.setValue(request);
                        Connection con = MySqlProcessor.connect();
                        MySqlProcessor.executeStatement(FirebaseMySQLClient.dbStatements, request, con, snapshot.getRef());
                        snapshot.getRef().setValue(request);
                        logger.info("Result: " + request.toString());
                        if (!request.executionFailed) {
                            request.message = "Success!";
                        }
                    } catch (Throwable e) {
                        request.message = e.getMessage();
                        request.executionFailed = true;
                        logger.error(e.getMessage(), e);
                    } finally {
                        dbReference.setValue(request);
                        dbQueueItem.removeValue();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    logger.info("onCancelled" + error.getMessage());
                }
            });
        }
    }

    @Override
    public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
        logger.info("OnChildChanged: " + previousChildName);
    }

    @Override
    public void onChildRemoved(DataSnapshot snapshot) {
        logger.info("OnChildRemoved" + snapshot.getKey());
    }

    @Override
    public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
        logger.info("OnChildMoved" + previousChildName);
    }

    @Override
    public void onCancelled(DatabaseError error) {
        logger.info("OnChilCancelled");
    }
}