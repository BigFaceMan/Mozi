package com.games.games.controller;

import com.games.games.consumer.SignUpdateGame;
import com.games.games.pojo.ResourceInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HearBeatController {
    @Autowired
    SignUpdateGame signUpdateGame;
    @PostMapping("/heart/info/")
    public ResourceInfo hearInfo(@RequestParam MultiValueMap<String, String> data) {
        return signUpdateGame.getResourceInfo();
    }
}
