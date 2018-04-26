package org.goods.living.tech.health.device.service.app;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Provider
public class GenericExceptionHandler implements ExceptionMapper<Throwable> {

	
	Logger logger = LogManager.getLogger();
	
	  class ErrorMessage {
	    int status;
	    String message;
	    String developerMessage;

	    public ErrorMessage(int status, String message) {
	      this.status = status;
	      String[] messages = message.split("\\|");
	      this.message = messages[0];
	      if(messages.length > 1) {
	        this.developerMessage = messages[1];
	      }
	    }

	    public int getStatus() { return this.status; }
	    public String getMessage() { return this.message; }
	    public String getDeveloperMessage() { return this.developerMessage; }
	  }

	  public Response toResponse(Throwable ex) {

	    ErrorMessage errorMessage = new ErrorMessage(500, "An internal error has occurred|Perhaps here the REQUESTID or some reference that could help you to track the problem...");
	 
	    logger.error(errorMessage);
	    return Response.serverError().entity(errorMessage).build();

	  }

	}
