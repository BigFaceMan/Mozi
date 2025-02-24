package org.example.backend.service.infer;

import org.example.backend.pojo.Infer;
import org.example.backend.pojo.Train;
import org.example.backend.pojo.TrainLog;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

public interface InferService {
    Map<String, String> addInfer(MultiValueMap<String, String> data);
    Map<String, String> addTensorboard(MultiValueMap<String, String> data);

    Map<String, String> deleteTensorboard();

    void putInfer(MultiValueMap<String, String> data);

    Map<String, String> stopInfer(MultiValueMap<String, String> data);

    Map<String, String> continueInfer(MultiValueMap<String, String> data);

    Map<String, String> killInfer(MultiValueMap<String, String> data);
    List<Infer> getList();

    Map<String, String> removeInfer(Map<String, String> data);
}
