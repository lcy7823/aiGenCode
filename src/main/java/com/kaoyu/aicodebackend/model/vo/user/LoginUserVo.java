package com.kaoyu.aicodebackend.model.vo.user;

import cn.hutool.core.bean.BeanUtil;
import com.kaoyu.aicodebackend.model.entity.User;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户 实体类。
 *
 * @author 小烤鱼
 */
@Data
public class LoginUserVo implements Serializable {

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
     * 用户头像
     */

    private String userAvatar;

    /**
     * 用户简介
     */

    private String userProfile;

    /**
     * 用户角色
     * user/admin
     */
    private String userRole;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


    /**
     * 将user转为LoginUserVo
     */
    public static LoginUserVo ToLoginUserVo(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVo loginUserVo = new LoginUserVo();
        BeanUtil.copyProperties(user, loginUserVo);
        return loginUserVo;
    }
}
