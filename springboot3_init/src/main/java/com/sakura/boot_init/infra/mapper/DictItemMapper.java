package com.sakura.boot_init.infra.mapper;

import com.mybatisflex.core.BaseMapper;
import com.sakura.boot_init.infra.persistence.entity.DictItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 字典明细 Mapper
 *
 * @author sakura
 */
@Mapper
public interface DictItemMapper extends BaseMapper<DictItem> {
}
