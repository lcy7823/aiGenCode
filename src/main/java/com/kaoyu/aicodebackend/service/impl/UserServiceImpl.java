package com.kaoyu.aicodebackend.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.kaoyu.aicodebackend.exception.BusinessException;
import com.kaoyu.aicodebackend.exception.ErrorCode;
import com.kaoyu.aicodebackend.exception.ThrowUtils;
import com.kaoyu.aicodebackend.mapper.UserMapper;
import com.kaoyu.aicodebackend.model.dto.user.UserAddRequest;
import com.kaoyu.aicodebackend.model.dto.user.UserLoginRequest;
import com.kaoyu.aicodebackend.model.dto.user.UserQueryRequest;
import com.kaoyu.aicodebackend.model.dto.user.UserRegisterRequest;
import com.kaoyu.aicodebackend.model.entity.User;
import com.kaoyu.aicodebackend.model.enums.UserRoleEnum;
import com.kaoyu.aicodebackend.model.vo.user.LoginUserVo;
import com.kaoyu.aicodebackend.model.vo.user.UserVo;
import com.kaoyu.aicodebackend.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.kaoyu.aicodebackend.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户 服务层实现。
 *
 * @author 小烤鱼
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @Override
    public Long userRegister(UserRegisterRequest userRegisterRequest) {
        String userAccount = userRegisterRequest.getUserAccount();
        String password = userRegisterRequest.getUserPassword();
        //查询用户是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.mapper.selectCountByQuery(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号已存在");
        }
        //对密码进行加密
        String encryptedPassword = User.encryptPassword(password);

        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptedPassword);
        user.setUserName("无名");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean save = this.save(user);
        ThrowUtils.throeIf(!save, ErrorCode.OPERATION_ERROR, "用户注册失败");
        return user.getId();
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @Override
    public LoginUserVo userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request) {
        String userAccount = userLoginRequest.getUserAccount();
        String password = userLoginRequest.getUserPassword();
        //加密密码
        String encryptPassword = User.encryptPassword(password);
        //查询用户是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount)
                .eq("userPassword", encryptPassword);
        User user = this.mapper.selectOneByQuery(queryWrapper);
        ThrowUtils.throeIf(ObjUtil.isNull(user), ErrorCode.PARAMS_ERROR, "用户不存在或者账号密码错误");
        //保存用户信息到session
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return LoginUserVo.ToLoginUserVo(user);
    }


    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (loginUser == null || loginUser.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户未登录");
        }
        User user = this.getById(loginUser.getId());
        ThrowUtils.throeIf(ObjUtil.isNull(user), ErrorCode.PARAMS_ERROR, "用户不存在");
        return loginUser;
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        ThrowUtils.throeIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        //判断是否登录
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (loginUser == null || loginUser.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户未登录");
        }
        //注销用户登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public Long userAdd(UserAddRequest userAddRequest) {
        ThrowUtils.throeIf(userAddRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        User user = User.ToUser(userAddRequest);
        //对密码进行加密
        final String DEFAULT_PASSWORD = "123456";
        String encryptedPassword = User.encryptPassword(DEFAULT_PASSWORD);
        user.setUserPassword(encryptedPassword);
        boolean save = this.save(user);
        ThrowUtils.throeIf(!save, ErrorCode.OPERATION_ERROR, "用户添加失败");
        return user.getId();
    }

    @Override
    public Page<UserVo> getUserVoList(UserQueryRequest userQueryRequest) {
        ThrowUtils.throeIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        QueryWrapper queryWrapper = buildQueryWrapper(userQueryRequest);
        int pageSize = userQueryRequest.getPageSize();
        int pageNum = userQueryRequest.getPageNum();
        Page<User> userPage = this.page(Page.of(pageNum, pageSize), queryWrapper);
        //将userPage中的user转换为userVo
        Page<UserVo> userVoPage = new Page<>(pageNum, pageSize, userPage.getTotalRow());
        List<UserVo> userVoList = userPage.getRecords().stream().map(UserVo::ToUserVo).toList();
        userVoPage.setRecords(userVoList);
        return userVoPage;

    }

    public QueryWrapper buildQueryWrapper(UserQueryRequest userQueryRequest) {
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortFiled = userQueryRequest.getSortFiled();
        String sortOrder = userQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .like("userAccount", userAccount)
                .like("userName", userName)
                .like("userProfile", userProfile)
                .eq("userRole", userRole)
                .orderBy(sortFiled, "ascend".equals(sortOrder));
    }


}
