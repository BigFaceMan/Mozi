package com.games.games;

import com.games.games.service.TrainService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

@SpringBootTest
public class TensorboardTest {
    @Autowired
    TrainService trainService;
    @Test
    public void test() throws InterruptedException {

        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("tensorboardpath", "D:\\research\\kaifa\\mozi\\games\\src\\main\\python\\logs\\CNNTrain_2025-02-26_13-37-13_墨子1");
        Map<String, String> result = trainService.addTensorboard(data);
        System.out.println(result);
        Thread.sleep(5000);
        data.add("port", result.get("port"));
        trainService.deleteTensorboard(data);
//        System.out.println("test");
    }
}
