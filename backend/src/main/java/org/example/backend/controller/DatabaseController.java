package org.example.backend.controller;

import org.example.backend.pojo.Train;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;

@RestController
public class DatabaseController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/testConnection")
    public List<Train> testDatabaseConnection() {
        // 查询 znt 模式下的 train 表的所有数据
        String sql = "SELECT id, trainingname, pytorchversion, scene, model, modelparams, checkpointpath, running, tensorboardpath, uid, upload, ip, port, processid, trainpypath FROM znt.\"train\"";
        List<Train> list = new LinkedList<>();
        return list;

        // 使用 JdbcTemplate 查询 train 表的数据
//        List<Train> trains = jdbcTemplate.query(sql, (rs, rowNum) -> {
//            Train train = new Train();
//            train.setId(rs.getLong("id"));
//            train.setTrainingname(rs.getString("trainingname"));
//            train.setPytorchversion(rs.getString("pytorchversion"));
//            train.setScene(rs.getString("scene"));
//            train.setModel(rs.getString("model"));
//            train.setModelparams(rs.getString("modelparams"));
//            train.setCheckpointpath(rs.getString("checkpointpath"));
//            train.setRunning(rs.getInt("running"));
//            train.setTensorboardpath(rs.getString("tensorboardpath"));
//            train.setUid(rs.getInt("uid"));
//            train.setUpload(rs.getInt("upload"));
//            train.setIp(rs.getString("ip"));
//            train.setPort(rs.getString("port"));
//            train.setProcessid(rs.getString("processid"));
//            train.setTrainpypath(rs.getString("trainpypath"));
//            return train;
//        });
//
//        return trains;
    }
}
