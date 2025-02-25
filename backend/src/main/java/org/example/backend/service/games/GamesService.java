package org.example.backend.service.games;

import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.example.backend.pojo.ResourceInfo;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

public interface GamesService {
    public Map<String, String> signGame(ResourceInfo resourceInfo);
    public List<ResourceInfo> getAllGameNode();
    public Map<String, String> addTrain(MultiValueMap<String, String> data);
    public Map<String, String> killTrain(MultiValueMap<String, String> data);
    public Map<String, String> stopTrain(MultiValueMap<String, String> data);
    public Map<String, String> continueTrain(MultiValueMap<String, String> data);
}
