package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

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

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 修改状态
     * @param orderStatus
     * @param orderPaidStatus
     * @param check_out_time
     * @param id
     */
    @Update("update orders set status = #{orderStatus},pay_status = #{orderPaidStatus} ,checkout_time = #{check_out_time} where id = #{id}")
    void updateStatus(Integer orderStatus, Integer orderPaidStatus, LocalDateTime check_out_time, Long id);

    /**
     * 分页查询所有历史订单
     * @param pageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO pageQueryDTO);

    /**
     * 根据订单id查看订单
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 查询各个订单状态的总数量
     * @param status
     * @return
     */
    @Select("select count(1) from orders where status = #{status}")
    Integer statusCount(Integer status);
}
