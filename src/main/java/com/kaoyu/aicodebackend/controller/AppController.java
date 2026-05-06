package com.kaoyu.aicodebackend.controller;

import cn.hutool.json.JSONUtil;
import com.kaoyu.aicodebackend.annotation.AuthCheck;
import com.kaoyu.aicodebackend.common.BaseResponse;
import com.kaoyu.aicodebackend.common.DeleteRequest;
import com.kaoyu.aicodebackend.common.ResultUtils;
import com.kaoyu.aicodebackend.constant.UserConstant;
import com.kaoyu.aicodebackend.model.dto.app.*;
import com.kaoyu.aicodebackend.model.vo.app.AppVo;
import com.kaoyu.aicodebackend.rateLimit.annotation.RateLimit;
import com.kaoyu.aicodebackend.rateLimit.enums.RateLimitType;
import com.kaoyu.aicodebackend.service.ProjectDownloadService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import com.kaoyu.aicodebackend.model.entity.App;
import com.kaoyu.aicodebackend.service.AppService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 应用 控制层。
 *
 * @author 小烤鱼
 */
@RestController
@RequestMapping("/app")
public class AppController {

    @Resource
    private AppService appService;

    @Resource
    private ProjectDownloadService projectDownloadService;


    /**
     * 添加应用
     *
     * @param appAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        App app = appService.addAPP(appAddRequest, request);
        return ResultUtils.success(app.getId());
    }

    /**
     * 更新应用
     *
     * @param appUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateApp(@RequestBody AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        appService.updateAPP(appUpdateRequest, request);
        return ResultUtils.success(true);
    }

    /**
     * 删除应用
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        appService.deleteAPP(deleteRequest, request);
        return ResultUtils.success(true);
    }

    /**
     * 获取应用详情
     *
     * @param id
     * @return
     */
    @GetMapping("/get/Vo")
    public BaseResponse<AppVo> getAppVoById(long id) {
        AppVo appVo = appService.getAppVoById(id);
        return ResultUtils.success(appVo);
    }

    /**
     * 获取自己创建的应用列表
     *
     * @param appQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/Vo/list/page")
    public BaseResponse<Page<AppVo>> getMyAppVoListPage(@RequestBody AppQueryRequest appQueryRequest, HttpServletRequest request) {
        Page<AppVo> appVoPage = appService.getMyAppVoListPage(appQueryRequest, request);
        return ResultUtils.success(appVoPage);
    }

    /**
     * 获取精选应用列表
     *
     * @param appQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/sift/Vo/list/page")
    @Cacheable(
            value = "good_app_page",
            key = "T(com.kaoyu.aicodebackend.utils.CacheKeyUtils).generateCacheKey(#appQueryRequest)",
            condition = "#appQueryRequest.pageNum<=10"
    )
    public BaseResponse<Page<AppVo>> getSiftAppVoListPage(@RequestBody AppQueryRequest appQueryRequest, HttpServletRequest request) {
        Page<AppVo> appVoPage = appService.getSiftAppVoListPage(appQueryRequest, request);
        return ResultUtils.success(appVoPage);
    }

    /**
     * 删除应用
     * 仅管理员可用
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/admin/delete")
    public BaseResponse<Boolean> deleteAdminApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        appService.deleteAdminAPP(deleteRequest, request);
        return ResultUtils.success(true);
    }

    /**
     * 更新应用
     * 仅管理员可用
     *
     * @param appAdminUpdateRequest
     * @param request
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/admin/update")
    public BaseResponse<Boolean> updateAdminApp(@RequestBody AppAdminUpdateRequest appAdminUpdateRequest, HttpServletRequest request) {
        appService.updateAdminAPP(appAdminUpdateRequest, request);
        return ResultUtils.success(true);
    }

    /**
     * 获取应用列表
     * 仅管理员可用
     *
     * @param appQueryRequest
     * @param request
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/admin/list/vo/page")
    public BaseResponse<Page<AppVo>> getAdminAppVoListPage(@RequestBody AppQueryRequest appQueryRequest, HttpServletRequest request) {
        Page<AppVo> appVoPage = appService.getAdminAppVoListPage(appQueryRequest, request);
        return ResultUtils.success(appVoPage);
    }

    /**
     * 根据id获取应用详情
     * 仅管理员可用
     *
     * @param id
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/admin/get/Vo")
    public BaseResponse<AppVo> getAdminAppVoById(long id) {
        AppVo appVo = appService.getAppVoById(id);
        return ResultUtils.success(appVo);
    }

    /**
     * 对话生成代码
     *
     * @param appId
     * @param message
     * @param request
     * @return
     */
    @RateLimit(limitType = RateLimitType.USER, rate = 5, rateInterval = 60,message = "AI 对话请求频率过快，请稍后重试！")
    @GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatToGenCode(@RequestParam Long appId,
                                                       @RequestParam String message,
                                                       HttpServletRequest request) {
        //空格传给前端会丢失，需要封装一层，转换为ServerSentEvent
        Flux<String> chatToGenCode = appService.chatToGenCode(appId, message, request);
        return chatToGenCode.map(chunk -> {
            //将内容包装为JSON对象
            Map<String, String> wrapper = Map.of("d", chunk);
            String jsonStr = JSONUtil.toJsonStr(wrapper);
            return ServerSentEvent.<String>builder()
                    .data(jsonStr)
                    .build();
        }).concatWith(Mono.just(
                ServerSentEvent.<String>builder()
                        .event("done")
                        .data("")
                        .build()
        ));
    }

    /**
     * 部署应用
     *
     * @param appDeployRequest
     * @param request
     * @return
     */
    @PostMapping("/deploy")
    public BaseResponse<String> deployApp(@RequestBody AppDeployRequest appDeployRequest, HttpServletRequest request) {
        String deployUrl = appService.deployApp(appDeployRequest, request);
        return ResultUtils.success(deployUrl);
    }

    /**
     * 下载应用代码
     *
     * @param appId
     * @param request
     * @param response
     */
    @GetMapping("/download/{appId}")
    public void downloadApp(@PathVariable Long appId,
                            HttpServletRequest request,
                            HttpServletResponse response) {
        projectDownloadService.downloadProject(appId, request, response);
    }


}
