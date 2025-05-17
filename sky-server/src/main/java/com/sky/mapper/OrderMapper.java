package com.sky.mapper;

import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 34255
 * Date: 2025-05-17
 * Time: 16:01
 */
@Mapper
public interface OrderMapper {

    /**
     * 插入订单记录
     * @param order
     */
    void insert(Orders order);
}
