package com.yeqifu.growth.service;

import com.yeqifu.growth.entity.Competition;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author cxc-
 * @since 2022-02-28
 */
public interface ICompetitionService extends IService<Competition> {
    Integer countCompetition();
}
