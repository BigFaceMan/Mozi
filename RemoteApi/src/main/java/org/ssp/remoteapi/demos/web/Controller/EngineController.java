package org.ssp.remoteapi.demos.web.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sun.awt.image.ImageWatched;

import java.util.*;

@RestController
public class EngineController {
    @GetMapping("/xtserver/node/getAllNodeByType")
    public String getEngines() {
        System.out.println("getEngines");
        HashMap<Object, Object> data = new HashMap<>();
        HashMap<Object, Object> mapDto = new HashMap<>();
        HashMap<Object, Object> engine = new HashMap<>();
        List<Map<String, Object>> freeList  = new LinkedList<>();
        List<Map<String, Object>> usingList  = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("engineId", i);
            map.put("nodeName", "engine" + i);
            map.put("ip", "127.0.0.1");
            map.put("port", "4001");
            freeList.add(map);
        }
        engine.put("freeList", freeList);
        engine.put("usingList", freeList);
        mapDto.put("engine", engine);
        data.put("mapDto", mapDto);
//        data.put("mapDto", mapDto.put("engine", engine.put("freeList", freeList)));
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("success", true);
        response.put("message", "成功");
        response.put("data", data);
        return toJsonString(response);
    }

    @PostMapping("/esserver/assess/system/taskData/findDataByDeduceAsTree")
    public String getEvals(@RequestParam String deduceId, @RequestParam int simTime) {
        System.out.println("getEvals : " + deduceId);
        HashMap<Object, Object> data = new HashMap<>();
        List<Map<String, Object>> dataList  = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("name", "评估指标体系" + i);
            map.put("nodeName", "engine" + i);
            map.put("isLeaf", 1);
            map.put("children", null);
            map.put("weight", 0.5);
            dataList.add(map);
        }
        data.put("list", dataList);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("success", true);
        response.put("message", "成功");
        response.put("data", data);
        return toJsonString(response);
    }
    @GetMapping("/sys/menu/queryMenusByRole")
    public String queryMenusByRole(
            @RequestParam String roleId,
            @RequestParam String serviceName
    ) {
        // 这里可以根据 roleId 和 serviceName 进行数据库查询
        List<Map<String, Object>> menuList = getPermissions();

        Map<String, Object> data = new HashMap<>();
        data.put("list", menuList);
        LinkedList<Map<String, Object>> dataList = new LinkedList<>();
        dataList.add(data);
        Map<String, Object> response = new HashMap<>();
        response.put("msg", "success");
        response.put("code", 0);
        response.put("data", dataList);
        return toJsonString(response);
    }

    private List<Map<String, Object>> getPermissions() {
        // 所有页面默认权限为 "查看,编辑"
        String defaultPermission = "编辑,导出";

        // 定义所有菜单项
//        String[] menuNames = {
//                "home", "remoteLog_index", "pk_index", "dataAnalysis_index",
//                "gameNodes_index", "engineNodes_index", "logConduct_index",
//                "envconduct_index", "modeltrain_index", "modelconduct_index",
//                "help_index", "useropt_index", "userinfo_index",
//                "user_account_login", "user_account_register"
//        };

        String[] menuNames = {
                "home", "remoteLog_index", "pk_index", "dataAnalysis_index",
                "gameNodes_index", "engineNodes_index", "logConduct_index",
                "envconduct_index", "modeltrain_index", "modelconduct_index",
                "useropt_index", "userinfo_index"
        };
        List<Map<String, Object>> menuList = new ArrayList<>();
        for (String menuName : menuNames) {
            Map<String, Object> menuItem = new HashMap<>();
            menuItem.put("name", menuName);
            menuItem.put("function", defaultPermission);
            menuList.add(menuItem);
        }

        return menuList;
    }

    private String listToJson(List<?> list) {
        StringBuilder json = new StringBuilder("[");
        for (Object item : list) {
            if (item instanceof Map) {
                json.append(toJsonString((Map<String, Object>) item));
            } else if (item instanceof String) {
                json.append("\"").append(item).append("\"");
            } else {
                json.append(item);
            }
            json.append(",");
        }
        if (!list.isEmpty()) {
            json.deleteCharAt(json.length() - 1); // 删除最后一个逗号
        }
        json.append("]");
        return json.toString();
    }
    private String toJsonString(Map<String, Object> map) {
        // 使用简单的 JSON 序列化（实际项目中可以使用 Jackson 或 Gson）
        StringBuilder json = new StringBuilder("{");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            json.append("\"").append(entry.getKey()).append("\":");
            if (entry.getValue() instanceof String) {
                json.append("\"").append(entry.getValue()).append("\"");
            } else if (entry.getValue() instanceof List) {
                json.append(listToJson((List<?>) entry.getValue()));
            } else if (entry.getValue() instanceof Map) {
                json.append(toJsonString((Map<String, Object>) entry.getValue()));
            } else {
                json.append(entry.getValue());
            }
            json.append(",");
        }
        json.deleteCharAt(json.length() - 1); // 删除最后一个逗号
        json.append("}");
        return json.toString();
    }
}
