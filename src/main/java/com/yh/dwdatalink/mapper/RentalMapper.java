package com.yh.dwdatalink.mapper;

import com.yh.dwdatalink.domain.Rental;

import java.util.List;

public interface RentalMapper {
    int deleteByPrimaryKey(Integer rentalId);

    int insert(Rental record);

    int insertSelective(Rental record);

    Rental selectByPrimaryKey(Integer rentalId);

    int updateByPrimaryKeySelective(Rental record);

    int updateByPrimaryKey(Rental record);

    List<Rental> selectRLock(int custId);
}