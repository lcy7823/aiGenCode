package com.kaoyu.aicodebackend.controller;

import cn.hutool.core.util.ObjUtil;
import com.kaoyu.aicodebackend.annotation.AuthCheck;
import com.kaoyu.aicodebackend.common.BaseResponse;
import com.kaoyu.aicodebackend.common.DeleteRequest;
import com.kaoyu.aicodebackend.common.ResultUtils;
import com.kaoyu.aicodebackend.constant.UserConstant;
import com.kaoyu.aicodebackend.exception.ErrorCode;
import com.kaoyu.aicodebackend.exception.ThrowUtils;
import com.kaoyu.aicodebackend.model.dto.user.*;
import com.kaoyu.aicodebackend.model.vo.user.LoginUserVo;
import com.kaoyu.aicodebackend.model.vo.user.UserVo;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.kaoyu.aicodebackend.model.entity.User;
import com.kaoyu.aicodebackend.service.UserService;

import java.util.List;

/**
 * 用户 控制层。
 *
 * @author 小烤鱼
 */
@RestController
@RequestMapping("/user")
@MapperScan("com.kaoyu.aicodebackend.mapper")
public class UserController {
    @Resource
    private UserService userService;


    /**
     * 用户注册
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        UserRegisterRequest.check(userRegisterRequest);
        return ResultUtils.success(userService.userRegister(userRegisterRequest));
    }


    /**
     * 用户登录
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVo> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        UserLoginRequest.check(userLoginRequest);
        return ResultUtils.success(userService.userLogin(userLoginRequest, request));
    }

    /**
     * 获取当前登录用户
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVo> getLoginUser(HttpServletRequest request) {
        return ResultUtils.success(LoginUserVo.ToLoginUserVo(userService.getLoginUser(request)));
    }

    /**
     * 用户注销
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 创建用户
     * 管理员权限
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/add")
    public BaseResponse<Long> userAdd(@RequestBody UserAddRequest userAddRequest) {
        return ResultUtils.success(userService.userAdd(userAddRequest));
    }

    /**
     * 删除用户
     * 管理员权限
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/delete")
    public BaseResponse<Boolean> userDelete(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throeIf(deleteRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        boolean result = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 更新用户
     * 管理员权限
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/update")
    public BaseResponse<Boolean> userUpdate(@RequestBody UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throeIf(userUpdateRequest == null || userUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        boolean result = userService.updateById(User.ToUser(userUpdateRequest));
        return ResultUtils.success(result);
    }

    /**
     * 根据id获取用户信息
     * 未脱敏
     * 管理员权限
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/get")
    public BaseResponse<User> getUserById(Long id) {
        ThrowUtils.throeIf(id == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        User user = userService.getById(id);
        ThrowUtils.throeIf(ObjUtil.isNull(user), ErrorCode.PARAMS_ERROR, "用户不存在");
        return ResultUtils.success(user);
    }

    /**
     * 根据id获取用户信息
     * 脱敏
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVo> getUserVoById(Long id) {
        ThrowUtils.throeIf(id == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        User user = userService.getById(id);
        ThrowUtils.throeIf(ObjUtil.isNull(user), ErrorCode.PARAMS_ERROR, "用户不存在");
        return ResultUtils.success(UserVo.ToUserVo(user));
    }

    /**
     * 分页查询用户
     * 需要脱敏
     * 管理员权限
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/list/get/vo")
    public BaseResponse<Page<UserVo>> getUserVoList(@RequestBody UserQueryRequest userQueryRequest) {
        Page<UserVo> userVoList = userService.getUserVoList(userQueryRequest);
        return ResultUtils.success(userVoList);
    }


}
