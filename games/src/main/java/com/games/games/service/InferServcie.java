package com.games.games.service;

import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.Map;

public interface InferServcie {
    public Map<String, String> addInfer(MultiValueMap<String, String> data);
    public Map<String, String> killInfer(MultiValueMap<String, String> data) throws IOException;
    public Map<String, String> stopInfer(MultiValueMap<String, String> data) throws IOException;
    public Map<String, String> continueInfer(MultiValueMap<String, String> data) throws IOException;

}
