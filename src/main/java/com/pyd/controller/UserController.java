package com.pyd.controller;

import cn.hutool.core.map.MapUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pyd.common.Result;
import com.pyd.common.LoginDto;
import com.pyd.entity.User;
import com.pyd.mapper.UserMapper;
import com.pyd.service.DocService;
import com.pyd.service.FolderService;
import com.pyd.service.UserService;
import com.pyd.util.JwtUtils;
import com.pyd.util.ShiroUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Api(tags = "用户模块")
@RequestMapping("/user")
@RestController
public class UserController {

    @Autowired
    UserMapper userMapper;

    @Autowired
    UserService userService;

    @Autowired
    FolderService folderService;

    @Autowired
    DocService docService;

    @Autowired
    JwtUtils jwtUtils;

    // 用户名判重
    private Boolean checkUserNameIsUnique(User user) {
        LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
        lqw.eq(User::getUsername, user.getUsername());
        List<User> userList = userMapper.selectList(lqw);
        return userList.size() == 0;
    }

    @ApiOperation(value = "用户注册")
    @PostMapping("/register")
    public Result register(@Validated @RequestBody User user){
        if (this.checkUserNameIsUnique(user)){
            user.setPassword(SecureUtil.md5(user.getPassword()));
            user.setEmail(user.getEmail());
            user.setRole("user");
            userMapper.insert(user);
            return Result.succ(MapUtil.builder()
                    .put("id", user.getId())
                    .put("username", user.getUsername())
                    .put("email", user.getEmail())
                    .put("role", user.getRole())
                    .map()
            );
        }else {
            return Result.fail("用户名已存在");
        }
    }

    @ApiOperation(value = "用户登录")
    @PostMapping("/login")
    public Result login(@Validated @RequestBody LoginDto loginDto, HttpServletResponse response) {
        User user = userService.getOne(new QueryWrapper<User>().eq("username", loginDto.getUsername())); // 判断用户是否存在
        Assert.notNull(user, "用户不存在");
        String password = SecureUtil.md5(loginDto.getPassword());
        if(!user.getPassword().equals(password)){
            return Result.fail("密码不正确");
        }
        String jwt = jwtUtils.generateToken(user.getId());
        response.setHeader("Authorization", jwt);
        response.setHeader("Access-control-Expose-Headers", "Authorization");
        return Result.succ(MapUtil.builder()
                .put("id", user.getId())
                .put("username", user.getUsername())
                .put("email", user.getEmail())
                .put("token",jwt)
                .map()
        );
    }

    @ApiOperation(value = "用户登出")
    @GetMapping("/logout")
    public Result logout() {
        SecurityUtils.getSubject().logout();
        return Result.succ(null);
    }

    @ApiOperation(value = "忘记密码")
    @GetMapping("/pwd/{inf}")
    public Result forgetPwd(@PathVariable String inf){
        String[] str = inf.split(","); // 邮箱 + 新密码
        if (str[0].equals(ShiroUtil.getProfile().getEmail())){
            return Result.succ(userService.forgetPwd(str[1]));
        }else {
            return Result.fail("邮箱验证错误");
        }
    }

    @ApiOperation(value = "修改密码")
    @GetMapping("/change/{pwds}")
    public Result changePwd(@PathVariable String pwds){
        String[] str = pwds.split(",");  // 原密码 + 新密码
        String rawPwd = userMapper.selectById(ShiroUtil.getProfile().getId()).getPassword();
        if (SecureUtil.md5(str[0]).equals(rawPwd)){
            return Result.succ(userService.changePwd(str[1]));
        }else {
            return Result.fail("原密码错误");
        }
    }

    @ApiOperation(value = "注销账户")
    @GetMapping("/cancel/{id}")
    public Result cancelAccount(@PathVariable Long id){
        Long receiveFolder = docService.searchFolder("接收文件", ShiroUtil.getProfile().getId());
        Long sendFolder = docService.searchFolder("发送文件", ShiroUtil.getProfile().getId());
        folderService.deleteFolder(sendFolder);
        folderService.deleteFolder(receiveFolder);
        return Result.succ(userMapper.deleteById(id));
    }

    @ApiOperation(value = "获取用户列表")
    @GetMapping("/search")
    public Result getUserInfo(){
        return Result.succ(userService.getUserInfo());
    }

    @ApiOperation(value = "获取管理员列表")
    @GetMapping("/admin")
    public Result getAdminUserList(){
        return Result.succ(userService.getAdminUserList());
    }

    @ApiOperation(value = "授予管理员权限")
    @GetMapping("/authorize/{id}")
    public Result authorizeAdmin(@PathVariable Long id){
        User user = userMapper.selectById(id);
        if (user.getRole().equals("admin")){
            return Result.fail("该用户已是管理员");
        }else {
            user.setRole("admin");
            return Result.succ(userMapper.updateById(user));
        }
    }
}
