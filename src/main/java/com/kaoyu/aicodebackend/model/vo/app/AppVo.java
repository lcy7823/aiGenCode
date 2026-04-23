package com.kaoyu.aicodebackend.model.vo.app;

import cn.hutool.core.bean.BeanUtil;
import com.kaoyu.aicodebackend.exception.BusinessException;
import com.kaoyu.aicodebackend.exception.ErrorCode;
import com.kaoyu.aicodebackend.model.entity.App;
import com.kaoyu.aicodebackend.model.vo.user.UserVo;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 应用 脱敏
 *
 * @author 小烤鱼
 */
@Data
public class AppVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;


    /**
     * 应用封面
     */
    private String cover;

    /**
     * 应用初始化的 prompt
     */
    private String initPrompt;

    /**
     * 代码生成类型（枚举）
     */
    private String codeGenType;

    /**
     * 部署标识
     */
    private String deployKey;

    /**
     * 部署时间
     */
    private LocalDateTime deployedTime;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 编辑时间
     */
    private LocalDateTime editTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */

    private LocalDateTime updateTime;

    /**
     * 用户实体类
     */
    private UserVo userVo;

    public static AppVo ToAppVo(App app) {
        if (app == null) {
            return null;
        }
        AppVo appVo = new AppVo();
        BeanUtil.copyProperties(app, appVo);
        return appVo;
    }


}
