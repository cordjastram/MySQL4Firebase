package com.cjastram.mysql2firebase.model;

public class QueueItem {

    public boolean isProcessed;

    public String dbRef;

    public String dbPathToProcess;

    @Override
    public String toString() {
        return "QueueItem{" +
                "isProcessed=" + isProcessed +
                ", dbRef='" + dbRef + '\'' +
                ", dbPathToProcess='" + dbPathToProcess + '\'' +
                '}';
    }
}
