package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import org.springframework.stereotype.Service;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 34255
 * Date: 2025-05-16
 * Time: 19:37
 */
public interface ShoppingCartService {

    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);
}
