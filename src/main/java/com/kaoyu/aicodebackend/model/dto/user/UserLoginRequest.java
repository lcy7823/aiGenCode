package com.kaoyu.aicodebackend.model.dto.user;

import cn.hutool.core.util.StrUtil;
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
public class UserLoginRequest implements Serializable {


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


    public static void check(UserLoginRequest userLoginRequest) {
        ThrowUtils.throeIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR, "参数错误");
        String userAccount = userLoginRequest.getUserAccount();
        String password = userLoginRequest.getUserPassword();
        if (StrUtil.hasBlank(userAccount, password)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码不能为空");
        }
        if (userAccount.length() > 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (password.length() > 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
    }
}
