package com.kaoyu.aicodebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.kaoyu.aicodebackend.ai.AiCodeGenTypeRoutingService;
import com.kaoyu.aicodebackend.common.DeleteRequest;
import com.kaoyu.aicodebackend.constant.AppConstant;
import com.kaoyu.aicodebackend.constant.UserConstant;
import com.kaoyu.aicodebackend.exception.BusinessException;
import com.kaoyu.aicodebackend.exception.ErrorCode;
import com.kaoyu.aicodebackend.exception.ThrowUtils;
import com.kaoyu.aicodebackend.facade.AiCodeGeneratorFacade;
import com.kaoyu.aicodebackend.facade.builder.VueProjectBuilder;
import com.kaoyu.aicodebackend.facade.handler.StreamHandleExecutor;
import com.kaoyu.aicodebackend.model.dto.app.*;
import com.kaoyu.aicodebackend.model.entity.User;
import com.kaoyu.aicodebackend.model.enums.CodeTypeEnum;
import com.kaoyu.aicodebackend.model.enums.MessageTypeEnum;
import com.kaoyu.aicodebackend.model.vo.app.AppVo;
import com.kaoyu.aicodebackend.model.vo.user.UserVo;
import com.kaoyu.aicodebackend.service.ChatHistoryService;
import com.kaoyu.aicodebackend.service.ScreenshotService;
import com.kaoyu.aicodebackend.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.kaoyu.aicodebackend.model.entity.App;
import com.kaoyu.aicodebackend.mapper.AppMapper;
import com.kaoyu.aicodebackend.service.AppService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author 小烤鱼
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private StreamHandleExecutor streamHandleExecutor;

    @Resource
    private VueProjectBuilder vueProjectBuilder;
    
    @Resource
    private ScreenshotService screenshotService;

    @Resource
    private AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService;


    /**
     * 聊天生成代码
     *
     * @param appId
     * @param message
     * @param request
     * @return
     */
    @Override
    public Flux<String> chatToGenCode(Long appId, String message, HttpServletRequest request) {
        if (appId == null || message == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用id或消息不能为空");
        }
        App app = this.getById(appId);
        ThrowUtils.throeIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        //验证权限
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null || !loginUser.getId().equals(app.getUserId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户权限不足");
        }
        String codeGenType = app.getCodeGenType();
        CodeTypeEnum codeTypeEnum = CodeTypeEnum.getEnumByValue(codeGenType);
        ThrowUtils.throeIf(codeTypeEnum == null, ErrorCode.PARAMS_ERROR, "不支持的代码类型");
        //保存对话历史
        chatHistoryService.addChatHistory(appId, loginUser.getId(), message, MessageTypeEnum.USER.getValue());
        Flux<String> result = aiCodeGeneratorFacade.generatorAndSaveCodeStreaming(message, codeTypeEnum, appId);
        log.info("生成代码流: {}", result);
        return streamHandleExecutor.doExecute(result, chatHistoryService, appId, loginUser, codeTypeEnum);
    }


    @Override
    public App addAPP(AppAddRequest appAddRequest, HttpServletRequest request) {
        ThrowUtils.throeIf(appAddRequest == null, ErrorCode.PARAMS_ERROR, "初始化提示不能为空");
        String initPrompt = appAddRequest.getInitPrompt();
        User loginUser = userService.getLoginUser(request);
        App app = new App();
        App.copyApp(appAddRequest, app);
        app.setUserId(loginUser.getId());
        //应用名称取initPrompt的前五位
        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 5)));

        //暂时设置为VUE_PROJECT生成    todo 后续根据需求调整
        //app.setCodeGenType(CodeTypeEnum.VUE_PROJECT.getValue());

        CodeTypeEnum codeGenType = aiCodeGenTypeRoutingService.routeCodeType(initPrompt);
        app.setCodeGenType(codeGenType.getValue());
        //存入数据库
        boolean result = this.save(app);
        ThrowUtils.throeIf(!result, ErrorCode.OPERATION_ERROR, "应用添加失败");
        return app;
    }

    @Override
    public void updateAPP(AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throeIf(appUpdateRequest == null || appUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR, "应用更新请求不能为空");
        Long id = appUpdateRequest.getId();
        String appName = appUpdateRequest.getAppName();
        User loginUser = userService.getLoginUser(request);
        App app = this.getById(id);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        //判断应用是否属于当前用户
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "应用不属于当前用户");
        }
        //更新应用名称
        app.setAppName(appName);
        //更新数据库
        boolean result = this.updateById(app);
        ThrowUtils.throeIf(!result, ErrorCode.OPERATION_ERROR, "应用更新失败");
    }

    @Override
    public void deleteAPP(DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throeIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR, "应用删除请求不能为空");
        Long id = deleteRequest.getId();
        User loginUser = userService.getLoginUser(request);
        App app = this.getById(id);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        //判断应用是否属于当前用户 或者管理员可以删除
        if (!app.getUserId().equals(loginUser.getId()) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "权限不够");
        }
        //删除数据库
        boolean result = this.removeById(id);
        //删除应用下的所有对话历史
        try {
            boolean deleteResult = chatHistoryService.deleteChatHistory(app.getId());
        } catch (Exception e) {
            log.info("删除应用下的所有对话历史失败，应用ID：{}", e.getMessage());
        }
        ThrowUtils.throeIf(!result, ErrorCode.OPERATION_ERROR, "应用删除失败");
    }

    /**
     * 根据id查询应用vo
     *
     * @param id
     * @return
     */
    @Override
    public AppVo getAppVoById(long id) {
        ThrowUtils.throeIf(id <= 0, ErrorCode.PARAMS_ERROR, "应用id不能为空");
        App app = this.getById(id);
        return AppVo.ToAppVo(app);
    }

    /**
     * 查询我的应用列表
     *
     * @param appQueryRequest
     * @param request
     * @return
     */
    @Override
    public Page<AppVo> getMyAppVoListPage(AppQueryRequest appQueryRequest, HttpServletRequest request) {
        ThrowUtils.throeIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR, "应用查询请求不能为空");
        int pageNum = appQueryRequest.getPageNum();
        int pageSize = appQueryRequest.getPageSize();
        //限制每页数量最多20
        ThrowUtils.throeIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页数量最多20");
        User loginUser = userService.getLoginUser(request);
        //只查询当前登录用户的应用
        appQueryRequest.setUserId(loginUser.getId());
        //创建查询条件
        QueryWrapper queryWrapper = this.getQueryWrapper(appQueryRequest);
        //查询应用列表
        Page<App> page = this.page(new Page<>(pageNum, pageSize), queryWrapper);
        Page<AppVo> pageVo = new Page<>(pageNum, pageSize, page.getTotalRow());
        List<AppVo> appVoList = this.getAppVoList(page.getRecords());
        pageVo.setRecords(appVoList);
        return pageVo;
    }

    /**
     * 获取应用列表
     *
     * @param appList
     * @return
     */
    @Override
    public List<AppVo> getAppVoList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        //查询获取用户vo列表，根据userId获取并赋值到appVo中的userVo
        Set<Long> userIds = appList.stream().map(App::getUserId).collect(Collectors.toSet());
        //根据userId查询用户vo列表，将用户vo赋值到应用vo中
        Map<Long, UserVo> userVoMap = userService.listByIds(userIds).stream().collect(Collectors.toMap(User::getId, UserVo::ToUserVo));
        return appList.stream().map(app -> {
            AppVo appVo = AppVo.ToAppVo(app);
            appVo.setUserVo(userVoMap.get(app.getUserId()));
            return appVo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取应用查询条件
     *
     * @param appQueryRequest
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortFiled = appQueryRequest.getSortFiled();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create().
                eq("userId", userId)
                .eq("Id", id)
                .like("userName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("deployKey", deployKey)
                .eq("codeGenType", codeGenType)
                .eq("priority", priority)
                .orderBy(sortFiled, "ascend".equals(sortOrder));
    }

    /**
     * 查询精选应用列表
     *
     * @param appQueryRequest
     * @param request
     * @return
     */
    @Override
    public Page<AppVo> getSiftAppVoListPage(AppQueryRequest appQueryRequest, HttpServletRequest request) {
        ThrowUtils.throeIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR, "应用查询请求不能为空");
        int pageNum = appQueryRequest.getPageNum();
        int pageSize = appQueryRequest.getPageSize();
        //限制每页数量最多20
        ThrowUtils.throeIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页数量最多20");
        User loginUser = userService.getLoginUser(request);
        appQueryRequest.setPriority(AppConstant.GOOD_APP_PRIORITY);
        //创建查询条件
        QueryWrapper queryWrapper = this.getQueryWrapper(appQueryRequest);
        //查询应用列表
        Page<App> page = this.page(Page.of(pageNum, pageSize), queryWrapper);
        Page<AppVo> pageVo = new Page<>(pageNum, pageSize, page.getTotalRow());
        List<AppVo> appVoList = this.getAppVoList(page.getRecords());
        pageVo.setRecords(appVoList);
        return pageVo;
    }

    /**
     * 删除应用（仅管理员可用）
     * 删除任意应用
     *
     * @param deleteRequest
     * @param request
     */
    @Override
    public void deleteAdminAPP(DeleteRequest deleteRequest, HttpServletRequest request) {
        this.deleteAPP(deleteRequest, request);
    }

    /**
     * 更新应用（仅管理员可用）
     * 更新任意字段
     *
     * @param appAdminUpdateRequest
     * @param request
     */
    @Override
    public void updateAdminAPP(AppAdminUpdateRequest appAdminUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throeIf(appAdminUpdateRequest == null, ErrorCode.PARAMS_ERROR, "应用应用更新请求不能为空");
        Long id = appAdminUpdateRequest.getId();
        App app = this.getById(id);
        ThrowUtils.throeIf(ObjUtil.isNull(app), ErrorCode.PARAMS_ERROR, "应用不存在");
        App.copyApp(appAdminUpdateRequest, app);
        app.setEditTime(LocalDateTime.now());
        boolean result = this.updateById(app);
        ThrowUtils.throeIf(!result, ErrorCode.PARAMS_ERROR, "更新应用失败");
    }

    @Override
    public Page<AppVo> getAdminAppVoListPage(AppQueryRequest appQueryRequest, HttpServletRequest request) {
        ThrowUtils.throeIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR, "应用查询请求不能为空");
        int pageNum = appQueryRequest.getPageNum();
        int pageSize = appQueryRequest.getPageSize();
        //限制每页数量最多20
        ThrowUtils.throeIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页数量最多20");
        //创建查询条件
        QueryWrapper queryWrapper = this.getQueryWrapper(appQueryRequest);
        //查询应用列表
        Page<App> page = this.page(Page.of(pageNum, pageSize), queryWrapper);
        //创建应用vo分页
        Page<AppVo> pageAppVo = new Page<>(pageNum, pageSize, page.getTotalRow());
        //转换为应用vo列表
        List<App> appList = page.getRecords();
        List<AppVo> appVoList = this.getAppVoList(appList);
        pageAppVo.setRecords(appVoList);
        return pageAppVo;
    }


    @Override
    public String deployApp(AppDeployRequest appDeployRequest, HttpServletRequest request) {
        ThrowUtils.throeIf(appDeployRequest == null, ErrorCode.PARAMS_ERROR, "应用部署请求不能为空");
        Long appId = appDeployRequest.getAppId();
        ThrowUtils.throeIf(appId <= 0, ErrorCode.PARAMS_ERROR, "参数错误");
        App app = this.getById(appId);
        ThrowUtils.throeIf(ObjUtil.isNull(app), ErrorCode.PARAMS_ERROR, "应用不存在");
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throeIf(ObjUtil.isNull(loginUser), ErrorCode.NO_AUTH_ERROR, "未登录用户");
        //验证权限
        if (!loginUser.getId().equals(app.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有权限部署应用");
        }
        //查询部署标识
        String deployKey = app.getDeployKey();
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        //先获取文件路径
        String codeGenType = app.getCodeGenType();
        String fileName = codeGenType + "_" + app.getId();
        String filePath = AppConstant.CODE_OUTPUT_DIR + File.separator + fileName;
        //验证文件是否存在
        File sourceDir = new File(filePath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用代码不存在");
        }
        //vue项目构建
        CodeTypeEnum codeTypeEnum = CodeTypeEnum.getEnumByValue(codeGenType);
        if (codeTypeEnum == CodeTypeEnum.VUE_PROJECT) {
            boolean buildSuccess = vueProjectBuilder.buildProject(filePath);
            ThrowUtils.throeIf(!buildSuccess, ErrorCode.PARAMS_ERROR, "Vue项目构建失败");
            //验证dist目录是否存在
            File distDir = new File(filePath, "dist");
            ThrowUtils.throeIf(!distDir.exists(), ErrorCode.PARAMS_ERROR, "dist目录未生成");
            //将dist作为部署源
            sourceDir=distDir;
            log.info("vue项目构建成功，部署源为{}", distDir.getAbsolutePath());
        }

        //部署目录
        String deployDir = AppConstant.CODE_DEPLOY_DIR + File.separator + deployKey;
        //移动文件到部署目录
        try {
            FileUtil.copyContent(sourceDir, new File(deployDir), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用代码部署失败");
        }
        //更新数据库
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean result = this.updateById(updateApp);
        ThrowUtils.throeIf(!result, ErrorCode.PARAMS_ERROR, "更新应用部署失败");
        String appDeployUrl = String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
        //异步生成截图并更新应用封面
        generateAppScreenshotAsync(appId,appDeployUrl);
        //返回部署url
        return appDeployUrl;
    }

    /**
     * 异步生成应用封面
     * 1. 生成截图并上传到cos
     * 2. 更新应用封面
     *
     * @param appId 应用id
     * @param appDeployUrl 应用部署url
     */
    private void generateAppScreenshotAsync(Long appId, String appDeployUrl) {
        //异步生成应用封面,虚拟线程异步执行
        Thread.startVirtualThread(()->{
           //生成截图并上传
            String cosUrl = screenshotService.generateAndUploadScreenshot(appDeployUrl);
            //获取url并更新
            App app = new App();
            app.setId(appId);
            app.setCover(cosUrl);
            boolean result = this.updateById(app);
            ThrowUtils.throeIf(!result, ErrorCode.PARAMS_ERROR, "更新应用封面失败");
        });
    }


}
