package org.goods.living.tech.health.device.filter;

import java.io.IOException;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import javax.ws.rs.Priorities;
import javax.annotation.Priority;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;

import io.jsonwebtoken.SignatureAlgorithm;
import org.goods.living.tech.health.device.service.security.CustomSecurityContext;
import org.goods.living.tech.health.device.service.security.ValidUser;
import org.goods.living.tech.health.device.service.security.qualifier.Secured;
import org.goods.living.tech.health.device.utility.ApplicationParameters;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION) //provide by java ee 7 up
@RequestScoped
public class AuthenticationFilter implements ContainerRequestFilter {
	
	@Inject 
	private ApplicationParameters applicationParameters;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
    	
    	System.out.println("request filter: " + applicationParameters.getTokenLife());
    	
        // Get the HTTP Authorization header from the request
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        // Check if the HTTP Authorization header is present and formatted correctly 
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new NotAuthorizedException("Authorization header must be provided");
        }
        // Extract the token from the HTTP Authorization header
        String token = authorizationHeader.substring("Bearer".length()).trim();

        try {
            // Set the custom security context
            final ValidUser user = validateToken(token);
            String scheme = "Token-Based-Auth-Scheme";
            requestContext.setSecurityContext(new CustomSecurityContext(user, scheme));

        } catch (Exception e) {
            System.out.println("Exception: " + e);
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }

    public ValidUser validateToken(String token) throws Exception {

        Claims claim = Jwts.parser().setSigningKey(applicationParameters.getHashKey()).parseClaimsJws(token).getBody();
        String username = (String) claim.getSubject();
        String api = (String) claim.getId();
        String site = (String) claim.get("site");
        String firstname = (String) claim.get("firstName");
        String role = (String) claim.get("roles");

        System.out.println("Here comes the api code: " + api);
        System.out.println("Site of the user is: " + site);
        ValidUser user = new ValidUser();
        user.setUserName(username);
        user.setSite(site);
        user.setFirstName(firstname);
        user.setJwtToken(token);
        user.setApiToken(api);
        user.setRole(role);
        user.getJwtToken();

        return user;
    }
}
