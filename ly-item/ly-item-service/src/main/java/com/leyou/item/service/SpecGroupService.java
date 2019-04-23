package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnums;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SpecGroupService {
    @Autowired
    private SpecGroupMapper specGroupMapper;
    @Autowired
    private SpecParamMapper specParamMapper;

    public List<SpecGroup> queryGroupByCid(Long cid) {
        SpecGroup group = new SpecGroup();
        group.setCid(cid);
        List<SpecGroup> list = specGroupMapper.select(group);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnums.SPEC_GROUP_NOT_FOUND);
        }
        return list;
    }

    public List<SpecParam> queryParamByGid(Long gid, Long cid, Boolean searching) {
        SpecParam param = new SpecParam();
        param.setGroupId(gid);
        param.setCid(cid);
        param.setSearching(searching);
        List<SpecParam> list = specParamMapper.select(param);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnums.SPEC_PARAM_NOT_FOUND);
        }
        return list;
    }

    public List<SpecGroup> queryListByCid(Long cid) {
        // 查询规格组
        List<SpecGroup> list = queryGroupByCid(cid);
        // 查询当前分类下的参数
        List<SpecParam> specParams = queryParamByGid(null, cid, null);
        // 先把规格参数变成map, map的key是规格组的id,map的值是组下的所有参数
        Map<Long, List<SpecParam>> map = new HashMap<>();
        for (SpecParam specParam : specParams) {
            if(!map.containsKey(specParam.getGroupId())){
                // 这个组id在map中不存在，新增一个list
                map.put(specParam.getGroupId(), new ArrayList<>());
            }
            map.get(specParam.getGroupId()).add(specParam);
        }
        // 填充param到group
        for (SpecGroup specGroup : list) {
            specGroup.setParams(map.get(specGroup.getId()));
        }
        return list;
    }
}
