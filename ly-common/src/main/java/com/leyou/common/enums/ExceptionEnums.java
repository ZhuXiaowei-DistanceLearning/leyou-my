package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ExceptionEnums {
    PRICE_CANNOT_BE_NULL(400, "价格不能为空!"),
    CATEGORY_NOT_FOUND(404,"商品分类没查到！"),
    BRAND_NOT_FOUND(404,"品牌不存在"),
    SPEC_GROUP_NOT_FOUND(404,"商品规格组不存在"),
    SPEC_PARAM_NOT_FOUND(404,"商品规格参数不存在"),
    GOODS_NOT_FOUND(404,"商品不存在"),
    BRAND_SAVE_ERROR(500,"新增品牌失败"),
    CATEGORY_BRAND_SAVE_ERROR(500,"新增品牌分类失败"),
    UPLOAD_FILE_ERROR(500,"文件上传失败"),
    INVAILD_FILE_TYPE(500,"文件类型格式不正确"),
    GOODS_SAVE_ERROR(500,"新增商品失败"),
    GOODS_SKU_NOT_FOUND(500,"SKU未找到"),
    INVAILD_USER_DATA_TYPE(400,"用户的数据类型无效"),
    UN_AUTHORIZED(403,"未授权"),
    INVAILD_VERIFY_CODE(400,"无效验证码"),
    INVAILD_USERNAME_PASSWORD(400,"用户名或密码错误"),
    CREATE_TOKEN_ERROR(500,"用户凭证失效"),

    ;
    private Integer code;
    private String msg;

}
