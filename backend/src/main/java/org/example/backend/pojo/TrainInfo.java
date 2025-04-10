package org.example.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
