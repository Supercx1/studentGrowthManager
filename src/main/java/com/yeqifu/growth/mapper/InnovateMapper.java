package com.yeqifu.growth.mapper;

import com.yeqifu.growth.entity.Innovate;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author cxc-
 * @since 2022-02-22
 */
public interface InnovateMapper extends BaseMapper<Innovate> {
    Integer countInnovate();
}
