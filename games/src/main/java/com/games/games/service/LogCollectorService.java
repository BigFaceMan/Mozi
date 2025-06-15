package com.games.games.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.games.games.mapper.ExceptionLogMapper;
import com.games.games.mapper.TrainInfoMapper;
import com.games.games.mapper.TrainLogMapper;
import com.games.games.pojo.ExceptionLog;
import com.games.games.pojo.TrainInfo;
import com.games.games.pojo.TrainLog;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;

@Service
public class LogCollectorService {
    @Autowired
    private TrainLogMapper trainLogMapper;
    @Autowired
    private TrainInfoMapper trainInfoMapper;
    @Autowired
    private ExceptionLogMapper exceptionLogMapper;

    @KafkaListener(topics = "train-log-topic", groupId = "log-collector-group")
    public void listen(String message) throws IOException {
//        System.out.println("Consumer get message : " + message);
        var node = new ObjectMapper().readTree(message);
        String processId = node.get("processId").asText();
        String type = node.get("type").asText();
        long ts = node.get("timestamp").asLong();
        String content = node.get("content").asText();
        String trainingName = node.get("trainingName").asText();
        String userName = node.get("userName").asText();
        int trainId = node.get("trainId").asInt();

        if ("info".equals(type)) {
            String[] parts = content.split(" ");
            if (parts.length >= 7) {
                TrainInfo info = new TrainInfo(null, trainId,
                        Integer.parseInt(parts[1]), Float.parseFloat(parts[2]),
                        Float.parseFloat(parts[3]), Float.parseFloat(parts[4]),
                        Float.parseFloat(parts[5]), Float.parseFloat(parts[6]));
                trainInfoMapper.insert(info);
            }
        } else if ("log".equals(type)) {
            TrainLog log = new TrainLog(null, userName, trainingName, content, new Date(ts));
            trainLogMapper.insert(log);
        } else if ("error".equals(type)) {
            ExceptionLog err = new ExceptionLog(null, userName, content, new Date(ts));
            exceptionLogMapper.insert(err);
        }
    }
}
