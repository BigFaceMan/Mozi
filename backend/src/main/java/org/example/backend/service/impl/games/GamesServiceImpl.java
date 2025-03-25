package org.example.backend.service.impl.games;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.backend.consumer.WebSocketClient;
import org.example.backend.mapper.ExamplesMapper;
import org.example.backend.mapper.SceneEntityMapper;
import org.example.backend.pojo.Examples;
import org.example.backend.pojo.ResourceInfo;
import org.example.backend.pojo.SceneEntity;
import org.example.backend.service.games.GamesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GamesServiceImpl implements GamesService {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ExamplesMapper examplesMapper;
    @Autowired
    private SceneEntityMapper sceneEntityMapper;
    @Autowired
    private WebSocketClient webSocketClient;
    Map<String, ResourceInfo> gameNodes = new ConcurrentHashMap<>();
    public void trainWs() {
        try {
            webSocketClient.connect("ws://localhost:8765");
            HashMap<String, String> map = new HashMap<>();
            map.put("Type", "SimCtrl");
            map.put("Msg", "Stop");
            LinkedList<Object> list = new LinkedList<>();
            list.add(map);
            HashMap<String, Object> messageMap = new HashMap<>();
            messageMap.put("Cmds", list);
            ObjectMapper objectMapper = new ObjectMapper();
            String message = objectMapper.writeValueAsString(messageMap);
            webSocketClient.sendMessage(message);
        } catch (Exception e) {
            System.out.println("WebSocket 连接失败");
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, String> trainAcc(int speed) {
        try {
            webSocketClient.connect("ws://localhost:8765");
            HashMap<String, Object> map = new HashMap<>();
            map.put("Type", "SimCtrl");
            map.put("Msg", "Speed");
            map.put("Speed", speed);
            LinkedList<Object> list = new LinkedList<>();
            list.add(map);
            HashMap<String, Object> messageMap = new HashMap<>();
            messageMap.put("Cmds", list);
            ObjectMapper objectMapper = new ObjectMapper();
            String message = objectMapper.writeValueAsString(messageMap);
            webSocketClient.sendMessage(message);
        } catch (Exception e) {
            System.out.println("WebSocket 连接失败");
            e.printStackTrace();
            Map<String, String> res = new HashMap<>();
            res.put("msg", "fail");
        }
        Map<String, String> res = new HashMap<>();
        res.put("msg", "success");
        return res;
    }

    @Override
    public Map<String, String> signGame(ResourceInfo resourceInfo) {
        String gameKey = resourceInfo.getIp() + resourceInfo.getPort();
        gameNodes.put(gameKey, resourceInfo);
//        System.out.println("sign Node : " + resourceInfo);
        Map<String, String> res = new HashMap<>();
        res.put("status", "success");
        return res;
    }
    // 定期检查节点是否超时
    @Scheduled(fixedRate = 2000) // 每40秒检查一次
    public void checkGameNode() {
        long now = System.currentTimeMillis();
//        System.out.println("total gameNode : " + gameNodes.size());

        // 使用 Iterator 遍历并安全删除
        Iterator<Map.Entry<String, ResourceInfo>> iterator = gameNodes.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ResourceInfo> entry = iterator.next();
            String nodeId = entry.getKey();
            ResourceInfo resourceInfo = entry.getValue();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                Date date = sdf.parse(resourceInfo.getUpdateTime());
                long lastUpdateTime = date.getTime();
                if (now - lastUpdateTime > 3000) { // 15秒未更新心跳，认为死亡
                    System.out.println("节点 " + nodeId + " 可能已死亡");
                    iterator.remove(); // 使用迭代器删除，避免并发修改异常
                }
            } catch (Exception e) {
                System.err.println("解析时间失败: " + resourceInfo.getUpdateTime());
            }
        }
    }

    @Override
    public List<ResourceInfo> getAllGameNode() {
        return new ArrayList<>(gameNodes.values());
    }
    @Override
    public Map<String, String> addTrain(MultiValueMap<String, String> data) throws JsonProcessingException {
//        System.out.println("data is : \n" + data);
        String ip = data.getFirst("ip");
        String port = data.getFirst("port");
        HashMap<String, String> map = new HashMap<>();
        if (ip == null || port == null) {
            map.put("status", "error");
            map.put("msg", "ip or port is null");
            return map;
        }

        String scene = data.getFirst("scene");
        QueryWrapper<Examples> queryWrapperExamples = new QueryWrapper<>();
        Examples examples = examplesMapper.selectOne(queryWrapperExamples.eq("projectname", scene));
        int sceneId = examples.getId();
        QueryWrapper<SceneEntity> queryWrapperSceneEntity = new QueryWrapper<>();
        List<SceneEntity> sceneEntityList = sceneEntityMapper.selectList(queryWrapperSceneEntity.eq("sceneid", sceneId));
        HashMap<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("sceneEntitySelect", sceneEntityList);
        paramsMap.put("trainIters", data.getFirst("trainIters"));
        paramsMap.put("trainBatchSize", data.getFirst("trainBatchSize"));
        paramsMap.put("batchSize", data.getFirst("batchSize"));
        paramsMap.put("learningRate", data.getFirst("learningRate"));
        String selectMetricsStr = data.getFirst("selectedMetrics");
        // 使用 ObjectMapper 将字符串解析为 List<String>
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> selectMetrics = null;

        try {
            // 将 selectMetricsStr 转换为 List<String>
            selectMetrics = objectMapper.readValue(selectMetricsStr, new TypeReference<List<String>>() {
            });
            System.out.println("selectedMetrics : " + selectMetrics);
            paramsMap.put("selectedMetrics", selectMetrics);
        } catch (Exception e) {
            e.printStackTrace();
        }
        paramsMap.put("selectedMetrics", selectMetrics);
        paramsMap.put("trainEpochs", data.getFirst("trainEpochs"));
        paramsMap.put("trainTime", data.getFirst("trainTime"));
//        System.out.println("selectedMetrics : " + data.getFirst("selectedMetrics"));
//        selectedMetrics : ["精度","速度","稳定性", "资源消耗"]
//        ObjectMapper objectMapper = new ObjectMapper();
        String params = objectMapper.writeValueAsString(paramsMap); // 转换为 JSON
        data.add("params", params);

        System.out.println("addTrain params is : " + params);

        String url = "http://" + ip + ":" + port + "/train/add/";
//        if (gameNodes.containsKey(gameKey)) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);


        // 将 data 封装到 HttpEntity 中
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(data, headers);

        // 发送 POST 请求，获取响应
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        // 返回响应结果
        return response.getBody();
    }

    @Override
    public Map<String, String> killTrain(MultiValueMap<String, String> data) {
        try {
            webSocketClient.connect("ws://localhost:8765");
            HashMap<String, String> map = new HashMap<>();
            map.put("Type", "SimCtrl");
            map.put("Msg", "Stop");
            LinkedList<Object> list = new LinkedList<>();
            list.add(map);
            HashMap<String, Object> messageMap = new HashMap<>();
            messageMap.put("Cmds", list);
            ObjectMapper objectMapper = new ObjectMapper();
            String message = objectMapper.writeValueAsString(messageMap);
            webSocketClient.sendMessage(message);
        } catch (Exception e) {
            System.out.println("WebSocket 连接失败");
            e.printStackTrace();
        }

//        System.out.println("data is : \n" + data);
        String ip = data.getFirst("ip");
        String port = data.getFirst("port");
        HashMap<String, String> map = new HashMap<>();
        if (ip == null || port == null) {
            map.put("status", "error");
            map.put("msg", "ip or port is null");
            return map;
        }

        String url = "http://" + ip + ":" + port + "/train/kill/";
//        if (gameNodes.containsKey(gameKey)) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 将 data 封装到 HttpEntity 中
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(data, headers);

        // 发送 POST 请求，获取响应
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        // 返回响应结果
        return response.getBody();
    }

    @Override
    public Map<String, String> stopTrain(MultiValueMap<String, String> data) {
        try {
            webSocketClient.connect("ws://localhost:8765");
            HashMap<String, String> map = new HashMap<>();
            map.put("Type", "SimCtrl");
            map.put("Msg", "Stop");
            LinkedList<Object> list = new LinkedList<>();
            list.add(map);
            HashMap<String, Object> messageMap = new HashMap<>();
            messageMap.put("Cmds", list);
            ObjectMapper objectMapper = new ObjectMapper();
            String message = objectMapper.writeValueAsString(messageMap);
            webSocketClient.sendMessage(message);
        } catch (Exception e) {
            System.out.println("WebSocket 连接失败");
            e.printStackTrace();
        }
        String ip = data.getFirst("ip");
        String port = data.getFirst("port");
        HashMap<String, String> map = new HashMap<>();
        if (ip == null || port == null) {
            map.put("status", "error");
            map.put("msg", "ip or port is null");
            return map;
        }

        String url = "http://" + ip + ":" + port + "/train/stop/";
//        if (gameNodes.containsKey(gameKey)) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 将 data 封装到 HttpEntity 中
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(data, headers);

        // 发送 POST 请求，获取响应
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        // 返回响应结果
        return response.getBody();
    }

    @Override
    public Map<String, String> continueTrain(MultiValueMap<String, String> data) {
        String ip = data.getFirst("ip");
        String port = data.getFirst("port");
        HashMap<String, String> map = new HashMap<>();
        if (ip == null || port == null) {
            map.put("status", "error");
            map.put("msg", "ip or port is null");
            return map;
        }

        String url = "http://" + ip + ":" + port + "/train/continue/";
//        if (gameNodes.containsKey(gameKey)) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 将 data 封装到 HttpEntity 中
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(data, headers);

        // 发送 POST 请求，获取响应
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        // 返回响应结果
        return response.getBody();
    }

    @Override
    public Map<String, String> addInfer(MultiValueMap<String, String> data) throws JsonProcessingException {
        String ip = data.getFirst("ip");
        String port = data.getFirst("port");
        HashMap<String, String> map = new HashMap<>();
        if (ip == null || port == null) {
            map.put("status", "error");
            map.put("msg", "ip or port is null");
            return map;
        }

//        String scene = data.getFirst("scene");
//        QueryWrapper<Examples> queryWrapperExamples = new QueryWrapper<>();
//        Examples examples = examplesMapper.selectOne(queryWrapperExamples.eq("projectname", scene));
//        int sceneId = examples.getId();
//        QueryWrapper<SceneEntity> queryWrapperSceneEntity = new QueryWrapper<>();
//        List<SceneEntity> sceneEntityList = sceneEntityMapper.selectList(queryWrapperSceneEntity.eq("sceneid", sceneId));
        HashMap<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("trainIters", 100);
        ObjectMapper objectMapper = new ObjectMapper();
        String params = objectMapper.writeValueAsString(paramsMap); // 转换为 JSON
        data.add("params", params);

        String url = "http://" + ip + ":" + port + "/infer/add/";
//        if (gameNodes.containsKey(gameKey)) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 将 data 封装到 HttpEntity 中
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(data, headers);

        // 发送 POST 请求，获取响应
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        // 返回响应结果
        return response.getBody();
    }

    @Override
    public Map<String, String> killInfer(MultiValueMap<String, String> data) {
//        System.out.println("data is : \n" + data);
        String ip = data.getFirst("ip");
        String port = data.getFirst("port");
        HashMap<String, String> map = new HashMap<>();
        if (ip == null || port == null) {
            map.put("status", "error");
            map.put("msg", "ip or port is null");
            return map;
        }

        String url = "http://" + ip + ":" + port + "/infer/kill/";
//        if (gameNodes.containsKey(gameKey)) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 将 data 封装到 HttpEntity 中
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(data, headers);

        // 发送 POST 请求，获取响应
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        // 返回响应结果
        return response.getBody();
    }

    @Override
    public Map<String, String> stopInfer(MultiValueMap<String, String> data) {
        String ip = data.getFirst("ip");
        String port = data.getFirst("port");
        HashMap<String, String> map = new HashMap<>();
        if (ip == null || port == null) {
            map.put("status", "error");
            map.put("msg", "ip or port is null");
            return map;
        }

        String url = "http://" + ip + ":" + port + "/infer/stop/";
//        if (gameNodes.containsKey(gameKey)) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 将 data 封装到 HttpEntity 中
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(data, headers);

        // 发送 POST 请求，获取响应
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        // 返回响应结果
        return response.getBody();
    }

    @Override
    public Map<String, String> continueInfer(MultiValueMap<String, String> data) {
        String ip = data.getFirst("ip");
        String port = data.getFirst("port");
        HashMap<String, String> map = new HashMap<>();
        if (ip == null || port == null) {
            map.put("status", "error");
            map.put("msg", "ip or port is null");
            return map;
        }
        String url = "http://" + ip + ":" + port + "/infer/continue/";
//        if (gameNodes.containsKey(gameKey)) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 将 data 封装到 HttpEntity 中
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(data, headers);

        // 发送 POST 请求，获取响应
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        // 返回响应结果
        return response.getBody();
    }

    @Override
    public Map<String, String> addTensorboard(MultiValueMap<String, String> data) {
        String ip = data.getFirst("ip");
        String port = data.getFirst("port");
        HashMap<String, String> map = new HashMap<>();
        if (ip == null || port == null) {
            map.put("status", "error");
            map.put("msg", "ip or port is null");
            return map;
        }

        String url = "http://" + ip + ":" + port + "/train/addTensorboard/";
//        if (gameNodes.containsKey(gameKey)) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 将 data 封装到 HttpEntity 中
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(data, headers);

        // 发送 POST 请求，获取响应
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        // 返回响应结果
        return response.getBody();
    }

    @Override
    public Map<String, String> deleteTensorboard(MultiValueMap<String, String> data) {
        String ip = data.getFirst("ip");
        String port = data.getFirst("port");
        HashMap<String, String> map = new HashMap<>();
        if (ip == null || port == null) {
            map.put("status", "error");
            map.put("msg", "ip or port is null");
            return map;
        }

        String url = "http://" + ip + ":" + port + "/train/deleteTensorboard/";
//        if (gameNodes.containsKey(gameKey)) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 将 data 封装到 HttpEntity 中
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(data, headers);

        // 发送 POST 请求，获取响应
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        // 返回响应结果
        return response.getBody();
    }
}
