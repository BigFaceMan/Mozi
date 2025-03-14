package org.example.backend.controller.situation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backend.mapper.ExamplesMapper;
import org.example.backend.mapper.SceneEntityMapper;
import org.example.backend.pojo.Examples;
import org.example.backend.pojo.SceneEntity;
import org.example.backend.pojo.User;
import org.example.backend.service.impl.utils.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
public class RemoteSituationController {
    @Autowired
    private ExamplesMapper examplesMapper;
    @Autowired
    private SceneEntityMapper sceneEntityMapper;
    @Autowired
    private RestTemplate restTemplate;
//    Fake
    private String url0 = "http://127.0.0.1:4001";
    private String url1 = "http://127.0.0.1:4001";
    private String url2 = "http://127.0.0.1:4001";
    private String url3 = "http://127.0.0.1:4001";
    private String url4 = "http://127.0.0.1:4001";
//    private String url1 = "http://192.1.116.100:7210";
//    private String url2 = "http://192.1.116.100:8082";
//    private String url3 = "http://192.1.116.100:8081";
//    private String url4 = "http://192.1.116.100:8002";
    @PostMapping("/remote/getSituations/")
    public Map<String, Object> getSituatioes(@RequestParam Map<String, String> data) {
//        True
//        String url = "http://192.1.116.100:7210/wwe/task/page";
//        Fake
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
                System.out.println("request Situation Page : " + Integer.toString(i));
                String urli = url1 + "/wwe/task/page?pageIndex=" + Integer.toString(i + 1);
                String situationIJson = restTemplate.getForObject(urli, String.class);
                Map<String, Object> mapI = objectMapper.readValue(situationIJson, new TypeReference<Map<String, Object>>() {});
                Map<String, Object> dataIMap = (Map<String, Object>) mapI.get("data");
                List<Object> recordsI = (List<Object>) dataIMap.get("records");
                records.addAll(recordsI);
            }

            Map<String, Object> resMap = new HashMap<>();
            resMap.put("code", 200);
            resMap.put("success", true);
            resMap.put("message", "success");
            resMap.put("data", records);
            return resMap;
        } catch (Exception e) {
            // 如果请求失败，返回自定义数据
            e.printStackTrace();
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

    @PostMapping("/remote/saveRExample/")
    public Map<String, Object> saveRExample(@RequestParam Map<String, String> data) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 解析前端传来的参数
            String situationId = data.get("situationId");
            String solutionId = data.get("solutionId");
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
            example.setCreatetime(new Date()); // 设置当前时间


            UsernamePasswordAuthenticationToken authentication =
                    (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

            UserDetailsImpl loginUser = (UserDetailsImpl) authentication.getPrincipal();
            User user = loginUser.getUser();
            example.setUid(user.getId());

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
                    String groupId = entity.get("groupId");
                    sceneEntity.setEntityid(entityId);
                    sceneEntity.setGroupid(groupId);
                    sceneEntity.setSceneid(generatedId);
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
        Map<String, Object> response = new HashMap<>();
        try {
            List<Examples> examplesList = examplesMapper.selectList(null); // 查询所有实例
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
