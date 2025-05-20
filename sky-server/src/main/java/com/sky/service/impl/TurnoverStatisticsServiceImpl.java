package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.TurnoverStatisticsService;
import com.sky.vo.TurnoverReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 34255
 * Date: 2025-05-20
 * Time: 11:21
 */
@Service
public class TurnoverStatisticsServiceImpl implements TurnoverStatisticsService {

    private final OrderMapper orderMapper;

    public TurnoverStatisticsServiceImpl(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    /**
     * 根据指定时间区间统计每一天的营业额
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnover(LocalDate begin, LocalDate end) {

        List<LocalDate> list = new ArrayList<>();

        //根据给定的区间计算出每一天日期
        list.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            list.add(begin);
        }

        List<Double> turnoverList = new ArrayList<>();

        for(LocalDate dayTime : list){
            //获取当天的最小最大时间
            LocalDateTime beginTime = LocalDateTime.of(dayTime, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(dayTime, LocalTime.MAX);

            //将查询条件封装成map集合
            Map<String,Object> map = new HashMap<>();
            map.put("beginTime",beginTime);
            map.put("endTime",endTime);
            map.put("status", Orders.COMPLETED);

            //根据计算出来的每一天日期去获取当天的营业额
            //select sum(amount) from orders where order_time > ? and order_time < ? and status = 5
            Double turnover = orderMapper.sumByMap(map);
            //判断当天营业额是否未null
            turnover = (turnover == null) ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        //将获取的日期取出转化成指定格式
        String dateString = StringUtils.join(list, ",");
        //将获取的当天营业额转化成指定格式
        String turnoverString = StringUtils.join(turnoverList, ",");

        return TurnoverReportVO.builder()
                .dateList(dateString)
                .turnoverList(turnoverString)
                .build();
    }
}

