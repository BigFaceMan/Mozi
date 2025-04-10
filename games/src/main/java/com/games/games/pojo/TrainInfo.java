package com.games.games.pojo;

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
public class TrainInfo {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer trainid;
    private Integer step;
    private Float accuracy;
    private Float speed;
    private Float stability;
    private Float loss;
    private Float reward;
}
