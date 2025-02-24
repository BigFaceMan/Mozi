package org.example.backend.service.impl.Situation;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.backend.mapper.ModelMapper;
import org.example.backend.mapper.SituationMapper;
import org.example.backend.pojo.Model;
import org.example.backend.pojo.Situation;
import org.example.backend.service.Situation.SituationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SituationServiceImpl implements SituationService {

    @Autowired
    private SituationMapper situationMapper;

    @Override
    public Map<String, String> add(Map<String, String> data) {
        Map<String, String> map = new HashMap<>();
        String name = data.get("name");
        String summary = data.get("summary");
        String params = data.get("params");
        String situationSelect = data.get("situationSelection");
        System.out.println("想定选择的 params: " + params);
        Situation situation = new Situation(null, name, summary, params, situationSelect);
        situationMapper.insert(situation);
        map.put("success_message", "success");
        return map;
    }

    @Override
    public List<Situation> getList() {
        List<Situation> situations = situationMapper.selectList(null); // 假设 modelMapper 有一个方法可以查询所有模型
        return situations;
    }

    @Override
    public Map<String, String> remove(Map<String, String> data) {
        Map<String, String> map = new HashMap<>();

        // 获取模型名
        String situation_id = data.get("id");

        // 使用 MyBatis-Plus 查询模型
        Situation situation = situationMapper.selectOne(new QueryWrapper<Situation>().eq("id", situation_id));
        if (situation == null) {
            map.put("error_message", "态势不存在");
            return map;
        }

        // 使用 MyBatis-Plus 删除模型
        situationMapper.delete(new QueryWrapper<Situation>().eq("id", situation_id)); // 使用 QueryWrapper 根据 name 删除模型

        map.put("success_message", "success");
        return map;
    }

    @Override
    public Map<String, String> update(Map<String, String> data) {
        Map<String, String> map = new HashMap<>();

        String name = data.get("name");
        String summary = data.get("summary");
        String params = data.get("params");

        if (name == null || name.trim().isEmpty()) {
            map.put("error_message", "态势名不能为空");
            return map;
        }

        // 使用 MyBatis-Plus 查询模型对象
        Situation situation = situationMapper.selectOne(new QueryWrapper<Situation>().eq("name", name));
        if (situation == null) {
            map.put("error_message", "态势不存在");
            return map;
        }

        if (summary != null && !summary.trim().isEmpty()) {
            situation.setSummary(summary);
        }

        if (params != null && !params.trim().isEmpty()) {
            situation.setParams(params);
        }

        // 使用 MyBatis-Plus 更新模型信息
        situationMapper.updateById(situation); // 更新操作直接使用 updateById

        map.put("success_message", "success");
        return map;
    }
}
