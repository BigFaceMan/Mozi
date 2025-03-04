package com.games.games.controller;

import com.games.games.service.InferServcie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
public class InferController {
    @Autowired
    private InferServcie inferService;

    @PostMapping("/infer/add/")
    public Map<String, String> addTrain(@RequestParam MultiValueMap<String, String> data) {
        return inferService.addInfer(data);
    }

    @PostMapping("/infer/kill/")
    public Map<String, String> killTrain(@RequestParam MultiValueMap<String, String> data) throws IOException {
        return inferService.killInfer(data);
    }

    @PostMapping("/infer/stop/")
    public Map<String, String> stopTrain(@RequestParam MultiValueMap<String, String> data) throws IOException {
        return inferService.stopInfer(data);
    }

    @PostMapping("/infer/continue/")
    public Map<String, String> continueTrain(@RequestParam MultiValueMap<String, String> data) throws IOException {
        return inferService.continueInfer(data);
    }

}
