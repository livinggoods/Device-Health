package org.goods.living.tech.health.device.cdi;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;

@Named
@RequestScoped
public class Hello {

    private String test = "Hello world";

    @Inject
	private ServletContext servletContext;

    public Hello() {
    	System.out.println("Hello Constructor called");

    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

}