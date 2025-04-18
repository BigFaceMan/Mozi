package org.example.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Train {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String trainingname;
    private String pytorchversion;
    private String scene;
    private String model;
    private String modelparams;
    private String checkpointpath;
    private Integer running;
    private String tensorboardpath;
    private Integer uid;
    private Integer upload;
    private String ip;
    private String port;
    private String processid;
    private String trainpypath;
    private String params;
    private Integer mversion;
    private Integer step;
    private Integer type;
}
