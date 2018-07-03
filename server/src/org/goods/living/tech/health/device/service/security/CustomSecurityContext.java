package org.goods.living.tech.health.device.service.security;

import javax.ws.rs.core.SecurityContext;

import java.security.Principal;

public class CustomSecurityContext implements SecurityContext {

	private final ValidUser user;
    private final String scheme;

    public CustomSecurityContext(ValidUser user, String scheme) {
        this.user = user;
        this.scheme = scheme;
    }

    @Override
    public Principal getUserPrincipal() {
        return this.user;
    }

    @Override
    public boolean isUserInRole(String s) {
        if (user.getRole() != null) {
            return user.getRole().contains(s);
        }
        return false;
    }

    @Override
    public boolean isSecure() {
        //return "https".equals(this.scheme);
        return true;
    }

    @Override
    public String getAuthenticationScheme() {
        return SecurityContext.BASIC_AUTH;
    }
}
