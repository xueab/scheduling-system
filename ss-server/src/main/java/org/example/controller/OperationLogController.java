package org.example.controller;

import com.alibaba.fastjson.TypeReference;
import org.example.feign.EnterpriseFeignService;
//import org.example.entity.Enterprise;
import org.example.entity.Store;
import org.example.entity.OperationLog;
import org.example.enums.ResultCodeEnum;
import org.example.result.Result;
import org.example.vo.system.OperationLogVo;
import org.example.service.OperationLogService;
import org.example.utils.PageUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;


/**
 * 操作日志表
 *
 * @author dam
 * @email 1782067308@qq.com
 * @date 2023-03-13 16:42:08
 */
@RestController
@RequestMapping("system/operationLog")
public class OperationLogController {
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private EnterpriseFeignService enterpriseFeignService;
    private static final String title = "操作日志管理";

    /**
     * 列表
     */
    @RequestMapping("/list")
    @PreAuthorize("hasAuthority('bnt.operLog.list')")
    public Result list(@RequestParam Map<String, Object> params, HttpServletRequest httpRequest) {

        String token = httpRequest.getHeader("token");

        ////查询数据
        PageUtils page = operationLogService.queryPage(params,token);

        ////封装数据给前端展示
        List<OperationLog> operationLogEntityList = (List<OperationLog>) page.getList();
        List<OperationLogVo> operationLogVoList = new ArrayList<>();
        Set<Long> storeIdList = new HashSet<>();
        //Set<Long> enterpriseIdList = new HashSet<>();
        for (OperationLog operationLog : operationLogEntityList) {
            OperationLogVo operationLogVo = new OperationLogVo();
            BeanUtils.copyProperties(operationLog, operationLogVo);
            if (operationLog.getStoreId() != null) {
               // enterpriseIdList.add(operationLog.getEnterpriseId());
                storeIdList.add(operationLog.getStoreId());
                operationLogVoList.add(operationLogVo);
            }
        }
        //设置企业名称
//        Result r1 = enterpriseFeignService.getEnterpriseMapByIdList(new ArrayList<>(enterpriseIdList));
//        if (r1.getCode() == ResultCodeEnum.SUCCESS.getCode().intValue()) {
//            Map<Long, Enterprise> idAndEnterpriseEntityMap = r1.getData("idAndEnterpriseEntityMap",
//                    new TypeReference<Map<Long, Enterprise>>() {
//                    });
//            for (OperationLogVo operationLogVo : operationLogVoList) {
//                Long enterpriseId = operationLogVo.getEnterpriseId();
//                operationLogVo.setEnterpriseName(idAndEnterpriseEntityMap.get(enterpriseId).getName());
//            }
//        }
        //设置门店名称
        Result r2 = enterpriseFeignService.getStoreMapByIdList(new ArrayList<>(storeIdList));
        if (r2.getCode() == ResultCodeEnum.SUCCESS.getCode().intValue()) {
            Map<Long, Store> idAndStoreEntityMap = r2.getData("idAndStoreEntityMap",
                    new TypeReference<Map<Long, Store>>() {
                    });
            for (OperationLogVo operationLogVo : operationLogVoList) {
                Long storeId = operationLogVo.getStoreId();
                operationLogVo.setStoreName(idAndStoreEntityMap.get(storeId).getName());
            }
        }
        page.setList(operationLogVoList);

        return Result.ok().addData("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    @PreAuthorize("hasAuthority('bnt.operLog.list')")
    public Result info(@PathVariable("id") Long id) {
        OperationLog operationLog = operationLogService.getById(id);

        return Result.ok().addData("operationLog", operationLog);
    }

    /**
     * 保存
     */
//    @RequestMapping("/save")
//    @PreAuthorize("hasAuthority('bnt.operLog.add')")
//    public Result save(@RequestBody OperationLogEntity operationLog) {
//        operationLogService.save(operationLog);
//
//        return Result.ok();
//    }

    /**
     * 修改
     */
//    @RequestMapping("/update")
//    @PreAuthorize("hasAuthority('bnt.operLog.update')")
//    public Result update(@RequestBody OperationLogEntity operationLog) {
//        operationLogService.updateById(operationLog);
//
//        return Result.ok();
//    }

    /**
     * 删除
     */
    @RequestMapping("/deleteBatch")
    @PreAuthorize("hasAuthority('bnt.operLog.delete')")
    public Result delete(@RequestBody Long[] ids) {
        operationLogService.removeByIds(Arrays.asList(ids));
        return Result.ok();
    }

}
