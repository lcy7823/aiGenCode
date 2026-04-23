package com.kaoyu.aicodebackend.model.entity;

import cn.hutool.core.bean.BeanUtil;
import com.kaoyu.aicodebackend.constant.UserConstant;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;

import java.io.Serializable;
import java.time.LocalDateTime;

import java.io.Serial;

import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.DigestUtils;

/**
 * 用户 实体类。
 *
 * @author 小烤鱼
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 账号
     */
    @Column("userAccount")
    private String userAccount;

    /**
     * 密码
     */
    @Column("userPassword")
    private String userPassword;

    /**
     * 用户昵称
     */
    @Column("userName")
    private String userName;

    /**
     * 用户头像
     */
    @Column("userAvatar")
    private String userAvatar;

    /**
     * 用户简介
     */
    @Column("userProfile")
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    @Column("userRole")
    private String userRole;

    /**
     * 编辑时间
     */
    @Column("editTime")
    private LocalDateTime editTime;

    /**
     * 创建时间
     */
    @Column("createTime")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column("updateTime")
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @Column(value = "isDelete", isLogicDelete = true)
    private Integer isDelete;

    /**
     * 对密码进行md5加密
     */
    public static String encryptPassword(String password) {
        final String SALT = "kaoYu";
        return DigestUtils.md5DigestAsHex((SALT + password).getBytes());
    }

    /**
     * 将请求类转换为User实体类
     */
    public static User ToUser(Object object) {
        if (object == null) {
            return null;
        }
        User user = new User();
        BeanUtil.copyProperties(object, user);
        return user;
    }

    //判断是否为管理员
    public static boolean isAdmin(User user) {
        return UserConstant.ADMIN_ROLE.equals(user.getUserRole());
    }


}
