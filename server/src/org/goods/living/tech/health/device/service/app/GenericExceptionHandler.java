package org.goods.living.tech.health.device.service.app;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Provider
public class GenericExceptionHandler implements ExceptionMapper<Exception> {// ClientErrorException

	Logger logger = LogManager.getLogger();

	class ErrorMessage {
		int status;
		String message;
		String developerMessage;

		public ErrorMessage(int status, String message) {
			this.status = status;
			String[] messages = message.split("\\|");
			this.message = messages[0];
			if (messages.length > 1) {
				this.developerMessage = messages[1];
			}
		}

		public int getStatus() {
			return this.status;
		}

		public String getMessage() {
			return this.message;
		}

		public String getDeveloperMessage() {
			return this.developerMessage;
		}
	}

	public Response toResponse(Exception ex) {

		ErrorMessage errorMessage = new ErrorMessage(Status.INTERNAL_SERVER_ERROR.getStatusCode(),
				"An internal error has occurred|Perhaps here the REQUESTID or some reference that could help you to track the problem...");

		logger.error(errorMessage, ex);
		return Response.status(Status.INTERNAL_SERVER_ERROR).entity(errorMessage).type(MediaType.APPLICATION_JSON)
				.build();
		// Response.serverError().entity(errorMessage).build();

	}

}
