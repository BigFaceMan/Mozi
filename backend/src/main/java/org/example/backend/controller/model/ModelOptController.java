package org.example.backend.controller.model;

import org.example.backend.pojo.Model;
import org.example.backend.service.model.ModelOptService;
import org.example.backend.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ModelOptController {
    @Autowired
    private ModelOptService modelOptService;
    @PostMapping("/model/add/")
    public Map<String, String> add( @RequestParam("name") String name,
        @RequestParam("summary") String summary,
        @RequestParam("environment") String environment,
        @RequestParam("ability") String ability,
        @RequestParam("structureimage") String structureimage,
        @RequestParam("code") String code,
        @RequestParam("modelstruct") String modelstruct,
        @RequestParam("modelselect") String modelselect,
        @RequestParam("situationselect") String situationselect,
        @RequestParam(value = "modelPth", required = false) MultipartFile modelPth) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("summary", summary);
        data.put("environment", environment);
        data.put("ability", ability);
        data.put("structureimage", structureimage);
        data.put("code", code);
//        data.put("inferCode", inferCode);
        data.put("modelstruct", modelstruct);
        data.put("modelselect", modelselect);
        data.put("situationselect", situationselect);
        data.put("modelPth", modelPth);

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

    @PostMapping("/model/rollback/")
    public Result<String> rollback(@RequestParam Map<String, String> data) {
        return modelOptService.rollback(data);
    }

    @GetMapping("/model/trainPth/")
    public byte[] getModelPth(@RequestParam(defaultValue = "-1") Integer trainId) {
        return modelOptService.getModelPth(trainId);
    }
}
