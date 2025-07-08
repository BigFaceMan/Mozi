package com.games.games.handler;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class KillPythonOnRejectPolicy implements RejectedExecutionHandler {
    public boolean terminateProcess(String pid) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            Process process;
            if (os.contains("win")) {
                process = new ProcessBuilder("taskkill", "/F", "/T", "/PID", pid).start();
            } else {
                process = new ProcessBuilder("kill", "-9", pid).start();
            }
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            System.out.println("终止进程失败: " + e.getMessage());
            return false;
        }
    }
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        if (r instanceof TrainTask) {
            String processId = ((TrainTask) r).getProcessId();
            terminateProcess(processId);
        }
        throw new RejectedExecutionException("任务被线程池拒绝");
    }
}
