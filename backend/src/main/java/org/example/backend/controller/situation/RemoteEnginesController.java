package org.example.backend.controller.situation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backend.pojo.EngineInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class RemoteEnginesController {
    Map<String, EngineInfo> engineNodes = new ConcurrentHashMap<>();
    @Autowired
    RestTemplate restTemplate;
    @Value("${enginePlatformUrl}")
    private String enginePlatformUrl;// 服务平台的URL
    @Scheduled(fixedRate = 4000) // 每40秒检查一次
    public void getEngineStatus() {
//        System.out.println("EngineUrl : " + enginePlatformUrl);
        String url = enginePlatformUrl + "/xtserver/node/getAllNodeByType";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        try {
            String situationJson = restTemplate.getForObject(url, String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> remoteMap = objectMapper.readValue(situationJson, new TypeReference<Map<String, Object>>() {});
            Map<String, Object> data = ((remoteMap.get("data") == null ? new HashMap<>() : (Map) remoteMap.get("data")).get("mapDto") == null
                    ? new HashMap<>() : (Map) ((Map) remoteMap.get("data")).get("mapDto"))
                    .get("engine") == null ? new HashMap<>() : (Map) ((Map) ((Map) remoteMap.get("data")).get("mapDto")).get("engine");

            List<Map<String, Object>> engineList = (List<Map<String, Object>>) data.get("freeList");

            // 先清空 engineNodes
            engineNodes.clear();

            for (Map<String, Object> engine : engineList) {
                EngineInfo engineInfo = new EngineInfo();
                engineInfo.setEngineId((Integer) engine.get("engineId"));
                engineInfo.setIp(engine.get("ip").toString());
                engineInfo.setPort(engine.get("port").toString());
                engineInfo.setNodeName(engine.get("nodeName").toString());

                // 将新的数据加入 Map
                engineNodes.put(engine.get("nodeName").toString(), engineInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @GetMapping("/engine/getAll")
    public List<EngineInfo> getEngineInfo() {
        return new ArrayList<>(engineNodes.values());
    }
}
