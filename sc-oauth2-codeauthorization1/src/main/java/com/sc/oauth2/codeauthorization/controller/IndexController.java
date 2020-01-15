package com.sc.oauth2.codeauthorization.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {
    /**
     * 会员列表页面
     */
    @RequestMapping("/")
    public ModelAndView list() {
    	System.out.println("into index controller");
        ModelAndView modelAndView = new ModelAndView("/index");
        return modelAndView;
    }
}
