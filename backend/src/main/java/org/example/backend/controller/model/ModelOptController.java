package org.example.backend.controller.model;

import org.example.backend.pojo.Model;
import org.example.backend.service.model.ModelOptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class ModelOptController {
    @Autowired
    private ModelOptService modelOptService;
    @PostMapping("/model/add/")
    public Map<String, String> add(@RequestParam Map<String, String> data) {
        return modelOptService.add(data);
    }

    @PostMapping("/model/update/")
    public Map<String, String> update(@RequestParam Map<String, String> data) {
        return modelOptService.update(data);
    }

    @PostMapping("/model/remove/")
    public Map<String, String> remove(@RequestParam Map<String, String> data) {
        return modelOptService.remove(data);
    }

    @GetMapping("/model/getlist/")
    public List<Model> getlist() {
        return modelOptService.getList();
    }
}
