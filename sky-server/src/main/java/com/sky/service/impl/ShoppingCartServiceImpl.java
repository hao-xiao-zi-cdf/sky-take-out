package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 34255
 * Date: 2025-05-16
 * Time: 19:39
 */
@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    @Override
    @Transactional
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {

        ShoppingCart shoppingCart = new ShoppingCart();
        //对象属性拷贝
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        //设置userId
        shoppingCart.setUserId(BaseContext.getCurrentId());

        //1.判断该用户的购物车中是否存在该商品记录（这里使用list集合是为了代码的复用性）
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        if(list != null && list.size() > 0){
            //2.有，修改购物车商品记录，再原有的number基础上+1
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.update(cart);
        }else{
            //3.没有，新插入一条记录
            if(shoppingCartDTO.getDishId() != null){
                //新插入的购物车商品为菜品，获取菜品信息
                Dish dish = dishMapper.getById(shoppingCartDTO.getDishId());
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            }else{
                //新插入的购物车商品为套餐
                Setmeal setmeal = setmealMapper.getById(shoppingCartDTO.getSetmealId());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }
}
