package com.leyou.item.pojo;

import lombok.Data;

import javax.persistence.Transient;
import java.util.List;

@Data
public class SpuBO extends Spu {

    @Transient
    String cname;// 商品分类名称
    @Transient
    String bname;// 品牌名称
    @Transient
    SpuDetail spuDetail;// 商品详情
    @Transient
    List<Sku> skus;// sku列表
}
