package org.example.backend.service.impl.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.backend.pojo.User;
import org.example.backend.service.impl.utils.UserDetailsImpl;
import org.example.backend.service.account.LoginService;
import org.example.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LoginServiceImpl implements LoginService {

    // Logger实例
    private static final Logger logger = LoggerFactory.getLogger(LoginServiceImpl.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public Map<String, String> getToken(String username, String password) {
        // 记录接收到的用户名和密码（可选择记录，避免泄露密码）
        logger.info("Received login attempt for username: {}", username);

        // 构建认证Token
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);

        try {
            // 调用AuthenticationManager进行认证
            Authentication authenticate = authenticationManager.authenticate(authenticationToken);
            logger.info("Login successful for username: {}", username);

            // 获取用户信息
            UserDetailsImpl loginUser = (UserDetailsImpl) authenticate.getPrincipal();
            User user = loginUser.getUser();
            String jwt = JwtUtil.createJWT(user.getId().toString());

            // 生成返回的token信息
            Map<String, String> map = new HashMap<>();
            map.put("error_message", "success");
            map.put("token", jwt);
            map.put("username", user.getUsername());
            map.put("urank", user.getUrank());

            // 记录返回的 token 信息（注意不要泄露敏感信息，如密码）
            logger.info("Token generated for user: {}", user.getUsername());
            return map;

        } catch (Exception e) {
            // 捕获异常并记录日志
            logger.error("Login failed for username: {}", username, e);
            // 如果登录失败，返回相关的错误信息
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error_message", "Login failed: " + e.getMessage());
            return errorMap;
        }
    }
}
