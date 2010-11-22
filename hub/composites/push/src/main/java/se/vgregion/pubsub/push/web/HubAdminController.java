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

package se.vgregion.pubsub.push.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import se.vgregion.push.services.DistributionRequest;
import se.vgregion.push.services.RetrievalRequest;

/*@Controller*/
public class HubAdminController {

    @Resource(name="retrieveQueue")
    private BlockingQueue<RetrievalRequest> retrieverQueue;

    @Resource(name="distributionQueue")
    private BlockingQueue<DistributionRequest> distributionQueue;
    
    @RequestMapping(value="/admin", method=RequestMethod.GET)
    public void post(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter();
        
        response.setContentType("text/plain");
        
        writer.println("retriever_depth=" + retrieverQueue.size());
        writer.println("distributor_depth=" + distributionQueue.size());
    }

}
