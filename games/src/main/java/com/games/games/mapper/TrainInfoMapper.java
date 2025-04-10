package com.games.games.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.games.games.pojo.TrainInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TrainInfoMapper extends BaseMapper<TrainInfo> {
    // 这里可以添加自定义的查询方法
    // 例如：List<TrainInfo> findByTrainId(Integer trainId);
}
