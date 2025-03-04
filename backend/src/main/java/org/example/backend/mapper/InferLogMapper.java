package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fasterxml.jackson.databind.ser.Serializers;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.pojo.InferLog;

@Mapper
public interface InferLogMapper extends BaseMapper<InferLog> {
}
