package org.example.backend.controller.games;


import org.apache.ibatis.annotations.Param;
import org.example.backend.pojo.ResourceInfo;
import org.example.backend.service.games.GamesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class GamesController {
    @Autowired
    private GamesService gamesService;
    @PostMapping("/games/sign/")
    public Map<String, String> gamesSign(@RequestBody ResourceInfo resourceInfo) {
        return gamesService.signGame(resourceInfo);
    }

    @GetMapping("/games/testWs/")
    public Map<String, Object> testWs() {
        return gamesService.trainWs();
    }
    @PostMapping("/games/stopEngine/")
    public Map<String, Object> stopEngine(@RequestBody Map<String, Object> data) {
        System.out.println("stopEngine receive : " + data);
        List<String> zjjId = (List<String>) data.get("zzjId");
        List<String> engineAddr = (List<String>) data.get("engineAddr");
        return gamesService.stopEngine(zjjId, engineAddr);
    }

    @PostMapping("/games/removeEngineBytUid/")
    public Map<String, String> removeEngines(@RequestParam String tUid) {
        gamesService.removeTrainEngines(tUid);
        Map<String, String> resp = new HashMap<>();
        resp.put("code", "200");
        resp.put("msg", "success");
        return resp;
    }
    @GetMapping("/games/get/all/")
    public List<ResourceInfo> gameGetAllNodes() {
        return gamesService.getAllGameNode();
    }
}
