package se.vgregion.pubsub.admin.controller;

import java.net.URI;
import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import se.vgregion.pubsub.admin.service.AdminService;
import se.vgregion.pubsub.push.PushSubscriber;

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

    @RequestMapping("/admin/{id}/edit")
    public ModelAndView editPushSubscriber(@PathVariable("id") UUID id) {
        PushSubscriber subscriber = adminService.getPushSubscriber(id);
        if(subscriber != null) {
            ModelAndView mav = new ModelAndView("admin/edit");
            mav.addObject("subscriber", subscriber);
            return mav;
        } else {
            throw new RuntimeException("Unknown subscriber");
        }
        
        
    }
    
    @RequestMapping(value="/admin/new", method=RequestMethod.POST)
    public ModelAndView createPushSubscriber(
        @RequestParam("topic") URI topic,    
        @RequestParam("callback") URI callback,    
        @RequestParam(value="leaseSeconds", required=false) Integer leaseSeconds,    
        @RequestParam(value="verifyToken", required=false) String verifyToken) {

        if(leaseSeconds == null) {
            leaseSeconds = 0;
        }
        adminService.createPushSubscriber(topic, callback, leaseSeconds, verifyToken);
        
        return new ModelAndView("redirect:");
    }

    @RequestMapping(value="/admin/{id}/edit", method=RequestMethod.POST)
    public ModelAndView updatePushSubscriber(
        @RequestParam("id") UUID id,    
        @RequestParam("topic") URI topic,    
        @RequestParam("callback") URI callback,    
        @RequestParam(value="leaseSeconds", required=false) Integer leaseSeconds,    
        @RequestParam(value="verifyToken", required=false) String verifyToken,
        @RequestParam(value="delete", required=false) String delete) {

        if(leaseSeconds == null) {
            leaseSeconds = 0;
        }
        
        if(delete != null) {
            // delete the subscriber
            adminService.removePushSubscriber(id);
        } else {
            // update
            adminService.updatePushSubscriber(id, topic, callback, leaseSeconds, verifyToken);
        }
        
        return new ModelAndView("redirect:..");
    }

    public AdminService getAdminService() {
        return adminService;
    }

    public void setAdminService(AdminService adminService) {
        this.adminService = adminService;
    }
}
