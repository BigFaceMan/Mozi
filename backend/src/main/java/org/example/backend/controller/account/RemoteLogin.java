package org.example.backend.controller.account;

import org.example.backend.service.account.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RemoteLogin {
    @Autowired
    private LoginService loginService;

    @PostMapping("/log/remote/")
    public Map<String, String> getToken(@RequestParam Map<String, String> map) {
        System.out.println("已经入getToken");
        String userName = map.get("userName");
        String userId = map.get("userId");
        String password = "123456";
        return loginService.getRemoteToken(userName, password, userId);
    }
}
