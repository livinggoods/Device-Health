package org.goods.living.tech.health.device.filter;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Priority;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

//import sg.edu.ntu.medicine.lkc.cephas.redcap.enums.COPDUserCategory;
import org.goods.living.tech.health.device.service.security.qualifier.UserCategory;

import javax.ws.rs.Priorities;
import org.goods.living.tech.health.device.service.security.qualifier.Secured;



@Secured
@Provider
@Priority(Priorities.AUTHORIZATION)
@RequestScoped
public class AuthorizationFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        // Get the resource class which matches with the requested URL
        // Extract the roles declared by it
        Class<?> resourceClass = resourceInfo.getResourceClass();
        List<UserCategory> classRoles = extractRoles(resourceClass);

        // Get the resource method which matches with the requested URL
        // Extract the roles declared by it
        Method resourceMethod = resourceInfo.getResourceMethod();
        List<UserCategory> methodRoles = extractRoles(resourceMethod);

        try {

            // Check if the user is allowed to execute the method
            // The method annotations override the class annotations
            if (methodRoles.isEmpty()) {
                checkPermissions(requestContext.getSecurityContext(),classRoles);
            } else {
                checkPermissions(requestContext.getSecurityContext(),methodRoles);
            }
        } catch (Exception e) {
            requestContext.abortWith(
                Response.status(Response.Status.FORBIDDEN).build());
        }
    }

    // Extract the roles from the annotated element
    private List<UserCategory> extractRoles(AnnotatedElement annotatedElement) {
        if (annotatedElement == null) {
            return new ArrayList<UserCategory>();
        } else {
            Secured secured = annotatedElement.getAnnotation(Secured.class);
            if (secured == null) {
                return new ArrayList<UserCategory>();
            } else {
            	UserCategory[] allowedRoles = secured.value();
                return Arrays.asList(allowedRoles);
            }
        }
    }

    private void checkPermissions(SecurityContext securityContext,List<UserCategory> allowedRoles) throws Exception {
        // Check if the user contains one of the allowed roles
        // Throw an Exception if the user has not permission to execute the method
        if(allowedRoles.isEmpty()) return;
        for (UserCategory category:allowedRoles){
    		if(securityContext.isUserInRole(category.toString().toUpperCase()))
    			return;
    	}

    	throw new Exception("User is not allowed to use this method");
    }

}