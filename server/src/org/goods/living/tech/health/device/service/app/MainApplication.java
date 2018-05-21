package org.goods.living.tech.health.device.service.app;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.jboss.resteasy.plugins.interceptors.CorsFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goods.living.tech.health.device.service.UserService;


//@ApplicationPath("/main")
public class MainApplication {//extends Application {

	Logger logger = LogManager.getLogger();// .getName());
	
    /**
     * Set<Class<?>> classes is required for CDI
     */
    private Set<Class<?>> classes = new HashSet<>();
    private Set<Object> singletons;

    public MainApplication() {
    	logger.debug("RestApplication() called");
        
        classes.add(UserService.class);
    }

  //  @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }

  //  @Override
    public Set<Object> getSingletons() {
        if (singletons == null) {
            CorsFilter corsFilter = new CorsFilter();
            corsFilter.getAllowedOrigins().add("*");
            corsFilter.setAllowCredentials(true);
            corsFilter.setAllowedMethods("GET, POST, PUT, DELETE, OPTIONS, HEAD");
            corsFilter.setAllowedHeaders("origin, content-type, accept, authorization");
            corsFilter.setCorsMaxAge(1209600);
            singletons = new LinkedHashSet<Object>();
            singletons.add(corsFilter);

//            CustomCorsFilter customCorsFilter = new CustomCorsFilter();
//            singletons.add(customCorsFilter);
        }
        return singletons;
    }

}