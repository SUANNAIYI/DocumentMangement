package com.pyd.service;

import com.pyd.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;


public interface UserService extends IService<User> {
    List<User> getUserInfo();
    List<User> getAdminUserList();
    String forgetPwd(String pwd);
    String changePwd(String pwd);
}
