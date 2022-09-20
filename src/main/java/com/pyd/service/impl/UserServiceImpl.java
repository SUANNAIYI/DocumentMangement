package com.pyd.service.impl;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pyd.entity.User;
import com.pyd.mapper.UserMapper;
import com.pyd.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pyd.util.ShiroUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    UserMapper userMapper;

    // 获取用户列表
    @Override
    public List<User> getUserInfo() {
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.select("id", "username");
        return userMapper.selectList(userQueryWrapper);
    }

    // 获取管理员列表
    @Override
    public List<User> getAdminUserList() {
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("role", "admin").select("id", "username", "role");
        return userMapper.selectList(userQueryWrapper);
    }

    // 忘记密码
    @Override
    public String forgetPwd(String pwd) {
        User user = userMapper.selectById(ShiroUtil.getProfile().getId());
        user.setPassword(SecureUtil.md5(pwd));
        userMapper.updateById(user);
        return "更新成功";
    }

    // 修改密码
    @Override
    public String changePwd(String pwd) {
        User user = userMapper.selectById(ShiroUtil.getProfile().getId());
        user.setPassword(SecureUtil.md5(pwd));
        userMapper.updateById(user);
        return "更新成功";
    }
}
