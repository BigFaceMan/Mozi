package com.games.games.controller;


import com.games.games.service.TrainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class TrainController {

    @Autowired
    private TrainService trainService;

    @PostMapping("/train/add/")
    public Map<String, String> addTrain(@RequestParam MultiValueMap<String, String> data) {
        return trainService.addTrain(data);
    }

    @PostMapping("/train/kill/")
    public Map<String, String> killTrain(@RequestParam MultiValueMap<String, String> data) throws IOException {
        return trainService.killTrain(data);
    }

    @PostMapping("/train/stop/")
    public Map<String, String> stopTrain(@RequestParam MultiValueMap<String, String> data) throws IOException {
        return trainService.stopTrain(data);
    }

    @PostMapping("/train/continue/")
    public Map<String, String> continueTrain(@RequestParam MultiValueMap<String, String> data) throws IOException {
        return trainService.continueTrain(data);
    }

//    @PostMapping("/train/upload/")
//    public Map<String, String> uploadTrain(@RequestParam MultiValueMap<String, String> data) {
//        return modelTrainingService.upLoadModel(data);
//    }


//    @PostMapping("/train/validataModel/")
//    public Map<String, String> validateModel(@RequestParam MultiValueMap<String, String> data) {
//        return modelTrainingService.validateModel(data);
//    }
//    @PostMapping("/train/stop/")
//    public Map<String, String> stopTrain(@RequestParam MultiValueMap<String, String> data) {
//        return trainService.stopTrain(data);
//    }
//

//    @PostMapping("/train/addTensorboard/")
//    public Map<String, String> addTensorboard(@RequestParam MultiValueMap<String, String> data) {
//        return trainService.addTensorboard(data);
//    }

//    @PostMapping("/train/deleteTensorboard/")
//    public Map<String, String> deleteTensorboard(@RequestParam MultiValueMap<String, String> data) {
//        return modelTrainingService.deleteTensorboard();
//    }
}
