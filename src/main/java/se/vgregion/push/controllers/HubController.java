package se.vgregion.push.controllers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HubController {

    private final static Logger LOG = LoggerFactory.getLogger(HubController.class);

    
    @Resource(name="retrieverQueue")
    private BlockingQueue<String> retrieverQueue;
    
    public BlockingQueue<String> getRetrieverQueue() {
        return retrieverQueue;
    }

    public void setRetrieverQueue(BlockingQueue<String> retrieverQueue) {
        this.retrieverQueue = retrieverQueue;
    }


    @RequestMapping(value="/", method=RequestMethod.POST)
    public ModelAndView post(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String mode = request.getParameter("hub.mode");
        
        System.out.println(mode);
        if("publish".equals(mode)) {
            LOG.info("Received publish request");
            String[] urls = request.getParameterValues("hub.url");
            if(urls != null) {
                for(String url : urls) {
                    publish(url, response);
                }
            } else {
                response.sendError(500, "Missing hub.url parameter");
            }
        }
        
        return null;
    }

    private void publish(String url, HttpServletResponse response) throws IOException {
        try {
            URI uri = new URI(url);
            
            if("http".equals(uri.getScheme())) {
                 // TODO handle https
                
                boolean wasAdded = false;
                try {
                    wasAdded = retrieverQueue.offer(url, 2000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    wasAdded = false;
                }
                
                if(wasAdded) {
                    response.sendError(204);
                } else {
                    response.sendError(500, "Internal error, failed to publish update");
                }
            } else {
                response.sendError(500, "Only HTTP URLs allowed: " + url);
            }
        } catch (URISyntaxException e1) {
            response.sendError(500, "Invalid hub.url value: " + url);
        }
    }
}
