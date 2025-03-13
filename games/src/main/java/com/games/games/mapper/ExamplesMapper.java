package com.games.games.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.games.games.pojo.Examples;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface ExamplesMapper extends BaseMapper<Examples> {
    @Insert("INSERT INTO examples (situationid, solutionid, exampleid, examplename, projectname, country, createtime, uid) " +
            "VALUES (#{situationid}, #{solutionid}, #{exampleid}, #{examplename}, #{projectname}, #{country}, #{createtime}, #{uid})")
    @Options(useGeneratedKeys = true, keyProperty = "id") // 关键点
    int insert(Examples example);
}
