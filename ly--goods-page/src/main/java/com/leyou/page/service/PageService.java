package com.leyou.page.service;

import com.leyou.item.pojo.*;
import com.leyou.page.client.BrandClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.page.client.SpecificationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PageService {

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClinet;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private TemplateEngine templateEngine;

    public Map<String, Object> loadModel(Long spuId) {
        Map<String, Object> model = new HashMap<>();
        // 查询spu
        Spu spu = goodsClient.querySpuById(spuId);
        // 查询skus
        List<Sku> skus = spu.getSkus();
        // 查询详情
        SpuDetail spuDetail = spu.getSpuDetail();
        // 查询brand
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        // 查询商品分类
        List<Category> categories = categoryClinet.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        // 查询规格参数
        List<SpecGroup> groups = specificationClient.queryGroupByCid(spu.getCid3());
        List<SpecParam> specParams = this.specificationClient.queryParamByGid(null, spu.getCid3(), null);

        // 处理成id:name格式的键值对
        Map<Long,String> paramMap = new HashMap<>();
        for (SpecParam param : specParams) {
            paramMap.put(param.getId(), param.getName());//4 机身颜色，5，内存
        }
        model.put("spu",spu);
        model.put("skus",skus);
        model.put("spuDetail",spuDetail);
        model.put("brand",brand);
        model.put("categories",categories);
        model.put("groups",groups);
        model.put("paramMap", paramMap);
        return  model;
    }

    public void createHtml(Long spuId){
        // 上下文
        Context context = new Context();
        context.setVariables(loadModel(spuId));
        // 输出流
        File dest = new File("D:\\English soft\\nginx-1.12.2\\html\\item", spuId + ".html");
        try(PrintWriter writer = new PrintWriter(dest, "UTF-8")){
        templateEngine.process("item",context,writer);
        }catch (Exception e){
            log.error("[静态页服务] 生成静态页异常！", e);
        };
        // 生成html
    }
}
