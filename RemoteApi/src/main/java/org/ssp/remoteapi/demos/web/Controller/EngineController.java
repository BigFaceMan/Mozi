package org.ssp.remoteapi.demos.web.Controller;

import com.sun.org.glassfish.gmbal.ParameterNames;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
        for (int i = 0; i < 10; i++) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("engineId", i);
            map.put("nodeName", "engine" + i);
            map.put("ip", "127.0.0.1");
            map.put("port", "4001");
            freeList.add(map);
        }
        engine.put("freeList", freeList);
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
