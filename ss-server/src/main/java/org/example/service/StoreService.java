package org.example.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.Store;
import org.example.utils.PageUtils;

import java.util.Map;
//门店管理
/**
 * 门店表
 *
 * @author dam
 * @email 1782067308@qq.com
 * @date 2023-02-09 11:17:26
 */
public interface StoreService extends IService<Store> {

    PageUtils queryPage(Map<String, Object> params, QueryWrapper<Store> wrapper);
}

