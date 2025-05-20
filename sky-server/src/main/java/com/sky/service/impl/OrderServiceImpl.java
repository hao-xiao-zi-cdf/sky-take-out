package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.*;
import com.sky.websocket.WebSocketServer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
 * Date: 2025-05-17
 * Time: 15:58
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private WebSocketServer webSocketServer;

    private Orders orders;

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {

        //检验订单的收货地址，用户的购物车是否为null(增强程序健壮性，防止技术人员通过postman下单🤣🤣🤣)
        AddressBook address = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(address == null){
            //检验收货地址是否为null
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        ShoppingCart shoppingCart = ShoppingCart.builder().userId(BaseContext.getCurrentId()).build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if(list == null || list.size() == 0){
            //检验用户购物车是否为null
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //1.往订单表中插入1条记录
        Orders order = new Orders();
        //对象属性拷贝
        BeanUtils.copyProperties(ordersSubmitDTO,order);
        //设置其他属性
        order.setNumber(Long.toString(System.currentTimeMillis()));
        order.setStatus(Orders.PENDING_PAYMENT);
        order.setUserId(BaseContext.getCurrentId());
        order.setOrderTime(LocalDateTime.now());
        order.setPayStatus(Orders.UN_PAID);
        order.setPhone(address.getPhone());
        this.orders = order;
        order.setAddress(address.getDetail());
        order.setConsignee(address.getConsignee());

        orderMapper.insert(order);

        List<OrderDetail> orderDetailList = new ArrayList<>();
        //2.往订单明细表中插入n条记录
        for(ShoppingCart spCart : list){
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(spCart,orderDetail);
            orderDetail.setOrderId(order.getId());
            orderDetailList.add(orderDetail);
        }
        //批量插入订单明细记录
        orderDetailMapper.insertBatch(orderDetailList);

        //3.下单成功后清空用户购物车
        shoppingCartMapper.clear(BaseContext.getCurrentId());

        //4.封装返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(order.getId())
                .orderAmount(ordersSubmitDTO.getAmount())
                .orderNumber(order.getNumber())
                .orderTime(order.getOrderTime())
                .build();
        return orderSubmitVO;
    }

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);
/*        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }
*/

        Orders orders = orderMapper.getByNumber(ordersPaymentDTO.getOrderNumber());
        //订单支付成功后，使用websocket向客户端发送推送信息
        //构造消息格式和内容
        Map<String,Object> map = new HashMap();
        map.put("type",1);//推送消息类型 来单提醒
        map.put("orderId",orders.getId());//订单id
        map.put("content", "订单号：" + orders.getNumber());//自定义弹框信息:订单号
        String jsonString = JSON.toJSONString(map);

        //推送信息
        webSocketServer.sendToAllClient(jsonString);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code","ORDERPAID");
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));
        Integer OrderPaidStatus = Orders.PAID;//支付状态，已支付
        Integer OrderStatus = Orders.TO_BE_CONFIRMED;  //订单状态，待接单
        LocalDateTime check_out_time = LocalDateTime.now();//更新支付时间
        orderMapper.updateStatus(OrderStatus, OrderPaidStatus, check_out_time, this.orders.getId());
        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    @Override
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 分页查询所有历史订单
     * @param pageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQueryHistoryOrders(OrdersPageQueryDTO pageQueryDTO) {
        //调用分页插件设置分页条件
        PageHelper.startPage(pageQueryDTO.getPage(),pageQueryDTO.getPageSize());

        //根据订单状态进行分页查询
        Page<Orders> page = orderMapper.pageQuery(pageQueryDTO);

        //创建OrderVO列表
        List<OrderVO> resultList = new ArrayList<>();

        //遍历分页查询结果，获取订单详情，并将其和分页查询结果封装到OrderVO列表
        if(page != null && page.getTotal() > 0){
            for(Orders order : page){
                //获取订单明细
                List<OrderDetail> list = orderDetailMapper.getByOrderId(order.getId());
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order,orderVO);
                orderVO.setOrderDetailList(list);
                resultList.add(orderVO);
            }
        }

        //封装成PageResult对象
        return new PageResult(page.getTotal(),resultList);
    }

    /**
     * 根据订单id查看订单详情
     * @param id
     * @return
     */
    @Override
    public OrderVO details(Long id) {

        //根据id查询订单记录
        Orders orders = orderMapper.getById(id);

        //根据id查看订单明细
        List<OrderDetail> list = orderDetailMapper.getByOrderId(id);

        //封装成OrderVO对象返回
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        orderVO.setOrderDetailList(list);

        return orderVO;
    }

    /**
     * 用户取消订单
     * @param id
     */
    @Override
    public void userCancel(Long id) throws Exception {
        //1.根据id获取订单记录，查看订单状态
        Orders order = orderMapper.getById(id);
        Integer status = order.getStatus();

        //2.已接单或派送中需要来联系商家沟通,由商家管理端来设置是否取消订单
        if(status > 2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //3.判断订单状态是否为待接单，调用微信接口进行退款
        if(status.equals(Orders.REFUND)){
//            //调用微信支付退款接口
//            weChatPayUtil.refund(
//                    order.getNumber(), //商户订单号
//                    order.getNumber(), //商户退款单号
//                    new BigDecimal(0.01),//退款金额，单位 元
//                    new BigDecimal(0.01));//原订单金额

            //支付状态修改为 退款
            order.setPayStatus(Orders.REFUND);
        }

        //4.设置订单状态为已取消,取消时间，取消原因
        order.setStatus(Orders.CANCELLED);
        order.setCancelReason("用户取消");
        order.setCancelTime(LocalDateTime.now());
        orderMapper.update(order);
    }

    /**
     * 再来一单
     * @param id
     */
    @Override
    @Transactional
    public void repetition(Long id) {

        //根据订单id获取订单明细
        List<OrderDetail> orderDetailsList = orderDetailMapper.getByOrderId(id);

        //将获取的订单明细批量插入购物车中
        for(OrderDetail orderDetail : orderDetailsList){
            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setUserId(BaseContext.getCurrentId());
            BeanUtils.copyProperties(orderDetail,shoppingCart);
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    /**
     * 搜索订单
     * @param pageQueryDTO
     * @return
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO pageQueryDTO) {
        //调用分页插件设置分页条件
        PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());

        //设置其他查询条件
        Page<Orders> page = orderMapper.pageQuery(pageQueryDTO);

        //获取OrderVO列表（主要设置OrderDishes属性）
        List<OrderVO> list = getOrderVOList(page);

        return new PageResult(page.getTotal(),list);
    }

    //获取OrderVO列表
    private List<OrderVO> getOrderVOList(Page<Orders> page){
        //创建OrderVO列表
        List<OrderVO> list = new ArrayList<>();

        if(page != null && page.getTotal() > 0){
            //遍历分页查询结果，设置订单菜品详情
            for(Orders order : page){
                //根据id获取订单商品明细记录
                List<OrderDetail> orderDetailsList = orderDetailMapper.getByOrderId(order.getId());

                //创建orderVO，设置OrderDishes属性和其余属性
                OrderVO orderVO = getOrderDishes(order,orderDetailsList);

                list.add(orderVO);
            }
        }
        return list;
    }

    //设置菜品详情
    private OrderVO getOrderDishes(Orders order, List<OrderDetail> orderDetailsList){
        //遍历集合，拼接字符串：(宫保鸡丁 * 3;)
        String orderDishes = "";
        for(OrderDetail orderDetail : orderDetailsList){
            orderDishes = orderDishes + orderDetail.getName() + " * " + orderDetail.getNumber() + ";";
        }
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order,orderVO);
        orderVO.setOrderDishes(orderDishes);
        return orderVO;
    }

    /**
     * 统计各个订单状态数量
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {

        //获取各个状态的订单总数量(待接单、待派送、派送中的订单数量)
        Integer toBeConfirmed = orderMapper.statusCount(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.statusCount(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.statusCount(Orders.DELIVERY_IN_PROGRESS);

        //封装成对象返回
        return new OrderStatisticsVO(toBeConfirmed,confirmed,deliveryInProgress);
    }

    /**
     * 接单
     * @param ordersConfirmDTO
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        //设置订单状态为已接单（接单前前就已经打到商家账户上，无需调用微信平台接口）
        Orders order = Orders.builder().id(ordersConfirmDTO.getId()).status(Orders.CONFIRMED).build();
        orderMapper.update(order);
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        //检验状态的合法性，是否为待接单状态
        Orders orders = orderMapper.getById(ordersRejectionDTO.getId());
        if(orders == null || !orders.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //设置订单状态为已取消,拒单原因，订单id,支付状态,取消时间
        Orders order = Orders.builder().status(Orders.CANCELLED)
                .cancelReason(ordersRejectionDTO.getRejectionReason())
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .payStatus(Orders.REFUND)
                .id(orders.getId())
                .cancelTime(LocalDateTime.now())
                .build();
        orderMapper.update(order);
        //调用微信平台接口进行退款
        //...
    }

    /**
     * 商家取消订单
     * @param ordersCancelDTO
     */
    @Override
    @Transactional
    public void adminCancel(OrdersCancelDTO ordersCancelDTO) {
        //检验订单合法性,判断是否为代派送，派送中状态
        Orders order = orderMapper.getById(ordersCancelDTO.getId());
        if(order == null || !(order.getStatus().equals(Orders.CONFIRMED) ||
                order.getStatus().equals(Orders.DELIVERY_IN_PROGRESS) ||
                order.getStatus().equals(Orders.COMPLETED))){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //设置订单状态为已取消，取消时间，取消原因，订单id，支付状态
        Orders orders = Orders.builder().id(order.getId())
                .status(Orders.CANCELLED)
                .cancelTime(LocalDateTime.now())
                .cancelReason(ordersCancelDTO.getCancelReason())
                .payStatus(Orders.REFUND)
                .build();
        orderMapper.update(orders);
        //调用微信平台接口进行退款
        //...
    }

    /**
     * 派送订单
     * @param id
     */
    @Override
    public void delivery(Long id) {
        //检验订单合法性，状态是否为待派送
        Orders order = orderMapper.getById(id);
        if(order == null || !order.getStatus().equals(Orders.CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //设置订单状态为派送中
        Orders orders = Orders.builder().id(id)
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();
        orderMapper.update(orders);
    }

    /**
     * 完成订单
     * @param id
     */
    @Override
    public void complete(Long id) {
        //检验订单合法性，状态是否为派送中
        Orders order = orderMapper.getById(id);
        if(order == null || !order.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //设置订单状态，送达时间
        Orders orders = Orders.builder().id(id)
                .status(Orders.COMPLETED)
                .deliveryTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);
    }

    /**
     * 用户催单
     * @param id
     */
    @Override
    public void reminder(Long id) {

        //查询订单
        Orders orders = orderMapper.getById(id);
        if(orders == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //用户点击催单后，使用webSocket向商家端推送催单提醒

        //构造信息格式和数据
        Map<String,Object> map = new HashMap<>();
        map.put("type",2);//用户催单
        map.put("orderId",id);//订单id
        map.put("content", "订单号：" + orders.getNumber());//订单号

        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }

    /**
     * 订单数据统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {

        List<LocalDate> dateList = new ArrayList<>();

        //根据指定的日期区间计算出每一天日期
        dateList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Integer> totalOrderList = new ArrayList<>();
        List<Integer> effectivityOrderList = new ArrayList<>();

        for(LocalDate date : dateList){
            //计算出当天的最小最大时间
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //将查询条件封装到map集合中
            Map<String,Object> map = new HashMap<>();
            map.put("beginTime",beginTime);
            map.put("endTime",endTime);

            //select count(1) from orders where order_time >= ? and order_time <= ? and status = 5
            //计算当天的总订单
            Double totalOrder = orderMapper.countOrderByMap(map);

            //计算当天的有效订单数
            map.put("status",Orders.COMPLETED);
            Double effectivityOrder = orderMapper.countOrderByMap(map);

            //判断获取的数据是否为null
            totalOrder = totalOrder == null ? 0.0 : totalOrder;
            effectivityOrder = effectivityOrder == null ? 0.0 : effectivityOrder;

            totalOrderList.add(totalOrder.intValue());
            effectivityOrderList.add(effectivityOrder.intValue());
        }

        //将获取的数据转化为指定的格式
        String dateString = StringUtils.join(dateList, ",");
        String totalOrderString = StringUtils.join(totalOrderList, ",");
        String effectivityOrderString = StringUtils.join(effectivityOrderList, ",");

        //计算指定日期区间内的订单总数，有效订单数量
        Integer totalCount = 0;
        Integer effectivityCount = 0;
        for(Integer item : totalOrderList){totalCount += item;}
        for(Integer item : effectivityOrderList){effectivityCount += item;}

        //计算订单完成率
        Double orderCompletionRate = 0.0;
        if(totalCount != 0){
            orderCompletionRate = effectivityCount.doubleValue() / totalCount;
        }

        return OrderReportVO.builder()
                .dateList(dateString)
                .orderCountList(totalOrderString)
                .validOrderCountList(effectivityOrderString)
                .totalOrderCount(totalCount)
                .validOrderCount(effectivityCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }
}
