package com.leyou.item.web;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuBO;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @GetMapping("/spu/page")
    public ResponseEntity<PageResult<Spu>> querySpuByPage(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                                          @RequestParam(value = "rows", defaultValue = "5") Integer rows,
                                                          @RequestParam(value = "saleable", required = false) Boolean saleable,
                                                          @RequestParam(value = "key", required = false) String key) {
            return ResponseEntity.ok(goodsService.querySpuByPage(page, rows, saleable, key));
    }

//    @PostMapping("goods")
//    public ResponseEntity<Void> saveGood(@RequestBody Spu spu){
//        goodsService.saveGoods(spu);
//        return ResponseEntity.status(HttpStatus.CREATED).build();
//    }
//
//    @GetMapping("sku/list")
//    public ResponseEntity<List<Sku>> querySkuBySpuId(@RequestParam("id") Long id) {
//        List<Sku> skus = this.goodsService.querySkuBySpuId(id);
//        if (skus == null || skus.size() == 0) {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//        return ResponseEntity.ok(skus);
//    }
@PostMapping("goods")
public ResponseEntity<Void> saveGoods(@RequestBody SpuBO spuBO){

    try {
        this.goodsService.saveGoods(spuBO);
        return new ResponseEntity<>(HttpStatus.CREATED);
    } catch (Exception e) {
        e.printStackTrace();
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>> querySkuBySpuId(@RequestParam("id") Long id){
        List<Sku> skus = this.goodsService.querySkuBySpuId(id);
        if (skus == null || skus.size()==0) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return ResponseEntity.ok(skus);
    }

    /**
     * 根据sku查询的id集合查询所有sku
     * @param ids
     * @return
     */
    @GetMapping("sku/list/ids")
    public ResponseEntity<List<Sku>> querySkuBySpuId(@RequestParam("ids") List<Long> ids){
        List<Sku> skus = this.goodsService.querySkuBySpuIds(ids);
        if (skus == null || skus.size()==0) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return ResponseEntity.ok(skus);
    }

    @GetMapping("/spu/detail/{id}")
    public ResponseEntity<SpuDetail> querySpuDetailById(@PathVariable("id") Long id){
        SpuDetail spuDetail = this.goodsService.querySpuDetailById(id);
        if (null==spuDetail){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(spuDetail);
    }

    @GetMapping("spu/{id}")
    public ResponseEntity<Spu> querySpuById(@PathVariable("id") Long id){
        // 查询spu
        Spu spu = this.goodsService.querySpuById(id);
        if(spu == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(spu);
    }

    //
    public void testUpdate(Long spu){
        //索引库也要修改
        //静态页面的生成
    }


    @GetMapping("sku/{id}")
    public ResponseEntity<Sku> querySkuById(@PathVariable("id") Long id){
        Sku sku = this.goodsService.querySkuById(id);
        if(sku == null){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(sku);
    }

}
