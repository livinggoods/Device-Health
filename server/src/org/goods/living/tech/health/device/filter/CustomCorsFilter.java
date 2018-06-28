package org.goods.living.tech.health.device.filter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.resteasy.resteasy_jaxrs.i18n.Messages;
import org.jboss.resteasy.spi.CorsHeaders;

/**
 * Handles CORS requests both preflight and simple CORS requests. You must bind
 * this as a singleton and set up allowedOrigins and other settings to use.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
// @PreMatching
@Provider
public class CustomCorsFilter implements ContainerRequestFilter, ContainerResponseFilter {
	protected boolean allowCredentials = true;
	protected String allowedMethods;
	protected String allowedHeaders;
	protected String exposedHeaders;
	protected int corsMaxAge = -1;
	protected Set<String> allowedOrigins = new HashSet<String>();

	Logger logger = LogManager.getLogger();

	/**
	 * Put "*" if you want to accept all origins
	 *
	 * @return
	 */
	public Set<String> getAllowedOrigins() {
		;
		return allowedOrigins;
	}

	/**
	 * Defaults to true
	 *
	 * @return
	 */
	public boolean isAllowCredentials() {
		return allowCredentials;
	}

	public void setAllowCredentials(boolean allowCredentials) {
		this.allowCredentials = allowCredentials;
	}

	/**
	 * Will allow all by default
	 *
	 * @return
	 */
	public String getAllowedMethods() {
		return allowedMethods;
	}

	/**
	 * Will allow all by default comma delimited string for
	 * Access-Control-Allow-Methods
	 *
	 * @param allowedMethods
	 */
	public void setAllowedMethods(String allowedMethods) {
		this.allowedMethods = allowedMethods;
	}

	public String getAllowedHeaders() {
		return allowedHeaders;
	}

	/**
	 * Will allow all by default comma delimited string for
	 * Access-Control-Allow-Headers
	 *
	 * @param allowedHeaders
	 */
	public void setAllowedHeaders(String allowedHeaders) {
		this.allowedHeaders = allowedHeaders;
	}

	public int getCorsMaxAge() {
		return corsMaxAge;
	}

	public void setCorsMaxAge(int corsMaxAge) {
		this.corsMaxAge = corsMaxAge;
	}

	public String getExposedHeaders() {
		return exposedHeaders;
	}

	/**
	 * comma delimited list
	 *
	 * @param exposedHeaders
	 */
	public void setExposedHeaders(String exposedHeaders) {
		this.exposedHeaders = exposedHeaders;
	}

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		logger.debug("CorsFilter filter with request");
		String origin = requestContext.getHeaderString(CorsHeaders.ORIGIN);
		if (origin == null) {
			return;
		}
		if (requestContext.getMethod().equalsIgnoreCase("OPTIONS")) {
			preflight(origin, requestContext);
		} else {
			checkOrigin(requestContext, origin);
		}
	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		logger.debug("CorsFilter filter with request and response");
		String origin = requestContext.getHeaderString(CorsHeaders.ORIGIN);
		if (origin == null || requestContext.getMethod().equalsIgnoreCase("OPTIONS")
				|| requestContext.getProperty("cors.failure") != null) {
			// don't do anything if origin is null, its an OPTIONS request, or
			// cors.failure is set
			return;
		}

		// if (!responseContext.getHeaders().containsValue(origin)) {
		// responseContext.getHeaders().putSingle(CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
		// origin);
		// }
		if (allowCredentials)
			responseContext.getHeaders().putSingle(CorsHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");

		if (exposedHeaders != null) {
			responseContext.getHeaders().putSingle(CorsHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, exposedHeaders);
		}
	}

	protected void preflight(String origin, ContainerRequestContext requestContext) throws IOException {
		logger.debug("CorsFilter preflight with origin request");
		checkOrigin(requestContext, origin);

		Response.ResponseBuilder builder = Response.ok();
		builder.header(CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
		if (allowCredentials)
			builder.header(CorsHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
		String requestMethods = requestContext.getHeaderString(CorsHeaders.ACCESS_CONTROL_REQUEST_METHOD);
		if (requestMethods != null) {
			if (allowedMethods != null) {
				requestMethods = this.allowedMethods;
			}
			builder.header(CorsHeaders.ACCESS_CONTROL_ALLOW_METHODS, requestMethods);
		}
		String allowHeaders = requestContext.getHeaderString(CorsHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
		if (allowHeaders != null) {
			if (allowedHeaders != null) {
				allowHeaders = this.allowedHeaders;
			}
			builder.header(CorsHeaders.ACCESS_CONTROL_ALLOW_HEADERS, allowHeaders);
		}
		if (corsMaxAge > -1) {
			builder.header(CorsHeaders.ACCESS_CONTROL_MAX_AGE, corsMaxAge);
		}
		requestContext.abortWith(builder.build());

	}

	protected void checkOrigin(ContainerRequestContext requestContext, String origin) {
		logger.debug("CorsFilter checkOrigin with request origin");

		allowedOrigins.add("*");
		// allowedOrigins.add("localhost");
		if (!allowedOrigins.contains("*") && !allowedOrigins.contains(origin)) {
			requestContext.setProperty("cors.failure", true);
			throw new ForbiddenException(Messages.MESSAGES.originNotAllowed(origin));
		}
	}
}