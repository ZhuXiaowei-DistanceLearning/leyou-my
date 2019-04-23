package com.leyou;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Category;
import com.leyou.item.pojo.SpuBO;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CategoryClientTest {

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private SearchService searchService;

    @Autowired
    private ElasticsearchTemplate template;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Test
    public void queryCategoryByIds() {
        List<Category> list = categoryClient.queryCategoryByIds(Arrays.asList(1L, 2L, 3L));
        for (Category category : list) {
            System.out.println(category);
        }
    }

    @Test
    public void longData() {
        int page = 1;
        int rows = 100;
        int size = 0;
        do {
            // 查询spu
            PageResult<SpuBO> result = this.goodsClient.querySpuByPage(page, rows, true, null);

            List<SpuBO> spus = result.getItems();


            // spu转为goods
            List<Goods> goods = spus.stream().map(spu -> this.searchService.buildGoods(spu))
                    .collect(Collectors.toList());

            // 把goods放入索引库
            this.goodsRepository.saveAll(goods);

            size = spus.size();
            page++;
        } while (size == 100);
    }

    @Test
    public void hello(){
        String str = "aaabbb";
        String s = str.substring(str.indexOf("a"), str.lastIndexOf("b"));
        System.out.println(s);
    }
}
