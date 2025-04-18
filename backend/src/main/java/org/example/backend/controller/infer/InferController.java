package org.example.backend.controller.infer;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.backend.pojo.Infer;
import org.example.backend.pojo.Train;
import org.example.backend.pojo.User;
import org.example.backend.service.games.GamesService;
import org.example.backend.service.impl.utils.UserDetailsImpl;
import org.example.backend.service.infer.InferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class InferController {
    @Autowired
    private InferService inferService;

    @Autowired
    private GamesService gamesService;
    @PostMapping("/infer/add/")
    public Map<String, String> addInfer(@RequestParam MultiValueMap<String, String> data) throws JsonProcessingException {
        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl loginUser = (UserDetailsImpl) authentication.getPrincipal();
        User user = loginUser.getUser();
        data.add("uId", user.getId().toString());
        data.add("userName", user.getUsername());
        return gamesService.addInfer(data);
    }

    @PostMapping("/infer/stop/")
    public Map<String, String> stopInfer(@RequestParam MultiValueMap<String, String> data) {
        return gamesService.stopInfer(data);
    }

    @PostMapping("/infer/continue/")
    public Map<String, String> continueInfer(@RequestParam MultiValueMap<String, String> data) {
        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl loginUser = (UserDetailsImpl) authentication.getPrincipal();
        User user = loginUser.getUser();
        data.add("uId", user.getId().toString());
        data.add("userName", user.getUsername());
        return gamesService.continueInfer(data);
    }

    @PostMapping("/infer/kill/")
    public Map<String, String> killInfer(@RequestParam MultiValueMap<String, String> data) {
        return gamesService.killInfer(data);
    }

    @PostMapping("/infer/addTensorboard/")
    public Map<String, String> addTensorboard(@RequestParam MultiValueMap<String, String> data) {
        return inferService.addTensorboard(data);
    }

    @PostMapping("/infer/deleteTensorboard/")
    public Map<String, String> deleteTensorboard(@RequestParam MultiValueMap<String, String> data) {
        return inferService.deleteTensorboard();
    }

    @GetMapping("/infer/getlist/")
    public List<Train> getList() {
        return inferService.getList();
    }

    @PostMapping("/infer/remove/")
    public Map<String, String> remove(@RequestParam Map<String, String> data) {
        return inferService.removeInfer(data);
    }

}
