package org.example.backend.service.model;

import org.example.backend.pojo.Model;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ModelOptService {
    Map<String, String> add(Map<String, Object> data) throws IOException;

    List<Model> getList();

    Map<String, String> remove(Map<String, String> data);

    Map<String, String> update(Map<String, String> data);

    byte[] getModelPth(Integer trainId);
}

