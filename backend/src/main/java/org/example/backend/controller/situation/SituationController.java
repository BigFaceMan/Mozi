package org.example.backend.controller.situation;

import org.example.backend.pojo.Model;
import org.example.backend.pojo.Situation;
import org.example.backend.service.Situation.SituationService;
import org.example.backend.service.model.ModelOptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class SituationController {
    @Autowired
    private SituationService situationService;
    @PostMapping("/situation/add/")
    public Map<String, String> add(@RequestParam Map<String, String> data) {
        return situationService.add(data);
    }

    @PostMapping("/situation/update/")
    public Map<String, String> update(@RequestParam Map<String, String> data) {
        return situationService.update(data);
    }

    @PostMapping("/situation/remove/")
    public Map<String, String> remove(@RequestParam Map<String, String> data) {
        return situationService.remove(data);
    }

    @GetMapping("/situation/getlist/")
    public List<Situation> getlist() {
        return situationService.getList();
    }
}
