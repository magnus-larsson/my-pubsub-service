package se.vgregion.pubsub.admin.controller;

import java.net.URI;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import se.vgregion.pubsub.admin.service.AdminService;

@Controller
public class AdminController {

    @Resource
    private AdminService adminService;
    
    
    @RequestMapping("/admin")
    public ModelAndView index() {
        ModelAndView mav = new ModelAndView("admin/index");
        
        mav.addObject("pushSubscribers", adminService.getAllPushSubscribers());
        
        return mav;
    }

    @RequestMapping("/admin/new")
    public ModelAndView newPushSubscriber() {
        ModelAndView mav = new ModelAndView("admin/edit");
        
        return mav;
    }

    @RequestMapping(value="/admin/new", method=RequestMethod.POST)
    public ModelAndView creataPushSubscriber(
        @RequestParam("topic") URI topic,    
        @RequestParam("callback") URI callback,    
        @RequestParam(value="leaseSeconds", required=false) int leaseSeconds,    
        @RequestParam(value="verifyToken", required=false) String verifyToken) {

        adminService.createPushSubscriber(topic, callback, leaseSeconds, verifyToken);
        
        return new ModelAndView("redirect:..");
    }

}
