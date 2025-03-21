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
public class Examples {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer uid;
    private String projectname;
    private String examplename;
    private String situationid;
    private String situationName;
    private String solutionid;
    private String solutionName;
    private String exampleid;
    private String country;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date createtime;
}
