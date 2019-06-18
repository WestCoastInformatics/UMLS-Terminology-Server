package com.wci.umls.server.rest.impl;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Servlet Filter implementation class UserActivityLoggingFilter
 */
public class UserActivityLoggingFilter implements Filter {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final Logger activityLog = LoggerFactory.getLogger("USER_ACTIVITY_LOGGER");
	private static final String[] webContent = {".js",".css",".html",".jpg",".png"};


	/**
	 * Default constructor.
	 */
	public UserActivityLoggingFilter() {
		// default constructor
	}

	/**
	 * @see Filter#destroy()
	 */
	@Override
	public void destroy() {
		// not used
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		// log all requests

		HttpServletRequest sr = (HttpServletRequest) request;
		log.debug("URI: {}", sr.getRequestURI());

		if (Arrays.stream(webContent).parallel().noneMatch(sr.getRequestURI()::contains))
		{
			StringBuilder logString = new StringBuilder();

			logString.append("[HOST: ").append(sr.getRemoteAddr()).append("]");
			logString.append(" [AUTH: ").append(sr.getHeader("authorization")).append("]");
			logString.append("[USER_AGENT: ").append(sr.getHeader("user-agent")).append("]");

			// don't fail if can't parse cookie, just log and continue.
			try {
				if (getCookieValue(sr, "user") != null) {
					String userCookie = URLDecoder.decode(getCookieValue(sr, "user"), "UTF-8");
					logString.append(" [USER: ").append(getValueFromJson(userCookie, "userName")).append("]");
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}

			logString.append(" [PATH: ").append(sr.getRequestURI()).append("]");

			activityLog.info("{}", logString);
		}

		sr.setAttribute("isApiCall", isApiCall(sr.getHeader("user-agent")));
		
		// pass the request along the filter chain
		chain.doFilter(request, response);
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	@Override
	public void init(FilterConfig fConfig) throws ServletException {
		// not used
	}

	/**
	 * 
	 * @param req
	 *            HttpServletRequest
	 * @param cookieName
	 *            String Name of the cookie to retrieve from the
	 *            HttpServletRequest
	 * @return
	 */
	private String getCookieValue(HttpServletRequest req, String cookieName) {

		if (req != null && req.getCookies() != null) {
			return Arrays.stream(req.getCookies()).filter(c -> c.getName().equals(cookieName)).findFirst()
					.map(Cookie::getValue).orElse(null);
		} else {
			return null;
		}
	}

	/**
	 * 
	 * This is for use when the JSON object cannot be mapped to a Java object.
	 * 
	 * @param jsonString
	 *            String JSON formatted string
	 * @param key
	 *            String Name of the property to retrieve
	 * @return value of the key
	 * @throws IOException
	 */
	private String getValueFromJson(String jsonString, String key) throws IOException {

		if (jsonString == null || key == null) {
			return null;
		} else {

			JsonFactory factory = new JsonFactory();
			ObjectMapper mapper = new ObjectMapper(factory);
			JsonNode rootNode = mapper.readTree(jsonString);

			Iterator<Map.Entry<String, JsonNode>> fieldsIterator = rootNode.fields();
			while (fieldsIterator.hasNext()) {
				Map.Entry<String, JsonNode> field = fieldsIterator.next();
				if (key.equalsIgnoreCase(field.getKey())) {
					return field.getValue().toString();
				}
			}
		}
		return null;
	}
	
	
	protected boolean isApiCall(String browserDetails) {

		final String[] browserCodes = {"msie", "safari", "opr", "opera", "chrome", "mozilla", "firefox", "rv" };
		
		return (Arrays.stream(browserCodes).parallel().noneMatch(browserDetails.toLowerCase()::contains));
	}

}
