package com.yupi.springbootinit.service;

import com.yupi.springbootinit.model.entity.InterfacesInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 张嘉鑫
* @description 针对表【interfaces_info(接口信息表)】的数据库操作Service
* @createDate 2024-09-29 19:16:50
*/
public interface InterfacesInfoService extends IService<InterfacesInfo> {
    /**
     * 校验
     * @param interfacesInfo
     * @param add
     */
    void validInterfacesInfo(InterfacesInfo interfacesInfo, boolean add);
}
