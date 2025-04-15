package org.example.backend.service.impl.games;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.backend.consumer.WebSocketClient;
import org.example.backend.controller.situation.RemoteEnginesController;
import org.example.backend.mapper.ExamplesMapper;
import org.example.backend.mapper.SceneEntityMapper;
import org.example.backend.mapper.TrainMapper;
import org.example.backend.pojo.*;
import org.example.backend.service.games.GamesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.sql.Array;
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
    private TrainMapper trainMapper;
    @Autowired
    private SceneEntityMapper sceneEntityMapper;
    @Autowired
    private WebSocketClient webSocketClient;
    @Autowired
    private RemoteEnginesController remoteEnginesController;
    @Value("${enginePlatformUrl}")
    private String enginePlatformUrl;// 服务平台的URL

    Map<String, ResourceInfo> gameNodes = new ConcurrentHashMap<>();
//    Map<String, List<EngineInfo>> trainEngines = new ConcurrentHashMap<>();

    public void removeTrainEngines(String tUid) {
//        List<EngineInfo> engineInfos = trainEngines.get(tUid);
//        if (engineInfos != null) {
//            trainEngines.remove(tUid);
//        }
    }
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
    public Map<String, String> trainAcc(String trainIdStr, int speed) {
//        List<EngineInfo> engineInfos = trainEngines.get(tUid);
        int trainId = Integer.parseInt(trainIdStr);
        Train train = trainMapper.selectOne(new QueryWrapper<Train>().eq("id", trainId));
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> trainParams;
        try {
            trainParams = objectMapper.readValue(train.getParams(), new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<String> engineInfos = (List<String>) trainParams.get("envAdress");
        try {
            for (int i = 0; i < engineInfos.size(); i ++) {
                String wsUrli = "ws://" + engineInfos.get(i) + ":1234";
                webSocketClient.connect(wsUrli);
                HashMap<String, Object> map = new HashMap<>();
                map.put("Type", "SimCtrl");
                map.put("Msg", "Speed");
                map.put("Speed", speed);
                LinkedList<Object> list = new LinkedList<>();
                list.add(map);
                HashMap<String, Object> messageMap = new HashMap<>();
                messageMap.put("Cmds", list);
                String message = objectMapper.writeValueAsString(messageMap);
                webSocketClient.sendMessage(message);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Map<String, String> res = new HashMap<>();
        res.put("msg", "success");
        res.put("code", "200");
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
    public Map<String, Object> addTrain(MultiValueMap<String, String> data) throws JsonProcessingException {
//        System.out.println("data is : \n" + data);
        String ip = data.getFirst("ip");
        String port = data.getFirst("port");
        HashMap<String, Object> map = new HashMap<>();
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
        paramsMap.put("reload", 0);
        paramsMap.put("sceneEntitySelect", sceneEntityList);
        paramsMap.put("trainRangeL", 0);
        paramsMap.put("trainRangeR", data.getFirst("trainIters"));

        paramsMap.put("trainBatchSize", data.getFirst("trainBatchSize"));
        paramsMap.put("batchSize", data.getFirst("batchSize"));
        paramsMap.put("learningRate", data.getFirst("learningRate"));
        String selectMetricsStr = data.getFirst("selectedMetrics");

        // 使用 ObjectMapper 将字符串解析为 List<String>
//        start ws zhongjianjian
        ObjectMapper objectMapper = new ObjectMapper();
        String exampleId = examples.getExampleid();
        paramsMap.put("exampleId", exampleId);
        int needEngines = Integer.parseInt(data.getFirst("needEngines"));
        System.out.println("needEngines : " + needEngines);
        List<EngineInfo> freeEngineList = remoteEnginesController.getFreeEngineList();
        int usedEngines = Math.min(needEngines, freeEngineList.size());
        System.out.println("usedEngines : " + usedEngines);
        String startEngineUrl = enginePlatformUrl + "/node/startOneNode?cmd=" + exampleId + " 1234&id=0";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        ArrayList<String> useEngineIp = new ArrayList<>();
        List<EngineInfo> selectedEngines = new ArrayList<>();
        for (int i = 0; i < usedEngines; i ++) {
            String engineInfoJson = restTemplate.getForObject(startEngineUrl, String.class);
            ObjectMapper objectMapper1 = new ObjectMapper();
            Map<String, Object> engineInfoMap = objectMapper1.readValue(engineInfoJson, new TypeReference<Map<String, Object>>() {
            });
            String runIp = (String) engineInfoMap.get("data");
            useEngineIp.add(runIp);
            for (EngineInfo e : freeEngineList) {
                if (e.getIp().equals(runIp)) {
                    selectedEngines.add(e);
                    break;
                }
            }
        }
//        trainEngines.put(tUidStr, selectedEngines);
        paramsMap.put("envAdress", useEngineIp);
//        确实获取指标

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
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);


        // 将 data 封装到 HttpEntity 中
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(data, headers);

        // 发送 POST 请求，获取响应
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        Map<String, String> responseBody = response.getBody();
        HashMap<String, Object> resp = new HashMap<>();
        if (responseBody.get("code").equals("200")) {
            resp.put("code", "200");
            resp.put("msg", "训练开始，分配了" + usedEngines + "个节点");
        } else {
            resp.put("code", "500");
            resp.put("msg", "启动失败: " + responseBody.get("msg"));
        }
        // 返回响应结果
        return resp;
    }

    @Override
    public Map<String, String> killTrain(MultiValueMap<String, String> data) {
        int trainId = Integer.parseInt(data.getFirst("trainId"));
        Train train = trainMapper.selectOne(new QueryWrapper<Train>().eq("id", trainId));
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> trainParams;
        try {
            trainParams = objectMapper.readValue(train.getParams(), new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        List<String> engineInfos = (List<String>) trainParams.get("envAdress");
        if (engineInfos != null) {
            try {
                for (int i = 0; i < engineInfos.size(); i++) {
                    String wsUrli = "ws://" + engineInfos.get(i) + ":1234";
                    webSocketClient.connect(wsUrli);
                    HashMap<String, String> map = new HashMap<>();
                    map.put("Type", "SimCtrl");
                    map.put("Msg", "Stop");
                    LinkedList<Object> list = new LinkedList<>();
                    list.add(map);
                    HashMap<String, Object> messageMap = new HashMap<>();
                    messageMap.put("Cmds", list);
                    String message = objectMapper.writeValueAsString(messageMap);
                    webSocketClient.sendMessage(message);
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
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
        int trainId = Integer.parseInt(data.getFirst("trainId"));
        Train train = trainMapper.selectOne(new QueryWrapper<Train>().eq("id", trainId));
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> trainParams;
        try {
            trainParams = objectMapper.readValue(train.getParams(), new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<String> engineInfos = (List<String>) trainParams.get("envAdress");
        if (engineInfos != null) {
            try {
                for (int i = 0; i < engineInfos.size(); i ++) {
                    String wsUrli = "ws://" + engineInfos.get(i) + ":1234";
                    webSocketClient.connect(wsUrli);
                    HashMap<String, String> map = new HashMap<>();
                    map.put("Type", "SimCtrl");
                    map.put("Msg", "Stop");
                    LinkedList<Object> list = new LinkedList<>();
                    list.add(map);
                    HashMap<String, Object> messageMap = new HashMap<>();
                    messageMap.put("Cmds", list);
                    String message = objectMapper.writeValueAsString(messageMap);
                    webSocketClient.sendMessage(message);
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
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
    public Map<String, Object> continueAfFinTrain(MultiValueMap<String, String> data) {
//        System.out.println("data is : \n" + data);
        String ip = data.getFirst("ip");
        String port = data.getFirst("port");
        HashMap<String, Object> map = new HashMap<>();
        if (ip == null || port == null) {
            map.put("status", "error");
            map.put("msg", "ip or port is null");
            return map;
        }

        Train train = trainMapper.selectOne(new QueryWrapper<Train>().eq("id", Integer.parseInt(data.getFirst("trainId"))));
        if (train == null) {
            map.put("code", "500");
            map.put("msg", "没有找到该训练任务");
            return map;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> trainParams;
        try {
            trainParams = objectMapper.readValue(train.getParams(), new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        int runStep = train.getStep();
        trainParams.put("trainRangeL", runStep + 1);
        trainParams.put("trainRangeR", runStep + Integer.parseInt(data.getFirst("trainIters")));
        trainParams.put("reload", 1);

        trainParams.put("trainBatchSize", data.getFirst("trainBatchSize"));
        trainParams.put("batchSize", data.getFirst("batchSize"));
        trainParams.put("learningRate", data.getFirst("learningRate"));
        String selectMetricsStr = data.getFirst("selectedMetrics");
        List<String> selectMetrics = null;
        try {
            selectMetrics = objectMapper.readValue(selectMetricsStr, new TypeReference<List<String>>() {
            });
            System.out.println("selectedMetrics : " + selectMetrics);
            trainParams.put("selectedMetrics", selectMetrics);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 使用 ObjectMapper 将字符串解析为 List<String>
//        start ws zhongjianjian

        QueryWrapper<Examples> queryWrapperExamples = new QueryWrapper<>();
        Examples examples = examplesMapper.selectOne(queryWrapperExamples.eq("projectname", train.getScene()));
        String exampleId = examples.getExampleid();
        int needEngines = Integer.parseInt(data.getFirst("needEngines"));
        System.out.println("needEngines : " + needEngines);
        List<EngineInfo> freeEngineList = remoteEnginesController.getFreeEngineList();
        int usedEngines = Math.min(needEngines, freeEngineList.size());
        System.out.println("usedEngines : " + usedEngines);
        String startEngineUrl = enginePlatformUrl + "/node/startOneNode?cmd=" + exampleId + " 1234&id=0";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        ArrayList<String> useEngineIp = new ArrayList<>();
        List<EngineInfo> selectedEngines = new ArrayList<>();
        try {
            for (int i = 0; i < usedEngines; i ++) {
                String engineInfoJson = restTemplate.getForObject(startEngineUrl, String.class);
                ObjectMapper objectMapper1 = new ObjectMapper();
                Map<String, Object> engineInfoMap = objectMapper1.readValue(engineInfoJson, new TypeReference<Map<String, Object>>() {
                });
                String runIp = (String) engineInfoMap.get("data");
                useEngineIp.add(runIp);
                for (EngineInfo e : freeEngineList) {
                    if (e.getIp().equals(runIp)) {
                        selectedEngines.add(e);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
//        trainEngines.put(tUidStr, selectedEngines);
        trainParams.put("envAdress", useEngineIp);
//        确实获取指标

        try {
            String params = objectMapper.writeValueAsString(trainParams); // 转换为 JSON
            data.add("params", params);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        String url = "http://" + ip + ":" + port + "/train/continue/";
//        if (gameNodes.containsKey(gameKey)) {
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

        Train train = trainMapper.selectOne(new QueryWrapper<Train>().eq("id", Integer.parseInt(data.getFirst("trainId"))));

        if (train == null) {
            map.put("code", "500");
            map.put("msg", "没有找到该训练任务");
            return map;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> trainParams;
        try {
            trainParams = objectMapper.readValue(train.getParams(), new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        int runStep = train.getStep();
        trainParams.put("trainRangeL", runStep + 1);
        trainParams.put("reload", 1);

        List<String> envAdress = (List<String>) trainParams.get("envAdress");
//        start ws zhongjianjian
        String exampleId = trainParams.get("exampleId").toString();
        int needEngines = envAdress.size();
        System.out.println("needEngines : " + needEngines);
        List<EngineInfo> freeEngineList = remoteEnginesController.getFreeEngineList();
        int usedEngines = Math.min(needEngines, freeEngineList.size());
        System.out.println("usedEngines : " + usedEngines);
        String startEngineUrl = enginePlatformUrl + "/node/startOneNode?cmd=" + exampleId + " 1234&id=0";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        ArrayList<String> useEngineIp = new ArrayList<>();
        List<EngineInfo> selectedEngines = new ArrayList<>();
        try {
            for (int i = 0; i < usedEngines; i ++) {
                String engineInfoJson = restTemplate.getForObject(startEngineUrl, String.class);
                ObjectMapper objectMapper1 = new ObjectMapper();
                Map<String, Object> engineInfoMap = objectMapper1.readValue(engineInfoJson, new TypeReference<Map<String, Object>>() {
                });
                String runIp = (String) engineInfoMap.get("data");
                useEngineIp.add(runIp);
                for (EngineInfo e : freeEngineList) {
                    if (e.getIp().equals(runIp)) {
                        selectedEngines.add(e);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        trainParams.put("envAdress", useEngineIp);

        try {
            String params = objectMapper.writeValueAsString(trainParams); // 转换为 JSON
            data.add("params", params);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        String url = "http://" + ip + ":" + port + "/train/continue/";
//        if (gameNodes.containsKey(gameKey)) {
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

        Train train = trainMapper.selectOne(new QueryWrapper<Train>().eq("id", Integer.parseInt(data.getFirst("trainId"))));
        if (train == null) {
            map.put("code", "500");
            map.put("msg", "没有找到该训练任务");
            return map;
        }
        data.put("model", Collections.singletonList(train.getModel()));
        data.put("modelParams", Collections.singletonList(train.getModelparams()));
        data.put("pytorchVersion", Collections.singletonList(train.getPytorchversion()));
        data.put("uId", Collections.singletonList(String.valueOf(train.getUid())));

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> trainParams;
        try {
            trainParams = objectMapper.readValue(train.getParams(), new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        trainParams.put("trainRangeL", 0);
        trainParams.put("trainRangeR", 1);

//        List<String> envAdress = (List<String>) trainParams.get("envAdress");
//        start ws zhongjianjian
        String exampleId = trainParams.get("exampleId").toString();
        int needEngines = 1;
        System.out.println("needEngines : " + needEngines);
        List<EngineInfo> freeEngineList = remoteEnginesController.getFreeEngineList();
        int usedEngines = Math.min(needEngines, freeEngineList.size());
        System.out.println("usedEngines : " + usedEngines);
        String startEngineUrl = enginePlatformUrl + "/node/startOneNode?cmd=" + exampleId + " 1234&id=0";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        ArrayList<String> useEngineIp = new ArrayList<>();
        List<EngineInfo> selectedEngines = new ArrayList<>();
        try {
            for (int i = 0; i < usedEngines; i ++) {
                String engineInfoJson = restTemplate.getForObject(startEngineUrl, String.class);
                ObjectMapper objectMapper1 = new ObjectMapper();
                Map<String, Object> engineInfoMap = objectMapper1.readValue(engineInfoJson, new TypeReference<Map<String, Object>>() {
                });
                String runIp = (String) engineInfoMap.get("data");
                useEngineIp.add(runIp);
                for (EngineInfo e : freeEngineList) {
                    if (e.getIp().equals(runIp)) {
                        selectedEngines.add(e);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        trainParams.put("envAdress", useEngineIp);
        trainParams.put("reload", 1);
        try {
            String params = objectMapper.writeValueAsString(trainParams); // 转换为 JSON
            data.add("params", params);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String url = "http://" + ip + ":" + port + "/infer/add/";
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
        int trainId = Integer.parseInt(data.getFirst("trainId"));
        Train train = trainMapper.selectOne(new QueryWrapper<Train>().eq("id", trainId));
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> trainParams;
        try {
            trainParams = objectMapper.readValue(train.getParams(), new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        List<String> engineInfos = (List<String>) trainParams.get("envAdress");
        if (engineInfos != null) {
            try {
                for (int i = 0; i < engineInfos.size(); i++) {
                    String wsUrli = "ws://" + engineInfos.get(i) + ":1234";
                    webSocketClient.connect(wsUrli);
                    HashMap<String, String> map = new HashMap<>();
                    map.put("Type", "SimCtrl");
                    map.put("Msg", "Stop");
                    LinkedList<Object> list = new LinkedList<>();
                    list.add(map);
                    HashMap<String, Object> messageMap = new HashMap<>();
                    messageMap.put("Cmds", list);
                    String message = objectMapper.writeValueAsString(messageMap);
                    webSocketClient.sendMessage(message);
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
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
        int trainId = Integer.parseInt(data.getFirst("trainId"));
        Train train = trainMapper.selectOne(new QueryWrapper<Train>().eq("id", trainId));
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> trainParams;
        try {
            trainParams = objectMapper.readValue(train.getParams(), new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<String> engineInfos = (List<String>) trainParams.get("envAdress");
        if (engineInfos != null) {
            try {
                for (int i = 0; i < engineInfos.size(); i ++) {
                    String wsUrli = "ws://" + engineInfos.get(i) + ":1234";
                    webSocketClient.connect(wsUrli);
                    HashMap<String, String> map = new HashMap<>();
                    map.put("Type", "SimCtrl");
                    map.put("Msg", "Stop");
                    LinkedList<Object> list = new LinkedList<>();
                    list.add(map);
                    HashMap<String, Object> messageMap = new HashMap<>();
                    messageMap.put("Cmds", list);
                    String message = objectMapper.writeValueAsString(messageMap);
                    webSocketClient.sendMessage(message);
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

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
