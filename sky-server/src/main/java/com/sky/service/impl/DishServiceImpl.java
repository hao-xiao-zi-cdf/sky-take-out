package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 34255
 * Date: 2025-05-12
 * Time: 15:36
 */
@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 新增菜品
     * @param dishDTO
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {

        //向菜品表插入1条记录
        //创建dish实体类
        Dish dish = new Dish();
        //对象属性拷贝
        BeanUtils.copyProperties(dishDTO,dish);

        dishMapper.insert(dish);

        //开启useGeneratedKeys，获取菜品id
        Long id = dish.getId();

        //向口味表插入n条记录
        //获取口味数组
        List<DishFlavor> flavors = dishDTO.getFlavors();
        //遍历数组，将id赋值给每一个元素
        if(flavors != null && flavors.size() > 0){
            for (DishFlavor flavor: flavors){
                flavor.setDishId(id);
            }
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        //设置分页条件
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        //设置其他查询条件
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);

        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 批量删除菜品
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //1.判断是否能够删除--菜品中是否已上架的菜
        for(Long id : ids){
            Dish dish = dishMapper.getById(id);
            if(dish.getStatus() == StatusConstant.ENABLE){
                //当前菜品处于起售中，不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        //2.判断是否能够删除--菜品中是否有菜品关联套餐
        List<Long> list = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if(list != null && list.size() > 0){
            //当前菜品被套餐关联了，不能删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

//        for (Long id : ids){
//            //3.根据ids批量删除菜品
//            dishMapper.deleteById(id);
//            //4.删除相关联的口味
//            //根据菜品id批量删除口味记录
//            dishFlavorMapper.deleteByDishId(id);
//        }
        dishMapper.deleteByIds(ids);
        dishFlavorMapper.deleteByDishIds(ids);
    }

    /**
     * 根据菜品id查询菜品和口味信息
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        //1.根据id查询菜品信息
        Dish dish = dishMapper.getById(id);

        //2.根据id查询口味信息
        List<DishFlavor> list = dishFlavorMapper.getByDishId(id);

        //将查询到的信息封装到DishVO
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(list);

        return dishVO;
    }

    /**
     * 修改菜品信息
     * @param dishDTO
     */
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {

        //对象属性拷贝
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);

        //1.修改菜品表信息
        dishMapper.update(dish);

        //2.根据菜品id删除口味信息
        dishFlavorMapper.deleteByDishId(dishDTO.getId());

        //3.插入口味信息
        List<DishFlavor> dishFlavors = dishDTO.getFlavors();
        if(dishFlavors != null && dishFlavors.size() > 0) {
            //设置菜品id
            for (DishFlavor flavor: dishFlavors){
                flavor.setDishId(dishDTO.getId());
            }
            dishFlavorMapper.insertBatch(dishFlavors);
        }
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> getByCategoryId(Long categoryId) {
        Dish dish = Dish.builder().categoryId(categoryId)
                      .status(StatusConstant.ENABLE)
                      .build();
        List<Dish> dishList = dishMapper.selectByCategoryId(dish);
        return dishList;
    }

    /**
     * 修改菜品起售停售状态
     * @param status
     * @param id
     */
    @Override
    @Transactional
    public void startOrStop(Integer status, Long id) {
        //判断是否关联起售状态的套餐--菜品停售->套餐停售
        if(status == StatusConstant.DISABLE){
            //获取关联的套餐id
            List<Long> setmealList = setmealDishMapper.getByDishId(id);
            for(Long setmealId : setmealList){
                //遍历套餐id获取套餐信息
                Setmeal setmeal = setmealMapper.getById(setmealId);
                if(setmeal.getStatus() == StatusConstant.ENABLE){
                    setmeal = Setmeal.builder().id(setmealId).status(StatusConstant.DISABLE).build();
                    setmealMapper.update(setmeal);
                }
            }
        }


        Dish dish = Dish.builder()
                        .id(id)
                        .status(status)
                        .build();
        dishMapper.update(dish);
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    @Override
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.selectByCategoryId(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}
