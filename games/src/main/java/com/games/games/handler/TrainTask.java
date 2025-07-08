package com.games.games.handler;

import org.apache.tomcat.jni.Proc;

public class TrainTask implements Runnable{
    private final String processId;
    private final Runnable actualTask;

    public TrainTask(String processId, Runnable actualTask) {
        this.processId = processId;
        this.actualTask = actualTask;
    }

    public String getProcessId() {
        return processId;
    }

    @Override
    public void run() {
        actualTask.run();
    }
}
