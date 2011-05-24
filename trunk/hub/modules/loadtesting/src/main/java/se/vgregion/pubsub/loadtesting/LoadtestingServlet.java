package se.vgregion.pubsub.loadtesting;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

@SuppressWarnings("serial")
public class LoadtestingServlet extends HttpServlet {

	private Map<UUID, CountDownLatch> publications = new ConcurrentHashMap<UUID, CountDownLatch>();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// ping publish
		// wait for notification
		// response
		// error if timeout

		if (req.getPathInfo().equals("/feed")) {
			// serve ATOM feed
			resp.getWriter().write(atom(getFragment(req)));
		} else {
			// publish

			UUID id = UUID.randomUUID();
			String publicationUrl = "http://" + req.getServerName() + ":"
					+ req.getServerPort() + req.getContextPath() + "/feed#"
					+ id;

			String hubUrl = System.getProperty("hub.url");
			if(hubUrl == null) {
				throw new RuntimeException("System properti hub.url missing");
			}
			
			try {
				CountDownLatch latch = new CountDownLatch(1);
				publications.put(id, latch);
	
				HttpPost publication = new HttpPost(hubUrl);
				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
				parameters.add(new BasicNameValuePair("hub.mode", "publish"));
				parameters.add(new BasicNameValuePair("hub.url", publicationUrl));
	
				publication.setEntity(new UrlEncodedFormEntity(parameters));
	
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpResponse publicationResponse = httpClient.execute(publication);

				if (publicationResponse.getStatusLine().getStatusCode() == 204) {
					try {
						if (latch.await(20000, TimeUnit.MILLISECONDS)) {
							// all happy, return
							return;
						} else {
							// timeout
							resp.sendError(591);
						}
					} catch (InterruptedException e) {
						// interrupted
						resp.sendError(592);
					}
				} else {
					// publication failed
					resp.sendError(publicationResponse.getStatusLine()
							.getStatusCode(), publicationResponse.getStatusLine()
							.getReasonPhrase());
				}
			} finally {
				// try to prevent memory leaks
				publications.remove(id);
			}
		}
	}

	private String getFragment(HttpServletRequest request) {
		// !!! Jetty dependent
		String s = request.toString();
		int hash = s.indexOf("#");
		int end = s.indexOf("]", hash);
		if (hash > -1) {
			return s.substring(hash + 1, end);
		} else {
			throw new RuntimeException("Can not parse fragment, this code is Jetty specific for now");
		}
	}

	private String atom(String id) {
		return "<feed xmlns='http://www.w3.org/2005/Atom'>"
				+ "<title>foobar</title>" + "<id>urn:f1</id>" + "<entry>"
				+ "<title>t1</title>" + "<id>uuid:" + id + "</id>"
				+ "<updated>" + print(new DateTime()) + "</updated>"
				+ "</entry></feed>";
	}

	public static String print(DateTime value) {
		DateTimeFormatter fmt = ISODateTimeFormat.dateTime().withZone(
				DateTimeZone.UTC);
		return fmt.print(value);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		// find the ID in the feed content
		BufferedReader reader = req.getReader();
		String line = reader.readLine();
		while(line != null) {
			int start = line.indexOf("uuid:");
			if(start > -1) {
				int end = line.indexOf("<", start);
				String id = line.substring(start + 5, end);
				UUID uuid = UUID.fromString(id);
				
				// found id, now try to count down latch
				CountDownLatch latch = publications.get(uuid);
				if(latch != null) {
					latch.countDown();
					
					publications.remove(id);
					return;
				} else {
					throw new RuntimeException("Could not find latch");
				}
			}
			line = reader.readLine();
		}
	}

}
