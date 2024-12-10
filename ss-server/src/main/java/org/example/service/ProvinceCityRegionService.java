package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.vo.enterprise.AreaItemVo;
import org.example.utils.PageUtils;//import com.dam.utils.PageUtils;
import org.example.entity.ProvinceCityRegion;//import com.dam.model.entity.enterprise.ProvinceCityRegionEntity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 省-市-区表
 *
 * @author dam
 * @email 1782067308@qq.com
 * @date 2023-02-09 11:17:26
 */
public interface ProvinceCityRegionService extends IService<ProvinceCityRegion> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAreaDataToDatabase() throws IOException;

    List<AreaItemVo> getAreaTree();

}

