package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.pojo.UserLog;


@Mapper
public interface UserLogMapper extends BaseMapper<UserLog> {
}
