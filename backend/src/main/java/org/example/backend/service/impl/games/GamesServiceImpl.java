package org.example.backend.service.impl.games;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
    Map<String, ResourceInfo> gameNodes = new ConcurrentHashMap<>();
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


//    public String getParamsJsonString(String scene) {
//        QueryWrapper<Examples> queryWrapperExamples = new QueryWrapper<>();
//        Examples examples = examplesMapper.selectOne(queryWrapperExamples.eq("projectname", scene));
//        int sceneId = examples.getId();
//        QueryWrapper<SceneEntity> queryWrapperSceneEntity = new QueryWrapper<>();
//        List<SceneEntity> sceneEntityList = sceneEntityMapper.selectList(queryWrapperSceneEntity.eq("sceneid", sceneId));
//        HashMap<String, Object> paramsMap = new HashMap<>();
//        paramsMap.put("sceneEntitySelect", sceneEntityList);
//        ObjectMapper objectMapper = new ObjectMapper();
//        try {
//            return objectMapper.writeValueAsString(paramsMap); // 转换为 JSON
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "{}"; // 出错时返回空 JSON
//        }
//    }


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
        ObjectMapper objectMapper = new ObjectMapper();
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
    public Map<String, String> addInfer(MultiValueMap<String, String> data) {
        String ip = data.getFirst("ip");
        String port = data.getFirst("port");
        HashMap<String, String> map = new HashMap<>();
        if (ip == null || port == null) {
            map.put("status", "error");
            map.put("msg", "ip or port is null");
            return map;
        }

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
