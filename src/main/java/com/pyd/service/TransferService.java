package com.pyd.service;

import com.pyd.entity.Transfer;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.List;

public interface TransferService extends IService<Transfer> {
    Transfer newTransferRec(Long firstID, Long nowID, String action, Long operatorID, LocalDateTime time);
    List<Transfer> showTransfer(Long nowID);
    Long getFirstID(Long nowID);
    Boolean deleteTransferRec(Long nowID);
}
