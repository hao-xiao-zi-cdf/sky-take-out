package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 34255
 * Date: 2025-05-16
 * Time: 19:37
 */
public interface ShoppingCartService {

    /**
     * 添加购物车商品数据
     * @param shoppingCartDTO
     */
    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);

    /**
     * 查看购物车商品列表
     * @return
     */
    List<ShoppingCart> getShoppingCartList();

    /**
     * 清空购物车
     */
    void clearShoppingCartList();
}
