package org.example.controller;

import java.util.*;

import com.alibaba.fastjson.TypeReference;
import org.example.feign.EnterpriseFeignService;
//import org.example.entity.Enterprise;
import org.example.entity.Store;
import org.example.entity.LoginLog;
import org.example.enums.ResultCodeEnum;
import org.example.result.Result;
import org.example.vo.system.LoginLogVo;
import org.example.service.LoginLogService;
import org.example.utils.PageUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;


/**
 * 登录日志表
 *
 * @author dam
 * @email 1782067308@qq.com
 * @date 2023-02-26 20:41:57
 */
@RestController
@RequestMapping("/system/loginLog")
public class LoginLogController {
    @Autowired
    private LoginLogService loginLogService;
    @Autowired
    private EnterpriseFeignService enterpriseFeignService;

    /**
     * 列表
     */
    @PreAuthorize("hasAuthority('bnt.loginLog.list')")
    @RequestMapping("/list")
    public Result list(@RequestParam Map<String, Object> params, HttpServletRequest httpRequest){
        String token = httpRequest.getHeader("token");

        PageUtils page = loginLogService.queryPage(params,token);

        ////封装数据给前端展示
        List<LoginLog> loginLogEntityList = (List<LoginLog>) page.getList();
        List<LoginLogVo> loginLogVoList = new ArrayList<>();
        Set<Long> storeIdList = new HashSet<>();
       // Set<Long> enterpriseIdList = new HashSet<>();
        for (LoginLog loginLogEntity : loginLogEntityList) {
            LoginLogVo operationLogVo = new LoginLogVo();
            BeanUtils.copyProperties(loginLogEntity, operationLogVo);
            if (loginLogEntity.getStoreId() != null) {
               // enterpriseIdList.add(loginLogEntity.getEnterpriseId());
                storeIdList.add(loginLogEntity.getStoreId());
                loginLogVoList.add(operationLogVo);
            }
        }
//        //设置企业名称
//        Result r1 = enterpriseFeignService.getEnterpriseMapByIdList(new ArrayList<>(enterpriseIdList));
//        if (r1.getCode() == ResultCodeEnum.SUCCESS.getCode().intValue()) {
//            Map<Long, EnterpriseEntity> idAndEnterpriseEntityMap = r1.getData("idAndEnterpriseEntityMap",
//                    new TypeReference<Map<Long, EnterpriseEntity>>() {
//                    });
//            for (LoginLogVo loginLogVo : loginLogVoList) {
//                Long enterpriseId = loginLogVo.getEnterpriseId();
//                loginLogVo.setEnterpriseName(idAndEnterpriseEntityMap.get(enterpriseId).getName());
//            }
//        }
        //设置门店名称
        Result r2 = enterpriseFeignService.getStoreMapByIdList(new ArrayList<>(storeIdList));
        if (r2.getCode() == ResultCodeEnum.SUCCESS.getCode().intValue()) {
            Map<Long, Store> idAndStoreEntityMap = r2.getData("idAndStoreEntityMap",
                    new TypeReference<Map<Long, Store>>() {
                    });
            for (LoginLogVo loginLogVo : loginLogVoList) {
                Long storeId = loginLogVo.getStoreId();
                loginLogVo.setStoreName(idAndStoreEntityMap.get(storeId).getName());
            }
        }
        page.setList(loginLogVoList);

        return Result.ok().addData("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public Result info(@PathVariable("id") Long id){
        LoginLog loginLog = loginLogService.getById(id);

        return Result.ok().addData("loginLog", loginLog);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public Result save(@RequestBody LoginLog loginLog){
        loginLogService.save(loginLog);

        return Result.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public Result update(@RequestBody LoginLog loginLog){
        loginLogService.updateById(loginLog);

        return Result.ok();
    }

    /**
     * 删除
     */
    @PreAuthorize("hasAuthority('bnt.loginLog.delete')")
    @RequestMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody Long[] ids){
        loginLogService.removeByIds(Arrays.asList(ids));

        return Result.ok();
    }

}
