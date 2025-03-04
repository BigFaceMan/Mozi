package com.games.games.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Infer {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String infername;
    private String scene;
    private String model;
    private Integer running;
    private String tensorboardpath;
    private Integer uid;
    private String ip;
    private String port;
    private String processid;
    private String inferpypath;
    private String checkpointpath;
}
