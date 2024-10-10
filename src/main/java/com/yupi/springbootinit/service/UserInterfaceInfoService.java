package com.yupi.springbootinit.service;

import com.yupi.springbootinit.model.entity.UserInterfaceInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.vo.UserInterfaceInfoVO;

/**
* @author 张嘉鑫
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service
* @createDate 2024-10-10 14:39:47
*/
public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {
    void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, Boolean flag);

    UserInterfaceInfoVO convert2VO(UserInterfaceInfo userInterfaceInfo);
}
