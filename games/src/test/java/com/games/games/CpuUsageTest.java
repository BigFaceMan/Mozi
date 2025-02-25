package com.games.games;
import com.sun.management.OperatingSystemMXBean;
import org.junit.jupiter.api.Test;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

import java.lang.management.ManagementFactory;

public class CpuUsageTest {

    @Test
    public void getCpuUsage() {
        SystemInfo systemInfo = new SystemInfo();
        CentralProcessor processor = systemInfo.getHardware().getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        try {
            // Pause briefly and get the new CPU ticks
            Thread.sleep(100);
            long[] ticks = processor.getSystemCpuLoadTicks();
            double cpuLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks);
            System.out.println(cpuLoad * 100);
        } catch (InterruptedException e) {
            System.out.println("exception");
            System.out.println(0);
        }
    }
}
