package com.cjastram.mysql4firebase.client;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

class SynchronizeHelper implements DatabaseReference.CompletionListener {

    public SynchronizeHelper() {

    }


    @Override
    public void onComplete(DatabaseError error, DatabaseReference ref) {


        cdl.countDown();
    }

    public void waitForExecution() {

        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private CountDownLatch cdl = new CountDownLatch(1);
}


class GetObjectListFromFirebaseHelper<T> implements ValueEventListener {

    GetObjectListFromFirebaseHelper(Class<T> clazz) {
        this.result = new ArrayList<>();
        this.clazz = clazz;
        this.cdl = new CountDownLatch(1);
    }


    @Override
    public void onDataChange(DataSnapshot snapshot) {

        Iterable<DataSnapshot> outerIterable = snapshot.getChildren();

        for (DataSnapshot ds : outerIterable) {

            Object obj = ds.getValue(this.clazz);
            System.out.println(obj);

            result.add(ds.getValue(this.clazz));

        }

        cdl.countDown();
    }

    @Override
    public void onCancelled(DatabaseError error) {
        cdl.countDown();
    }

    public List<T> getResult() {


        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    private List<T> result;
    private CountDownLatch cdl;
    private Class<T> clazz;
}
