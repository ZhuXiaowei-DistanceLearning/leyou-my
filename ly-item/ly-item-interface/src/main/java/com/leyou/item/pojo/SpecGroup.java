package com.leyou.item.pojo;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Table(name = "tb_spec_group")
@Data
public class SpecGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long cid;

    private String name;

    // getter和setter省略
    @Transient
    private List<SpecParam> params; // 该组下的所有规格参数集合
}