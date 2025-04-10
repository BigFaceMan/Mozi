package org.example.backend.controller.pk;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.backend.pojo.Train;
import org.example.backend.pojo.TrainLog;
import org.example.backend.pojo.User;
import org.example.backend.service.games.GamesService;
import org.example.backend.service.pk.ModelTrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.example.backend.service.impl.utils.UserDetailsImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ModeTrainingController {
    @Autowired
    private ModelTrainingService modelTrainingService;
    @Autowired
    private GamesService gamesService;

    @GetMapping("/train/ws/")
    public void accTrain() throws Exception {
        gamesService.trainWs();
    }

    @PostMapping("/train/acc/")
    public Map<String, String> accTrain(@RequestParam(defaultValue = "1") int speed) {
        System.out.println("In Train Acc : " + Integer.toString(speed));
        return gamesService.trainAcc(speed);
    }

    @PostMapping("/train/add/")
    public Map<String, String> addTrain(@RequestParam MultiValueMap<String, String> data) throws JsonProcessingException {
        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl loginUser = (UserDetailsImpl) authentication.getPrincipal();
        User user = loginUser.getUser();
        data.add("uId", user.getId().toString());
        data.add("userName", user.getUsername());
        return gamesService.addTrain(data);
//        return modelTrainingService.addTrain(data);
    }
    @PostMapping("/train/upload/")
    public Map<String, String> uploadTrain(@RequestParam MultiValueMap<String, String> data) {
        return modelTrainingService.upLoadModel(data);
    }

    @GetMapping("/train/info/")
    public Map<String, Object> getTrainInfo(@RequestParam MultiValueMap<String, String> data) {
        return modelTrainingService.getTrainInfo(data);
    }


    @PostMapping("/train/validataModel/")
    public Map<String, String> validateModel(@RequestParam MultiValueMap<String, String> data) {
        return modelTrainingService.validateModel(data);
    }
    @PostMapping("/train/stop/")
    public Map<String, String> stopTrain(@RequestParam MultiValueMap<String, String> data) {
        return gamesService.stopTrain(data);
    }

    @PostMapping("/train/continue/")
    public Map<String, String> continueTrain(@RequestParam MultiValueMap<String, String> data) {
        return gamesService.continueTrain(data);
    }

    @PostMapping("/train/kill/")
    public Map<String, String> killTrain(@RequestParam MultiValueMap<String, String> data) {
        return gamesService.killTrain(data);
//        return modelTrainingService.killTrain(data);
    }

    @PostMapping("/train/addTensorboard/")
    public Map<String, String> addTensorboard(@RequestParam MultiValueMap<String, String> data) {
        return gamesService.addTensorboard(data);
    }

    @PostMapping("/train/deleteTensorboard/")
    public Map<String, String> deleteTensorboard(@RequestParam MultiValueMap<String, String> data) {
        return gamesService.deleteTensorboard(data);
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
