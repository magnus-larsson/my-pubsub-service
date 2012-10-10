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
 */

package se.vgregion.pubsub.servlets;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ProbeServlet extends HttpServlet {

	private static final String CONFIG_FILE_PARAM_NAME = "configuration-file";
	private static final String CONFIG_PROPERTY = "probe.response";
	
	private String probeResponse;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		String configFilePath = config.getInitParameter(CONFIG_FILE_PARAM_NAME);
		
		if(configFilePath == null) {
			throw new ServletException("Servlet init parameter \"" + CONFIG_FILE_PARAM_NAME + "\" missing");
		}
		
		// handle user home
		configFilePath = configFilePath.replace("~", System.getProperty("user.home"));
		
		Properties configFile = new Properties();
		
		FileInputStream in = null;
		try {
			in = new FileInputStream(configFilePath);
			configFile.load(in);
			
			this.probeResponse = configFile.getProperty(CONFIG_PROPERTY);
			if(probeResponse == null) {
				throw new ServletException("Missing configuration property \"" + CONFIG_PROPERTY + "\" from " + configFilePath);
			}
		} catch (IOException e) {
			throw new ServletException("Can not load configuration file from " + configFilePath);
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}	
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setContentType("text/plain");
		resp.getWriter().write(probeResponse);
	}

	
}
