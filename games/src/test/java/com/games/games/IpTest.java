package com.games.games;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IpTest {
    public static String getLocalIp() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostAddress(); // 获取本机的 IP 地址
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "Unknown IP";
        }
    }

    public static void main(String[] args) {
        System.out.println("Local IP: " + getLocalIp());
    }
}
