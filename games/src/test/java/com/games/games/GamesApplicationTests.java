package com.games.games;

import com.games.games.service.impl.TrainServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.LinkedMultiValueMap;

import java.io.IOException;
import java.util.Map;

@SpringBootTest
class GamesApplicationTests {
    @Autowired
    TrainServiceImpl trainService;
    @Test
    void testString() throws IOException {
        Map<String, String> Response = trainService.addTrain(new LinkedMultiValueMap<>());

        System.out.println("ProcessId : " + Response.get("processId"));

        LinkedMultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("processId", Response.get("processId"));
        Map<String, String> ResponseKill = trainService.stopTrain(data);
        System.out.println(ResponseKill.get("status"));
        System.out.println(ResponseKill.get("message"));

    }
}
