package org.example.backend.service.Situation;

import org.example.backend.pojo.Situation;
import org.example.backend.pojo.Train;
import org.example.backend.pojo.TrainLog;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

public interface SituationService {
    Map<String, String> add(Map<String, String> data);

    List<Situation> getList();

    Map<String, String> remove(Map<String, String> data);

    Map<String, String> update(Map<String, String> data);
}
