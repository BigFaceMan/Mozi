package org.example.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlgParams {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer modelId;
    private Integer trainIterations;
    private Integer trainTime;
    private Double learningRate;
    private Integer batchSize;
}
