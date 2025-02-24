package com.games.games;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
//@SpringBootApplication 来标注一个主程序类 ， 说明这是一个Spring Boot应用
@SpringBootApplication
@EnableScheduling
public class GamesApplication {
    public static void main(String[] args) {
        //以为是启动了一个方法，没想到启动了一个服务
        SpringApplication.run(GamesApplication.class, args);
    }

}
