package org.example.backend.controller.situation;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backend.mapper.ExamplesMapper;
import org.example.backend.mapper.SceneEntityMapper;
import org.example.backend.mapper.UserMapper;
import org.example.backend.pojo.Examples;
import org.example.backend.pojo.SceneEntity;
import org.example.backend.pojo.User;
import org.example.backend.service.impl.utils.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

@RestController
public class RemoteSituationController {
    @Autowired
    private ExamplesMapper examplesMapper;
    @Autowired
    private SceneEntityMapper sceneEntityMapper;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private UserMapper userMapper;
//    Fake
    @Value("${url0}")
    private String url0;
    @Value("${url1}")
    private String url1;
    @Value("${url2}")
    private String url2;
    @Value("${url3}")
    private String url3;
    @Value("${url4}")
    private String url4;
    @Value("${url5}")
    private String url5;
//    private String url1 = "http://192.1.116.100:7210";
//    private String url2 = "http://192.1.116.100:8082";
//    private String url3 = "http://192.1.116.100:8081";
//    private String url4 = "http://192.1.116.100:8002";
//    private String url5 = "http://127.0.0.1:4001";

    private Map<String, List<Object>> situationCache = new HashMap<>();
    private ReentrantLock situationLock = new ReentrantLock();
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

    @PostMapping("/remote/getExampleIndicator/")
    public Map<String, Object> getEvalIndicator(@RequestParam String exampleId) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("msg", "成功得到指标");
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
                // 将返回的结果放入 response
                response.put("data", leafData);
            } catch (Exception e) {
                e.printStackTrace();
                response.put("code", 500);
                response.put("msg", "请求失败：" + e.getMessage());
            }
        } catch (Exception e) {
            // 如果请求失败，返回自定义数据
            e.printStackTrace();
            HashMap<String, Object> map = new HashMap<>();
            map.put("code", 200);
            map.put("success", true);
            map.put("message", "indicator error");
        }

        return response;
    }
    @Scheduled(fixedRate = 40000) // 每40秒检查一次
    public void getSituationsScheduled() {
        String cacheKey = "situationRecords"; // 缓存数据的键
        String url = url1 + "/wwe/task/page?pageIndex=1";
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
            List<Object> records = (List<Object>) dataMap.get("records");
            int total = (int) dataMap.get("pages");
            for (int i = 1; i < total; i++) {
//                System.out.println("request Situation Page : " + Integer.toString(i));
                String urli = url1 + "/wwe/task/page?pageIndex=" + Integer.toString(i + 1);
                String situationIJson = restTemplate.getForObject(urli, String.class);
                Map<String, Object> mapI = objectMapper.readValue(situationIJson, new TypeReference<Map<String, Object>>() {});
                Map<String, Object> dataIMap = (Map<String, Object>) mapI.get("data");
                List<Object> recordsI = (List<Object>) dataIMap.get("records");
                records.addAll(recordsI);
            }
            situationLock.lock();
            situationCache.put(cacheKey, records);
            situationLock.unlock();
        } catch (Exception e) {
            // 如果请求失败，返回自定义数据
            e.printStackTrace();
        }
    }

    @PostMapping("/remote/getSituations/")
    public Map<String, Object> getSituatioes(@RequestParam Map<String, String> data) {
//        True
//        String url = "http://192.1.116.100:7210/wwe/task/page";
//        Fake
        String cacheKey = "situationRecords"; // 缓存数据的键
        situationLock.lock();
        List<Object> recordsCache = situationCache.get(cacheKey); // 尝试从缓存中获取数据
        situationLock.unlock();

        if (recordsCache != null) {
            // 如果缓存中有数据，直接返回缓存的数据
            Map<String, Object> resMap = new HashMap<>();
            resMap.put("code", 200);
            resMap.put("success", true);
            resMap.put("message", "success");
            resMap.put("data", recordsCache);
            return resMap;
        } else {
            HashMap<String, Object> map = new HashMap<>();
            map.put("code", 200);
            map.put("success", true);
            map.put("message", "success");
            List<Object> dataList = new ArrayList<>();
            for (int i = 0; i < 30; i++) {
                HashMap<String, Object> dataMap = new HashMap<>();
                dataMap.put("ver", 38);
                dataMap.put("task_message", "真正的网电对抗");
                dataMap.put("utime", "2025-03-08T06:39:03.000+00:00");
                dataMap.put("ctime", "2025-03-08T06:39:03.000+00:00");
                dataMap.put("taskName", "西工大中部场景其他");
                dataMap.put("run_state", 1);
                dataMap.put("id", "111111111" + Integer.toString(i));
                dataMap.put("status", 1);
                dataList.add(dataMap);
            }
            map.put("data", dataList);
            return map;
        }
    }

    @PostMapping("/remote/getSolutions/")
    public Map<String, Object> getPlans(@RequestParam Map<String, String> data) {
        String situationId = data.get("id");
        System.out.println("At getSolutions, situationId : " + situationId);
        try {
            String url = url1 + "/wwe/paticipantmissiondata/selectSeatByTaskId?taskId="+situationId;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> dataTrans = new LinkedMultiValueMap<>();
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(dataTrans, headers);
            String situationJson = restTemplate.getForObject(url, String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> remoteMap = new HashMap<>();
            try {
                remoteMap = objectMapper.readValue(situationJson, new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                e.printStackTrace();
            }
            Map<String, Object> resMap = new HashMap<>();
            resMap.put("code", 200);
            resMap.put("success", true);
            resMap.put("message", "success");
            resMap.put("data", remoteMap.get("data"));
            return resMap;
        } catch(Exception e) {
            e.printStackTrace();
            HashMap<String, Object> map = new HashMap<>();
            map.put("code", 200);
            map.put("success", true);
            map.put("message", "success");
            List<Object> dataList = new ArrayList<>();
            for (int i = 0; i < 30; i ++) {
                HashMap<String, Object> dataMap = new HashMap<>();
                dataMap.put("ver", 38);
                dataMap.put("task_message", "真正的网电对抗");
                dataMap.put("utime", "2025-03-08T06:39:03.000+00:00");
                dataMap.put("ctime", "2025-03-08T06:39:03.000+00:00");
                dataMap.put("missionName", "网点作战" + Integer.toString(i));
                dataMap.put("run_state", 1);
                dataMap.put("id", "211111111" + Integer.toString(i));
                //  想定id
                int index = 0;
                if (i > 15) {
                    index = i;
                }
                dataMap.put("taskUID", "111111111" + Integer.toString(index));
                dataMap.put("status", 1);

                HashMap<String, Object> relationMap = new HashMap<>();
                relationMap.put("id", "4111111111");
                relationMap.put("utime", "2025-03-08T06:39:03.000+00:00");
                relationMap.put("ctime", "2025-03-08T06:39:03.000+00:00");
                relationMap.put("paticipantMissionId", "211111111" + Integer.toString(i));
                // 想定id
                relationMap.put("taskId", "111111111" + Integer.toString(index));
                relationMap.put("relCountry", "中国");
                HashMap<String, Object> relationMap1 = new HashMap<>();
                relationMap1.put("id", "4111111112");
                relationMap1.put("utime", "2025-03-08T06:39:03.000+00:00");
                relationMap1.put("ctime", "2025-03-08T06:39:03.000+00:00");
                relationMap1.put("paticipantMissionId", "211111111" + Integer.toString(i));
                relationMap1.put("taskId", "111111111" + Integer.toString(index));
                relationMap1.put("relCountry", "美国");
                List<Object> relationList = new ArrayList<>();
                relationList.add(relationMap);
                relationList.add(relationMap1);
                dataMap.put("relatemissionModels", relationList);
                dataList.add(dataMap);
            }
            List<Object> dataSelectList = new ArrayList<>();
            for (int i = 0; i < dataList.size(); i++) {
                Map<String, Object> item = (Map<String, Object>) dataList.get(i);  // 类型转换
                if (item.get("taskUID").equals(situationId)) {
                    dataSelectList.add(item);
                }
            }
            map.put("data", dataSelectList);
            return map;
        }


    }

    @PostMapping("/remote/getExamples/")
    public Map<String, Object> getExamples(@RequestParam Map<String, String> data) {
        String missionId = data.get("id");
        System.out.println("missionId : " + missionId);
        try {
            String url = url1 + "/wwe/deduce/shortcut/getByPMId?paticipantMissionUID="+missionId;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> dataTrans = new LinkedMultiValueMap<>();
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(dataTrans, headers);
            String situationJson = restTemplate.getForObject(url, String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> remoteMap = new HashMap<>();
            try {
                remoteMap = objectMapper.readValue(situationJson, new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                e.printStackTrace();
            }
            Map<String, Object> resMap = new HashMap<>();
            resMap.put("code", 200);
            resMap.put("success", true);
            resMap.put("message", "success");
            resMap.put("data", remoteMap.get("data"));
            return resMap;
        } catch(Exception e) {
            e.printStackTrace();
            HashMap<String, Object> map = new HashMap<>();
            map.put("code", 200);
            map.put("success", true);
            map.put("message", "success");
            List<Object> dataList = new ArrayList<>();
            for (int i = 0; i < 30; i ++) {
                HashMap<String, Object> dataMap = new HashMap<>();
                dataMap.put("ver", 38);
                dataMap.put("utime", "2025-03-08T06:39:03.000+00:00");
                dataMap.put("ctime", "2025-03-08T06:39:03.000+00:00");
                dataMap.put("name", "实例" + Integer.toString(i));
                dataMap.put("run_state", 1);
                int index = 0;
                if (i > 15) {
                    index = i;
                }
                dataMap.put("taskId", "111111111" + Integer.toString(index));
                dataMap.put("id", "311111111" + Integer.toString(i));
                dataMap.put("paticipantMissionUID", "211111111" + Integer.toString(index));
                dataMap.put("status", 2);
                dataList.add(dataMap);
            }

            List<Object> dataSelectList = new ArrayList<>();
            for (int i = 0; i < dataList.size(); i++) {
                Map<String, Object> item = (Map<String, Object>) dataList.get(i);  // 类型转换
                if (item.get("paticipantMissionUID").equals(missionId)) {
                    dataSelectList.add(item);
                }
            }
            map.put("data", dataSelectList);
            return map;
        }
    }
    @PostMapping("/remote/getEntity/")
    public Map<String, Object> getEntity(@RequestParam Map<String, String> data) {
        String id = data.get("situationId");
        String groupId = data.get("groupId");
        String equipment = "1";
        String country = data.get("country");
        System.out.println("At getEntity, situationId : " + id + " groupId : " + groupId + " country : " + country);
        try {
            String url = url4 + "/ygserver/experiment/scene/getScenarioByUnipGroupStatistics?taskId="+id+"&unipGroupId="+groupId+"&equipment="+equipment+"&country="+country;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> dataTrans = new LinkedMultiValueMap<>();
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(dataTrans, headers);
            String situationJson = restTemplate.getForObject(url, String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> remoteMap = new HashMap<>();
            try {
                remoteMap = objectMapper.readValue(situationJson, new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                e.printStackTrace();
            }
            Map<String, Object> resMap = new HashMap<>();
            resMap.put("code", 200);
            resMap.put("success", true);
            resMap.put("message", "success");
            resMap.put("data", remoteMap.get("data"));
            return resMap;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    @PostMapping("/remote/getGroup/")
    public Map<String, Object> getGroup(@RequestParam Map<String, String> data) {
        String id = data.get("situationId");
        String pid = data.get("pid");
        String country = data.get("country");
        System.out.println("At getGoup, situationId : " + id + " pid : " + pid + " country : " + country);
        try {
            String url = url4 + "/ygserver/experiment/scene/getUnipGroupTreeSync?id="+id+"&pid="+pid;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> dataTrans = new LinkedMultiValueMap<>();
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(dataTrans, headers);
            String situationJson = restTemplate.getForObject(url, String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> remoteMap = new HashMap<>();
            try {
                remoteMap = objectMapper.readValue(situationJson, new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                e.printStackTrace();
            }
// 处理 data
            List<Map<String, Object>> filteredData = new ArrayList<>();
            Object dataObj = remoteMap.get("data");
            int cnt = 0;
            if (dataObj instanceof List) {
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) dataObj;
                for (Map<String, Object> item : dataList) {
                    cnt ++;
                    System.out.println("country : " + item.get("country"));
                    if (country.equals(item.get("country"))) {
//                        item.put("children", new ArrayList<>()); // 清空 children
                        filteredData.add(item);
                    }
                }
            }
            System.out.println("This request get data : " + Integer.toString(cnt));
            Map<String, Object> resMap = new HashMap<>();
            resMap.put("code", 200);
            resMap.put("success", true);
            resMap.put("message", "success");
            resMap.put("data", filteredData);

            return resMap;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

//    国家
    @PostMapping("/remote/getCountry/")
    public Map<String, Object> getCountry(@RequestParam Map<String, String> data) {
        String situationId = data.get("situationId");
        System.out.println("At getCountry : situationId : " + situationId);
        try {
            String url = url4 + "/ygserver/experiment/scene/getUnipGroupTree?id="+situationId;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> dataTrans = new LinkedMultiValueMap<>();
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(dataTrans, headers);
            String situationJson = restTemplate.getForObject(url, String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> remoteMap = new HashMap<>();
            try {
                remoteMap = objectMapper.readValue(situationJson, new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                e.printStackTrace();
            }
            Map<String, Object> resMap = new HashMap<>();
            resMap.put("code", 200);
            resMap.put("success", true);
            resMap.put("message", "success");
            resMap.put("data", remoteMap.get("data"));
            return resMap;
        } catch(Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("code", "200");
            response.put("success", true);
            response.put("message", "成功");

            String[] countries = {"中国", "美国", "俄罗斯", "德国", "法国"};
            List<Map<String, Object>> dataList = new ArrayList<>();
            for (int i = 0; i < 5; i++) { // 生成两个示例数据
                String country;
                country = countries[i];
                Map<String, Object> uniformData = new HashMap<>();
                uniformData.put("country", country);
                uniformData.put("color", "rsb(0.47,255)");
                List<Map<String, Object>> unipGroupVOS = new ArrayList<>();
                Map<String, Object> uniform1 = new HashMap<>();
                uniform1.put("uniformName", "新建编辑");
                uniform1.put("country", country);
                uniform1.put("id", "511111111" + i);
                uniform1.put("unimodel", null);
                uniform1.put("childrenEdit", false);
                uniform1.put("children", null);
                unipGroupVOS.add(uniform1);

                Map<String, Object> uniform2 = new HashMap<>();
                uniform2.put("uniformName", "新建编辑");
                uniform2.put("country", country);
                uniform2.put("id", "511111111" + (i + 1));
                uniform2.put("unimodel", null);
                uniform2.put("childrenEdit", true);
                uniform2.put("children", null);
                unipGroupVOS.add(uniform2);

                uniformData.put("unipGroupVOS", unipGroupVOS);
                uniformData.put("scenarioModelVOS", null);
                uniformData.put("equipment", null);
                uniformData.put("expectyListV0", null);

                dataList.add(uniformData);
            }
            response.put("data", dataList);
            return response;
        }
    }

    @PostMapping("/remote/uploadRExample/")
    public Map<String, Object> uploadRExample(@RequestParam Map<String, String> data) {
        int projectId = Integer.parseInt(data.get("projectId"));
        UpdateWrapper<Examples> examplesUpdateWrapper = new UpdateWrapper<>();
        int isUpdate = examplesMapper.update(null, examplesUpdateWrapper.eq("id", projectId).set("visible", 2));
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        if (isUpdate == 1) {
            UsernamePasswordAuthenticationToken authentication =
                    (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl loginUser = (UserDetailsImpl) authentication.getPrincipal();
            User user = loginUser.getUser();
            UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
            userUpdateWrapper.eq("id", user.getId()).set("uploadcnt", user.getUploadcnt() + 1);  // 加 1
            boolean isUserUpdated = userMapper.update(null, userUpdateWrapper) > 0;  // 执行更新
            if (isUserUpdated) {
                System.out.println("成功更新模型上传状态，uploadcnt + 1");
                response.put("msg", "成功更新模型上传状态");
            } else {
                System.out.println("更新 uploadcnt 失败");
                response.put("msg", "更新 uploadcnt 失败");
            }
        } else {
            System.out.println("未成功更新模型上传状态");
            response.put("msg", "未成功更新模型上传状态");
        }
        return response;
    }
    @PostMapping("/remote/saveRExample/")
    public Map<String, Object> saveRExample(@RequestParam Map<String, String> data) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 解析前端传来的参数
            System.out.println("data : " + data);
            String situationId = data.get("situationId");
            String situationName = data.get("situationName");
            String solutionId = data.get("solutionId");
            String solutionName = data.get("solutionName");
            String exampleId = data.get("exampleId");
            String exampleName = data.get("exampleName");
            String projectName = data.get("projectName");
            String selectCountry = data.get("selectCountry");
            System.out.println("saveRExample Country : " + selectCountry);

            if (situationId == null || solutionId == null || exampleId == null) {
                response.put("code", 400);
                response.put("message", "缺少必要参数");
                return response;
            }

            // 构造实体对象
            Examples example = new Examples();
            example.setSituationid(situationId);
            example.setSolutionid(solutionId);
            example.setExampleid(exampleId);
            example.setExamplename(exampleName);
            example.setProjectname(projectName);
            example.setCountry(selectCountry);
            example.setSituationname(situationName);
            example.setSolutionname(solutionName);
            example.setVisible(0);
            example.setCreatetime(new Date()); // 设置当前时间


            UsernamePasswordAuthenticationToken authentication =
                    (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

            UserDetailsImpl loginUser = (UserDetailsImpl) authentication.getPrincipal();
            User user = loginUser.getUser();
            example.setUid(user.getId());
            System.out.println("example is : " + example);
            // 存入数据库
            boolean isSaved = examplesMapper.insert(example) > 0;

            if (isSaved) {
                int generatedId = example.getId();
                System.out.println("generatedId : " + generatedId);
                response.put("code", 200);
                response.put("message", "保存成功");
                Object entityListObject = data.get("selectedEntities");
                System.out.println("entityString : " + entityListObject);
                // 反序列化 JSON 字符串为 List<Map<String, String>>
                ObjectMapper objectMapper = new ObjectMapper();
                List<Map<String, String>> entityList = objectMapper.readValue((String) entityListObject, new TypeReference<List<Map<String, String>>>() {});
                System.out.println("entityList : " + entityList);
                for (int i = 0; i < entityList.size(); i ++) {
//                    System.out.println("entityList : " + entityList.get(i));
                    Map<String, String> entity = entityList.get(i);
                    System.out.println("entity : " + entity);
                    SceneEntity sceneEntity = new SceneEntity();
                    String entityId = entity.get("entityId");
                    String entityName = entity.get("entityName");
                    String groupId = entity.get("groupId");
                    String groupName = entity.get("groupName");
                    sceneEntity.setEntityid(entityId);
                    sceneEntity.setGroupid(groupId);
                    sceneEntity.setSceneid(generatedId);
                    sceneEntity.setEntityName(entityName);
                    sceneEntity.setGroupName(groupName);
                    sceneEntityMapper.insert(sceneEntity);
                }

            } else {
                response.put("code", 500);
                response.put("message", "保存失败");
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "系统异常：" + e.getMessage());
        }

        return response;
    }

    @PostMapping("/remote/getRExamples/")
    public Map<String, Object> getRExamples() {
        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl loginUser = (UserDetailsImpl) authentication.getPrincipal();
        User user = loginUser.getUser();
        System.out.println("in get RExamples, user : " + user);
        Map<String, Object> response = new HashMap<>();
        try {
            // 查询条件：uid 等于 user.getId() 或者 visible=1
            QueryWrapper<Examples> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uid", user.getId()).or().eq("visible", 1);

            List<Examples> examplesList = examplesMapper.selectList(queryWrapper); // 使用条件查询
            response.put("code", 200);
            response.put("success", true);
            response.put("message", "获取成功");
            response.put("data", examplesList);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "系统异常：" + e.getMessage());
        }
        return response;
    }

    @PostMapping("/remote/deleteRExample/")
    public Map<String, Object> deleteRExample(@RequestParam Map<String, String> data) {
        Map<String, Object> response = new HashMap<>();
        try {
            String exampleId = data.get("exampleId");
            if (exampleId == null) {
                response.put("code", 400);
                response.put("message", "缺少必要参数 exampleId");
                return response;
            }

            int rowsAffected = examplesMapper.deleteById(exampleId);
            if (rowsAffected > 0) {
                response.put("code", 200);
                response.put("message", "删除成功");
            } else {
                response.put("code", 404);
                response.put("message", "未找到对应的 RExample，删除失败");
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "系统异常：" + e.getMessage());
        }
        return response;
    }

    @GetMapping("/remote/getSceneEntitys/")
    public Map<String, Object> getSceneEntitys(@RequestParam Map<String, String> data) {
        String projectId = data.get("projectId");
        QueryWrapper<SceneEntity> queryWrapperSceneEntity = new QueryWrapper<>();
        List<SceneEntity> sceneEntityList = sceneEntityMapper.selectList(queryWrapperSceneEntity.eq("sceneid", projectId));
        HashMap<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("success", true);
        map.put("message", "success");
        map.put("data", sceneEntityList);
        return map;
    }
    @PostMapping("/remote/getEntitys/")
    public Map<String, Object> getEntitys(@RequestParam Map<String, String> data) {
        String exampleId = data.get("exampleId");
        HashMap<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("success", true);
        map.put("message", "success");
        List<Object> dataList = new ArrayList<>();
        for (int i = 0; i < 10; i ++) {
            HashMap<String, Object> dataMap = new HashMap<>();
            dataMap.put("id", "511111111" + Integer.toString(i));
            dataMap.put("name", "实体" + Integer.toString(i));
            if (i % 2 == 0) {
                dataMap.put("country", "美国");
            } else {
                dataMap.put("country", "中国");
            }
            dataMap.put("location", "121.514.31.910.0.000");
            dataMap.put("type", "具体指挥所");
            dataMap.put("modelName", "指挥所");
            dataMap.put("children", new ArrayList<>());
            dataMap.put("entity", new ArrayList<>());
            dataList.add(dataMap);
        }
        map.put("data", dataList);
        return map;
    }
}
