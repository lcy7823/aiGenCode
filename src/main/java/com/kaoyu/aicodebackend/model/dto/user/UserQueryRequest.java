package com.kaoyu.aicodebackend.model.dto.user;

import cn.hutool.core.bean.BeanUtil;
import com.kaoyu.aicodebackend.common.PageRequest;
import com.kaoyu.aicodebackend.model.entity.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户 实体类。
 *
 * @author 小烤鱼
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;


    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户简介
     */

    private String userProfile;

    /**
     * 用户角色
     * user/admin
     */
    private String userRole;


}
