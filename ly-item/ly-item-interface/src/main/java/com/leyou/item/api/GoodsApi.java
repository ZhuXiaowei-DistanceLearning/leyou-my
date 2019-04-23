package com.leyou.item.api;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuBO;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GoodsApi {
    @GetMapping("/spu/page")
    PageResult<SpuBO> querySpuByPage(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                   @RequestParam(value = "rows", defaultValue = "5") Integer rows,
                                   @RequestParam(value = "saleable", required = false) Boolean saleable,
                                   @RequestParam(value = "key", required = false) String key);

    @PostMapping("goods")
    Void saveGoods(@RequestBody SpuBO spuBO);

    @GetMapping("sku/list")
    List<Sku> querySkuBySpuId(@RequestParam("id") Long id);

    @GetMapping("/spu/detail/{id}")
    SpuDetail querySpuDetailById(@PathVariable("id") Long id);

    @GetMapping("spu/{id}")
    Spu querySpuById(@PathVariable("id") Long id);

    @GetMapping("sku/{id}")
    List<Sku> querySkuById(@PathVariable("id") Long id);

}
