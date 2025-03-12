package org.ssp.remoteapi.demos.web.Controller;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class EnvController {
    // 预生成一棵树结构
    private static final List<Map<String, Object>> TREE_DATA = generateTreeData();

//    public String getGroupSync(@RequestParam(defaultValue = "-1") String id, @RequestParam(defaultValue = "0") String pid) {
//        Map<String, Object> response = new HashMap<>();
//        response.put("code", 200);
//        response.put("success", true);
//        response.put("message", "成功");
//        return toJsonString(response);
//    }
// 预生成的数据
//private static final Map<String, List<Map<String, Object>>> SCENARIO_DATA = generateScenarioData();

    /**
     * 根据任务ID和编组ID查询实体
     * @param taskId 任务 ID（暂未使用，可扩展）
     * @param uniGroupId 编组 ID
     * @param equipment 设备（暂未使用，可扩展）
     * @param country 国家
     * @return 返回 JSON 结构
     */
    @GetMapping("/ygserver/experiment/scene/getScenarioByUnipGroupStatistics")
    public Map<String, Object> getEntity(@RequestParam(defaultValue = "-1") String taskId,
                                         @RequestParam(defaultValue = "-1") String unipGroupId,
                                         @RequestParam(defaultValue = "1") String equipment,
                                         @RequestParam(defaultValue = "中国") String country) {
        System.out.println("unipGroupId Id : " + unipGroupId);
        // 获取该编组的所有实体
        List<Map<String, Object>> allEntities = generateScenarioData(unipGroupId);

        // 根据国家筛选
        List<Map<String, Object>> filteredEntities = new ArrayList<>();
        for (Map<String, Object> entity : allEntities) {
            if (entity.get("country").equals(country)) {
                filteredEntities.add(entity);
            }
        }

        // 组织返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("scenarioModel", filteredEntities);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("success", true);
        response.put("message", "成功");
        response.put("data", data);

        return response;
    }

    /**
     * 生成编组 ID 对应的实体数据
     */
    private static List<Map<String, Object>> generateScenarioData(String uniGroupId) {
//        Map<String, List<Map<String, Object>>> scenarioData = new HashMap<>();
        String[] countries = {"中国", "美国", "俄罗斯", "德国", "法国"};
        String[] unitTypes = {"天基预警雷达站", "通用指挥所", "战术数据链站", "防空指挥中心"};
        Random random = new Random();

        // 生成 5 个编组 ID（模拟真实场景）
//        for (int i = 1; i <= 5; i++) {
//            String uniGroupId = "UG-" + i; // 例如 UG-1, UG-2
            List<Map<String, Object>> entities = new ArrayList<>();
//
            // 每个编组生成 3-5 个实体
            int entityCount =  35;
            for (int j = 1; j <= entityCount; j++) {
                String id = uniGroupId + Integer.toString(j);
                String unitType = unitTypes[j % 4];
                String country = countries[j % 5];

                Map<String, Object> entity = new HashMap<>();
                entity.put("scenarioModelName", unitType + "#" + j);
                entity.put("country", country);
                entity.put("unitModel", "地面");
                entity.put("unitType", 3);
                entity.put("id", id);
                entity.put("type", unitType);
                entity.put("modelType", "雷达站");
                entity.put("num", 0);

                entities.add(entity);
            }
            return entities;
//
//            scenarioData.put(uniGroupId, entities);
//        }

//        return scenarioData;
    }

    /**
     * 获取编组数据
     * @param id  当前查询的节点 ID
     * @param pid 父节点 ID，pid=0 表示获取顶级节点
     * @return  返回 JSON 结构
     */
    @GetMapping("/ygserver/experiment/scene/getUnipGroupTreeSync")
    public String getGroupSync(@RequestParam(defaultValue = "-1") String id,
                                            @RequestParam(defaultValue = "0") String pid) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("success", true);
        response.put("message", "成功");
        response.put("data", findChildren(pid));
        return toJsonString(response);
    }

    /**
     * 生成一棵随机的树结构
     */
    private static List<Map<String, Object>> generateTreeData() {
        List<Map<String, Object>> tree = new ArrayList<>();
        Random random = new Random();
        String[] countries = {"中国", "美国", "俄罗斯", "德国", "法国"};

        for (int i = 0; i < 26; i++) {
            String id = UUID.randomUUID().toString();  // 生成唯一 ID
            boolean hasChildren = true; // 随机决定是否有子节点
            Map<String, Object> node = new HashMap<>();
            node.put("unitGroupName", "编组 " + (i + 1));
            node.put("country", countries[i % 5]);
            node.put("id", id);
            node.put("unitModel", null);
            node.put("childrenEdit", hasChildren);
            node.put("children", hasChildren ? generateChildren(id, random, 1) : null);
            tree.add(node);
        }
        return tree;
    }

    /**
     * 生成子节点
     */
    private static List<Map<String, Object>> generateChildren(String parentId, Random random, int has) {
        List<Map<String, Object>> children = new ArrayList<>();
        String[] countries = {"中国", "美国", "俄罗斯", "德国", "法国"};
        if (has == 1) {
            for (int j = 0; j < 25; j++) {
                String id = UUID.randomUUID().toString();
                boolean hasChildren = false;
                Map<String, Object> child = new HashMap<>();
                child.put("unitGroupName", "子编组 " + (j + 1));
                child.put("country", countries[j % 5]);
                child.put("id", id);
                child.put("unitModel", null);
                child.put("childrenEdit", hasChildren);
                child.put("children", hasChildren ? generateChildren(id, random, 0) : null);
                children.add(child);
            }
        } else {
            for (int j = 0; j < random.nextInt(3) + 1; j++) {
                String id = UUID.randomUUID().toString();
                boolean hasChildren = random.nextBoolean();
                Map<String, Object> child = new HashMap<>();
                child.put("unitGroupName", "子编组 " + (j + 1));
                child.put("country", countries[random.nextInt(countries.length)]);
                child.put("id", id);
                child.put("unitModel", null);
                child.put("childrenEdit", hasChildren);
                child.put("children", hasChildren ? generateChildren(id, random, 0) : null);
                children.add(child);
            }
        }
        return children;
    }

    /**
     * 查找某个节点的子节点
     */
    private static List<Map<String, Object>> findChildren(String pid) {
        if ("0".equals(pid)) {
            // 返回顶级节点
            return TREE_DATA;
        }
        // 递归查找匹配 pid 的子节点
        return searchChildren(TREE_DATA, pid);
    }

    private static List<Map<String, Object>> searchChildren(List<Map<String, Object>> nodes, String pid) {
        for (Map<String, Object> node : nodes) {
//            System.out.println("node id : " + node.get("id"));
            if (node.get("id").equals(pid)) {
                System.out.println("find pid : " + pid);
                return (List<Map<String, Object>>) node.get("children");
            }
            List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");
            if (children != null) {
                List<Map<String, Object>> result = searchChildren(children, pid);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
    @GetMapping("/ygserver/experiment/scene/getUnipGroupTree")
    public String getGroup(@RequestParam(defaultValue = "-1") String id) {
        System.out.println("task Id : " + id);
        Map<String, Object> response = new HashMap<>();
        response.put("code", "200");
        response.put("success", true);
        response.put("message", "成功");

        String[] countries = {"中国", "美国", "俄罗斯", "德国", "法国"};
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (int i = 0; i < 5; i++) { // 生成两个示例数据
            String country;
            country = countries[i];
//            if (i == 0) {
//                country = "中国";
//            } else {
//                country = "美国";
//            }
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
        return toJsonString(response);
//        taskId 想定id
//        System.out.println("task Id : " + id);
//        // 构建返回的 JSON 结构
//        Map<String, Object> response = new HashMap<>();
//        response.put("code", 200);
//        response.put("success", true);
//        response.put("message", "成功");
//        // 将 Map 转换为 JSON 字符串
//        return toJsonString(response);
    }


    @GetMapping("/wwe/deduce/shortcut/getByPMId")
    public String getExamples(@RequestParam(defaultValue = "-1") String paticipantMissionUID) {
        System.out.println("mission Id : " + paticipantMissionUID);
        // 生成 60 条数据
        List<Map<String, Object>> allExamples = generateExamples(650, 30);
        List<Map<String, Object>> selectedExamples = new ArrayList<>();
        for (int i = 0; i < allExamples.size(); i ++) {
            Map<String, Object> item = (Map<String, Object>) allExamples.get(i);  // 类型转换
            if (item.get("paticipantMissionUID").equals(paticipantMissionUID)) {
                selectedExamples.add(item);
            }
        }
        // 构建返回的 JSON 结构
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("success", true);
        response.put("message", "成功");
        response.put("data", selectedExamples);
        // 将 Map 转换为 JSON 字符串
        return toJsonString(response);
    }
    @GetMapping("/wwe/paticipantmissiondata/selectSeatByTaskId")
    public String getPlans(@RequestParam(defaultValue = "-1") String taskId) {
        // 生成 60 条数据
        List<Map<String, Object>> allPlans = generatePlans(650, 50);
        List<Map<String, Object>> selectedPlans = new ArrayList<>();
        for (int i = 0; i < allPlans.size(); i ++) {
            Map<String, Object> item = (Map<String, Object>) allPlans.get(i);  // 类型转换
            if (item.get("taskUID").equals(taskId)) {
                selectedPlans.add(item);
            }
        }
        // 构建返回的 JSON 结构
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("success", true);
        response.put("message", "成功");
        response.put("data", selectedPlans);
        // 将 Map 转换为 JSON 字符串
        return toJsonString(response);
    }
//    /www/task/page?pageIndex=%d
    @GetMapping("/wwe/task/page")
    public String getSituation(@RequestParam(defaultValue = "1") int pageIndex) {
        // 生成 60 条数据
        List<Map<String, Object>> allRecords = generateRecords(650);

        // 每页 20 条数据
        int pageSize = 20;
        int totalRecords = allRecords.size();
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);

        // 根据 pageIndex 计算当前页的数据
        int startIndex = (pageIndex - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalRecords);
        List<Map<String, Object>> currentPageRecords = allRecords.subList(startIndex, endIndex);

        // 构建返回的 JSON 结构
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("success", true);
        response.put("message", "成功");

        Map<String, Object> data = new HashMap<>();
        data.put("records", currentPageRecords);
        data.put("total", totalRecords);
        data.put("size", pageSize);
        data.put("current", pageIndex);
        data.put("orders", new ArrayList<>());
        data.put("searchCount", true);
        data.put("pages", totalPages);

        response.put("data", data);

        // 将 Map 转换为 JSON 字符串
        return toJsonString(response);
    }

    private List<Map<String, Object>> generateRecords(int count) {
        List<Map<String, Object>> records = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Map<String, Object> record = new HashMap<>();
            record.put("ver", 38);
            record.put("task_message", "真正的网电对抗");
            record.put("utime", "2025-03-08T06:39:03.000+00:00");
            record.put("ctime", "2025-03-08T06:39:03.000+00:00");
            record.put("taskName", "西工大中部场景其他");
            record.put("run_state", 1);
            record.put("id", "111111111" + i);
            record.put("status", 1);
            records.add(record);
        }
        return records;
    }
    private List<Map<String, Object>> generatePlans(int count, int preCount) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (int i = 0; i < count; i ++) {
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
            if (i > preCount) {
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
        return dataList;
    }

    private List<Map<String, Object>> generateExamples(int count, int preCount) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (int i = 0; i < count; i ++) {
            HashMap<String, Object> dataMap = new HashMap<>();
            dataMap.put("ver", 38);
            dataMap.put("utime", "2025-03-08T06:39:03.000+00:00");
            dataMap.put("ctime", "2025-03-08T06:39:03.000+00:00");
            dataMap.put("name", "实例" + Integer.toString(i));
            dataMap.put("run_state", 1);
            int index = 0;
            if (i > preCount) {
                index = i;
            }
            dataMap.put("taskId", "111111111" + Integer.toString(index));
            dataMap.put("id", "311111111" + Integer.toString(i));
            dataMap.put("paticipantMissionUID", "211111111" + Integer.toString(index));
            dataMap.put("status", 2);
            dataList.add(dataMap);
        }
        return dataList;
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
}
