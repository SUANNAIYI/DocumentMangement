package com.pyd.mapper;

import com.pyd.entity.Record;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;


@Component
public interface RecordMapper extends BaseMapper<Record> {

}
