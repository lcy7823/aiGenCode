package com.kaoyu.aicodebackend.model.dto.user;

import com.kaoyu.aicodebackend.exception.BusinessException;
import com.kaoyu.aicodebackend.exception.ErrorCode;
import com.kaoyu.aicodebackend.exception.ThrowUtils;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author
 */
@Data
public class UserRegisterRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 确认密码
     */
    private String checkPassword;

    public static void check(UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throeIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR, "参数错误");
        String userAccount = userRegisterRequest.getUserAccount();
        String password = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (userAccount.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能为空");
        }
        if (userAccount.length() > 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度不能大于6位");
        }
        if (password.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不能为空");
        }
        if (password.length() > 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能大于6位");
        }
        if (!checkPassword.equals(password)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码输入不一致");
        }
    }


}
