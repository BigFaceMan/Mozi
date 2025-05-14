package com.games.games.service;

import java.io.IOException;
import java.util.Map;
import org.springframework.util.MultiValueMap;

public interface TrainService {
    public Map<String, String> addTrain(MultiValueMap<String, String> data);
    public boolean terminateProcess(String pid);
    public Map<String, String> killTrain(MultiValueMap<String, String> data) throws IOException;
    public Map<String, String> stopTrain(MultiValueMap<String, String> data) throws IOException;
    public Map<String, String> continueTrain(MultiValueMap<String, String> data) throws IOException;

    Map<String, String> addTensorboard(MultiValueMap<String, String> data);

    Map<String, String> deleteTensorboard(MultiValueMap<String, String> data);
}
