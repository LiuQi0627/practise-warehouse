package com.messi.system.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 封装基础的操作数据库功能
 */
public class BaseDAO<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> {


}
