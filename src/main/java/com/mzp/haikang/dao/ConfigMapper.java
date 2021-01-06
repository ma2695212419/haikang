package com.mzp.haikang.dao;

import com.mzp.haikang.model.Config;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @
 * @author  mzp
 * @date  2021/1/6 15:19
 * @version 1.0
 * 
 */
@Mapper
public interface ConfigMapper {
    int deleteByPrimaryKey(String ip);

    int insert(Config record);

    int insertSelective(Config record);

    Config selectByPrimaryKey(String ip);

    int updateByPrimaryKeySelective(Config record);

    int updateByPrimaryKey(Config record);

    List<Config> selectAll();
}