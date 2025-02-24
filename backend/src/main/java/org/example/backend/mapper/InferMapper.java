package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.pojo.Infer;
import org.example.backend.pojo.Situation;

@Mapper
public interface InferMapper extends BaseMapper<Infer> {
}
