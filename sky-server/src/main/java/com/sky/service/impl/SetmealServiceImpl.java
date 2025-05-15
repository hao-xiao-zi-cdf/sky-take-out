package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 34255
 * Date: 2025-05-13
 * Time: 9:41
 */
@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void saveWithSetmealDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        //对象属性拷贝
        BeanUtils.copyProperties(setmealDTO,setmeal);

        //1.向套餐表插入1条套餐信息
        setmealMapper.insert(setmeal);

        Long id = setmealDTO.getId();

        //2.向setmeal_dish表插入n记录
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        for(SetmealDish setmealDish : setmealDishes){
            setmealDish.setSetmealId(setmeal.getId());
        }

        //批量插入
        setmealDishMapper.insertBatch(setmealDishes);
    }

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        //调用分页插件
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());

        //设置其他查询条件
        Page<Setmeal> page = setmealMapper.pageQuery(setmealPageQueryDTO);

        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    @Override
    public void delete(List<Long> ids) {
        //1.判断套餐是否能够删除--选中套餐中不存在起售状态
        for(Long id : ids){
            Setmeal setmeal = setmealMapper.getById(id);
            if(setmeal.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }

        //2.批量删除套餐
        setmealMapper.deleteBatch(ids);

        //3.批量删除套餐关联的菜品记录
        setmealDishMapper.deleteBatch(ids);
    }

    /**
     * 根据id获取套餐信息
     * @param id
     * @return
     */
    @Override
    public SetmealVO getById(Long id) {

        SetmealVO setmealVO = SetmealVO.builder().build();

        //1.根据id获取套餐信息
        Setmeal setmeal = setmealMapper.getById(id);

        //2.根据id获取关联的菜品信息
        List<SetmealDish> setmealDishList = setmealDishMapper.getBySetmealId(id);

        //3.将获取的信息封装成SetmealVO对象返回
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(setmealDishList);
        return setmealVO;
    }

    /**
     * 修改套餐信息
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {

        Setmeal setmeal = Setmeal.builder().build();
        BeanUtils.copyProperties(setmealDTO,setmeal);

        //1.修改套餐信息（set_meal）
        setmealMapper.update(setmeal);

        //2.修改套餐相关联的菜品（setmeal_dish）
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        List<Long> list = new ArrayList<>();
        list.add(setmealDTO.getId());

        //根据id删除关联的菜品记录
        setmealDishMapper.deleteBatch(list);

        //重新插入相关联的菜品记录
        for(SetmealDish setmealDish : setmealDishes){
            setmealDish.setSetmealId(setmealDTO.getId());
        }
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 修改套餐状态
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        //1.判断套餐状态是否能够修改--修改成起售时，套餐中不能有停售的菜品
        if(status == StatusConstant.ENABLE){
            //查找与套餐id相关联的SetmealDish表记录
            List<SetmealDish> list = setmealDishMapper.getBySetmealId(id);
            for(SetmealDish setmealDish : list){
                //遍历记录通过其中的id值获取dish表记录
                Dish dish = dishMapper.getById(setmealDish.getDishId());
                //判断菜品是否为停售状态
                if(dish.getStatus() == StatusConstant.DISABLE){
                    throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }
            }
        }

        //2.修改套餐状态
        Setmeal setmeal = Setmeal.builder().id(id).status(status).build();
        setmealMapper.update(setmeal);
    }

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    @Override
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    @Override
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
