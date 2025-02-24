package org.example.backend.controller.pk;

import org.example.backend.pojo.Train;
import org.example.backend.pojo.TrainLog;
import org.example.backend.service.pk.ModelTrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class ModeTrainingController {
    @Autowired
    private ModelTrainingService modelTrainingService;

    @PostMapping("/train/add/")
    public Map<String, String> addTrain(@RequestParam MultiValueMap<String, String> data) {
        return modelTrainingService.addTrain(data);
    }
    @PostMapping("/train/upload/")
    public Map<String, String> uploadTrain(@RequestParam MultiValueMap<String, String> data) {
        return modelTrainingService.upLoadModel(data);
    }


    @PostMapping("/train/validataModel/")
    public Map<String, String> validateModel(@RequestParam MultiValueMap<String, String> data) {
        return modelTrainingService.validateModel(data);
    }
    @PostMapping("/train/stop/")
    public Map<String, String> stopTrain(@RequestParam MultiValueMap<String, String> data) {
        return modelTrainingService.stopTrain(data);
    }

    @PostMapping("/train/continue/")
    public Map<String, String> continueTrain(@RequestParam MultiValueMap<String, String> data) {
        return modelTrainingService.continueTrain(data);
    }

    @PostMapping("/train/kill/")
    public Map<String, String> killTrain(@RequestParam MultiValueMap<String, String> data) {
        return modelTrainingService.killTrain(data);
    }

    @PostMapping("/train/addTensorboard/")
    public Map<String, String> addTensorboard(@RequestParam MultiValueMap<String, String> data) {
        return modelTrainingService.addTensorboard(data);
    }

    @PostMapping("/train/deleteTensorboard/")
    public Map<String, String> deleteTensorboard(@RequestParam MultiValueMap<String, String> data) {
        return modelTrainingService.deleteTensorboard();
    }

    @PostMapping("/trainlog/add/")
    public Map<String, String> addTrainLog(@RequestParam MultiValueMap<String, String> data) {
        return modelTrainingService.addTrainLog(data);
    }


    @GetMapping("/train/getlist/")
    public List<Train> getList() {
        return modelTrainingService.getList();
    }

    @GetMapping("/trainlog/getlist/")
    public List<TrainLog> getLogList(@RequestParam Map<String, String> data) {
        return modelTrainingService.getLogList(data);
    }

    @PostMapping("/train/remove/")
    public Map<String, String> removeTrain(@RequestParam Map<String, String> data) {
        return modelTrainingService.removeTrain(data);
    }

    @PostMapping("/trainlog/remove/")
    public Map<String, String> removeTrainLog(@RequestParam Map<String, String> data) {
        return modelTrainingService.removeTrainLog(data);
    }
}
