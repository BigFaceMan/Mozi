package org.example.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("znt.scene_entity")
public class SceneEntity {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer sceneid;
    private String groupid;
    private String groupName;
    private String entityid;
    private String entityName;
}
