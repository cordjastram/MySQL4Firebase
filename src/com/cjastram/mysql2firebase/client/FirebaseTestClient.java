package com.cjastram.mysql2firebase.client;

import com.cjastram.mysql2firebase.config.FirebaseConfig;
import com.cjastram.mysql2firebase.model.Parameter;
import com.cjastram.mysql2firebase.model.QueueItem;
import com.cjastram.mysql2firebase.model.SQLRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;

class FirebaseTestClient extends FirebaseClient {

    private static Logger logger = LoggerFactory.getLogger(FirebaseMySQLClient.class);

    private static String UID_TEST = "gFkm2CB0UjSh2J3qYzFtw95bQHa2";

    FirebaseTestClient() {
        super(FirebaseConfig.getInstance(UID_TEST));
    }

    public static void main(String[] args) {
        int rc = new FirebaseTestClient().run();
        logger.info(String.format("return code = %d", rc));
        System.exit(rc);
    }

    @Override
    void setupClient() {


    }

    @Override
    protected void runOnce() {

        QueueItem qi = new QueueItem();

        DatabaseReference dbRefToProcess = FirebaseDatabase.getInstance().getReference("user_data/" + UID_TEST + "/db_request").push();

        SQLRequest rq = new SQLRequest();
        rq.dbStatementName = "select_customers";
        rq.parameterList.add(Parameter.inParameter(1, Types.VARCHAR, "London"));
        rq.parameterList.add(Parameter.inParameter(1, Types.VARCHAR, "3"));

        dbRefToProcess.setValue(rq);

        DatabaseReference dbRefQueueItem = FirebaseDatabase.getInstance().getReference("queue").push();

        qi.dbPathToProcess = dbRefToProcess.getPath().toString();
        qi.isProcessed = false;

        SynchronizeHelper sh = new SynchronizeHelper();
        dbRefQueueItem.setValue(qi, sh);
        sh.waitForExecution();

        dbRefToProcess = FirebaseDatabase.getInstance().getReference("user_data/" + UID_TEST + "/db_request").push();
        rq = new SQLRequest();
        rq.dbStatementName = "plaing_sql";
        rq.parameterList.add(Parameter.inParameter(1, Types.VARCHAR, "select * from customers"));

        dbRefToProcess.setValue(rq);
        dbRefQueueItem = FirebaseDatabase.getInstance().getReference("queue").push();

        qi.dbPathToProcess = dbRefToProcess.getPath().toString();
        qi.isProcessed = false;

        sh = new SynchronizeHelper();
        dbRefQueueItem.setValue(qi, sh);
        sh.waitForExecution();
    }
}
