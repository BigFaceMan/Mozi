package com.games.games.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.games.games.pojo.ResourceInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.NetworkIF;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Map;

@Service
public class SignUpdateGame {

    @Value("${servicePlatformUrl}")
    private String servicePlatformUrl;// 服务平台的URL
    @Value("${server.port}")
    private int gamePort; // 将端口号注入到变量中
    @Value("${server.ip}")
    private String gameIp; // 将IP地址注入到变量中
    private final RestTemplate restTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public SignUpdateGame(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    @Scheduled(fixedRate = 100)
    public void sendResourceInfo() throws JsonProcessingException {
        ResourceInfo info = getResourceInfo();
        String member = info.getIp() + ":" + info.getPort();
        long now = System.currentTimeMillis();

        // 1. 更新ZSet心跳时间戳
        stringRedisTemplate.opsForZSet().add("game:active-nodes", member, now);

        // 2. 更新节点资源信息（hash）
        Map<String, String> nodeInfoMap = new HashMap<>();
        nodeInfoMap.put("cpuUsage", String.valueOf(info.getCpuUsage()));
        nodeInfoMap.put("gpuUsage", String.valueOf(info.getGpuUsage()));
        nodeInfoMap.put("memorySize", String.valueOf(info.getMemorySize()));
        nodeInfoMap.put("gpuMemorySize", String.valueOf(info.getGpuMemorySize()));
        nodeInfoMap.put("memoryUsage", String.valueOf(info.getMemoryUsage()));
        nodeInfoMap.put("gpuMemoryUsage", String.valueOf(info.getGpuMemoryUsage()));
        nodeInfoMap.put("networkUsage", String.valueOf(info.getNetworkUsage()));
        nodeInfoMap.put("updateTime", info.getUpdateTime());
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(info);
        stringRedisTemplate.opsForValue().set("game:node-info:" + member, json);
//        stringRedisTemplate.opsForHash().putAll("game:node-info:" + member, nodeInfoMap);
        // 3. 计算评分（越高越优）并更新 ZSet 排序
        double score = computeNodeScore(info);
        stringRedisTemplate.opsForZSet().add("game:node-score", member, score);
    }
    private double computeNodeScore(ResourceInfo info) {
        // 示例评分函数：显存大、CPU低、内存低更优
        return -info.getCpuUsage() * 0.4
                - info.getMemoryUsage() * 0.3
                + info.getGpuMemorySize() * 0.3;
    }


// 服务节点 中心化注册
//    @Scheduled(fixedRate = 1000) // 每 20s 执行一次
//    public void sendResourceInfo() {
//        ResourceInfo resourceInfo = getResourceInfo();
////        System.out.println("DI serviceUrl : " + servicePlatformUrl);
//
//        String url = servicePlatformUrl + "/games/sign/";
//        try {
//            restTemplate.postForObject(url, resourceInfo, String.class);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//    }

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
        CentralProcessor processor = systemInfo.getHardware().getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        try {
            // Pause briefly and get the new CPU ticks
            Thread.sleep(100);
            long[] ticks = processor.getSystemCpuLoadTicks();
            double cpuLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks);
//            System.out.println(cpuLoad * 100);
            return cpuLoad * 100;
        } catch (InterruptedException e) {
            System.out.println("exception");
            System.out.println(0);
        }
        return 0.0;
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
