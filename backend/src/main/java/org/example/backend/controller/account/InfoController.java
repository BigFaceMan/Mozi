package org.example.backend.controller.account;

import org.example.backend.pojo.User;
import org.example.backend.service.impl.utils.UserDetailsImpl;
import org.example.backend.service.account.InfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class InfoController {
    @Autowired
    private InfoService infoService;

    public String get_urank() {
        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        UserDetailsImpl loginUser = (UserDetailsImpl) authentication.getPrincipal();
        User user = loginUser.getUser();
        return user.getUrank();
    }
    @GetMapping("/user/account/info/")
    public Map<String, String> getinfo() {
        return infoService.getinfo();
    }

    @GetMapping("/user/getlist/")
    public List<User> getlist() {
        if (this.get_urank().equals('0'))
            return null;
        return infoService.getlist();
    }

    @PostMapping("/user/ansAsk/")
    public Map<String, String> ansAsk(@RequestParam Map<String, String> data) {
        if (this.get_urank().equals('0'))
            return null;
        return infoService.ansAsk(data);
    }

    @PostMapping("/user/deletebyid/")
    public Map<String, String> delete_user_by_id(@RequestParam Map<String, String> data) {
        if (this.get_urank().equals('0'))
            return null;
        return infoService.delete_user_by_id(data);
    }

    @PostMapping("/user/updatabyname/")
    public Map<String, String> updata_user_by_name(@RequestParam Map<String, String> data) {
        if (this.get_urank().equals('0'))
            return null;
        return infoService.updata_user_by_name(data);
    }
    @PostMapping("/user/addbyname/")
    public Map<String, String> add_user_by_name(@RequestParam Map<String, String> data) {
        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        UserDetailsImpl loginUser = (UserDetailsImpl) authentication.getPrincipal();
        User user = loginUser.getUser();

        if (this.get_urank().equals('0') && (user.getUsername() != data.get("username")) )
            return null;
        return infoService.add_user_by_name(data);
    }
}
