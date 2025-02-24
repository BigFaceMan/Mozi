package org.example.backend.service.impl.account;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.example.backend.mapper.TrainMapper;
import org.example.backend.mapper.UserMapper;
import org.example.backend.pojo.Train;
import org.example.backend.pojo.User;
import org.example.backend.service.impl.utils.UserDetailsImpl;
import org.example.backend.service.account.InfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InfoServiceImpl implements InfoService {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TrainMapper trainMapper;
    @Override
    public Map<String, String> getinfo() {
        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        UserDetailsImpl loginUser = (UserDetailsImpl) authentication.getPrincipal();
        User user = loginUser.getUser();

        Map<String, String> map = new HashMap<>();
        map.put("error_message", "success");
        map.put("id", user.getId().toString());
        map.put("username", user.getUsername());
        map.put("urank", user.getUrank());
        map.put("phone", user.getPhone());
        map.put("email", user.getEmail());
        map.put("uploadcnt", user.getUploadcnt().toString());
        return map;
    }

    @Override
    public List<User> getlist() {
        List<User> users = userMapper.selectList(null);
        System.out.println("get all users");
        return users;
    }

    @Override
    public Map<String, String> ansAsk(Map<String, String> data) {
        String trainingName = data.get("trainingName");
        // 确保 isAccepted 和 uid 都有值，避免 NullPointerException 或 NumberFormatException
        String isAcceptedStr = data.get("isAccepted");
        String uidStr = data.get("uid");

        // 检查 isAccepted 和 uid 是否为 null 或空字符串
        if (isAcceptedStr == null || isAcceptedStr.isEmpty()) {
            throw new IllegalArgumentException("isAccepted is required and cannot be null or empty");
        }
        if (uidStr == null || uidStr.isEmpty()) {
            throw new IllegalArgumentException("uid is required and cannot be null or empty");
        }

        int isAccepted = Integer.parseInt(isAcceptedStr);
        int uid = Integer.parseInt(uidStr);

        // Step 1: 更新训练记录的 upload 字段
        UpdateWrapper<Train> updateWrapper = new UpdateWrapper<>();
        System.out.println("trainingName : " + trainingName);
        updateWrapper.eq("trainingname", trainingName).set("upload", isAccepted);

        boolean isUpdated = trainMapper.update(null, updateWrapper) > 0;
        if (isUpdated) {
            // Step 2: 通过 uid 查询 User 对象
            User user = userMapper.selectById(uid);  // 假设 userMapper 可以通过 ID 查询 User
            if (user != null) {
                // Step 3: 更新 User 的 upload 字段，将其减去 1
                int newUploadValue = user.getUploadcnt() - 1;  // 获取当前值并减去 1
                UpdateWrapper<User> updateWrapper1 = new UpdateWrapper<>();
                updateWrapper1.eq("id", uid).set("uploadcnt", newUploadValue);

                boolean isUserUpdated = userMapper.update(null, updateWrapper1) > 0;
                if (isUserUpdated) {
                    System.out.println("成功更新用户上传状态，uploadcnt -1");
                } else {
                    System.out.println("更新用户 uploadcnt 失败");
                }
            } else {
                System.out.println("未找到用户信息，无法更新 uploadcnt");
            }

            System.out.println("成功更新模型上传状态");
        } else {
            System.out.println("未成功更新模型上传状态");
        }

        // 返回成功的消息
        Map<String, String> result = new HashMap<>();
        result.put("error_message", "success");
        return result;
    }


    @Override
    public Map<String, String> delete_user_by_id(Map<String, String> data) {
        int user_id =  Integer.parseInt(data.get("id"));
        userMapper.deleteById(user_id);
        Map<String, String> map = new HashMap<>();
        map.put("error_message", "success");
        return map;
    }

    @Override
    public Map<String, String> add_user_by_name(Map<String, String> data) {
        String username = data.get(("username"));
        String urank = data.get(("urank"));
        String phone = data.get(("phone"));
        String email = data.get(("email"));
        String password = "12345";

        Map<String, String> map = new HashMap<>();
        if (username == null) {
            map.put("error_message", "用户名不能为空");
            return map;
        }

        if (urank == null) {
            urank = "0";
        }

        if (phone == null) {
            phone = "12345";
        }

        if (email == null) {
            email = "12345@163.com";
        }

        username = username.trim();
        if (username.length() == 0) {
            map.put("error_message", "用户名不能为空");
            return map;
        }

        if (username.length() > 100) {
            map.put("error_message", "用户名长度不能大于100");
            return map;
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        List<User> users = userMapper.selectList(queryWrapper);
        if (!users.isEmpty()) {
            map.put("error_message", "用户名已存在");
            return map;
        }

        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(null, username, encodedPassword, urank, phone, email, 0);
//        User user = new User(null, username, encodedPassword);
        userMapper.insert(user);

        map.put("error_message", "success");
        return map;
    }

    @Override
    public Map<String, String> updata_user_by_name(Map<String, String> data) {
        String username = data.get("username");
        String urank = data.get("urank");
        String phone = data.get("phone");
        String email = data.get("email");

        Map<String, String> map = new HashMap<>();
        if (username == null || username.trim().isEmpty()) {
            map.put("error_message", "用户名不能为空");
            return map;
        }

        username = username.trim();
        if (username.length() > 100) {
            map.put("error_message", "用户名长度不能大于100");
            return map;
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User existingUser = userMapper.selectOne(queryWrapper);

        if (existingUser == null) {
            map.put("error_message", "用户不存在");
            return map;
        }

        // Update fields if provided, otherwise retain existing values
        existingUser.setUrank(urank != null ? urank : existingUser.getUrank());
        existingUser.setPhone(phone != null ? phone : existingUser.getPhone());
        existingUser.setEmail(email != null ? email : existingUser.getEmail());

        userMapper.updateById(existingUser);

        map.put("error_message", "success");
        return map;
    }

}
