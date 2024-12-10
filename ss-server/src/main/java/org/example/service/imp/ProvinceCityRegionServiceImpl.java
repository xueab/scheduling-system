package org.example.service.imp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.example.vo.enterprise.AreaItemVo;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.utils.PageUtils;
import org.example.utils.Query;

import org.example.dao.ProvinceCityRegionDao;
import org.example.entity.ProvinceCityRegion;
import org.example.service.ProvinceCityRegionService;


@Service("provinceCityRegionService")
public class ProvinceCityRegionServiceImpl extends ServiceImpl<ProvinceCityRegionDao, ProvinceCityRegion> implements ProvinceCityRegionService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProvinceCityRegion> page = this.page(
                new Query<ProvinceCityRegion>().getPage(params),
                new QueryWrapper<ProvinceCityRegion>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveAreaDataToDatabase() throws IOException {
        String path = "D:\\Projects\\damFile\\SmartSchedulingSystem\\smart-scheduling-system\\Code\\serve\\v1\\smart-scheduling-system-server\\file\\data\\省市区.txt";

        StringBuilder stringBuilder = new StringBuilder();

//        利用io流读取数据
        FileInputStream fileInputStream = new FileInputStream(path);
        InputStreamReader isr = new InputStreamReader(fileInputStream);
        BufferedReader br = new BufferedReader(isr);
        String line;
        while ((line = br.readLine()) != null) {
            stringBuilder.append(line);
        }
        HashMap map = JSONObject.parseObject(stringBuilder.toString(), HashMap.class);


        for (Object o : map.entrySet()) {
            this.saveData(o, 0L);
        }

    }

    public void saveData(Object o, Long parentId) {

        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            String json = entry.getValue().toString();
            if (json.contains(":")) {
                JSONObject value = JSON.parseObject(json);
                String name = (String) value.get("name");
                Integer type = -1;
                if (name.contains("省")) {
                    type = 0;
                } else if (name.contains("市")) {
                    type = 1;
                } else if (name.contains("区") || name.contains("县")) {
                    type = 2;
                }
                if (type != -1) {
                    ProvinceCityRegion provinceCityRegion = new ProvinceCityRegion();
                    provinceCityRegion.setName(name);
                    provinceCityRegion.setType(type);
                    provinceCityRegion.setParentId(parentId);
                    baseMapper.insert(provinceCityRegion);
                    JSONObject child = JSON.parseObject(JSON.toJSONString(value.get("child")));
                    this.saveData(child, provinceCityRegion.getId());
                }
            } else {

                String name = json;
                Integer type = -1;
                if (name.contains("省")) {
                    type = 0;
                } else if (name.contains("市")) {
                    type = 1;
                } else if (name.contains("区") || name.contains("县")) {
                    type = 2;
                }
                ProvinceCityRegion provinceCityRegion = new ProvinceCityRegion();
                provinceCityRegion.setName(name);
                provinceCityRegion.setType(type);
                provinceCityRegion.setParentId(parentId);
                baseMapper.insert(provinceCityRegion);
            }

            int temp = 0;
        }
    }


    @Override
    public List<AreaItemVo> getAreaTree() {
        ////声明变量
        List<AreaItemVo> areaItemVoList = new ArrayList<>();
        //查询出所有省市区数据
        List<ProvinceCityRegion> provinceCityRegionList = baseMapper.selectList(null);
        //过滤出所有省
        List<ProvinceCityRegion> fatherList = provinceCityRegionList.stream().filter(item -> {
            if (item.getParentId() == 0) {
                return true;
            } else {
                return false;
            }
        }).collect(Collectors.toList());
        for (ProvinceCityRegion father : fatherList) {
            AreaItemVo areaItemVo = new AreaItemVo(father.getId(), father.getName());
            this.searchSon(areaItemVo, provinceCityRegionList);
            areaItemVoList.add(areaItemVo);
        }

        return areaItemVoList;
    }

    private void searchSon(AreaItemVo father, List<ProvinceCityRegion> provinceCityRegionList) {
        List<AreaItemVo> sonList = provinceCityRegionList.stream().filter(item -> {
            if (item.getParentId() == father.getValue().intValue()) {
                return true;
            } else {
                return false;
            }
        }).map(item1 -> {
            AreaItemVo son = new AreaItemVo(item1.getId(), item1.getName());
            //继续给儿子寻找孙子
            this.searchSon(son, provinceCityRegionList);
            return son;
        }).collect(Collectors.toList());
        father.setChildren(sonList);
    }


}