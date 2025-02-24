package org.example.backend.service.pk;

import org.example.backend.pojo.Model;
import org.example.backend.pojo.Train;
import org.example.backend.pojo.TrainLog;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

public interface ModelTrainingService {

    Map<String, String> addTrain(MultiValueMap<String, String> data);
    Map<String, String> upLoadModel(MultiValueMap<String, String> data);
    Map<String, String> validateModel(MultiValueMap<String, String> data);
    Map<String, String> addTensorboard(MultiValueMap<String, String> data);

    Map<String, String> deleteTensorboard();
    Map<String, String> addTrainLog(MultiValueMap<String, String> data);

    void putTrain(MultiValueMap<String, String> data);

    Map<String, String> continueTrain(MultiValueMap<String, String> data);
    Map<String, String> killTrain(MultiValueMap<String, String> data);
    Map<String, String> stopTrain(MultiValueMap<String, String> data);

    List<Train> getList();
    List<TrainLog> getLogList(Map<String, String> data);

    Map<String, String> removeTrain(Map<String, String> data);
    Map<String, String> removeTrainLog(Map<String, String> data);
}
