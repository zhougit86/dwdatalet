package com.yh.dwdatalink.service;

import com.yh.dwdatalink.domain.Rental;
import com.yh.dwdatalink.mapper.RentalMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustSvcImpl implements CustSvc {
    @Autowired
    RentalMapper rentalMapper;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void queryByLock(int custId){

        List<Rental> rentalList = rentalMapper.selectRLock(custId);
        System.err.printf("q: %d, got %d \n",custId,rentalList.size());
        try{
            Thread.sleep(6000);
        }catch (Exception e){

        }
        System.err.printf("q: %d ended \n",custId);
    }

}
