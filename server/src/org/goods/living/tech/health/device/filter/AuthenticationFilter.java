package org.goods.living.tech.health.device.filter;

import java.io.IOException;

import javax.annotation.Priority;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.DatatypeConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goods.living.tech.health.device.jpa.controllers.AdminUsersJpaController;
import org.goods.living.tech.health.device.jpa.controllers.UsersJpaController;
import org.goods.living.tech.health.device.jpa.dao.AdminUsers;
import org.goods.living.tech.health.device.jpa.dao.Users;
import org.goods.living.tech.health.device.service.security.CustomSecurityContext;
import org.goods.living.tech.health.device.service.security.ValidUser;
import org.goods.living.tech.health.device.service.security.qualifier.Secured;
import org.goods.living.tech.health.device.service.security.qualifier.UserCategory;
import org.goods.living.tech.health.device.utility.ApplicationParameters;
import org.goods.living.tech.health.device.utility.Constants;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION) // provide by java ee 7 up
@RequestScoped
public class AuthenticationFilter implements ContainerRequestFilter {

	@Inject
	private ApplicationParameters applicationParameters;

	@Inject
	UsersJpaController usersJpaController;

	@Inject
	AdminUsersJpaController adminUsersJpaController;

	Logger logger = LogManager.getLogger();

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {

		logger.debug("request filter: ", requestContext.getHeaders());

		// Get the HTTP Authorization header from the request
		String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

		// Check if the HTTP Authorization header is present and formatted correctly
		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {

			if (isAdminEndpoint(requestContext)) {
				logger.debug("Unauthorised - aborting request");
				requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
				return;
				// throw new NotAuthorizedException("Authorization header must be provided");
			}

			// requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
			logger.debug("Skipping auth filter. TODO remove this soon");
			return;

		}
		// Extract the token from the HTTP Authorization header
		String token = authorizationHeader.substring("Bearer".length()).trim();

		try {
			// Set the custom security context
			final ValidUser user = validateToken(token);
			String scheme = "Token-Based-Auth-Scheme";
			final SecurityContext currentSecurityContext = requestContext.getSecurityContext();
			requestContext.setSecurityContext(new CustomSecurityContext(user, scheme));

		} catch (Exception e) {
			logger.error("Exception", e);

			if (isAdminEndpoint(requestContext)) {
				logger.debug("Unauthorised - aborting request");
				requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
				return;
				// throw new NotAuthorizedException("Authorization header must be provided");
			}

			logger.debug("Skipping auth filter. TODO remove this soon");
			return;
			// logger.debug("Forbidden");
			// requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
		}

	}

	boolean isAdminEndpoint(ContainerRequestContext requestContext) {
		boolean isadmin = requestContext.getUriInfo().getPath().toString().contains(Constants.URL.FIND);

		isadmin = isadmin || requestContext.getUriInfo().getPath().toString().contains(Constants.URL.FIND);
		return (isadmin);
	}

	public ValidUser validateToken(String token) throws Exception {

		Claims claim = Jwts.parser()
				.setSigningKey(DatatypeConverter.parseBase64Binary(applicationParameters.getHashKey()))
				.parseClaimsJws(token).getBody();
		String username = (String) claim.getSubject();
		Long id = Long.parseLong(claim.getId());
		String site = (String) claim.get("site");
		String role = (String) claim.get("roles");
		String chvId = (String) claim.get("chvId");

		logger.debug("Here comes the id code: " + id);
		logger.debug("ChvId: " + chvId);
		logger.debug("Site of the user is: " + site);

		ValidUser user = new ValidUser();
		user.setId(id);
		user.setUserName(username);
		user.setSite(site);
		user.setJwtToken(token);
		// user.setApiToken();
		user.setRole(role);

		if (role.contains(UserCategory.USER.toString())) {
			Users users = usersJpaController.findUsers(id);
			if (users == null) {
				logger.debug("Token is valid but no user " + id);
				throw new Exception("Token is valid but no user " + id);
			}
		}
		if (role.contains(UserCategory.ADMIN.toString())) {
			AdminUsers users = adminUsersJpaController.find(username);
			if (users == null) {
				logger.debug("Token is valid but no user " + id);
				throw new Exception("Token is valid but no user " + id);
			}
		}

		return user;
	}
}
