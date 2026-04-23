package com.kaoyu.aicodebackend.service;

import com.kaoyu.aicodebackend.common.DeleteRequest;
import com.kaoyu.aicodebackend.model.dto.app.*;
import com.kaoyu.aicodebackend.model.vo.app.AppVo;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.kaoyu.aicodebackend.model.entity.App;
import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author 小烤鱼
 */
public interface AppService extends IService<App> {

    /**
     * 创建应用
     *
     * @param appAddRequest
     * @param request
     * @return
     */
    App addAPP(AppAddRequest appAddRequest, HttpServletRequest request);

    /**
     * 更新应用
     *
     * @param appUpdateRequest
     * @param request
     * @return
     */
    void updateAPP(AppUpdateRequest appUpdateRequest, HttpServletRequest request);

    void deleteAPP(DeleteRequest deleteRequest, HttpServletRequest request);

    AppVo getAppVoById(long id);

    Page<AppVo> getMyAppVoListPage(AppQueryRequest appQueryRequest, HttpServletRequest request);

    List<AppVo> getAppVoList(List<App> records);

    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    Page<AppVo> getSiftAppVoListPage(AppQueryRequest appQueryRequest, HttpServletRequest request);

    void deleteAdminAPP(DeleteRequest deleteRequest, HttpServletRequest request);

    void updateAdminAPP(AppAdminUpdateRequest appAdminUpdateRequest, HttpServletRequest request);

    Page<AppVo> getAdminAppVoListPage(AppQueryRequest appQueryRequest, HttpServletRequest request);

    Flux<String> chatToGenCode(Long appId, String message, HttpServletRequest request);

    String deployApp(AppDeployRequest appDeployRequest, HttpServletRequest request);
}
