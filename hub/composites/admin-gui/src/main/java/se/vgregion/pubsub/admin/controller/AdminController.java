/**
 * Copyright 2010 Västra Götalandsregionen
 *
 *   This library is free software; you can redistribute it and/or modify
 *   it under the terms of version 2.1 of the GNU Lesser General Public
 *   License as published by the Free Software Foundation.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the
 *   Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *   Boston, MA 02111-1307  USA
 *
 */

package se.vgregion.pubsub.admin.controller;

import java.io.IOException;
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
import se.vgregion.pubsub.push.FailedSubscriberVerificationException;
import se.vgregion.pubsub.push.PolledPublisher;
import se.vgregion.pubsub.push.PushSubscriber;

/**
 * Spring MVC controller for administering PuSH subscribers
 *
 */
@Controller
public class AdminController {

    @Resource
    private AdminService adminService;
    
    @RequestMapping("/admin")
    public ModelAndView index() {
        ModelAndView mav = new ModelAndView("admin/index");
        
        mav.addObject("pushSubscribers", adminService.getAllPushSubscribers());
        mav.addObject("polledPublishers", adminService.getAllPolledPublishers());
        
        return mav;
    }

    @RequestMapping("/admin/push/new")
    public ModelAndView newPushSubscriber() {
        ModelAndView mav = new ModelAndView("admin/push-edit");
        
        return mav;
    }

    @RequestMapping("/admin/push/{id}/edit")
    public ModelAndView editPushSubscriber(@PathVariable("id") UUID id) {
        PushSubscriber subscriber = adminService.getPushSubscriber(id);
        if(subscriber != null) {
            ModelAndView mav = new ModelAndView("admin/push-edit");
            mav.addObject("subscriber", subscriber);
            return mav;
        } else {
            throw new RuntimeException("Unknown subscriber");
        }
        
        
    }
    
    @RequestMapping(value="/admin/push/new", method=RequestMethod.POST)
    public ModelAndView createPushSubscriber(
        @RequestParam("topic") URI topic,    
        @RequestParam("callback") URI callback,    
        @RequestParam(value="leaseSeconds", required=false) Integer leaseSeconds,    
        @RequestParam(value="verifyToken", required=false) String verifyToken,
        @RequestParam(value="secret", required=false) String secret
        ) throws IOException, FailedSubscriberVerificationException {

        if(leaseSeconds == null) {
            leaseSeconds = 0;
        }
        adminService.createPushSubscriber(topic, callback, leaseSeconds, verifyToken, secret);
        
        return new ModelAndView("redirect:..");
    }

    @RequestMapping(value="/admin/push/{id}/edit", method=RequestMethod.POST)
    public ModelAndView updatePushSubscriber(
        @RequestParam("id") UUID id,    
        @RequestParam("topic") URI topic,    
        @RequestParam("callback") URI callback,    
        @RequestParam(value="leaseSeconds", required=false) Integer leaseSeconds,    
        @RequestParam(value="verifyToken", required=false) String verifyToken,
        @RequestParam(value="secret", required=false) String secret,
        @RequestParam(value="delete", required=false) String delete) throws IOException, FailedSubscriberVerificationException {

        if(leaseSeconds == null) {
            leaseSeconds = 0;
        }
        
        if(delete != null) {
            // delete the subscriber
            adminService.removePushSubscriber(id);
        } else {
            // update
            adminService.updatePushSubscriber(id, topic, callback, leaseSeconds, verifyToken, secret);
        }
        
        return new ModelAndView("redirect:../..");
    }

    
    @RequestMapping("/admin/polled/new")
    public ModelAndView newPolledPublisher() {
        ModelAndView mav = new ModelAndView("admin/polled-edit");
        
        return mav;
    }

    @RequestMapping("/admin/polled/{id}/edit")
    public ModelAndView editPolledPublisher(@PathVariable("id") UUID id) {
        PolledPublisher publisher = adminService.getPolledPublishers(id);
        if(publisher != null) {
            ModelAndView mav = new ModelAndView("admin/polled-edit");
            mav.addObject("publisher", publisher);
            return mav;
        } else {
            throw new RuntimeException("Unknown publisher");
        }
        
        
    }
    
    @RequestMapping(value="/admin/polled/new", method=RequestMethod.POST)
    public ModelAndView createPolledPublisher(
        @RequestParam("url") URI url    
    	) throws IOException, FailedSubscriberVerificationException {

        adminService.createPolledPublishers(url);
        
        return new ModelAndView("redirect:..");
    }

    @RequestMapping(value="/admin/polled/{id}/edit", method=RequestMethod.POST)
    public ModelAndView updatePolledPublisher(
        @RequestParam("id") UUID id,    
        @RequestParam("url") URI url,    
        @RequestParam(value="delete", required=false) String delete) throws IOException, FailedSubscriberVerificationException {

        if(delete != null) {
            // delete the subscriber
            adminService.removePolledPublishers(id);
        } else {
            // update
            adminService.updatePolledPublishers(id, url);
        }
        
        return new ModelAndView("redirect:../..");
    }

    
    public AdminService getAdminService() {
        return adminService;
    }

    public void setAdminService(AdminService adminService) {
        this.adminService = adminService;
    }
}
