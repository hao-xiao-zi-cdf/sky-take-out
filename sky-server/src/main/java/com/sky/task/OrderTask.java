package com.sky.task;

import com.sky.entity.Orders;
import com.sky.entity.OrdersTask;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 34255
 * Date: 2025-05-19
 * Time: 16:31
 */
@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时未支付订单
     */
    @Scheduled(cron = "0 * * * * ? ")//每分钟触发
    public void timeOutTask(){
        log.info("检查是否存在超时未支付订单 {}", LocalDateTime.now());

        //获取当前时间-15的时间
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime time = now.plusMinutes(-15);

        List<Long> ordersIdList = orderMapper.getByStatusAndTime(Orders.PENDING_PAYMENT,time);
        //判断是否未空
        if(ordersIdList != null && ordersIdList.size() > 0){
            OrdersTask ordersTask = OrdersTask.builder().status(Orders.CANCELLED)
                    .cancelTime(now)
                    .cancelReason("用户订单超时未支付，系统自动取消订单").build();
            //根据订单id批量处理超时未支付订单
            orderMapper.updateByTask(ordersTask,ordersIdList);
        }
    }

    /**
     * 处理一直处于派送中的订单
     */
    @Scheduled(cron = "0 0 1 * * ?")//每天凌晨1点触发
//    @Scheduled(cron = "0/5 * * * * ?")//测试
    public void deliveryTask(){
        log.info("检查上个工作日派送遗落的订单 {}", LocalDateTime.now());
        //获取当前时间-60的时间
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime time = now.plusMinutes(-60);

        List<Long> ordersIdList = orderMapper.getByStatusAndTime(Orders.DELIVERY_IN_PROGRESS,time);
        //判断是否未空
        if(ordersIdList != null && ordersIdList.size() > 0){
            OrdersTask ordersTask = OrdersTask.builder().status(Orders.COMPLETED)
                    .deliveryTime(now).build();
            //根据订单id批量处理上个工作日一直处于派送中的订单
            orderMapper.updateByTask(ordersTask,ordersIdList);
        }
    }
}
