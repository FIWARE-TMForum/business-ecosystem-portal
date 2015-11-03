package org.fiware.apps.bep.controllers.rest.v2;

/*
 * #%L
 * FiwareMarketplace
 * %%
 * Copyright (C) 2014 CoNWeT Lab, Universidad Politécnica de Madrid
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of copyright holders nor the names of its contributors
 *    may be used to endorse or promote products derived from this software 
 *    without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.fiware.apps.bep.exceptions.NotAuthorizedException;
import org.fiware.apps.bep.exceptions.ValidationException;
import org.fiware.apps.bep.model.ErrorType;
import org.fiware.apps.bep.model.APIError;
import org.hibernate.HibernateException;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;


public abstract class AbstractController {
	
	private final String contraintViolationMessage;
	private Logger logger;
	
	public AbstractController(Logger logger) {
		this.logger = logger;
		this.contraintViolationMessage = null;
	}
	
	public AbstractController(Logger logger, String contraintViolationMessage) {
		this.logger = logger;
		this.contraintViolationMessage = contraintViolationMessage;
	}
	
	public Response badRequestResponse(DataIntegrityViolationException ex) {
		Response response;
		if (ex.getCause() instanceof HibernateException) {
			response = badRequestResponse((HibernateException) ex.getCause());
		} else {
			response = internalServerError(ex);
		}
		
		return response;
	}
	
	public Response badRequestResponse(HibernateException ex) {
		String message;
		if (ex instanceof ConstraintViolationException 
				&& this.contraintViolationMessage != null)  {
			logger.warn("Constraint Violation", ex);
			message = this.contraintViolationMessage;
		} else {
			logger.error("Data Access Error", ex);
			message = ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage();
		}
		
		return Response.status(Status.BAD_REQUEST).entity(
				new APIError(ErrorType.BAD_REQUEST, message)).build();
	}
	
	public Response badRequestResponse(String message) {
		logger.warn("Bad Request {}", message);
		return Response.status(Status.BAD_REQUEST).entity(
				new APIError(ErrorType.BAD_REQUEST, message)).build();
	}
	
	public Response validationErrorResponse(ValidationException ex) {
		logger.warn("Validation Exception {} {}", ex.getFieldName(), ex.getFieldError());
		return Response.status(Status.BAD_REQUEST).entity(
				new APIError(ErrorType.VALIDATION_ERROR, ex.getFieldName(), ex.getFieldError())).build();

	}
	
	public Response entityNotFoundResponse(Exception ex) {
		logger.warn("Not Found {}", ex.getMessage());
		return Response.status(Status.NOT_FOUND).entity(
				new APIError(ErrorType.NOT_FOUND, ex.getMessage())).build();
	}
	
	public Response notAuthorizedResponse(NotAuthorizedException exception) {
		String message = exception.getMessage();
		logger.warn(message);
		return Response.status(Status.FORBIDDEN).entity(
				new APIError(ErrorType.FORBIDDEN, message)).build();

	}
	
	public Response internalServerError(Exception exception) {
		logger.error("Unexpected exception", exception);
		if (exception.getCause() != null) {
			return internalServerError(exception.getCause().getMessage());
		} else {
			return internalServerError(exception.getMessage());
		}
	}
	
	public Response internalServerError(String cause) {
		logger.error("Unexpected exception {}", cause);
		return Response.status(Status.INTERNAL_SERVER_ERROR).entity(
				new APIError(ErrorType.INTERNAL_SERVER_ERROR, cause)).build();
	}
	
	public Response serviceUnavailableResponse(String cause) {
		logger.warn("Service unavailable {}", cause);
		return Response.status(Status.SERVICE_UNAVAILABLE).entity(
				new APIError(ErrorType.SERVICE_UNAVAILABLE, cause)).build();
	}

}
