package org.example.backend.service.model;

import org.example.backend.pojo.AlgParams;
import org.example.backend.pojo.Model;
import org.example.backend.utils.Result;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ModelOptService {
    Map<String, String> add(Map<String, Object> data) throws IOException;

    List<Model> getList();

    Map<String, String> remove(Map<String, String> data);
    List<AlgParams> getModelParamsAll();
    Map<String, String> update(Map<String, String> data);
    Result<String> rollback(Map<String, String> data);

    byte[] getModelPth(Integer trainId);
}

