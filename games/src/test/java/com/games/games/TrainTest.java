package com.games.games;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TrainTest {

    private final ConcurrentHashMap<String, Thread> processMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        TrainTest trainTest = new TrainTest();
        trainTest.testTrain();
    }
    public void testTrain() {
        String processId = UUID.randomUUID().toString();
        System.out.println("processId " + processId);
        Thread trainingThread = new Thread(() -> {
            try {
                String[] command = {
                        "cmd.exe", "/c", // Windows 下需要使用 cmd.exe
                        "conda activate ssp &&python D:\\research\\kaifa\\mozi\\games\\src\\main\\python\\train.py --process_id " + processId + " --tensorboardpath  D:\\Vscode\\games\\src\\main\\python\\logs"
                };

                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.redirectErrorStream(true); // 合并标准输出和错误输出
                Process pythonProcess = processBuilder.start();
                try {
                    // 等待 Python 进程结束
                    int exitCode = pythonProcess.waitFor(); // 阻塞直到进程结束
                    // 进程结束后执行数据库操作
                    if (exitCode == 0) {
                        // 成功执行 Python 进程
                        System.out.println("Python 进程成功结束，开始执行数据库操作...");
//                        updateDatabase();
                    } else {
                        // Python 进程异常结束
                        System.out.println("Python 进程异常结束，退出代码: " + exitCode);
                    }
                } catch (InterruptedException e) {
                    // 捕获线程中断异常
                    Thread.currentThread().interrupt(); // 恢复中断状态
                    System.out.println("进程等待被中断");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                processMap.remove(processId); // 从 map 中移除进程
            }
        });

        processMap.put(processId, trainingThread);

        trainingThread.start();
    }
}
