package com.cjastram.mysql2firebase.model;

public class QueueItem {

    public boolean isProcessed;

    public String dbPathToProcess;

    @Override
    public String toString() {
        return "QueueItem{" +
                "isProcessed=" + isProcessed +
                ", dbPathToProcess='" + dbPathToProcess + '\'' +
                '}';
    }
}
