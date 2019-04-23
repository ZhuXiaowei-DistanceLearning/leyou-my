package com.leyou.page.web;

import com.leyou.page.service.FileService;
import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/item")
public class PageController {

    @Autowired
    private PageService pageService;

    @Autowired
    private FileService fileService;

    @GetMapping("{id}.html")
    public String toItemPage(@PathVariable("id") Long spuId, Model model){
//        // 查询模型数据
//        Map<String, Object> attributes = pageService.loadModel(spuId);
//        // 准备模型数据
//        System.out.println(attributes);
//        model.addAllAttributes(attributes);
//        // 返回视图
//        if(!this.fileService.exists(spuId)){
//            this.fileService.syncCreateHtml(spuId);
//        }
//        return "item";
        model.addAllAttributes(pageService.loadModel(spuId));

//         判断是否需要生成新的页面
        if(!this.fileService.exists(spuId)){
            this.fileService.syncCreateHtml(spuId);
        }
        return "item";
    }
}
