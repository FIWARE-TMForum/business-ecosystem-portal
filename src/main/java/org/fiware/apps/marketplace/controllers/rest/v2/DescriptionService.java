package org.fiware.apps.marketplace.controllers.rest.v2;

/*
 * #%L
 * FiwareMarketplace
 * %%
 * Copyright (C) 2012 SAP
 * Copyright (C) 2014-2015 CoNWeT Lab, Universidad Politécnica de Madrid
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

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.fiware.apps.marketplace.bo.DescriptionBo;
import org.fiware.apps.marketplace.exceptions.DescriptionNotFoundException;
import org.fiware.apps.marketplace.exceptions.NotAuthorizedException;
import org.fiware.apps.marketplace.exceptions.StoreNotFoundException;
import org.fiware.apps.marketplace.exceptions.ValidationException;
import org.fiware.apps.marketplace.model.Descriptions;
import org.fiware.apps.marketplace.model.Description;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.slf4j.LoggerFactory;

@Component
@Path("/api/v2/store/{storeName}/description/")	
public class DescriptionService {

	// OBJECT ATTRIBUTES //
	@Autowired private DescriptionBo descriptionBo;

	// CLASS ATTRIBUTES //
	private static final ErrorUtils ERROR_UTILS = new ErrorUtils(
			LoggerFactory.getLogger(DescriptionService.class), 
			"There is already a Description in this Store with that name");

	@POST
	@Consumes({"application/xml", "application/json"})
	@Path("/")	
	public Response createDescription(@Context UriInfo uri, 
			@PathParam("storeName") String storeName, 
			Description description) {	
		Response response;

		try {
			// Save the offerings description
			descriptionBo.save(storeName, description);
			
			// Get the URL and return created
			URI newURI = UriBuilder
					.fromUri(uri.getPath())
					.path(description.getName())
					.build();
			
			response = Response.created(newURI).build();
		} catch (NotAuthorizedException ex) {
			response = ERROR_UTILS.notAuthorizedResponse(ex);
		} catch (ValidationException ex) {
			response = ERROR_UTILS.validationErrorResponse(ex);
		} catch (StoreNotFoundException ex) {
			//The Store is an URL... If the Store does not exist a 404
			//should be returned instead of a 400
			response = ERROR_UTILS.entityNotFoundResponse(ex);
		} catch (DataIntegrityViolationException ex) {
			response = ERROR_UTILS.badRequestResponse(ex);
		} catch (HibernateException ex) {
			response = ERROR_UTILS.badRequestResponse(ex);
		} catch (Exception ex) {
			response = ERROR_UTILS.internalServerError(ex);
		}

		return response;
	}


	@POST
	@Consumes({"application/xml", "application/json"})
	@Path("/{descriptionName}")
	public Response updateDescription(@PathParam("storeName") String storeName, 
			@PathParam("descriptionName") String descriptionName, 
			Description descriptionInfo) {

		Response response;

		try {
			descriptionBo.update(storeName, descriptionName, descriptionInfo);
			response = Response.status(Status.OK).build();	
		} catch (NotAuthorizedException ex) {
			response = ERROR_UTILS.notAuthorizedResponse(ex);
		} catch (ValidationException ex) {
			response = ERROR_UTILS.validationErrorResponse(ex);
		} catch (DescriptionNotFoundException ex) {
			response = ERROR_UTILS.entityNotFoundResponse(ex);
		} catch (StoreNotFoundException ex) {
			response = ERROR_UTILS.entityNotFoundResponse(ex);
		} catch (DataIntegrityViolationException ex) {
			response = ERROR_UTILS.badRequestResponse(ex);
		} catch (HibernateException ex) {
			response = ERROR_UTILS.badRequestResponse(ex);
		} catch (Exception ex) {
			response = ERROR_UTILS.internalServerError(ex);
		}

		return response;
	}

	@DELETE
	@Path("/{descriptionName}")	
	public Response deleteDescription(@PathParam("storeName") String storeName, 
			@PathParam("descriptionName") String descriptionName) {
		Response response;

		try {
			descriptionBo.delete(storeName, descriptionName);
			response = Response.status(Status.NO_CONTENT).build();
		} catch (NotAuthorizedException ex) {
			response = ERROR_UTILS.notAuthorizedResponse(ex);
		} catch (DescriptionNotFoundException ex) {
			response = ERROR_UTILS.entityNotFoundResponse(ex);
		} catch (StoreNotFoundException ex) {
			response = ERROR_UTILS.entityNotFoundResponse(ex);
		} catch (Exception ex) {
			response = ERROR_UTILS.internalServerError(ex);
		}

		return response;
	}

	@GET
	@Produces({"application/xml", "application/json"})
	@Path("/{descriptionName}")	
	public Response getDescription(@PathParam("storeName") String storeName, 
			@PathParam("descriptionName") String descriptionName) {	
		Response response;

		try {
			Description description = descriptionBo.
					findByNameAndStore(storeName, descriptionName);

			response = Response.status(Status.OK).entity(description).build();
		} catch (NotAuthorizedException ex) {
			response = ERROR_UTILS.notAuthorizedResponse(ex);
		} catch (DescriptionNotFoundException ex) {
			response = ERROR_UTILS.entityNotFoundResponse(ex);
		} catch (StoreNotFoundException ex) {
			response = ERROR_UTILS.entityNotFoundResponse(ex);
		} catch (Exception ex) {
			response = ERROR_UTILS.internalServerError(ex);
		}

		return response;
	}

	@GET
	@Produces({"application/xml", "application/json"})
	@Path("/")	
	public Response listDescriptionsInStore(@PathParam("storeName") String storeName, 
			@DefaultValue("0") @QueryParam("offset") int offset,
			@DefaultValue("100") @QueryParam("max") int max) {
		Response response;

		if (offset < 0 || max <= 0) {
			// Offset and Max should be checked
			response = ERROR_UTILS.badRequestResponse("offset and/or max are not valid");
		} else {
			try {
				Descriptions returnedDescriptions = new Descriptions(descriptionBo
						.getStoreDescriptionsPage(storeName, offset, max));
				
				response = Response.status(Status.OK).entity(returnedDescriptions).build();
					
			} catch (NotAuthorizedException ex) {
				response = ERROR_UTILS.notAuthorizedResponse(ex);
			} catch (StoreNotFoundException ex) {
				response = ERROR_UTILS.entityNotFoundResponse(ex);
			} catch (Exception ex) {
				response = ERROR_UTILS.internalServerError(ex);
			}
		}

		return response;
	}
}
