package com.yh.dwdatalink.controller;


import com.yh.dwdatalink.service.CustSvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@org.springframework.web.bind.annotation.RestController
public class RestController {

	@Autowired
	CustSvc custSvc;



		@RequestMapping(value="/cust/{id}",method= RequestMethod.GET)
		public String sayHello(@PathVariable("id") Integer id){
			custSvc.queryByLock(id);

			return "haha";
		}

	


//	@RequestMapping("/kill")
//	@ResponseBody
//	public String ping() {
//
//		return "kill";
//	}
}
