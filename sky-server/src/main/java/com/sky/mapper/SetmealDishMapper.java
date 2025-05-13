package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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

    /**
     * 批量插入记录
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 批量删除套餐关联的菜品记录
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据套餐id获取关联的菜品信息
     * @param setmealId
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getBySetmealId(Long setmealId);

    /**
     * 修改
     * @param setmealDishes
     */
    void update(List<SetmealDish> setmealDishes);

    /**
     * 根据菜品id获取关联套餐id
     * @param dishId
     * @return
     */
    @Select("select setmeal_id from setmeal_dish where dish_id = #{dishId}")
    List<Long> getByDishId(Long dishId);
}
