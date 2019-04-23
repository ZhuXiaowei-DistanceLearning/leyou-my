package com.leyou;

import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.pojo.Sku;
import com.leyou.item.web.GoodsController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MyTest {
    @Autowired
    private GoodsController goodsController;

    @Autowired
    private SkuMapper skuMapper;

    @Test
    public void good() {
        List<Sku> list = skuMapper.selectAll();
        int i = 1;
        for (Sku spu : list) {
            Long id = spu.getId();
            String title = spu.getImages();
            if (title.contains("image")&&title.contains("localhost")&&!title.contains("group")) {
                Sku sku = new Sku();
                sku.setId(id);
                String image = "//localhost/"+title.split("\\/",2)[1];
                sku.setImages(image);
                System.out.println(image);
                skuMapper.updateByPrimaryKeySelective(sku);
            }
        }
        System.out.println(i);
    }
}
