package com.leyou.item.web;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("spec")
public class SpecGroupController {

    @Autowired
    private SpecGroupService specGroupService;

    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupByCid(@PathVariable("cid") Long cid) {
        return ResponseEntity.ok(specGroupService.queryGroupByCid(cid));
    }

    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> queryParamByGid(@RequestParam(value = "gid", required = false) Long gid, @RequestParam(value = "cid", required = false) Long cid, @RequestParam(value = "searching", required = false) Boolean searching) {
        return ResponseEntity.ok(specGroupService.queryParamByGid(gid,cid,searching));
    }

    /**
     * 根据分类查询规格组及组内参数
     * @param cid
     * @return
     */
    @GetMapping("group")
    public ResponseEntity<List<SpecGroup>> queryListByCid(@RequestParam("cid") Long cid){
        return ResponseEntity.ok(specGroupService.queryListByCid(cid));
    }
}
