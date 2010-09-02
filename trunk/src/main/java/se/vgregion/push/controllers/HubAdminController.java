package se.vgregion.push.controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class HubAdminController {

    @Resource(name="retrieverQueue")
    private BlockingQueue<String> retrieverQueue;

    @Resource(name="distributionQueue")
    private BlockingQueue<String> distributionQueue;
    
    @RequestMapping(value="/admin", method=RequestMethod.GET)
    public void post(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter();
        
        response.setContentType("text/plain");
        
        writer.println("retriever_depth=" + retrieverQueue.size());
        writer.println("distributor_depth=" + distributionQueue.size());
    }

}
