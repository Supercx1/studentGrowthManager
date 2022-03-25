package com.yeqifu.growth.vo;

import com.yeqifu.growth.entity.Innovate;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class InnovateVo extends Innovate {
    /**
     * 分页参数，当前是第一页，每页10条数据
     */
    private Integer page=1;
    private Integer limit=10;

    /**
     * 批量删除
     */
    private Integer[] ids;
}
