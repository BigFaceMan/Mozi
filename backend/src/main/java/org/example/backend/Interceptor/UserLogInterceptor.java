package org.example.backend.Interceptor;

import org.example.backend.pojo.User;
import org.example.backend.pojo.UserLog;
import org.example.backend.service.impl.utils.UserDetailsImpl;
import org.example.backend.service.logs.UserLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class UserLogInterceptor implements HandlerInterceptor {

    @Autowired
    private UserLogService userLogService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken){
            System.out.println("用户未登录，访问接口：" + request.getRequestURI());
            return true;
        }

        UserDetailsImpl loginUser = (UserDetailsImpl) authentication.getPrincipal();
        User user = loginUser.getUser();
        String uri = request.getRequestURI();
        String username = user.getUsername();
        String method = " 访问接口：" + uri;
        Date now = new Date();
        UserLog log = new UserLog(null, username, method, now);
        userLogService.save(log);
        return true;
    }
}
