package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.model.entity.UserInterfaceInfo;
import com.yupi.springbootinit.model.vo.UserInterfaceInfoVO;
import com.yupi.springbootinit.service.UserInterfaceInfoService;
import com.yupi.springbootinit.mapper.UserInterfaceInfoMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
* @author 张嘉鑫
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service实现
* @createDate 2024-10-10 14:39:47
*/
@Service
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
    implements UserInterfaceInfoService{
    @Override
    public void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, Boolean add) {
        if (userInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //id 不能为空
        Long id = userInterfaceInfo.getId();
        if (id == null || id < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口Id不能为空!");
        }

        //调用用户的 Id 不能为空
        Long userId = userInterfaceInfo.getUserId();
        if (userId == null || userId < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户Id不能为空!");
        }

        //总共可调用次数不少于 0
        Integer totalNum = userInterfaceInfo.getTotalNum();
        Integer leftNum = userInterfaceInfo.getLeftNum();

        if (leftNum == null || leftNum < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "剩余可调用次数不少于 0");
        }

        Integer isDelete = userInterfaceInfo.getIsDelete();
        if (null == isDelete || (1 != isDelete && 0 != isDelete)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "更新删除状态不正确");
        }

        // 创建时，参数不能为空
        if (add) {
            if (StringUtils.isAnyBlank(totalNum.toString())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "总共可调用次数不能为空");
            }
        }
    }

    @Override
    public UserInterfaceInfoVO convert2VO(UserInterfaceInfo userInterfaceInfo) {
        UserInterfaceInfoVO VO = new UserInterfaceInfoVO();
        BeanUtils.copyProperties(userInterfaceInfo, VO);
        return VO;
    }

    @Override
    public boolean invokeCount(long interfaceInfoId, long userId) {
        //判断(其实这里还应该校验存不存在，这里就不用校验了，因为它不存在，也更新不到那条记录)
        if(interfaceInfoId <= 0 || userId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //使用 UpdateWrapper 对象来构建更新条件
        UpdateWrapper<UserInterfaceInfo> updateWrapper = new UpdateWrapper<>();
        //在 updateWrapper 中设置了两个条件：interfaceInfoId 等于给定的 interfaceInfoId 和 userId 等于给定的 userId。
        updateWrapper.eq("interfaceInfoId", interfaceInfoId);
        updateWrapper.eq("userId", userId);
        //setSql 方法用于设置要更新的 SQL 语句。这里通过 SQL 表达式实现了两个字段的更新操作：
        //leftNum=leftNum-1和totalNum=totalNum+1。意思是将leftNum字段减一，totalNum字段加一。
        updateWrapper.setSql("leftNum=leftNum-1,totalNum=totalNum+1");
        //最后，执行 update 方法，返回一个布尔值，表示更新是否成功。
        return this.update(updateWrapper);
    }
}




