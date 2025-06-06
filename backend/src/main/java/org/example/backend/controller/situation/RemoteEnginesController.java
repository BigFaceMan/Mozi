package org.example.backend.controller.situation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.catalina.Engine;
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
import java.util.concurrent.locks.ReentrantLock;

@RestController
public class RemoteEnginesController {
    Map<String, EngineInfo> engineFreeNodes = new ConcurrentHashMap<>();
    Map<String, EngineInfo> engineUsingNodes = new ConcurrentHashMap<>();
    ReentrantLock engineLock = new ReentrantLock();
    @Autowired
    RestTemplate restTemplate;
    @Value("${enginePlatformUrl}")
    private String enginePlatformUrl;// 服务平台的URL
    @Scheduled(fixedRate =500) // 每40秒检查一次
    public void getEngineStatus() {
//        System.out.println("EngineUrl : " + enginePlatformUrl);
//        String url = enginePlatformUrl + "/xtserver/node/getAllNodeByType";
        String url = enginePlatformUrl + "/node/getAllNodeByType";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        try {
            String situationJson = restTemplate.getForObject(url, String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> remoteMap = objectMapper.readValue(situationJson, new TypeReference<Map<String, Object>>() {});
            Map<String, Object> data = ((remoteMap.get("data") == null ? new HashMap<>() : (Map) remoteMap.get("data")).get("mapDto") == null
                    ? new HashMap<>() : (Map) ((Map) remoteMap.get("data")).get("mapDto"))
                    .get("dkyCaiji") == null ? new HashMap<>() : (Map) ((Map) ((Map) remoteMap.get("data")).get("mapDto")).get("dkyCaiji");

            List<Map<String, Object>> engineList = (List<Map<String, Object>>) data.get("freeList");
            List<Map<String, Object>> engineUsingList = (List<Map<String, Object>>) data.get("usingList");

            engineLock.lock();
            // 先清空 engineNodes
            engineFreeNodes.clear();
            engineUsingNodes.clear();
            int index = 0;
            if (engineList != null) {
                for (Map<String, Object> engine : engineList) {
                    EngineInfo engineInfo = new EngineInfo();
                    engineInfo.setEngineId(engine.get("id").toString());
                    engineInfo.setIp(engine.get("ip").toString());
                    engineInfo.setPort(engine.get("port").toString());
                    engineInfo.setNodeName(engine.get("nodeName").toString());
//                    System.out.println("engineInfo : " + engineInfo);
                    // 将新的数据加入 Map
                    engineFreeNodes.put(Integer.toString(index), engineInfo);
                    index++;
                }

            }
//            System.out.println("get freeList zjj :　" + index);
            if (engineUsingList != null) {
                index = 0;
                for (Map<String, Object> engine : engineUsingList) {
                    EngineInfo engineInfo = new EngineInfo();
                    engineInfo.setEngineId(engine.get("id").toString());
                    engineInfo.setIp(engine.get("ip").toString());
                    engineInfo.setPort(engine.get("port").toString());
                    engineInfo.setNodeName(engine.get("nodeName").toString());
                    // 将新的数据加入 Map
                    engineUsingNodes.put(Integer.toString(index), engineInfo);
                    index ++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            engineLock.unlock();
        }
    }
    public List<EngineInfo> getFreeEngineList() {
        engineLock.lock();
        List<EngineInfo> respList = new ArrayList<>(engineFreeNodes.values());
        engineLock.unlock();
        return respList;
    }
    @GetMapping("/engine/getAll")
    public Map<String, Object> getEngineInfo() {
        engineLock.lock();
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("msg", "得到引擎节点");
        response.put("freeList", new ArrayList<>(engineFreeNodes.values()));
        response.put("usingList", new ArrayList<>(engineUsingNodes.values()));
        engineLock.unlock();
        return response;
    }
}
