package com.games.games.consumer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.sql.DataSourceDefinition;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceInfo {
    private String ip;
    private int port;
    private double cpuUsage; // CPU使用情况
    private double gpuUsage; // GPU使用情况
    private double memorySize; // 内存大小
    private double gpuMemorySize; // 显存大小
    private double memoryUsage; // 内存使用情况
    private double gpuMemoryUsage; // 显存使用情况
    private double networkUsage; // 网络资源使用情况
    private String updateTime; // 更新时间

    // Getters and Setters
}
