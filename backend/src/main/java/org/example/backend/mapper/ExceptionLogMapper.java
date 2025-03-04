package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.pojo.ExceptionLog;

@Mapper
public interface ExceptionLogMapper extends BaseMapper<ExceptionLog> {
}
