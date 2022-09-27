package com.pyd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pyd.entity.Transfer;
import com.pyd.mapper.TransferMapper;
import com.pyd.mapper.UserMapper;
import com.pyd.service.TransferService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class TransferServiceImpl extends ServiceImpl<TransferMapper, Transfer> implements TransferService {

    @Autowired
    TransferMapper transferMapper;

    @Autowired
    UserMapper userMapper;

    //新增文件流转记录
    @Override
    public Transfer newTransferRec(Long firstID, Long nowID, String action, Long operatorID, LocalDateTime time) {
        Transfer transfer = new Transfer();
        transfer.setFirstID(firstID);
        transfer.setNowID(nowID);
        transfer.setAction(action);
        transfer.setOperatorID(operatorID);
        transfer.setTime(time);
        transferMapper.insert(transfer);
        return transfer;
    }

    //展示文件流转记录
    @Override
    public List<Transfer> showTransfer(Long nowID) {
        Long firstID = getFirstID(nowID);
        QueryWrapper<Transfer> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("firstID", firstID);
        List<Transfer> transferList = transferMapper.selectList(queryWrapper);
        for (Transfer transfer : transferList){
            transfer.setOperator(userMapper.selectById(transfer.getOperatorID()).getUsername());  // 设置操作者姓名
        }
        return transferList;
    }

    // 获取根文件ID
    @Override
    public Long getFirstID(Long nowID) {
        List<Transfer> nowDocList = transferMapper.selectList(new QueryWrapper<Transfer>().eq("nowID", nowID));
        if (nowDocList.isEmpty()){
            return null;
        }else {
            return nowDocList.get(0).getFirstID();
        }
    }

    // 删除文件流转记录
    @Override
    public Boolean deleteTransferRec(Long nowID) {
        QueryWrapper<Transfer> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("nowID", nowID);
        transferMapper.delete(queryWrapper);
        return true;
    }
}
