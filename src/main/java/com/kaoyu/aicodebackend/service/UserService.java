package com.kaoyu.aicodebackend.service;

import com.kaoyu.aicodebackend.model.dto.user.UserAddRequest;
import com.kaoyu.aicodebackend.model.dto.user.UserLoginRequest;
import com.kaoyu.aicodebackend.model.dto.user.UserQueryRequest;
import com.kaoyu.aicodebackend.model.dto.user.UserRegisterRequest;
import com.kaoyu.aicodebackend.model.vo.user.LoginUserVo;
import com.kaoyu.aicodebackend.model.vo.user.UserVo;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.kaoyu.aicodebackend.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户 服务层。
 *
 * @author 小烤鱼
 */

public interface UserService extends IService<User> {

    Long userRegister(UserRegisterRequest userRegisterRequest);


    LoginUserVo userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request);

    User getLoginUser(HttpServletRequest request);

    boolean userLogout(HttpServletRequest request);

    Long userAdd(UserAddRequest userAddRequest);

    Page<UserVo> getUserVoList(UserQueryRequest userQueryRequest);
}
