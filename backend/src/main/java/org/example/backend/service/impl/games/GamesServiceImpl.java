package org.example.backend.service.impl.games;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.javassist.CodeConverter;
import org.example.backend.consumer.WebSocketClient;
import org.example.backend.controller.situation.RemoteEnginesController;
import org.example.backend.mapper.AlgParamsMapper;
import org.example.backend.mapper.ExamplesMapper;
import org.example.backend.mapper.SceneEntityMapper;
import org.example.backend.mapper.TrainMapper;
import org.example.backend.pojo.*;
import org.example.backend.service.games.GamesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.rmi.CORBA.ValueHandler;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class GamesServiceImpl implements GamesService {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ExamplesMapper examplesMapper;
    @Autowired
    private TrainMapper trainMapper;
    @Autowired
    private AlgParamsMapper algParamsMapper;
    @Autowired
    private SceneEntityMapper sceneEntityMapper;
    @Autowired
    private WebSocketClient webSocketClient;
    @Autowired
    private RemoteEnginesController remoteEnginesController;
    @Value("${url5}")
    private String url5;
    @Value("${enginePlatformUrl}")
    private String enginePlatformUrl;// 服务平台的URL
    @Value("${xtUrl}")
    private String xtUrl;// 服务平台的URL
    @Value("${engineQTime}")
    private Integer engineQTime;// 服务平台的URL
    Map<String, ResourceInfo> gameNodes = new ConcurrentHashMap<>();
    ReentrantLock gameLock = new ReentrantLock();
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    static Queue<Integer> freePorts = new LinkedList<>();
    {
        for (int i = 0; i < 8000; i ++) {
            freePorts.offer(1234 + i);
        }
    }
    ReentrantLock portQLock = new ReentrantLock();
    public void removeTrainEngines(String tUid) {

    }
    public List<Map<String, Object>> getLeafIndicator(Map<String, Object> node) {
        System.out.println(node.get("isLeaf").toString());
        if (node.get("isLeaf").toString().equals("1")) {
            List<Map<String, Object>> res = new ArrayList<>();
            res.add(node);
            return res;
        }
        List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");
        List<Map<String, Object>> res = new ArrayList<>();
        for (int i = 0; i < children.size(); i ++) {
            List<Map<String, Object>> leaf = getLeafIndicator(children.get(i));
            res.addAll(leaf);
        }
        return res;
    }

    List<Map<String, Object>> getEvalIndicator(String exampleId) {
        System.out.println("In getEvalIndicator !!!! " + exampleId);

        String url = url5 + "/assess/task/findTaskByConfigId?configId="+exampleId+"&eModel=3";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> dataTrans = new LinkedMultiValueMap<>();
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(dataTrans, headers);
            // 尝试发送请求并获取响应
            String situationJson = restTemplate.getForObject(url, String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> map = objectMapper.readValue(situationJson, new TypeReference<Map<String, Object>>() {});
            Map<String, Object> dataMap = (Map<String, Object>) map.get("data");
            String systemId = dataMap.get("systemId").toString();
            System.out.println("getExampleIndicator Get systemId : " + systemId);
            String url1 = url5 + "/assess/system/case/findSystemByIdAsTree";
            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("systemId", systemId);

            // 设置请求头
            HttpHeaders headersPost = new HttpHeaders();
            headersPost.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // 组装请求体
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headersPost);

            try {
                // 发送 POST 请求
                String situationJsonPost = restTemplate.postForObject(url1, requestEntity, String.class);

                // 解析 JSON
                Map<String, Object> resultMap = objectMapper.readValue(situationJsonPost, new TypeReference<Map<String, Object>>() {});
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) ((Map<String, Object>)((List<Map<String, Object>>) resultMap.get("data")).get(0)).get("children");

                List<Map<String, Object>> leafData = new ArrayList<>();
                for (int i = 0; i < dataList.size(); i ++) {
                    List<Map<String, Object>> leaf = getLeafIndicator(dataList.get(i));
                    leafData.addAll(leaf);
                }
                System.out.println("leaf data is : " + leafData);
                return leafData;
                // 将返回的结果放入 response
            } catch (Exception e) {

            }
        } catch (Exception e) {
            // 如果请求失败，返回自定义数据
            e.printStackTrace();
            List<Map<String, Object>> leafData = new ArrayList<>();
        }
        List<Map<String, Object>> leafData = new ArrayList<>();
        return  leafData;
    }

    public Map<String, Object> trainWs() {
        HashMap<String, Object> resp = new HashMap<>();
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
            resp.put("code", "200");
            resp.put("msg", "success");
        } catch (Exception e) {
            System.out.println("WebSocket 连接失败");
            e.printStackTrace();
            resp.put("code", "600");
            resp.put("msg", "fail");
        }
        System.out.println("离开 websocket Exception !!!");
        return resp;
    }

    @Override
    public Map<String, Object> stopEngine(List<String> zjjId, List<String> engineAddr) {

        System.out.println("want stop zzjId " + zjjId);
        System.out.println("want stop engineAddr " + engineAddr);
        ObjectMapper objectMapper = new ObjectMapper();
        StringBuilder sb = new StringBuilder();
        if (engineAddr != null) {
            for (int i = 0; i < engineAddr.size(); i++) {
                try {
                    String wsUrli = engineAddr.get(i);
                    System.out.println("websocket stop envAddress : " + wsUrli);
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
                    sb.append("stop : " + engineAddr.get(i) + " suc, ");
                } catch (Exception e) {
                    e.printStackTrace();
                    sb.append("stop : " + engineAddr.get(i) + " fail, ");
                } finally {
                    String wsUrli = engineAddr.get(i);
//                    ws://localhost:
                    String wsPort = wsUrli.split(":")[2];
                    putWsPort(Integer.parseInt(wsPort));
                }
            }
        }
        if (zjjId != null) {
            for (int i = 0; i < zjjId.size(); i++) {
                try {
                    String xtKillEngineURL = enginePlatformUrl + "/demo/stopEngineList?engineId=" + zjjId.get(i);
                    String runEngineJson = restTemplate.getForObject(xtKillEngineURL, String.class);
                    Map<String, Object> remoteMap = new HashMap<>();
                    String rCode = null;
                    try {
                        remoteMap = objectMapper.readValue(runEngineJson, new TypeReference<Map<String, Object>>() {
                        });
                        rCode = (String) remoteMap.get("code");
                        sb.append("stop : " + zjjId.get(i) + " suc, ");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    sb.append("stop : " + zjjId.get(i) + " fail, ");
                    e.printStackTrace();
                }
            }
        }
        HashMap<String, Object> resp = new HashMap<>();
        resp.put("code", "200");
        resp.put("msg", sb.toString());
        return resp;
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

            for (int i = 0; i < engineInfos.size(); i ++) {
                String wsUrli = engineInfos.get(i);
                try {
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
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }


        Map<String, String> res = new HashMap<>();
        res.put("msg", "success");
        res.put("code", "200");
        return res;
    }

    @Override
    public Map<String, String> signGame(ResourceInfo resourceInfo) {

        String gameKey = resourceInfo.getIp() + resourceInfo.getPort();
        gameLock.lock();
        gameNodes.put(gameKey, resourceInfo);
        gameLock.unlock();
//        System.out.println("sign Node : " + resourceInfo);
        Map<String, String> res = new HashMap<>();
        res.put("status", "success");
        return res;
    }
//    服务节点中心化 V
    // 定期检查节点是否超时
//    @Scheduled(fixedRate = 2000) // 每40秒检查一次
//    public void checkGameNode() {
//        long now = System.currentTimeMillis();
////        System.out.println("total gameNode : " + gameNodes.size());
//
//        // 使用 Iterator 遍历并安全删除
//        gameLock.lock();
//        Iterator<Map.Entry<String, ResourceInfo>> iterator = gameNodes.entrySet().iterator();
//        while (iterator.hasNext()) {
//            Map.Entry<String, ResourceInfo> entry = iterator.next();
//            String nodeId = entry.getKey();
//            ResourceInfo resourceInfo = entry.getValue();
//            try {
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
//                Date date = sdf.parse(resourceInfo.getUpdateTime());
//                long lastUpdateTime = date.getTime();
//                if (now - lastUpdateTime > 3000) { // 15秒未更新心跳，认为死亡
//                    System.out.println("节点 " + nodeId + " 可能已死亡");
//                    iterator.remove(); // 使用迭代器删除，避免并发修改异常
//                }
//            } catch (Exception e) {
//                System.err.println("解析时间失败: " + resourceInfo.getUpdateTime());
//            }
//        }
//        gameLock.unlock();
//    }

    /*
        redis 去中心化版本
    * */

    public String selectBestNode() {
        // 获取得分最高的前1个节点（ZREVRANGE）
        Set<String> best = stringRedisTemplate.opsForZSet()
                .reverseRange("game:node-score", 0, 0);

        if (best != null && !best.isEmpty()) {
            return best.iterator().next(); // 返回最优节点 IP:PORT
        }
        return null;
    }

    @Scheduled(fixedRate = 2000)
    public void checkGameNode() {
        String key = "leader-lock";
        String nodeId = "service-node-01"; // 当前服务节点标识
        Boolean isLeader = stringRedisTemplate.opsForValue().setIfAbsent(key, nodeId, Duration.ofSeconds(10));
        if (!Boolean.TRUE.equals(isLeader)) {
            return; // 不是Leader节点，不执行清理
        }

        long now = System.currentTimeMillis();
        long expireBefore = now - 10000;

        Set<String> expiredNodes = stringRedisTemplate.opsForZSet()
                .rangeByScore("game:active-nodes", 0, expireBefore);
//        System.out.println("最优节点为 : " + selectBestNode());

        if (expiredNodes != null && !expiredNodes.isEmpty()) {
            for (String node : expiredNodes) {
                stringRedisTemplate.opsForZSet().remove("game:active-nodes", node);
                stringRedisTemplate.delete("game:node-info:" + node);
                stringRedisTemplate.opsForZSet().remove("game:node-score", node);
            }
        }
    }

    /*
        偷个懒，直接从redis中把node拿到map中，对齐原来的写法（后续再改成实时获取版本
    * */
    @Scheduled(fixedRate = 2000)
    public void getGameNode() {
        long now = System.currentTimeMillis();
        long expireBefore = now - 10000;

        // 查询所有未过期节点
        Set<String> validNodes = stringRedisTemplate.opsForZSet()
                .rangeByScore("game:active-nodes", expireBefore + 1, now);

        if (validNodes == null || validNodes.isEmpty()) {
            gameNodes.clear(); // 没有活跃节点，清空 map
            return;
        }

        Map<String, ResourceInfo> tempMap = new HashMap<>();
        for (String nodeKey : validNodes) {
            String redisKey = "game:node-info:" + nodeKey;
            String json = stringRedisTemplate.opsForValue().get(redisKey);
            if (json != null) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    ResourceInfo info = objectMapper.readValue(json, ResourceInfo.class);
                    tempMap.put(nodeKey, info);
                } catch (Exception e) {
                    System.err.println("解析节点失败: " + nodeKey);
                    e.printStackTrace();
                }
            }
        }

        // 原子替换旧 map
        synchronized (gameNodes) {
            gameNodes.clear();
            gameNodes.putAll(tempMap);
        }

//        System.out.println("已刷新节点数量: " + gameNodes.size());
    }

    @Override
    public List<ResourceInfo> getAllGameNode() {
        gameLock.lock();
        List<ResourceInfo> resp = new ArrayList<>(gameNodes.values());
        gameLock.unlock();
        return resp;
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

        if (!data.getFirst("modelId").equals("-1")) {
            AlgParams nAlgParams = new AlgParams();
            nAlgParams.setModelId(Integer.parseInt(data.getFirst("modelId")));
            nAlgParams.setTrainIterations(Integer.parseInt(data.getFirst("trainIters")));
            nAlgParams.setBatchSize(Integer.parseInt(data.getFirst("batchSize")));
            nAlgParams.setTrainTime(Integer.parseInt(data.getFirst("trainTime")));
            nAlgParams.setLearningRate(Double.parseDouble(data.getFirst("learningRate")));
            algParamsMapper.update(nAlgParams, new QueryWrapper<AlgParams>().eq("model_id", nAlgParams.getModelId()));
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
        System.out.println("freeEngineList : " + freeEngineList.size());
        int usedEngines = Math.min(needEngines, freeEngineList.size());
        System.out.println("usedEngines : " + usedEngines);

        HashMap<String, Object> resp = new HashMap<>();

        if (usedEngines < 1) {
            resp.put("code", "400");
            resp.put("msg", "无可用引擎");
            return resp;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        ArrayList<String> useEngineIp = new ArrayList<>();
        List<String> selectedEnginesId = new ArrayList<>();
        for (int i = 0; i < usedEngines; i ++) {
            int wsPort = getWsPort();
            String startEngineUrl = enginePlatformUrl + "/node/startOneNode?cmd=" + exampleId + " " + Integer.toString(wsPort) + "&id=0";
            String engineInfoJson = restTemplate.getForObject(startEngineUrl, String.class);
            ObjectMapper objectMapper1 = new ObjectMapper();
            Map<String, Object> engineInfoMap = objectMapper1.readValue(engineInfoJson, new TypeReference<Map<String, Object>>() {
            });
//            ws://localhost:8765
            Map<String, Object> engineData = (Map<String, Object>)engineInfoMap.get("data");
            String runIp = (String) engineData.get("ip");

            String runWs = "ws://" + runIp + ":" + Integer.toString(wsPort);
//                        String runWs = "ws://" + "192.1.116.29" + ":8765";
            useEngineIp.add(runWs);
            selectedEnginesId.add((String)engineData.get("id"));

            if (engineQTime > 0) {
                try {
                    System.out.println(Thread.currentThread().getName() + " sleep " + engineQTime);
                    Thread.sleep(engineQTime * 1000);
                    System.out.println(Thread.currentThread().getName() + " wake up ");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
//            for (EngineInfo e : freeEngineList) {
//                if (e.getIp().equals(runIp)) {
//                    selectedEnginesId.add(e.getEngineId());
//                    break;
//                }
//            }
        }
//        trainEngines.put(tUidStr, selectedEngines); envAdress
        paramsMap.put("envAdress", useEngineIp);
        paramsMap.put("envsId", selectedEnginesId);

//        确实获取指标
        List<Map<String, Object>> IndecatorList = getEvalIndicator(exampleId);
        Map<Integer, Object> indicators = new HashMap<>();
        for (int i = 0; i <IndecatorList.size(); i ++) {
            Map<String, Object> obji = IndecatorList.get(i);
            indicators.put((Integer) obji.get("id"), obji.get("weight"));
        }
        paramsMap.put("indicators", indicators);

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
        System.out.println("add train : responseBody : " + responseBody);
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
        List<String> envsId = (List<String>) trainParams.get("envsId");
        Map<String, Object> stopData = stopEngine(envsId, engineInfos);
        System.out.println(stopData.get("msg"));
//        if (engineInfos != null) {
//            for (int i = 0; i < engineInfos.size(); i++) {
//                try {
//                    String wsUrli = engineInfos.get(i);
//                    System.out.println("websocket stop envAddress : " + wsUrli);
//                    webSocketClient.connect(wsUrli);
//                    HashMap<String, String> map = new HashMap<>();
//                    map.put("Type", "SimCtrl");
//                    map.put("Msg", "Stop");
//                    LinkedList<Object> list = new LinkedList<>();
//                    list.add(map);
//                    HashMap<String, Object> messageMap = new HashMap<>();
//                    messageMap.put("Cmds", list);
//                    String message = objectMapper.writeValueAsString(messageMap);
//                    webSocketClient.sendMessage(message);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//        }
//
//        System.out.println("离开stop 区域!!!!");

//        if (envsId != null) {
//            for (int i = 0; i < envsId.size(); i++) {
//                try {
//                    String xtKillEngineURL = enginePlatformUrl + "/demo/stopEngineList?engineId=" + envsId.get(i);
//                    String runEngineJson = restTemplate.getForObject(xtKillEngineURL, String.class);
//                    Map<String, Object> remoteMap = new HashMap<>();
//                    String rCode = null;
//                    try {
//                        remoteMap = objectMapper.readValue(runEngineJson, new TypeReference<Map<String, Object>>() {
//                        });
//                        rCode = (String) remoteMap.get("code");
//                        System.out.println("kilkl : " + envsId.get(i) + " " + rCode);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                } catch (Exception e) {
//                    System.out.println("kill train fail");
//                    e.printStackTrace();
//                }
//            }
//        }



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
        List<String> envsId = (List<String>) trainParams.get("envsId");
        Map<String, Object> stopData = stopEngine(envsId, engineInfos);
        System.out.println(stopData.get("msg"));

//        if (engineInfos != null) {
//            for (int i = 0; i < engineInfos.size(); i++) {
//                try {
//                    String wsUrli = engineInfos.get(i);
//                    System.out.println("websocket stop envAddress : " + wsUrli);
//                    webSocketClient.connect(wsUrli);
//                    HashMap<String, String> map = new HashMap<>();
//                    map.put("Type", "SimCtrl");
//                    map.put("Msg", "Stop");
//                    LinkedList<Object> list = new LinkedList<>();
//                    list.add(map);
//                    HashMap<String, Object> messageMap = new HashMap<>();
//                    messageMap.put("Cmds", list);
//                    String message = objectMapper.writeValueAsString(messageMap);
//                    webSocketClient.sendMessage(message);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//        }
//
//        System.out.println("离开stop 区域!!!!");

//        if (envsId != null) {
//            for (int i = 0; i < envsId.size(); i++) {
//                try {
//                    String xtKillEngineURL = enginePlatformUrl + "/demo/stopEngineList?engineId=" + envsId.get(i);
//                    String runEngineJson = restTemplate.getForObject(xtKillEngineURL, String.class);
//                    Map<String, Object> remoteMap = new HashMap<>();
//                    String rCode = null;
//                    try {
//                        remoteMap = objectMapper.readValue(runEngineJson, new TypeReference<Map<String, Object>>() {
//                        });
//                        rCode = (String) remoteMap.get("code");
//                        System.out.println("kilkl : " + envsId.get(i) + " " + rCode);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                } catch (Exception e) {
//                    System.out.println("kill train fail");
//                    e.printStackTrace();
//                }
//            }
//        }


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
    public Map<String, Object> continueAfFinTrain(MultiValueMap<String, String> data) throws JsonProcessingException {
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
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        List<String> useEngineIp = new ArrayList<>();
        List<String> selectedEnginesId = new ArrayList<>();
        for (int i = 0; i < usedEngines; i ++) {
            int wsPort = getWsPort();
            String startEngineUrl = enginePlatformUrl + "/node/startOneNode?cmd=" + exampleId + " " + Integer.toString(wsPort) + "&id=0";
            String engineInfoJson = restTemplate.getForObject(startEngineUrl, String.class);
            ObjectMapper objectMapper1 = new ObjectMapper();
            Map<String, Object> engineInfoMap = objectMapper1.readValue(engineInfoJson, new TypeReference<Map<String, Object>>() {
            });
//            ws://localhost:8765
            Map<String, Object> engineData = (Map<String, Object>)engineInfoMap.get("data");
            String runIp = (String) engineData.get("ip");

            String runWs = "ws://" + runIp + ":" + Integer.toString(wsPort);
//                        String runWs = "ws://" + "192.1.116.29" + ":8765";
            useEngineIp.add(runWs);
            selectedEnginesId.add((String)engineData.get("id"));
//            for (EngineInfo e : freeEngineList) {
//                if (e.getIp().equals(runIp)) {
//                    selectedEnginesId.add(e.getEngineId());
//                    break;
//                }
//            }
        }
//        trainEngines.put(tUidStr, selectedEngines);
        trainParams.put("envAdress", useEngineIp);
        trainParams.put("envsId", selectedEnginesId);
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
    public int getWsPort() {
        portQLock.lock();
        int wsPort = freePorts.poll();
        portQLock.unlock();
        System.out.println("get wsPort : " + wsPort);
        return wsPort;
    }

    @Override
    public void putWsPort(int wsPort) {
        portQLock.lock();
        freePorts.offer(wsPort);
        portQLock.unlock();
        System.out.println("put wsPort : " + wsPort);
    }

    @Override
    public Map<String, String> continueTrain(MultiValueMap<String, String> data) throws JsonProcessingException {
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

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        ArrayList<String> useEngineIp = new ArrayList<>();

        List<String> selectedEnginesId = new ArrayList<>();

        for (int i = 0; i < usedEngines; i ++) {
            int wsPort = getWsPort();
            String startEngineUrl = enginePlatformUrl + "/node/startOneNode?cmd=" + exampleId + " " + Integer.toString(wsPort) + "&id=0";
            String engineInfoJson = restTemplate.getForObject(startEngineUrl, String.class);
            ObjectMapper objectMapper1 = new ObjectMapper();
            Map<String, Object> engineInfoMap = objectMapper1.readValue(engineInfoJson, new TypeReference<Map<String, Object>>() {
            });
//            ws://localhost:8765
            Map<String, Object> engineData = (Map<String, Object>)engineInfoMap.get("data");
            String runIp = (String) engineData.get("ip");

            String runWs = "ws://" + runIp + ":" + Integer.toString(wsPort);
//                        String runWs = "ws://" + "192.1.116.29" + ":8765";
            useEngineIp.add(runWs);
            selectedEnginesId.add((String)engineData.get("id"));
//            for (EngineInfo e : freeEngineList) {
//                if (e.getIp().equals(runIp)) {
//                    selectedEnginesId.add(e.getEngineId());
//                    break;
//                }
//            }
        }

        trainParams.put("envAdress", useEngineIp);
        trainParams.put("envsId", selectedEnginesId);

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
        List<Integer> selectedEnginesId = new ArrayList<>();
        try {
            for (int i = 0; i < usedEngines; i ++) {
                String engineInfoJson = restTemplate.getForObject(startEngineUrl, String.class);
                ObjectMapper objectMapper1 = new ObjectMapper();
                Map<String, Object> engineInfoMap = objectMapper1.readValue(engineInfoJson, new TypeReference<Map<String, Object>>() {
                });
                String runIp = (String) engineInfoMap.get("data");
                String runWs = "ws://" + (String) engineInfoMap.get("data") + ":1234";

                useEngineIp.add(runWs);
                for (EngineInfo e : freeEngineList) {
                    if (e.getIp().equals(runIp)) {
                        selectedEnginesId.add(Integer.parseInt(e.getEngineId()));
                        break;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        trainParams.put("envAdress", useEngineIp);
        trainParams.put("envsId", selectedEnginesId);
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
//        List<String> engineInfos = (List<String>) trainParams.get("envAdress");
        List<Integer> envsId = (List<Integer>) trainParams.get("envsId");
        if (envsId != null) {
            try {
                for (int i = 0; i < envsId.size(); i++) {
                    String xtKillEngineURL = enginePlatformUrl + "/demo/stopEngineList?engineId="+envsId.get(i);
                    String runEngineJson = restTemplate.getForObject(xtKillEngineURL, String.class);
                    Map<String, Object> remoteMap = new HashMap<>();
                    String rCode = null;
                    try {
                        remoteMap = objectMapper.readValue(runEngineJson, new TypeReference<Map<String, Object>>() {});
                        rCode = (String) remoteMap.get("code");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            } catch (Exception e) {
                System.out.println("kill train fail");
                e.printStackTrace();
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
