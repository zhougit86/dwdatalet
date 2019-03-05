package com.yh.dwdatalink.controller;


import com.yh.dwdatalink.service.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RestController {

	@Autowired
	ProcessService procSvc;
	


	@RequestMapping("/kill")
	@ResponseBody
	public String ping() {
		try{
			procSvc.killProc();
		}catch (Exception e){
			return e.getMessage();
		}

		return "kill";
	}
}
