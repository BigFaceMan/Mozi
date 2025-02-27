package org.example.backend.service.impl.games;

import org.example.backend.pojo.ResourceInfo;
import org.example.backend.service.games.GamesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GamesServiceImpl implements GamesService {
    @Autowired
    private RestTemplate restTemplate;
    Map<String, ResourceInfo> gameNodes = new HashMap<>();
    @Override
    public Map<String, String> signGame(ResourceInfo resourceInfo) {
        String gameKey = resourceInfo.getIp() + resourceInfo.getPort();
        gameNodes.put(gameKey, resourceInfo);
        System.out.println("sign Node : " + resourceInfo);
        Map<String, String> res = new HashMap<>();
        res.put("status", "success");
        return res;
    }

    @Override
    public List<ResourceInfo> getAllGameNode() {
        return new ArrayList<>(gameNodes.values());
    }

    @Override
    public Map<String, String> addTrain(MultiValueMap<String, String> data) {
//        System.out.println("data is : \n" + data);
        String ip = data.getFirst("ip");
        String port = data.getFirst("port");
        HashMap<String, String> map = new HashMap<>();
        if (ip == null || port == null) {
            map.put("status", "error");
            map.put("msg", "ip or port is null");
            return map;
        }

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
