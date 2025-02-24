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
public class TrainLog {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String trainingname;
    private Integer progress;
    private Float loss;
    private Float cpuUsage;
    private Float gpuUsage;
    private Float memoryUsage;
    private Float vramUsage;
    private Float networkBandwidth;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date timestamp;
}
