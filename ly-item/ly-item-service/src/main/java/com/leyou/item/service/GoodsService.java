package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnums;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.item.pojo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GoodsService {
    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AmqpTemplate amqpTemplate;

    public PageResult<Spu> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key) {
        // 分页
        PageHelper.startPage(page, rows);
        // 过滤
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        // 搜索字段过滤
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }
        // 上下架过滤
        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }

        // 默认排序
        example.setOrderByClause("last_update_time DESC");
        List<Spu> list = spuMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnums.GOODS_NOT_FOUND);
        }
        loadCategoryAndBrandName(list);
        PageInfo<Spu> info = new PageInfo<>(list);
        return new PageResult<>(info.getTotal(), list);
    }

    private void loadCategoryAndBrandName(List<Spu> list) {
        for (Spu spu : list) {
            // 处理分类名称
            List<String> collect = categoryService.queryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3())).stream().map(Category::getName).collect(Collectors.toList());
            spu.setCname(StringUtils.join(collect, "/"));
            // 处理品牌名称
            spu.setBname(brandService.queryById(spu.getBrandId()).getName());
        }
    }

//    @Transactional
//    public void saveGoods(Spu spu) {
//        // 新增spu
//        spu.setId(null);
//        spu.setCreateTime(new Date());
//        spu.setLastUpdateTime(spu.getCreateTime());
//        spu.setSaleable(true);
//        spu.setValid(false);
//        int count = spuMapper.insert(spu);
//        if (count != 1) {
//            throw new LyException(ExceptionEnums.GOODS_SAVE_ERROR);
//        }
//        // 新增detail
//        SpuDetail detail = spu.getSpuDetail();
//        detail.setSpuId(spu.getId());
//        spuDetailMapper.insert(detail);
//        // 新增sku
//        List<Stock> stockList = new ArrayList<>();
//        List<Sku> list = spu.getSkus();
//        for (Sku sku : list) {
//            sku.setCreateTime(new Date());
//            sku.setLastUpdateTime(sku.getCreateTime());
//            sku.setSpuId(spu.getId());
//            count = skuMapper.insert(sku);
//            if (count != 1) {
//                throw new LyException(ExceptionEnums.GOODS_SAVE_ERROR);
//            }
//            // 新增库存
//            Stock stock = new Stock();
//            stock.setSkuId(sku.getId());
//            stock.setStock(sku.getStock());
//            count = stockMapper.insert(stock);
//            if (count != 1) {
//                throw new LyException(ExceptionEnums.GOODS_SAVE_ERROR);
//            }
//            stockList.add(stock);
//        }
//
//        // 批量新增库存
//        stockMapper.insertList(stockList);
//    }
//
//    public List<Sku> querySkuBySpuId(Long spuId) {
//        // 查询sku
//        Sku record = new Sku();
//        record.setSpuId(spuId);
//        List<Sku> skus = this.skuMapper.select(record);
//        for (Sku sku : skus) {
//            // 同时查询出库存
//            sku.setStock(this.stockMapper.selectByPrimaryKey(sku.getId()).getStock());
//        }
//        return skus;
//    }

    @Transactional
    public void saveGoods(SpuBO spuBO) {

        // 保存spu
        spuBO.setSaleable(true);
        spuBO.setValid(true);
        spuBO.setCreateTime(new Date());
        spuBO.setLastUpdateTime(spuBO.getCreateTime());
        this.spuMapper.insert(spuBO);

        //保存spuDetail信息
        spuBO.getSpuDetail().setSpuId(spuBO.getId());

        spuDetailMapper.insert(spuBO.getSpuDetail());

        // 保存sku和库存信息
        saveSkuAndStock(spuBO.getSkus(), spuBO.getId());

        //发送消息
       sendMessage(spuBO.getId(), "insert");
    }

    private void saveSkuAndStock(List<Sku> skus, Long spuId) {

        for (Sku sku : skus) {
            if (!sku.getEnable()) {
                continue;
            }
            //保存sku
            sku.setSpuId(spuId);
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());

            this.skuMapper.insert(sku);

            //保存库存信息
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());

            this.stockMapper.insert(stock);

        }
    }

    public List<Sku> querySkuBySpuId(Long id) {
        Sku sku = new Sku();
        sku.setSpuId(id);
        return skuMapper.select(sku);
    }

    public SpuDetail querySpuDetailById(Long id) {

        return spuDetailMapper.selectByPrimaryKey(id);
    }

    public Spu querySpuById(Long id) {
        Spu spu = this.spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new LyException(ExceptionEnums.GOODS_NOT_FOUND);
        }
        // 查询sku
        spu.setSkus(querySkuBySpuId(id));
        // 查询detail
        spu.setSpuDetail(querySpuDetailById(id));
        return spu;
    }

    /**
     * //     * @param id
     * //     * @param type 操作的类型
     * //
     */
    private void sendMessage(Long id, String type) {
        // 发送消息
        try {
            this.amqpTemplate.convertAndSend("item." + type, id);
        } catch (Exception e) {
//            logger.error("{}商品消息发送异常，商品id：{}", type, id, e);
        }
    }
    public Sku querySkuById(Long id) {
        return this.skuMapper.selectByPrimaryKey(id);
    }

    public List<Sku> querySkuBySpuIds(List<Long> ids) {
        List<Sku> list = skuMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnums.GOODS_SKU_NOT_FOUND);
        }
        loadStockSku(ids, list);
        return list;
    }

    private void loadStockSku(List<Long> ids, List<Sku> list) {
        List<Stock> stockList = stockMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnums.GOODS_SKU_NOT_FOUND);
        }
        Map<Long, Integer> stockMap = stockList.stream().collect(Collectors.toMap(Stock::getSkuId, Stock::getStock));
        list.forEach(s -> s.setStock(stockMap.get(s.getId())));
    }
}
