package org.example.backend;

import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class Test {
    @org.junit.jupiter.api.Test
    void test() {
        String wsUrl = "ws://192.1.116.115:1234";
        String[] wsList = wsUrl.split(":");
        System.out.println(wsList);
        System.out.println(wsList[2]);;

    }

}
