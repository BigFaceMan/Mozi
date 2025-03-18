package org.example.backend.service.games;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.example.backend.pojo.ResourceInfo;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

public interface GamesService {
    public Map<String, String> signGame(ResourceInfo resourceInfo);
    public List<ResourceInfo> getAllGameNode();
    public void checkGameNode();

    public void trainWs() throws Exception;

    public Map<String, String> trainAcc(int speed);
    public Map<String, String> addTrain(MultiValueMap<String, String> data) throws JsonProcessingException;
    public Map<String, String> killTrain(MultiValueMap<String, String> data);
    public Map<String, String> stopTrain(MultiValueMap<String, String> data);
    public Map<String, String> continueTrain(MultiValueMap<String, String> data);


    public Map<String, String> addInfer(MultiValueMap<String, String> data) throws JsonProcessingException;
    public Map<String, String> killInfer(MultiValueMap<String, String> data);
    public Map<String, String> stopInfer(MultiValueMap<String, String> data);
    public Map<String, String> continueInfer(MultiValueMap<String, String> data);
    Map<String, String> addTensorboard(MultiValueMap<String, String> data);

    Map<String, String> deleteTensorboard(MultiValueMap<String, String> data);
}
