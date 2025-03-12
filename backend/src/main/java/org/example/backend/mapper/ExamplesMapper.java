package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.example.backend.pojo.Examples;

@Mapper
public interface ExamplesMapper extends BaseMapper<Examples> {
    @Insert("INSERT INTO examples (situationid, solutionid, exampleid, examplename, projectname, country, createtime, uid) " +
            "VALUES (#{situationid}, #{solutionid}, #{exampleid}, #{examplename}, #{projectname}, #{country}, #{createtime}, #{uid})")
    @Options(useGeneratedKeys = true, keyProperty = "id") // 关键点
    int insert(Examples example);
}
