package com.games.games.consumer;

import org.springframework.beans.factory.annotation.Value;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.hardware.NetworkIF;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;
import java.text.SimpleDateFormat;

@Service
public class SignUpdateGame {
    private String servicePlatformUrl = "http://127.0.0.1:3000"; // 服务平台的URL
    @Value("${server.port}")
    private int gamePort; // 将端口号注入到变量中
    @Value("${server.ip}")
    private String gameIp; // 将IP地址注入到变量中
    private final RestTemplate restTemplate;

    public SignUpdateGame(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Scheduled(fixedRate = 60 * 60 * 1000) // 每60分钟执行一次
    public void sendResourceInfo() {
        ResourceInfo resourceInfo = getResourceInfo();
        String url = servicePlatformUrl + "/games/sign";
        restTemplate.postForObject(url, resourceInfo, String.class);
    }

    public ResourceInfo getResourceInfo() {
        SystemInfo systemInfo = new SystemInfo();
        GlobalMemory memory = systemInfo.getHardware().getMemory();

        ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.setIp(gameIp);
        resourceInfo.setPort(gamePort);
        resourceInfo.setCpuUsage(getCpuUsage());
        resourceInfo.setGpuUsage(getGpuUsage());
        resourceInfo.setMemorySize(memory.getTotal() / (1024.0 * 1024 * 1024)); // 转换为GB
        resourceInfo.setGpuMemorySize(getGpuMemorySizeInGB());
        resourceInfo.setMemoryUsage(getMemoryUsage());
        resourceInfo.setGpuMemoryUsage(getGpuMemoryUsage());
        resourceInfo.setNetworkUsage(getNetworkUsage());
        resourceInfo.setUpdateTime(getCurrentTime());
        return resourceInfo;
    }

    private double getCpuUsage() {
        SystemInfo systemInfo = new SystemInfo();
        return systemInfo.getHardware().getProcessor().getSystemLoadAverage(3)[0] * 100;
    }

    private double getMemoryUsage() {
        SystemInfo systemInfo = new SystemInfo();
        GlobalMemory memory = systemInfo.getHardware().getMemory();
        return (double) (memory.getTotal() - memory.getAvailable()) / memory.getTotal() * 100;
    }

    private double getGpuUsage() {
        try {
            Process process = new ProcessBuilder("nvidia-smi", "--query-gpu=utilization.gpu", "--format=csv,noheader,nounits").start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null) {
                    return Double.parseDouble(line.trim());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private double getGpuMemoryUsage() {
        try {
            Process process = new ProcessBuilder("nvidia-smi", "--query-gpu=memory.used,memory.total", "--format=csv,noheader,nounits").start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null) {
                    String[] parts = line.split(",");
                    return Double.parseDouble(parts[0].trim()) / Double.parseDouble(parts[1].trim()) * 100;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private double getGpuMemorySizeInGB() {
        try {
            Process process = new ProcessBuilder("nvidia-smi", "--query-gpu=memory.total", "--format=csv,noheader,nounits").start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null) {
                    return Double.parseDouble(line.trim()) / 1024; // 转换为GB
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private double getNetworkUsage() {
        SystemInfo systemInfo = new SystemInfo();
        NetworkIF[] networkIFs = systemInfo.getHardware().getNetworkIFs().toArray(new NetworkIF[0]);
        long totalBytes = 0;
        for (NetworkIF networkIF : networkIFs) {
            totalBytes += networkIF.getBytesRecv() + networkIF.getBytesSent();
        }
        return totalBytes / (1024.0 * 1024); // 转换为MB
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
    }
}
