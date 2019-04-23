package com.leyou.search.service;

import com.leyou.common.enums.ExceptionEnums;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.repository.GoodsRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Stream;

@Service
public class SearchService {

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private GoodsRepository goodsRepository;

    public Goods buildGoods(Spu spu) {
        Long spuId = spu.getId();
        // 查询分类
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        if (CollectionUtils.isEmpty(categories)) {
            throw new LyException(ExceptionEnums.CATEGORY_NOT_FOUND);
        }
        Stream<String> names = categories.stream().map(Category::getName);

        // 查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        if (brand == null) {
            throw new LyException(ExceptionEnums.BRAND_NOT_FOUND);
        }
        // 查询sku
        List<Sku> skuList = goodsClient.querySkuBySpuId(spuId);
        if (skuList == null) {
            throw new LyException(ExceptionEnums.GOODS_SKU_NOT_FOUND);
        }
//        Set<Long> priceList = skuList.stream().map(Sku::getPrice).collect(Collectors.toSet());
//        if(CollectionUtils.isEmpty(skuList)){
//            throw new LyException(ExceptionEnums.GOODS_SKU_NOT_FOUND)
//        }
        // 对sku进行处理
        List<Map<String, Object>> skus = new ArrayList<>();
        // 价格几何
        Set<Long> priceList = new HashSet<>();
        for (Sku sku : skuList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", sku.getId());
            map.put("title", sku.getTitle());
            map.put("price", sku.getPrice());
            map.put("images", StringUtils.substringBefore(sku.getImages(), ""));
            skus.add(map);
            priceList.add(sku.getPrice());
        }

        // 查询规格参数
        List<SpecParam> params = specificationClient.queryParamByGid(null, spu.getCid3(), true);
        if (CollectionUtils.isEmpty(params)) {
            throw new LyException(ExceptionEnums.SPEC_PARAM_NOT_FOUND);
        }

        SpuDetail spuDetail = goodsClient.querySpuDetailById(spuId);
//        String[] split = spuDetail.getGenericSpec().split("\\[",2)[1].split("\\]",-2);
//        int start_index = spuDetail.getGenericSpec().indexOf("[") + 1;
//        int end_index = spuDetail.getGenericSpec().indexOf("]");
//        String s = spuDetail.getGenericSpec().substring(start_index, end_index);
        // 获取通用规格参数
//        Map<String, String> genericMap = JsonUtils.parseMap(jj, String.class, String.class);
        // 获取特有规格参数
//        Map<String, List<String>> genericMap = JsonUtils.nativeRead(spuDetail.getGenericSpec(), new TypeReference<Map<String, List<String>>>() {
//        });
//        Map<String, List<String>> specialSpec = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<String, List<String>>>() {
//        });
//        // 查询商品详情
//        // 规格参数,key是规格参数的名字，值是规格参数的值
//        Map<String, Object> specs = new HashMap<>();
//        for (SpecParam param : params) {
//            // 规格名称
//            String key = param.getName();
//            Object value = "";
//            if (param.getGeneric()) {
//                value = genericMap.get(param.getId());
//                if (param.getNumeric()) {
//                    // 处理成段
//                    value = chooseSegment(value.toString(), param);
//                }
//            } else {
//                value = specialSpec.get(param.getId());
//            }
            // 存入map
//            specs.put(key, value);
//        }
        // 搜索所有字段
        String all = spu.getTitle() + StringUtils.join(names, " ");
        Goods goods = new Goods();
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid1(spu.getCid2());
        goods.setCid1(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setId(spu.getId());
        goods.setAll(all);// TODO 搜索字段，包含标题，分类，品牌，规格
        goods.setPrice(priceList); // TODO 所有sku的价格几何
        goods.setSkus(JsonUtils.serialize(skuList)); // TODO 所有sku的集合的json格式
//        goods.setSpecs(null); // TODO 所有的可搜索的规格参数
        goods.setSubTitle(spu.getSubTitle());
        return goods;
    }

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    public PageResult<Goods> search(SearchRequest request) {
        int page = request.getPage() -1;
        int size = request.getSize();
        // 创建查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 结果过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","subTitle","skus"},null));
        // 分页
        queryBuilder.withPageable(PageRequest.of(page, size));
        // 过滤
        queryBuilder.withQuery(QueryBuilders.matchQuery("all",request.getKey()));
        // 查询
        Page<Goods> result = goodsRepository.search(queryBuilder.build());
        // 解析结果
        Long total = result.getTotalElements();
        int pages = (total.intValue() + size -1 ) / size;
        List<Goods> goodsList = result.getContent();
        return new PageResult<>(total,pages,goodsList);
    }

    public void createOrUpdate(Long spuId) {
        Spu spu = goodsClient.querySpuById(spuId);
        // 构造goods
        Goods goods = buildGoods(spu);
        // 村塾索引库
        goodsRepository.save(goods);
    }

    public void deleteIndex(Long spuId) {
        goodsRepository.deleteById(spuId);
    }
}
