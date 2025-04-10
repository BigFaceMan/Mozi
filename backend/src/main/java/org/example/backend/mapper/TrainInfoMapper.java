package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.pojo.TrainInfo;

@Mapper
public interface TrainInfoMapper extends BaseMapper<TrainInfo> {
    // 这里可以添加自定义的查询方法
    // 例如：List<TrainInfo> findByTrainId(Integer trainId);
}
