package com.games.games.service;

import java.io.IOException;
import java.util.Map;
import org.springframework.util.MultiValueMap;

public interface trainService {
    public Map<String, String> addTrain(MultiValueMap<String, String> data);
    public Map<String, String> stopTrain(MultiValueMap<String, String> data) throws IOException;
}
