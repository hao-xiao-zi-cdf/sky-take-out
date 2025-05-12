package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* Created with IntelliJ IDEA.
* Description:
* User: 34255
* Date: 2025-05-12
* Time: 20:05
*/
@Mapper
public interface SetmealDishMapper {

    /**
     * 批量删除菜品
     * @param ids
     * @return
     */
    List<Long> getSetmealIdsByDishIds(List<Long> ids);
}
