package com.yupi.springbootinit.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.halcyon.halcyonclientsdk.client.OpenApiClient;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.constant.UserConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.model.dto.interfacesInfo.InterfaceInfoInvokeRequest;
import com.yupi.springbootinit.model.dto.interfacesInfo.InterfacesInfoAddRequest;
import com.yupi.springbootinit.model.dto.interfacesInfo.InterfacesInfoQueryRequest;
import com.yupi.springbootinit.model.dto.interfacesInfo.InterfacesInfoUpdateRequest;
import com.yupi.springbootinit.model.entity.InterfacesInfo;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.model.enums.InterfaceInfoStatusEnum;
import com.yupi.springbootinit.model.vo.InterfacesInfoVO;
import com.yupi.springbootinit.service.InterfacesInfoService;
import com.yupi.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 接口管理
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@RestController
@RequestMapping("/interfacesInfo")
@Slf4j
public class InterfacesInfoController {

    @Resource
    private InterfacesInfoService interfacesInfoService;

    @Resource
    private UserService userService;

    @Resource
    private OpenApiClient openApiClient;

    // region 增删改查

    /**
     * 创建
     *
     * @param interfacesInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addInterfacesInfo(@RequestBody InterfacesInfoAddRequest interfacesInfoAddRequest, HttpServletRequest request) {
        if (interfacesInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfacesInfo interfacesInfo = new InterfacesInfo();
        BeanUtils.copyProperties(interfacesInfoAddRequest, interfacesInfo);
        interfacesInfoService.validInterfacesInfo(interfacesInfo, true);
        User loginUser = userService.getLoginUser(request);
        interfacesInfo.setUserId(loginUser.getId());

        boolean result = interfacesInfoService.save(interfacesInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newInterfacesInfoId = interfacesInfo.getId();
        return ResultUtils.success(newInterfacesInfoId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteInterfacesInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        InterfacesInfo oldInterfacesInfo = interfacesInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfacesInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldInterfacesInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = interfacesInfoService.removeById(id);
        return ResultUtils.success(b);
    }
    /**
     * 更新（仅管理员）
     *
     * @param interfacesInfoUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateInterfacesInfo(@RequestBody InterfacesInfoUpdateRequest interfacesInfoUpdateRequest) {
        if (interfacesInfoUpdateRequest == null || interfacesInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfacesInfo interfacesInfo = new InterfacesInfo();
        BeanUtils.copyProperties(interfacesInfoUpdateRequest, interfacesInfo);
        // 参数校验
        interfacesInfoService.validInterfacesInfo(interfacesInfo, false);
        long id = interfacesInfoUpdateRequest.getId();
        // 判断是否存在
        InterfacesInfo oldInterfacesInfo = interfacesInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfacesInfo == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = interfacesInfoService.updateById(interfacesInfo);
        return ResultUtils.success(result);
    }

    /**
     * 发布接口（仅管理员）
     *
     * @param interfacesInfoUpdateRequest
     * @return
     */
    @PostMapping("/online")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE) // 基于AOP机制校验用户身份
    public BaseResponse<Boolean> onlineInterfaceInfo(@RequestBody InterfacesInfoUpdateRequest interfacesInfoUpdateRequest) {
        if (interfacesInfoUpdateRequest == null || interfacesInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //1.校验接口是否存在
        Long interfaceId = interfacesInfoUpdateRequest.getId();
        InterfacesInfo interfacesInfo = interfacesInfoService.getById(interfaceId);
        if(interfacesInfo == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        //2.判断该接口是否可以调用
        try {
            openApiClient.getNameByPost("HalcyonJX");
        }catch (Exception e){
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }

        //3.更新接口状态，配置枚举类
        interfacesInfo.setId(interfaceId);
        //修改接口数据库中的状态字段为1
        interfacesInfo.setStatus(InterfaceInfoStatusEnum.ONLINE.getValue());

        boolean result = interfacesInfoService.updateById(interfacesInfo);
        return ResultUtils.success(result);
    }
    /**
     * 下线接口（仅管理员）
     *
     * @param interfacesInfoUpdateRequest
     * @return
     */
    @PostMapping("/offline")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> offlineInterfaceInfo(@RequestBody InterfacesInfoUpdateRequest interfacesInfoUpdateRequest,HttpServletRequest request) {
        if (interfacesInfoUpdateRequest == null || interfacesInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //1.校验接口是否存在
        Long interfaceId = interfacesInfoUpdateRequest.getId();
        InterfacesInfo interfacesInfo = interfacesInfoService.getById(interfaceId);
        if(null == interfacesInfo){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        //2.更新接口状态，修改为下线
        interfacesInfo.setId(interfaceId);
        interfacesInfo.setStatus(InterfaceInfoStatusEnum.OFFLINE.getValue());

        boolean result = interfacesInfoService.updateById(interfacesInfo);
        return ResultUtils.success(result);
    }
    /**
     * 测试调用
     *
     * @param interfaceInfoInvokeRequest
     * @param request
     * @return
     */
    @PostMapping("/invoke")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Object> invokeInterfaceInfo(@RequestBody InterfaceInfoInvokeRequest interfaceInfoInvokeRequest, HttpServletRequest request) {
        //检查请求对象是否为空或者接口id是否小于等于0
        if (interfaceInfoInvokeRequest == null || interfaceInfoInvokeRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //1.校验接口是否存在
        Long id = interfaceInfoInvokeRequest.getId();
        // 获取用户请求参数
        String userRequestParams = interfaceInfoInvokeRequest.getUserRequestParams();
        //判断是否存在
        InterfacesInfo interfacesInfo = interfacesInfoService.getById(id);
        if(null == interfacesInfo){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 检查接口是否为下线状态
        if(interfacesInfo.getStatus() == InterfaceInfoStatusEnum.OFFLINE.getValue()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"接口已关闭");
        }
        //获取当前登录用户的ak和sk，这样相当于用户自己的这个身份去调用
        // 也不会但内心它刷接口，因为知道是谁刷了这个接口，会比较安全
        User loginUser = userService.getLoginUser(request);
        String accessKey = loginUser.getAccessKey();
        String secretKey = loginUser.getSecretKey();
        //创建一个临时的ApiClient对象，并传入ak和sk
        OpenApiClient tempApiClient = new OpenApiClient(accessKey, secretKey);
        Gson gson = new Gson();
        com.halcyon.halcyonclientsdk.model.User user = gson.fromJson(userRequestParams, com.halcyon.halcyonclientsdk.model.User.class);
        String userNameByPost = tempApiClient.getUserNameByPost(user);
        //返回成功响应，并包含调用结果
        return ResultUtils.success(userNameByPost);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<InterfacesInfoVO> getInterfacesInfoVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfacesInfo interfacesInfo = interfacesInfoService.getById(id);
        if (interfacesInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        InterfacesInfoVO vo = interfacesInfoService.convert2Vo(interfacesInfo);
        return ResultUtils.success(vo);
    }

    /**
     * 分页获取列表（仅管理员）
     *
     * @param interfacesInfoQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<InterfacesInfo>> listInterfacesInfoByPage(@RequestBody InterfacesInfoQueryRequest interfacesInfoQueryRequest) {
        long current = interfacesInfoQueryRequest.getCurrent();
        long size = interfacesInfoQueryRequest.getPageSize();

        String sortField = interfacesInfoQueryRequest.getSortField();
        String sortOrder = interfacesInfoQueryRequest.getSortOrder();

        InterfacesInfo interfacesInfoQuery = new InterfacesInfo();
        String description = interfacesInfoQuery.getDescription();

        // description 需要支持模糊搜索
        interfacesInfoQuery.setDescription(null);

        //限制爬虫
        if (size > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<InterfacesInfo> queryWrapper = new QueryWrapper<>(interfacesInfoQuery);
        queryWrapper.like(StringUtils.isNotBlank(description), "description", description);
        queryWrapper.orderBy(
                StringUtils.isNotBlank(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        Page<InterfacesInfo> interfacesInfoPage = interfacesInfoService.page(new Page<>(current, size));
//        Page<InterfacesInfo> interfacesInfoPage = interfacesInfoService.page(new Page<>(current, size),
//                interfacesInfoService.getQueryWrapper(interfacesInfoQueryRequest));
        return ResultUtils.success(interfacesInfoPage);
    }

//    /**
//     * 分页获取列表（封装类）
//     *
//     * @param interfacesInfoQueryRequest
//     * @param request
//     * @return
//     */
//    @PostMapping("/list/page/vo")
//    public BaseResponse<Page<InterfacesInfoVO>> listInterfacesInfoVOByPage(@RequestBody InterfacesInfoQueryRequest interfacesInfoQueryRequest,
//            HttpServletRequest request) {
//        long current = interfacesInfoQueryRequest.getCurrent();
//        long size = interfacesInfoQueryRequest.getPageSize();
//        // 限制爬虫
//        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
//        Page<InterfacesInfo> interfacesInfoPage = interfacesInfoService.page(new Page<>(current, size),
//                interfacesInfoService.getQueryWrapper(interfacesInfoQueryRequest));
//        return ResultUtils.success(interfacesInfoService.getInterfacesInfoVOPage(interfacesInfoPage, request));
//    }
//
//    /**
//     * 分页获取当前用户创建的资源列表
//     *
//     * @param interfacesInfoQueryRequest
//     * @param request
//     * @return
//     */
//    @PostMapping("/my/list/page/vo")
//    public BaseResponse<Page<InterfacesInfoVO>> listMyInterfacesInfoVOByPage(@RequestBody InterfacesInfoQueryRequest interfacesInfoQueryRequest,
//            HttpServletRequest request) {
//        if (interfacesInfoQueryRequest == null) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        User loginUser = userService.getLoginUser(request);
//        interfacesInfoQueryRequest.setUserId(loginUser.getId());
//        long current = interfacesInfoQueryRequest.getCurrent();
//        long size = interfacesInfoQueryRequest.getPageSize();
//        // 限制爬虫
//        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
//        Page<InterfacesInfo> interfacesInfoPage = interfacesInfoService.page(new Page<>(current, size),
//                interfacesInfoService.getQueryWrapper(interfacesInfoQueryRequest));
//        return ResultUtils.success(interfacesInfoService.getInterfacesInfoVOPage(interfacesInfoPage, request));
//    }
//
//    // endregion
//
//    /**
//     * 分页搜索（从 ES 查询，封装类）
//     *
//     * @param interfacesInfoQueryRequest
//     * @param request
//     * @return
//     */
//    @PostMapping("/search/page/vo")
//    public BaseResponse<Page<InterfacesInfoVO>> searchInterfacesInfoVOByPage(@RequestBody InterfacesInfoQueryRequest interfacesInfoQueryRequest,
//            HttpServletRequest request) {
//        long size = interfacesInfoQueryRequest.getPageSize();
//        // 限制爬虫
//        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
//        Page<InterfacesInfo> interfacesInfoPage = interfacesInfoService.searchFromEs(interfacesInfoQueryRequest);
//        return ResultUtils.success(interfacesInfoService.getInterfacesInfoVOPage(interfacesInfoPage, request));
//    }
//
//    /**
//     * 编辑（用户）
//     *
//     * @param interfacesInfoEditRequest
//     * @param request
//     * @return
//     */
//    @PostMapping("/edit")
//    public BaseResponse<Boolean> editInterfacesInfo(@RequestBody InterfacesInfoEditRequest interfacesInfoEditRequest, HttpServletRequest request) {
//        if (interfacesInfoEditRequest == null || interfacesInfoEditRequest.getId() <= 0) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        InterfacesInfo interfacesInfo = new InterfacesInfo();
//        BeanUtils.copyProperties(interfacesInfoEditRequest, interfacesInfo);
//        List<String> tags = interfacesInfoEditRequest.getTags();
//        if (tags != null) {
//            interfacesInfo.setTags(JSONUtil.toJsonStr(tags));
//        }
//        // 参数校验
//        interfacesInfoService.validInterfacesInfo(interfacesInfo, false);
//        User loginUser = userService.getLoginUser(request);
//        long id = interfacesInfoEditRequest.getId();
//        // 判断是否存在
//        InterfacesInfo oldInterfacesInfo = interfacesInfoService.getById(id);
//        ThrowUtils.throwIf(oldInterfacesInfo == null, ErrorCode.NOT_FOUND_ERROR);
//        // 仅本人或管理员可编辑
//        if (!oldInterfacesInfo.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
//            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
//        }
//        boolean result = interfacesInfoService.updateById(interfacesInfo);
//        return ResultUtils.success(result);
//    }

}
