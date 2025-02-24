package com.games.games.service.impl;

import com.games.games.service.trainService;
import org.springframework.util.MultiValueMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


import java.lang.reflect.Field;

public class trainServiceImpl implements trainService {

    // 用于存储进程的映射
    private final ConcurrentHashMap<String, Process> processMap = new ConcurrentHashMap<>();

    private static String getPythonProcessId(String processId) throws IOException {
//        System.out.println("processId " + processId);
        String pid = null;
        String line;
        // 使用 wmic 获取进程命令行信息
        ProcessBuilder builder = new ProcessBuilder("wmic", "process", "where", "\"commandline like 'python%'\"", "get", "ProcessId,CommandLine");
        Process wmicProcess = builder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(wmicProcess.getInputStream()))) {
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                // 检查是否包含传递的 processId
                if (line.contains(processId)) {
                    System.out.println(line);
                    // 处理 wmic 输出，获取 PID
                    String[] columns = line.split("\\s+");
                    if (columns.length > 1) {
                        pid = columns[columns.length-1]; // 获取 ProcessId
                        break;
                    }
                }
            }
        }
        return pid;
    }
    @Override
    public Map<String, String> addTrain(MultiValueMap<String, String> data) {
        Map<String, String> response = new HashMap<>();
        try {
            // 生成唯一的进程ID
            String processId = UUID.randomUUID().toString();

            // 构造Python脚本命令和参数
            String[] command = {
                    "cmd.exe", "/c", // Windows 下需要使用 cmd.exe
                    "conda activate ssp &&python D:\\Vscode\\games\\src\\main\\python\\train.py --process_id " + processId + " --tensorboardpath  D:\\Vscode\\games\\src\\main\\python\\logs"
            };
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true); // 合并错误输出到标准输出

            // 启动进程
            Process process = processBuilder.start();
            // 将进程存储到map中
            processMap.put(processId, process);
            response.put("status", "success");
            response.put("processId", processId);
        } catch (IOException e) {
            response.put("status", "error");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    @Override
    public Map<String, String> stopTrain(MultiValueMap<String, String> data) throws IOException {
        Map<String, String> response = new HashMap<>();

        String processId = data.getFirst("processId");
        if (processId == null || !processMap.containsKey(processId)) {
            response.put("status", "error");
            response.put("message", "Invalid processId");
            return response;
        }

        // 获取存储的进程
        Process process = processMap.get(processId);
        String processPid = getPythonProcessId(processId);

        // 如果找不到 Python 进程的 PID，返回错误
        if (processPid == null) {
            response.put("status", "error");
            response.put("message", "Could not find Python process");
            return response;
        }

        // 在 Windows 系统上终止整个进程树
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            new ProcessBuilder("taskkill", "/F", "/T", "/PID", processPid).start();
        } else {
            // 在类 Unix 系统上终止整个进程树
            process.destroy(); // 尝试优雅终止
        }
        response.put("status", "success");
        return response;
    }
}
