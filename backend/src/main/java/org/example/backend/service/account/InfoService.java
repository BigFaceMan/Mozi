package org.example.backend.service.account;

import org.example.backend.pojo.User;

import java.util.List;
import java.util.Map;

public interface InfoService {
    public Map<String, String> getinfo();
    public List<User> getlist();
    public Map<String, String> ansSceneAsk(Map<String, String> data);
    Map<String, String> ansAsk(Map<String, String> data);
    Map<String, String> delete_user_by_id(Map<String, String> data);
    Map<String, String> add_user_by_name(Map<String, String> data);
    Map<String, String> updata_user_by_name(Map<String, String> data);
}
