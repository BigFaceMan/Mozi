package org.example.backend.controller.situation;

import org.example.backend.mapper.ExamplesMapper;
import org.example.backend.pojo.Examples;
import org.example.backend.pojo.User;
import org.example.backend.service.impl.utils.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class RemoteSituationController {
    @Autowired
    private ExamplesMapper examplesMapper;
    @PostMapping("/remote/getSituations/")
    public Map<String, Object> getSituations(@RequestParam Map<String, String> data) {
        /*
        code:
        success
        message
        data[list]
         */
        HashMap<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("success", true);
        map.put("message", "success");
        List<Object> dataList = new ArrayList<>();
        for (int i = 0; i < 5; i ++) {
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

    @PostMapping("/remote/getSolutions/")
    public Map<String, Object> getPlans(@RequestParam Map<String, String> data) {
        String situationId = data.get("id");
        HashMap<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("success", true);
        map.put("message", "success");
        List<Object> dataList = new ArrayList<>();
        for (int i = 0; i < 5; i ++) {
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
            if (i > 2) {
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

    @PostMapping("/remote/getExamples/")
    public Map<String, Object> getExamples(@RequestParam Map<String, String> data) {
        String missionId = data.get("id");
        HashMap<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("success", true);
        map.put("message", "success");
        List<Object> dataList = new ArrayList<>();
        for (int i = 0; i < 5; i ++) {
            HashMap<String, Object> dataMap = new HashMap<>();
            dataMap.put("ver", 38);
            dataMap.put("utime", "2025-03-08T06:39:03.000+00:00");
            dataMap.put("ctime", "2025-03-08T06:39:03.000+00:00");
            dataMap.put("name", "实例" + Integer.toString(i));
            dataMap.put("run_state", 1);
            int index = 0;
            if (i > 2) {
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
            example.setCreatetime(new Date()); // 设置当前时间


            UsernamePasswordAuthenticationToken authentication =
                    (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

            UserDetailsImpl loginUser = (UserDetailsImpl) authentication.getPrincipal();
            User user = loginUser.getUser();
            example.setUid(user.getId().toString());

            // 存入数据库
            boolean isSaved = examplesMapper.insert(example) > 0;

            if (isSaved) {
                response.put("code", 200);
                response.put("message", "保存成功");
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
