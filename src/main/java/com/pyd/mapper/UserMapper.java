package com.pyd.mapper;

import com.pyd.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Component
public interface UserMapper extends BaseMapper<User> {

}
