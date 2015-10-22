package org.fiware.apps.marketplace.it;

/*
 * #%L
 * FiwareMarketplace
 * %%
 * Copyright (C) 2015 CoNWeT Lab, Universidad Politécnica de Madrid
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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;	

import org.fiware.apps.marketplace.model.Categories;
import org.fiware.apps.marketplace.model.Category;
import org.fiware.apps.marketplace.model.Description;
import org.fiware.apps.marketplace.model.Descriptions;
import org.fiware.apps.marketplace.model.ErrorType;
import org.fiware.apps.marketplace.model.Offering;
import org.fiware.apps.marketplace.model.Offerings;
import org.fiware.apps.marketplace.model.Review;
import org.fiware.apps.marketplace.model.Reviews;
import org.fiware.apps.marketplace.model.Service;
import org.fiware.apps.marketplace.model.Store;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class DescriptionServiceIT extends AbstractIT {
	
	private final static String USER_NAME = "marketplace";
	private final static String PASSWORD = "password1!a";
	private final static String EMAIL = "example@example.com";
	private final static String FIRST_STORE_NAME = "wstore";
	private final static String FIRST_STORE_URL = "http://store.lab.fiware.org";
	private final static String SECOND_STORE_NAME = "wstore-testbed";
	private final static String SECOND_STORE_URL = "http://store.testbed.fiware.org";
	
	private final static String MESSAGE_NAME_IN_USE = "This name is already in use in this Store.";
	private final static String MESSAGE_URL_IN_USE = "This URL is already in use in this Store.";
	private final static String MESSAGE_INVALID_RDF = "Your RDF could not be parsed";
	private final static String MESSAGE_DESCRIPTION_NOT_FOUND = "Description %s not found in Store %s";

	@Before
	public void setUp() {
		createUser(USER_NAME, EMAIL, PASSWORD);
		createStore(USER_NAME, PASSWORD, FIRST_STORE_NAME, FIRST_STORE_URL);

		startMockServer();
	}

	@After
	public void stopMockServer() {
		wireMock.stop();
	}

	///////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////// AUXILIAR //////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////

	private Response getDescription(String userName, String password, String storeName, String descriptionName) {
		Client client = ClientBuilder.newClient();
		return client.target(endPoint + "/api/v2/store/" + storeName + "/description/" + descriptionName)
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(userName, password)).get();
	}
	
	/**
	 * This method retrieves an offering from the list of obtained offerings and check if its
	 * price plans, services and categories are OK
	 * @param offerings The list of offerings that the service has returned
	 * @param storeName The name of the store where the offering is registered
	 * @param descriptionName The of the the description that contains the offering
	 * @param offering The offering to be checked. This instance contains the expected values.
	 */
	private void checkOfferingInternalState(List<Offering> offerings, String storeName, String descriptionName, 
			Offering offering) {
		
		// Required for indexOf to work
		Store store = new Store();
		store.setName(storeName);
		
		Description description = new Description();
		description.setName(descriptionName);
		description.setStore(store);
		
		offering.setDescribedIn(description);
				
		// Get offering from the list
		int index = offerings.indexOf(offering);
		Offering receivedOffering = offerings.get(index);
		assertThat(receivedOffering.getDisplayName()).isEqualTo(offering.getDisplayName());
		assertThat(receivedOffering.getDescription()).isEqualTo(offering.getDescription());
		assertThat(receivedOffering.getImageUrl()).isEqualTo(offering.getImageUrl());
		assertThat(receivedOffering.getAcquisitionUrl()).isEqualTo(offering.getAcquisitionUrl());
		assertThat(receivedOffering.getPricePlans()).isEqualTo(offering.getPricePlans());
		assertThat(offerings.get(index).getCategories()).isEqualTo(offering.getCategories());

		// Services should be manually compared, since the URI is not sent to the final user and is the only
		// field used in equals... 
		assertThat(receivedOffering.getServices().size()).isEqualTo(offering.getServices().size());
		for (Service service: offering.getServices()) {
			
			boolean found = false;
			
			Iterator<Service> servicesIterator = receivedOffering.getServices().iterator();
			
			while(servicesIterator.hasNext() && !found) {
				Service receivedService = servicesIterator.next();
			
				if (service.getDisplayName().equals(receivedService.getDisplayName()) && 
						service.getComment().equals(receivedService.getComment())) {
					found = true;
				}
			}
			
			// When the loop ends, the service should have been found
			assertThat(found).isTrue();
		}
		
	}
	
	private void checkOfferingInList(String storeName, String descriptionName, 
			String offeringUri, List<Offering> offeringsList) {
		
		// We must set the description into the offering in order to make "isIn" work appropriately
		// "isIn" is based on "equals" and it depends on the URL of the offering and its description
		Offering offering = new Offering();
		offering.setUri(offeringUri);
		
		// A description is managed by one store
		Store store = new Store();
		store.setName(storeName);
		
		// Description.equals depends on the name and its store
		Description description = new Description();
		description.setName(descriptionName);
		description.setStore(store);
		
		offering.setDescribedIn(description);
		
		// Check that the offering is contained in the list of offerings returned by WMarket
		assertThat(offering).isIn(offeringsList);
		
	}
	
	private void checkDescription(String userName, String password, String storeName, 
			String descriptionName, String displayName, String url, String comment) {
		
		Description retrievedDescription = getDescription(userName, password, storeName, descriptionName)
				.readEntity(Description.class);
		
		assertThat(retrievedDescription.getName()).isEqualTo(descriptionName);
		assertThat(retrievedDescription.getDisplayName()).isEqualTo(displayName);
		assertThat(retrievedDescription.getUrl()).isEqualTo(url);
		assertThat(retrievedDescription.getComment()).isEqualTo(comment);
		
		// Check offerings
		List<Offering> expectedOfferings = DESCRIPTIONS_OFFERINGS.get(url);
		List<Offering> descriptionOfferings = retrievedDescription.getOfferings();
		assertThat(descriptionOfferings.size()).isEqualTo(expectedOfferings.size());
		
		for (Offering expectedOffering: expectedOfferings) {
			
			checkOfferingInList(storeName, descriptionName, expectedOffering.getUri(), descriptionOfferings);
			
			// Check internal state (services, categories, price plans,...) 
			checkOfferingInternalState(descriptionOfferings, storeName, descriptionName, expectedOffering);
		}
	}
	
	private Response createOrUpdateDescription(String userName, String password, String storeName, 
			String descriptionName, String displayName, String url, String comment) {
		
		Description description = new Description();
		description.setDisplayName(displayName);
		description.setUrl(url);
		description.setComment(comment);
		
		Client client = ClientBuilder.newClient();
		Response response = client.target(endPoint + "/api/v2/store/" + storeName + "/description/" + descriptionName)
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(userName, password))
				.post(Entity.entity(description, MediaType.APPLICATION_JSON));
		
		return response;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////// CREATE ///////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////

	private Response createDescription(String userName, String password, String storeName, String displayName,
			String url, String comment) {
		return createOrUpdateDescription(userName, password, storeName, "", displayName, url, comment);
	}
	
	private void testCreation(String url) {
		String displayName = "Description 1";
		String descriptionName = "description-1";
		String descriptionComment = "Example Comment";
		
		Response response = createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, displayName, url, 
				descriptionComment);
		assertThat(response.getStatus()).isEqualTo(201);
		assertThat(response.getHeaderString("Location")).isEqualTo(endPoint + "/api/v2/store/" + FIRST_STORE_NAME +
				"/description/" + descriptionName);
		
		// Check that the description actually exists
		checkDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, descriptionName, displayName, url, 
				descriptionComment);

	}
	
	@Test
	public void testCreationDefaultUSDL() {
		testCreation(defaultUSDLPath);
	}
	
	@Test
	public void testCreationSecondaryUSDL() {
		testCreation(secondaryUSDLPath);
	}
	
	private void testCreationInvalidField(String displayName, String url, String comment, String invalidField,
			String message) {

		Response response = createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, displayName, url, comment);
		checkAPIError(response, 400, invalidField, message, ErrorType.VALIDATION_ERROR);
	}
	
	@Test
	public void testCreationDisplayNameInvalid() {
		testCreationInvalidField("Description!", defaultUSDLPath, "", "displayName", 
				MESSAGE_INVALID_DISPLAY_NAME);
	}
	
	@Test
	public void testCreationDisplayNameTooShort() {
		testCreationInvalidField("a", defaultUSDLPath, "", "displayName", 
				String.format(MESSAGE_TOO_SHORT, 3));
	}
	
	@Test
	public void testCreationDisplayNameTooLong() {
		testCreationInvalidField("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz"
				+ "abcdefghijklmnopqrstuvw", defaultUSDLPath, "", "displayName", 
				String.format(MESSAGE_TOO_LONG, 100));
	}
	
	@Test
	public void testCreationURLInvalid() {
		testCreationInvalidField("Description", "https:/127.0.0.1:" + wireMock.port(), "", "url", 
				MESSAGE_INVALID_URL);
	}
	
	@Test
	public void testCreationRDFInvalid() {
		testCreationInvalidField("Description", serverUrl, "", "url", MESSAGE_INVALID_RDF);

	}
	
	@Test
	public void testCreationCommentTooLong() {
		testCreationInvalidField("Description", defaultUSDLPath, "12345678901234567890123456789012345678901234567890"
				+ "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456"
				+ "7890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", 
				"comment", String.format(MESSAGE_TOO_LONG, 200));
	}

	private void testCreationFieldAlreadyExists(String displayName1, String displayName2, String url1, String url2,
			String field, String expectedMessage) {

		createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, displayName1, url1, "");
		Response response = createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, displayName2, url2, "");
		
		checkAPIError(response, 400, field, expectedMessage, ErrorType.VALIDATION_ERROR);

	}
	
	@Test
	public void testCreationDisplayNameAlreadyExists() {		
		String displayName = "Description 1";
		
		// name is based on display name and name is checked before display name...
		testCreationFieldAlreadyExists(displayName, displayName, defaultUSDLPath, defaultUSDLPath + "a", "displayName",
				MESSAGE_NAME_IN_USE);
	}
	
	@Test
	public void testCreationURLAlreadyExists() {
		testCreationFieldAlreadyExists("offering-1", "offering-2", defaultUSDLPath, defaultUSDLPath, "url",
				MESSAGE_URL_IN_USE);
	}
	
	@Test
	public void testCreationNameAndUrlAlreadyExistsInAnotherStore() {

		// Create another Store
		String descriptionName = "description-1"; 
		
		createStore(USER_NAME, PASSWORD, SECOND_STORE_NAME, SECOND_STORE_URL);
		
		Response createResponse1 = createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, 
				descriptionName, defaultUSDLPath, "");
		Response createResponse2 = createDescription(USER_NAME, PASSWORD, SECOND_STORE_NAME, 
				descriptionName, defaultUSDLPath, "");
		
		// Both offerings can be created
		assertThat(createResponse1.getStatus()).isEqualTo(201);
		assertThat(createResponse2.getStatus()).isEqualTo(201);
		
	}
	
	@Test
	public void testDeleteUserWithDescription() {
		String name = "description-1";
		
		Response createStoreResponse = createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, name, 
				defaultUSDLPath, "");
		assertThat(createStoreResponse.getStatus()).isEqualTo(201);
		checkDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, name, name, defaultUSDLPath, "");
		
		// Delete user
		Response deleteUserResponse = deleteUser(USER_NAME, PASSWORD, USER_NAME);
		assertThat(deleteUserResponse.getStatus()).isEqualTo(204);
		
		// Create another user to be able to check the store
		String newUserName = USER_NAME + "a";
		String email = "new_email__@example.com";
		Response createUserResponse = createUser(newUserName, email, PASSWORD);
		assertThat(createUserResponse.getStatus()).isEqualTo(201);
		
		// Check that the Store does not exist anymore
		Response getStoreResponse = getDescription(newUserName, PASSWORD, FIRST_STORE_NAME, name);
		checkAPIError(getStoreResponse, 404, null, String.format(MESSAGE_STORE_NOT_FOUND, FIRST_STORE_NAME), 
				ErrorType.NOT_FOUND);
		
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////// UPDATE ///////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	
	private Response updateDescription(String userName, String password, String storeName, 
			String descriptionName, String displayName, String url, String comment) {
		return createOrUpdateDescription(userName, password, storeName, descriptionName, displayName, url, comment);
	}
	
	private void testUpdate(String newDisplayName, String newUrl, String newComment) {
		// Create Description
		String name = "description-1";
		String displayName = "Description-1";
		String comment = "commnet1";
		
		Response createDescriptionResponse = createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, displayName, 
				defaultUSDLPath, comment);
		assertThat(createDescriptionResponse.getStatus()).isEqualTo(201);
		
		// Update the description		
		Response updateDescriptionResponse = updateDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, name, 
				newDisplayName, newUrl, newComment);
		assertThat(updateDescriptionResponse.getStatus()).isEqualTo(200);
		
		// Check that the description has been updated
		String expectedDisplayName = newDisplayName == null ? displayName : newDisplayName;
		String expectedUrl = newUrl == null ? defaultUSDLPath : newUrl;
		String expectedComment = newComment == null ? comment : newComment;
		
		checkDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, name, expectedDisplayName, 
				expectedUrl, expectedComment);

	}
	
	@Test
	public void testUpdateNameAndDescription() {
		testUpdate("Description 2", null, "comment-2");
	}
	
	@Test
	public void tesUpdateUrlSameUrl() {
		testUpdate(null, defaultUSDLPath, null);
	}
	
	@Test
	public void testUpdateUrlDifferentUrl() {
		testUpdate(null, secondaryUSDLPath, null);
	}
	
	private void testUpdateInvalidField(String newDisplayName, String newUrl, String newComment, 
			String invalidField, String message) {
		
		String name = "offering";
		String displayName = "Offering";
		String comment = "";
		
		Response createResponse = createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, displayName, 
				defaultUSDLPath, comment);
		assertThat(createResponse.getStatus()).isEqualTo(201);

		Response updateResponse = updateDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, name, newDisplayName, 
				newUrl, newComment);
		checkAPIError(updateResponse, 400, invalidField, message, ErrorType.VALIDATION_ERROR);
	}
	
	@Test
	public void testUpdateDisplayNameInvalid() {
		testUpdateInvalidField("Description!", defaultUSDLPath, "", "displayName", 
				MESSAGE_INVALID_DISPLAY_NAME);
	}
	
	@Test
	public void testUpdateDisplayNameTooShort() {
		testUpdateInvalidField("a", defaultUSDLPath, "", "displayName", 
				String.format(MESSAGE_TOO_SHORT, 3));
	}
	
	@Test
	public void testUpdateDisplayNameTooLong() {
		testUpdateInvalidField("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz"
				+ "abcdefghijklmnopqrstuvw", defaultUSDLPath, "", "displayName", 
				String.format(MESSAGE_TOO_LONG, 100));
	}
	
	@Test
	public void testUpdateURLInvalid() {
		testUpdateInvalidField("Description", "https:/store.lab.fiware.org/offering1.rdf", "", "url", 
				MESSAGE_INVALID_URL);
	}
	
	@Test
	public void testUpdateRDFInvalid() {
		testUpdateInvalidField("Description", serverUrl, "", "url", MESSAGE_INVALID_RDF);
	}
	
	@Test
	public void testUpdateCommentTooLong() {
		testUpdateInvalidField("Offering", serverUrl, "12345678901234567890123456789012345678901234567890"
				+ "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456"
				+ "7890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", 
				"comment", String.format(MESSAGE_TOO_LONG, 200));
	}
	
	/**
	 * This methods creates two descriptions, based on the different parameters and tries to updates the second
	 * description based on updatedDisplayName and updatedURL. However, it's expected that one of these parameters
	 * has been used to create the first description, so an error should arise. Error details should be contained in
	 * field an expectedMessage.
	 * @param nameDescription1 The name of the first description
	 * @param urlDescription1 The URL of the first description
	 * @param nameDescription2 The name of the second description (the one to be updated). This name is used to modify 
	 * the description so this is not a displayName but the name (the one without spaces, ...) 
	 * @param urlDescription2 The URL of the second description (the one to be updated)
	 * @param updatedDisplayName The new display name to be set in the second description
	 * @param updatedURL The new URL to be set in the second description
	 * @param field The field that is repeated
	 * @param expectedMessage Expected error message
	 */
	private void testUpdateFieldAlreayExists(
			String nameDescription1, String urlDescription1, 
			String nameDescription2, String urlDescription2,
			String updatedDisplayName, String updatedURL,
			String field, String expectedMessage) {
		
		Response createStore1Response = createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, nameDescription1, 
				urlDescription1, "");
		Response createStore2Response = createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, nameDescription2, 
				urlDescription2, "");
		assertThat(createStore1Response.getStatus()).isEqualTo(201);
		assertThat(createStore2Response.getStatus()).isEqualTo(201);
		
		Response updateResponse = updateDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, nameDescription2, 
				updatedDisplayName, updatedURL, null);
		checkAPIError(updateResponse, 400, field, expectedMessage, ErrorType.VALIDATION_ERROR);

	}
	
	@Test
	public void testUpdateDisplayNameAlreadyExists() {		
		String displayName = "description";
		
		testUpdateFieldAlreayExists(
				displayName, defaultUSDLPath, 
				"descritpion-2", secondaryUSDLPath, 
				displayName, null, 
				"displayName", MESSAGE_NAME_IN_USE);	
	}
	
	@Test
	public void testUpdateURLAlreadyExists() {
		testUpdateFieldAlreayExists(
				"description", defaultUSDLPath, 
				"description-2", secondaryUSDLPath, 
				"description-2", defaultUSDLPath, 
				"url", MESSAGE_URL_IN_USE);	
	}
	
	@Test
	public void testUpdateNonExisting() {
		
		String displayName = "offering-1";
		Response createDescriptionResponse = createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, displayName, 
				defaultUSDLPath, "");
		assertThat(createDescriptionResponse.getStatus()).isEqualTo(201);
		
		// Update non-existing description
		String descriptionToBeUpdated = displayName + "a";  	//This ID is supposed not to exist
		Response updateDescriptionResponse = updateDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, 
				descriptionToBeUpdated, "new display", null, null);
		
		checkAPIError(updateDescriptionResponse, 404, null, 
				String.format(MESSAGE_DESCRIPTION_NOT_FOUND, descriptionToBeUpdated, 
						FIRST_STORE_NAME), ErrorType.NOT_FOUND);	
	}
	
	@Test
	public void testUpdateWithAnotherUser() {
		
		String displayName = "offering-1";
		Response createDescriptionResponse = createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, displayName, 
				defaultUSDLPath, "");
		assertThat(createDescriptionResponse.getStatus()).isEqualTo(201);

		// Create another user
		String newUserName = USER_NAME + "a";
		String email = "new_email__@example.com";
		createUser(newUserName, email, PASSWORD);
		
		// Update description with the new user
		Response updateDescriptionResponse = updateDescription(newUserName, PASSWORD, FIRST_STORE_NAME, displayName, 
				"new display name", null, null);
		checkAPIError(updateDescriptionResponse, 403, null, 
				String.format(MESSAGE_NOT_AUTHORIZED, "update description"), ErrorType.FORBIDDEN);	

	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////// DELETE ///////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////

	private Response deleteDescription(String authUserName, String authPassword, String storeName, 
			String descriptionName) {
		
		Client client = ClientBuilder.newClient();
		Response response = client.target(endPoint + "/api/v2/store/" + storeName + "/description/" + descriptionName)
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(authUserName, authPassword))
				.delete();
		
		return response;

	}
	
	@Test
	public void testDelete() {
		
		String name = "description-1";
		
		// Create the description
		Response createDescriptionResponse = createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, name, 
				defaultUSDLPath, null);
		assertThat(createDescriptionResponse.getStatus()).isEqualTo(201);
		
		// Delete the description
		Response deleteDescriptionResponse = deleteDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, name);
		assertThat(deleteDescriptionResponse.getStatus()).isEqualTo(204);
	}
	
	@Test
	public void testDeleteNonExisting() {
		
		String name = "description-1";
		Response createDescriptionResponse = createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, name, 
				defaultUSDLPath, null);
		assertThat(createDescriptionResponse.getStatus()).isEqualTo(201);
		
		// Delete non-existing description
		String descriptionToBeDeleted = name + "a";  	//This ID is supposed not to exist
		Response deleteStoreResponse = deleteDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, 
				descriptionToBeDeleted);
		checkAPIError(deleteStoreResponse, 404, null, 
				String.format(MESSAGE_DESCRIPTION_NOT_FOUND, descriptionToBeDeleted, 
						FIRST_STORE_NAME), ErrorType.NOT_FOUND);	
	}
	
	@Test
	public void testDeleteWithAnotherUser() {
		
		String name = "description-1";
		Response createDescriptionResponse = createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, name, 
				defaultUSDLPath, null);
		assertThat(createDescriptionResponse.getStatus()).isEqualTo(201);

		// Create another user
		String newUserName = USER_NAME + "a";
		String email = "new_email__@example.com";
		createUser(newUserName, email, PASSWORD);
		
		//Delete user
		Response deleteStoreResponse = deleteDescription(newUserName, PASSWORD, FIRST_STORE_NAME, name);
		checkAPIError(deleteStoreResponse, 403, null, String.format(MESSAGE_NOT_AUTHORIZED, "delete description"), 
				ErrorType.FORBIDDEN);	

	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// LIST DESCRIPTIONS //////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	
	@Test
	public void testListAllDescriptionsInStore() {
		
		int DESCRIPTIONS_CREATED = 6;
		
		// Create some descriptions
		String displayNamePattern = "Store %d";
		String urlPattern = serverUrl + "/default%d.rdf";
		
		for (int i = 0; i < DESCRIPTIONS_CREATED; i++) {
			createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, String.format(displayNamePattern, i), 
					String.format(urlPattern, i), "");
		}
		
		// Get all descriptions
		Client client = ClientBuilder.newClient();
		Response response = client.target(endPoint + "/api/v2/store/" + FIRST_STORE_NAME + "/description")
				.request(MediaType.APPLICATION_JSON).header("Authorization", getAuthorization(USER_NAME, PASSWORD))
				.get();
		
		// Check the response
		assertThat(response.getStatus()).isEqualTo(200);
		Descriptions descriptions = response.readEntity(Descriptions.class);
		assertThat(descriptions.getDescriptions().size()).isEqualTo(DESCRIPTIONS_CREATED);
		
		// Users are supposed to be returned in order
		for (int i = 0; i < DESCRIPTIONS_CREATED; i++) {
			Description description = descriptions.getDescriptions().get(i);
			assertThat(description.getDisplayName()).isEqualTo(String.format(displayNamePattern, i));
			assertThat(description.getUrl()).isEqualTo(String.format(urlPattern, i));
		}
	}
	
	private void testListSomeDescriptionsInStore(int offset, int max) {
		
		int DESCRIPTIONS_CREATED = 10;
		
		// Create some descriptions
		String displayNamePattern = "Store %d";
		String urlPattern = serverUrl + "/default%d.rdf";
		
		for (int i = 0; i < DESCRIPTIONS_CREATED; i++) {
			createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, String.format(displayNamePattern, i), 
					String.format(urlPattern, i), "");
		}
		
		// Get required descriptions
		Client client = ClientBuilder.newClient();
		Response response = client.target(endPoint + "/api/v2/store/" + FIRST_STORE_NAME + "/description")
				.queryParam("offset", offset)
				.queryParam("max", max)
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(USER_NAME, PASSWORD))
				.get();
		
		// Check the response
		int expectedElements = (offset + max) > DESCRIPTIONS_CREATED ? DESCRIPTIONS_CREATED - offset : max;
		assertThat(response.getStatus()).isEqualTo(200);
		Descriptions descriptions = response.readEntity(Descriptions.class);
		assertThat(descriptions.getDescriptions().size()).isEqualTo(expectedElements);
		
		// Users are supposed to be returned in order
		for (int i = offset; i < offset + expectedElements; i++) {
			Description description = descriptions.getDescriptions().get(i - offset);
			assertThat(description.getDisplayName()).isEqualTo(String.format(displayNamePattern, i));
			assertThat(description.getUrl()).isEqualTo(String.format(urlPattern, i));
		}
	}
	
	@Test
	public void testListSomeDescriptionsMaxInRange() {
		testListSomeDescriptionsInStore(3, 4);
	}
	
	@Test
	public void testListSomeDescriptionsMaxNotInRange() {
		testListSomeDescriptionsInStore(5, 7);
	}
	
	private void testListDescriptionsInStoreInvalidParams(int offset, int max) {
		
		Client client = ClientBuilder.newClient();
		Response response = client.target(endPoint + "/api/v2/store/" + FIRST_STORE_NAME + "/description")
				.queryParam("offset", offset)
				.queryParam("max", max)
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(USER_NAME, PASSWORD))
				.get();
		
		checkAPIError(response, 400, null, MESSAGE_INVALID_OFFSET_MAX, ErrorType.BAD_REQUEST);

	}
	
	@Test
	public void testListDescriptionsInStoreInvalidOffset() {
		testListDescriptionsInStoreInvalidParams(-1, 2);
	}
	
	@Test
	public void testListDescriptionsInStoreInvalidMax() {
		testListDescriptionsInStoreInvalidParams(1, 0);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// ALL DESCRIPTIONS  //////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	
	
	private void testListDescriptions(int offset, int max) {
		
		// Create an additional Store
		Response createStoreResponse = createStore(USER_NAME, PASSWORD, SECOND_STORE_NAME, SECOND_STORE_URL);
		assertThat(createStoreResponse.getStatus()).isEqualTo(201);
		
		// Create descriptions (2)
		Description[] originalDescriptions = new Description[2];
		
		Description description0 = new Description();
		description0.setName("default");
		description0.setUrl(defaultUSDLPath);

		Description description1 = new Description();
		description1.setName("secondary");
		description1.setUrl(secondaryUSDLPath);
		
		originalDescriptions[0] = description0;
		originalDescriptions[1] = description1;
		
		// Insert descriptions (2) into the Stores (2). Total: 4 descriptions
		String[] stores = new String[]{FIRST_STORE_NAME, SECOND_STORE_NAME};
		
		for (String store: stores) {
			
			for (Description description: originalDescriptions) {
				Response createDescriptionResponse = createDescription(USER_NAME, PASSWORD, store, 
						description.getName(), description.getUrl(), "");
				assertThat(createDescriptionResponse.getStatus()).isEqualTo(201);

			}
		}
				
		// Get all descriptions
		Client client = ClientBuilder.newClient();
		Response response = client.target(endPoint + "/api/v2/description")
				.queryParam("offset", offset)
				.queryParam("max", max)
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(USER_NAME, PASSWORD))
				.get();
		
		// Check that the right number of descriptions has been returned...
		Descriptions retrievedDescriptions = response.readEntity(Descriptions.class);
		int descriptionsCreated = stores.length * originalDescriptions.length;	// 4 descriptions
		int expectedElements = offset + max > descriptionsCreated ? descriptionsCreated - offset : max;
		assertThat(retrievedDescriptions.getDescriptions().size()).isEqualTo(expectedElements);
		
		// Check descriptions
		for (int i = 0; i < retrievedDescriptions.getDescriptions().size(); i++) {
			
			Description description = retrievedDescriptions.getDescriptions().get(i);
			int indexInGeneralArray = offset + i;
			
			// Check that store name is correct
			// Store 0: [DESC0, DESC1], Store 1: [DESC2, DESC3], ...
			int storeIndex = indexInGeneralArray / originalDescriptions.length;
			assertThat(description.getStore().getName()).isEqualTo(stores[storeIndex]);
			
			// Check that the description is correct
			// Even -> Description 1 (description1Name, defaultUSDLPath)
			// Odd -> Description 2 (description2Name, secondaryUSDLPath)
			int descriptionIndex = indexInGeneralArray % originalDescriptions.length;
			assertThat(description.getName()).isEqualTo(originalDescriptions[descriptionIndex].getName());
			assertThat(description.getUrl()).isEqualTo(originalDescriptions[descriptionIndex].getUrl());			
		}
		
	}
	
	@Test
	public void testListAllDescriptions() {
		testListDescriptions(0,	100);
	}
	
	@Test
	public void testListSomeDescriptions() {
		testListDescriptions(1, 3);
	}
	
	private void testListDescriptionsInvalidParams(int offset, int max) {
		Client client = ClientBuilder.newClient();
		Response response = client.target(endPoint + "/api/v2/description")
				.queryParam("offset", offset)
				.queryParam("max", max)
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(USER_NAME, PASSWORD))
				.get();
		
		checkAPIError(response, 400, null, MESSAGE_INVALID_OFFSET_MAX, ErrorType.BAD_REQUEST);

	}
	
	@Test
	public void testListDescriptionsInvalidOffset() {
		testListDescriptionsInvalidParams(-1, 2);
	}
	
	@Test
	public void testListDescriptionsInvalidMax() {
		testListDescriptionsInvalidParams(1, 0);
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// STORE OFFERINGS ///////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	
	private Response getStoreOfferings(String userName, String password, String storeName) {
		Client client = ClientBuilder.newClient();
		return client.target(endPoint + "/api/v2/store/" + storeName + "/offering/")
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(userName, password)).get();
	}
	
	private Response getStoreOfferings(String userName, String password, String storeName, int offset, int max) {
		Client client = ClientBuilder.newClient();
		return client.target(endPoint + "/api/v2/store/" + storeName + "/offering/")
				.queryParam("offset", offset)
				.queryParam("max", max)
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(userName, password)).get();
	}
	
	@Test
	public void testStoreOfferings() {
		
		final String NAME = "store";
		final String USDL = "usdl";
		String description1Name = "displayname";
		String description2Name = "secondary";
		
		Map<String, Map<String, String>> storeDescriptions = new HashMap<>();
		
		Map<String, String> firstStoreDescription = new HashMap<>();
		firstStoreDescription.put(NAME, description1Name);
		firstStoreDescription.put(USDL, defaultUSDLPath);
		storeDescriptions.put(FIRST_STORE_NAME, firstStoreDescription);
		
		Map<String, String> secondStoreDescription = new HashMap<>();
		secondStoreDescription.put(NAME, description2Name);
		secondStoreDescription.put(USDL, secondaryUSDLPath);
		storeDescriptions.put(SECOND_STORE_NAME, secondStoreDescription);
		
		// Create an additional Store
		Response createStoreResponse = createStore(USER_NAME, PASSWORD, SECOND_STORE_NAME, SECOND_STORE_URL);
		assertThat(createStoreResponse.getStatus()).isEqualTo(201);

		// Push each description in a different store
		for (String storeName: storeDescriptions.keySet()) {
			
			String descriptionName = storeDescriptions.get(storeName).get(NAME);
			String usdl = storeDescriptions.get(storeName).get(USDL);
			
			Response createDescRes = createDescription(USER_NAME, PASSWORD, storeName, descriptionName, usdl, "");
			assertThat(createDescRes.getStatus()).isEqualTo(201);
		}
		
		// Get stores offerings
		for (String storeName: storeDescriptions.keySet()) {
			Response storeOfferingResponse = getStoreOfferings(USER_NAME, PASSWORD, storeName);
			assertThat(storeOfferingResponse.getStatus()).isEqualTo(200);

			Offerings offerings = storeOfferingResponse.readEntity(Offerings.class);
			String descriptionName = storeDescriptions.get(storeName).get(NAME);
			String descriptionUSDLPath = storeDescriptions.get(storeName).get(USDL);
			List<Offering> expectedStoreOfferings = DESCRIPTIONS_OFFERINGS.get(descriptionUSDLPath);
			List<Offering> actualStoreOfferings = offerings.getOfferings();
			assertThat(actualStoreOfferings.size()).isEqualTo(expectedStoreOfferings.size());
			
			// Check that all the expected offerings are in the list of offerings contained in the store
			for (Offering offering: expectedStoreOfferings) {
				checkOfferingInList(storeName, descriptionName, offering.getUri(), offerings.getOfferings());
				checkOfferingInternalState(offerings.getOfferings(), storeName, descriptionName, offering);				
			}

			
		}
	}
	
	private void testGetSomeStoreOfferings(int offset, int max) {
		
		int OFFERINGS_IN_DESCRIPTION = 2;
		
		// We are using the description that contains two offerings
		// and checking if offset and max works in an appropriate way
		Response createDescriptionResponse = createDescription(USER_NAME, PASSWORD, 
				FIRST_STORE_NAME, "displayName", secondaryUSDLPath, "");
		assertThat(createDescriptionResponse.getStatus()).isEqualTo(201);
		
		// Check that the number of returned offerings is correct
		Response storeOfferingResponse = getStoreOfferings(USER_NAME, PASSWORD, FIRST_STORE_NAME, offset, max);
		Offerings offerings = storeOfferingResponse.readEntity(Offerings.class);
		int expectedElements = offset + max > OFFERINGS_IN_DESCRIPTION ? OFFERINGS_IN_DESCRIPTION - offset : max;
		assertThat(offerings.getOfferings().size()).isEqualTo(expectedElements);
	}
	
	@Test
	public void testGetFirstStoreOffering() {
		testGetSomeStoreOfferings(0, 1);
	}
	
	@Test
	public void tesGetSecondStoreOffering() {
		testGetSomeStoreOfferings(1, 1);
	}
	
	@Test
	public void tesGetAllStoreOffering() {
		testGetSomeStoreOfferings(0, 2);
	}
	
	private void testListOfferingsInStoreInvalidParams(int offset, int max, String orderBy, String expectedMessage) {
		Client client = ClientBuilder.newClient();
		Response response = client.target(endPoint + "/api/v2/store/" + FIRST_STORE_NAME + "/offering")
				.queryParam("offset", offset)
				.queryParam("max", max)
				.queryParam("orderBy", orderBy)
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(USER_NAME, PASSWORD))
				.get();
		
		checkAPIError(response, 400, null, expectedMessage, ErrorType.BAD_REQUEST);

	}
	
	@Test
	public void testListOfferingsInStoreInvalidOffset() {
		testListOfferingsInStoreInvalidParams(-1, 2, "id", MESSAGE_INVALID_OFFSET_MAX);
	}
	
	@Test
	public void testListOfferingsInStoreInvalidMax() {
		testListOfferingsInStoreInvalidParams(1, 0, "id", MESSAGE_INVALID_OFFSET_MAX);
	}
	
	@Test
	public void testListOfferingsInStoreInvalidOrderBy() {
		String orderBy = "namea";
		testListOfferingsInStoreInvalidParams(1, 1, orderBy, "Offerings cannot be ordered by " + orderBy + ".");
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////// ALL OFFERINGS ////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	
	private void intializeStoresWithOfferings(Map<String, String> descriptions) {
		// Create an additional Store
		Response createStoreResponse = createStore(USER_NAME, PASSWORD, SECOND_STORE_NAME, SECOND_STORE_URL);
		assertThat(createStoreResponse.getStatus()).isEqualTo(201);

		// Push both descriptions in both stores
		String[] stores = new String[]{FIRST_STORE_NAME, SECOND_STORE_NAME};
		
		for (String store: stores) {
			
			for (Entry<String, String> description: descriptions.entrySet()) {
				Response createDescriptionResponse = createDescription(USER_NAME, PASSWORD, store, 
						description.getKey(), description.getValue(), "");
				assertThat(createDescriptionResponse.getStatus()).isEqualTo(201);
			}			
		}

	}
	
	@Test
	public void testGetAllOfferings() {
		
		final int TOTAL_OFFERINGS = 6;			// 6 offerings: 3 in each store.
		
		Map<String, String> descriptions = new HashMap<>();
		descriptions.put("default", defaultUSDLPath);
		descriptions.put("secondary", secondaryUSDLPath);
		intializeStoresWithOfferings(descriptions);
		
		// Get all the offerings
		Client client = ClientBuilder.newClient();
		Response allOfferingsResponse = client.target(endPoint + "/api/v2/offering")
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(USER_NAME, PASSWORD))
				.get();
		assertThat(allOfferingsResponse.getStatus()).isEqualTo(200);
		
		Offerings offerings = allOfferingsResponse.readEntity(Offerings.class);
		assertThat(offerings.getOfferings().size()).isEqualTo(TOTAL_OFFERINGS);

	}
	
	private void testGetSomeOfferings(int offset, int max) {
		
		final int TOTAL_OFFERINGS = 6;
		
		Map<String, String> descriptions = new HashMap<>();
		descriptions.put("default", defaultUSDLPath);
		descriptions.put("secondary", secondaryUSDLPath);
		intializeStoresWithOfferings(descriptions);
		
		// Get all the offerings
		Client client = ClientBuilder.newClient();
		Response allOfferingsResponse = client.target(endPoint + "/api/v2/offering")
				.queryParam("offset", offset)
				.queryParam("max", max)
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(USER_NAME, PASSWORD))
				.get();
		assertThat(allOfferingsResponse.getStatus()).isEqualTo(200);
		
		Offerings offerings = allOfferingsResponse.readEntity(Offerings.class);
		int expectedElements = offset + max > TOTAL_OFFERINGS ? TOTAL_OFFERINGS - offset : max;
		assertThat(offerings.getOfferings().size()).isEqualTo(expectedElements);

	}
	
	@Test
	public void testGetFirstTwoElements() {
		testGetSomeOfferings(0, 2);
	}
	
	@Test
	public void testGetMoreElementsThanExisting() {
		testGetSomeOfferings(3, 9);
	}
	
	private void testListOfferingsInvalidParams(int offset, int max, String orderBy, String expectedMessage) {
		Client client = ClientBuilder.newClient();
		Response response = client.target(endPoint + "/api/v2/offering")
				.queryParam("offset", offset)
				.queryParam("max", max)
				.queryParam("orderBy", orderBy)
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(USER_NAME, PASSWORD))
				.get();
		
		checkAPIError(response, 400, null, expectedMessage, ErrorType.BAD_REQUEST);

	}
	
	@Test
	public void testListOfferingsInvalidOffset() {
		testListOfferingsInvalidParams(-1, 2, "id", MESSAGE_INVALID_OFFSET_MAX);
	}
	
	@Test
	public void testListOfferingsInvalidMax() {
		testListOfferingsInvalidParams(1, 0, "id", MESSAGE_INVALID_OFFSET_MAX); 
	}
	
	@Test
	public void testListOfferingsInvalidOrderBy() {
		String orderBy = "namea";
		testListOfferingsInvalidParams(1, 1, orderBy, "Offerings cannot be ordered by " + orderBy + "."); 
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// BOOKMARKS //////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	
	private Response getBookmarkedOfferings() {
		// Get all the offerings
		Client client = ClientBuilder.newClient();
		Response allOfferingsResponse = client.target(endPoint + "/api/v2/offering/bookmark")
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(USER_NAME, PASSWORD))
				.get();
		
		return allOfferingsResponse;
	}
	
	/**
	 * This method bookmarks or unbookmarks an offering depending on its previous state
	 * @param userName User that will bookmark the offering
	 * @param password user's password
	 * @param storeName The name of the store where the offering to be bookmarked is contained
	 * @param descriptionName The name of the descritpion where the offering to be bookmarked is described 
	 * @param offeringName The name of the offering to be bookmarked
	 * @return The response from the server
	 */
	private Response bookmarkOrUnbookmarkOffering(String userName, String password,String storeName, 
			String descriptionName, String offeringName) {
		Client client = ClientBuilder.newClient();
		Response response = client.target(endPoint + "/api/v2/store/" + storeName + "/description/" + 
					descriptionName + "/offering/" + offeringName + "/bookmark")
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(userName, password))
				.post(null);

		return response;
	}
	
	@Test
	public void testBookmarkAndUnbookmarkOffering() {
		
		String firstDescriptionName = "default";
		String secondDescriptionName = "secondary";
		Map<String, String> descriptions = new HashMap<>();
		descriptions.put(firstDescriptionName, defaultUSDLPath);
		descriptions.put(secondDescriptionName, secondaryUSDLPath);
		intializeStoresWithOfferings(descriptions);
		
		// Select one offering
		String descriptionWithOfferingToBookmark = firstDescriptionName;
		Offering bookmarkedOffering = DESCRIPTIONS_OFFERINGS.get(
				descriptions.get(descriptionWithOfferingToBookmark)).get(0);
		
		// Bookmark one offering
		Response bookmarkResponse = bookmarkOrUnbookmarkOffering(USER_NAME, PASSWORD, FIRST_STORE_NAME, 
				descriptionWithOfferingToBookmark, bookmarkedOffering.getName());
		assertThat(bookmarkResponse.getStatus()).isEqualTo(204);
		
		// Set store and description for offering. Otherwise, test will fail since offering.equals is based
		// on these fields
		Store store = new Store();
		store.setName(FIRST_STORE_NAME);
		
		Description description = new Description();
		description.setName(descriptionWithOfferingToBookmark);
		description.setStore(store);
		
		bookmarkedOffering.setDescribedIn(description);
		
		// Check that bookmarked offerings contains the bookmarked offering
		Response bookmarkedOfferingsResponse = getBookmarkedOfferings();
		assertThat(bookmarkedOfferingsResponse.getStatus()).isEqualTo(200);
		List<Offering> bookmarkedOfferings = bookmarkedOfferingsResponse.readEntity(Offerings.class).getOfferings();
		assertThat(bookmarkedOfferings.size()).isEqualTo(1);
		// bookmarkedOffering parameters MUST be properly initialized 
		assertThat(bookmarkedOfferings.get(0)).isEqualTo(bookmarkedOffering);
		assertThat(bookmarkedOfferings.get(0).getDescribedIn().getName()).isEqualTo(descriptionWithOfferingToBookmark);
		assertThat(bookmarkedOfferings.get(0).getDescribedIn().getStore().getName()).isEqualTo(FIRST_STORE_NAME);
		
		// Unbookmark the offering
		Response unbookmarkResponse = bookmarkOrUnbookmarkOffering(USER_NAME, PASSWORD,FIRST_STORE_NAME, 
				descriptionWithOfferingToBookmark, bookmarkedOffering.getName());
		assertThat(unbookmarkResponse.getStatus()).isEqualTo(204);

		// Check that bookmarked offerings is empty
		bookmarkedOfferingsResponse = getBookmarkedOfferings();
		assertThat(bookmarkedOfferingsResponse.getStatus()).isEqualTo(200);
		bookmarkedOfferings = bookmarkedOfferingsResponse.readEntity(Offerings.class).getOfferings();
		assertThat(bookmarkedOfferings).isEmpty();
	}
	
	@Test
	public void testBookmarkTwoOfferings() {
		
		String firstDescriptionName = "default";
		String secondDescriptionName = "secondary";
		Map<String, String> descriptions = new HashMap<>();
		descriptions.put(firstDescriptionName, defaultUSDLPath);
		descriptions.put(secondDescriptionName, secondaryUSDLPath);
		intializeStoresWithOfferings(descriptions);
		
		// Select one offering
		String descriptionWithOfferingToBookmark = firstDescriptionName;
		Offering bookmarkedOffering = DESCRIPTIONS_OFFERINGS.get(descriptions.get(firstDescriptionName)).get(0);
		
		// Bookmark the offerings from different stores (same offering in different stores)
		String[] stores = {FIRST_STORE_NAME, SECOND_STORE_NAME};
		for (String storeName: stores) {
			Response bookmarkResponse = bookmarkOrUnbookmarkOffering(USER_NAME, PASSWORD, storeName, 
					descriptionWithOfferingToBookmark, bookmarkedOffering.getName());
			assertThat(bookmarkResponse.getStatus()).isEqualTo(204);
		}
		
		// Check that bookmarked offerings contains both offerings
		Response bookmarkedOfferingsResponse = getBookmarkedOfferings();
		assertThat(bookmarkedOfferingsResponse.getStatus()).isEqualTo(200);
		List<Offering> bookmarkedOfferings = bookmarkedOfferingsResponse.readEntity(Offerings.class).getOfferings();
		assertThat(bookmarkedOfferings.size()).isEqualTo(stores.length);
		
		for (int i = 0; i < bookmarkedOfferings.size(); i++) {			
			checkOfferingInList(stores[i], descriptionWithOfferingToBookmark, bookmarkedOffering.getUri(), 
					bookmarkedOfferings);
		}
	}
	
	/**
	 * Test for a know bug (fixed): When an user tries to remove an offering that has been
	 * bookmarked, the system throws an exception 
	 */
	@Test
	public void testRemoveBookmarkedOffering() {
		
		String usdlPath = defaultUSDLPath;
		
		String descriptionName = "description";
		Response createDescRes = createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, 
				descriptionName, usdlPath, "");
		assertThat(createDescRes.getStatus()).isEqualTo(201);
		
		// Bookmark the offering
		Response bookmarkRes = bookmarkOrUnbookmarkOffering(USER_NAME, PASSWORD, FIRST_STORE_NAME, 
				descriptionName, DESCRIPTIONS_OFFERINGS.get(usdlPath).get(0).getName());
		assertThat(bookmarkRes.getStatus()).isEqualTo(204);
		
		// Delete the offering
		Response deleteRes = deleteDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, descriptionName);
		assertThat(deleteRes.getStatus()).isEqualTo(204);
	}
	
	/**
	 * Test for a know bug (fixed): When a user bookmarks an offering, the system throws an
	 * exception when this user tries to remove their own account.
	 */
	@Test
	public void testRemoveUserWithBookmarkedOfferings() {
		
		String usdlPath = defaultUSDLPath;
		
		// Create an offering
		String descriptionName = "description";
		Response createDescRes = createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, 
				descriptionName, usdlPath, "");
		assertThat(createDescRes.getStatus()).isEqualTo(201);
		
		// Create another user
		String userName = USER_NAME + "A";
		String email = "example3@example.com";
		Response createUserRes = createUser(userName, email, PASSWORD);
		assertThat(createUserRes.getStatus()).isEqualTo(201);
		
		// Bookmark the offering with the new user
		Response bookmarkRes = bookmarkOrUnbookmarkOffering(userName, PASSWORD, FIRST_STORE_NAME, 
				descriptionName, DESCRIPTIONS_OFFERINGS.get(usdlPath).get(0).getName());
		assertThat(bookmarkRes.getStatus()).isEqualTo(204);
		
		// Delete the user
		Response deleteUserRes = deleteUser(userName, PASSWORD, userName);
		assertThat(deleteUserRes.getStatus()).isEqualTo(204);
		
		// Check that the offering has not been deleted
		Response getOffRes = getOffering(USER_NAME, PASSWORD, FIRST_STORE_NAME, 
				descriptionName, DESCRIPTIONS_OFFERINGS.get(usdlPath).get(0).getName());
		assertThat(getOffRes.getStatus()).isEqualTo(200);
		
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////// REVIEWS ///////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	
	private Response getOffering(String userName, String password, String storeName, String descriptionName, 
			String offeringName) {
		Client client = ClientBuilder.newClient();
		return client.target(endPoint + "/api/v2/store/" + storeName + "/description/" + descriptionName + 
				"/offering/" + offeringName)
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(userName, password)).get();
	}


	private int createOfferingAndReview(String storeName, String descriptionName, String usdlPath, 
			String offeringName, int score, String comment) {
				
		Response createDescriptionResponse = createDescription(USER_NAME, PASSWORD, storeName, 
				descriptionName, usdlPath, "");
		assertThat(createDescriptionResponse.getStatus()).isEqualTo(201);

		Response res = createOfferingReview(USER_NAME, PASSWORD, storeName, descriptionName, 
				offeringName, score, comment);
		assertThat(res.getStatus()).isEqualTo(201);
		
		// Get offering and its average score
		Offering reviewdOffering = getOffering(USER_NAME, PASSWORD, storeName, descriptionName, offeringName)
				.readEntity(Offering.class);
		assertThat(reviewdOffering.getAverageScore()).isEqualTo(score);
		
		// Get review ID
		String[] urlParts = res.getLocation().getPath().split("/");
		int reviewId = Integer.parseInt(urlParts[urlParts.length - 1]); 
		assertThat(res.getLocation().getPath()).endsWith("/api/v2/store/" + storeName + "/description/" + 
					descriptionName + "/offering/" + offeringName + "/review/" + reviewId);
		
		return reviewId;
	}
	
	@Test
	public void testReviewOffering() {

		String usdlPath = defaultUSDLPath;
		String storeName = FIRST_STORE_NAME;
		String descriptionName = "default";
		String offeringName = DESCRIPTIONS_OFFERINGS.get(usdlPath).get(0).getName();
		
		int score = 5;
		String comment = "Basic comment";
		
		int reviewId = createOfferingAndReview(storeName, descriptionName, usdlPath, offeringName, 
				score, comment);
		checkOfferingReview(USER_NAME, PASSWORD, storeName, descriptionName, offeringName, 
				reviewId, score, comment);
	}
	
	@Test
	public void testUserCannotReviewOneStoreTwice() {
		
		String usdlPath = defaultUSDLPath;
		String storeName = FIRST_STORE_NAME;
		String descriptionName = "default";
		String offeringName = DESCRIPTIONS_OFFERINGS.get(usdlPath).get(0).getName();
		
		int score = 5;
		String comment = "Basic comment";
		
		createOfferingAndReview(storeName, descriptionName, usdlPath, offeringName, 
				score, comment);

		// Create another review for the same offering with the same user
		Response res = createOfferingReview(USER_NAME, PASSWORD, storeName, descriptionName, 
				offeringName, score, comment);
		checkAPIError(res, 403, null, "You are not authorized to review Offering. An entity can only be reviewed once", 
				ErrorType.FORBIDDEN);
	}
	
	@Test
	public void testUpdateReview() {
		
		String usdlPath = defaultUSDLPath;
		String storeName = FIRST_STORE_NAME;
		String descriptionName = "default";
		String offeringName = DESCRIPTIONS_OFFERINGS.get(usdlPath).get(0).getName();
		
		int score = 5;
		String comment = "Basic comment";
		
		int reviewId = createOfferingAndReview(storeName, descriptionName, usdlPath, offeringName, 
				score, comment);
		
		// Update review
		int newScore = 3;
		String newComment = "This is a new comment";
		Response updateRes = updateOfferingReview(USER_NAME, PASSWORD, storeName, descriptionName, offeringName,
				reviewId, newScore, newComment);
		assertThat(updateRes.getStatus()).isEqualTo(200);
		
		// Check updated review
		checkOfferingReview(USER_NAME, PASSWORD, storeName, descriptionName, offeringName, 
				reviewId, newScore, newComment);
		
		// Check that average score has been properly updated
		Offering reviewOffering = getOffering(USER_NAME, PASSWORD, storeName, descriptionName, offeringName)
				.readEntity(Offering.class);
		assertThat(reviewOffering.getAverageScore()).isEqualTo(newScore);

	}
	
	@Test
	public void testDeleteReview() {
		
		String usdlPath = defaultUSDLPath;
		String storeName = FIRST_STORE_NAME;
		String descriptionName = "default";
		String offeringName = DESCRIPTIONS_OFFERINGS.get(usdlPath).get(0).getName();
		
		int score = 5;
		String comment = "Basic comment";
		
		int reviewId = createOfferingAndReview(storeName, descriptionName, usdlPath, offeringName, 
				score, comment);
		Response deleteResponse = deleteOfferingReview(USER_NAME, PASSWORD, storeName, descriptionName, 
				offeringName, reviewId);
		assertThat(deleteResponse.getStatus()).isEqualTo(204);
		
		// Get review should return 404
		Response getResponse = getOfferingReview(USER_NAME, PASSWORD, storeName, descriptionName, 
				offeringName, reviewId);
		assertThat(getResponse.getStatus()).isEqualTo(404);
		
		// Average score should be zero when the last review is deleted
		Offering reviewedOffering = getOffering(USER_NAME, PASSWORD, storeName, descriptionName, offeringName)
				.readEntity(Offering.class);
		assertThat(reviewedOffering.getAverageScore()).isEqualTo(0);
		
	}
	
	private String getCommentFromBase(String baseComment, int score) {
		return baseComment + " " + score;
	}
	
	private double createNReviews(String storeName, String descriptionName, String offeringName, String baseComment, 
			int nReviews, int initialScore) {
		
		double totalScore = 0;
		String alphabet = "abcdefghijklmonpqrstuvwxyz";
		
		for (int score = initialScore; score < nReviews + initialScore; score++) {
			
			// Create user for review
			char userSuffix = alphabet.charAt(score % alphabet.length());
			String userName = "userforrating" + userSuffix;
			String email = "rating" + userSuffix + "@example.com";
			Response createUserRes = createUser(userName, email, PASSWORD);
			assertThat(createUserRes.getStatus()).isEqualTo(201);

			// Review the offering with the new user (one user can only review one offering once)
			String comment = getCommentFromBase(baseComment, score);
			Response res = createOfferingReview(userName, PASSWORD, storeName, descriptionName, offeringName, 
					score, comment);
			assertThat(res.getStatus()).isEqualTo(201);
			
			totalScore += score;
		}

		return totalScore;
	}
	
	@Test
	public void testCreateNReviews() {
		
		String usdlPath = defaultUSDLPath;
		String storeName = FIRST_STORE_NAME;
		String descriptionName = "default";
		String offeringName = DESCRIPTIONS_OFFERINGS.get(usdlPath).get(0).getName();
		String baseComment = "Basic comment";
		
		// Create offering
		Response createDescriptionResponse = createDescription(USER_NAME, PASSWORD, storeName, 
				descriptionName, usdlPath, "");
		assertThat(createDescriptionResponse.getStatus()).isEqualTo(201);
		
		// Create reviews
		int reviewsNumber = 4;
		int initialScore = 1;
		double totalScore = createNReviews(storeName, descriptionName, offeringName, baseComment, 
				reviewsNumber, initialScore);
		
		// Get average score
		double average = totalScore / ((double) reviewsNumber);
		Offering reviewdOffering = getOffering(USER_NAME, PASSWORD, storeName, descriptionName, offeringName)
				.readEntity(Offering.class);
		assertThat(reviewdOffering.getAverageScore()).isEqualTo(average);
		
		// Get reviews list and check values
		Reviews reviews = getOfferingReviews(USER_NAME, PASSWORD, storeName, descriptionName, offeringName, 
				0, 100, "id", false).readEntity(Reviews.class);
		List<Review> reviewsList = reviews.getReviews();
		assertThat(reviews.getReviews()).hasSize((int) reviewsNumber);
		
		for (int i = 0; i < reviewsList.size(); i++) {
			Review review = reviewsList.get(i);
			
			int score = i + initialScore;
			// Comment is based on the score
			assertThat(review.getComment()).isEqualTo(getCommentFromBase(baseComment, score));
			assertThat(review.getScore()).isEqualTo(score);
			
		}
		
		// Get two reviews: ordered by score DESC, offset is 1
		int max = 2;
		int secondBestScore = initialScore + reviewsNumber - 2;
		reviews = getOfferingReviews(USER_NAME, PASSWORD, storeName, descriptionName, offeringName, 
				1, 2, "score", true).readEntity(Reviews.class);
		reviewsList = reviews.getReviews();
		assertThat(reviewsList).hasSize(max);
		
		for (int i = 0; i < reviewsList.size(); i++) {
			Review review = reviewsList.get(i);
			
			int score = secondBestScore - i;
			// Comment is based on the score
			assertThat(review.getComment()).isEqualTo(getCommentFromBase(baseComment, score));
			assertThat(review.getScore()).isEqualTo(score);
			
		}
	}
	
	@Test
	public void testUpdateReviewComplex() {
		
		String usdlPath = defaultUSDLPath;
		String storeName = FIRST_STORE_NAME;
		String descriptionName = "default";
		String offeringName = DESCRIPTIONS_OFFERINGS.get(usdlPath).get(0).getName();
		int score = 5;
		String baseComment = "Basic comment";
		
		int reviewId = createOfferingAndReview(storeName, descriptionName, usdlPath, offeringName, 
				score, baseComment);
		
		// Create additional reviews
		int reviewsNumber = 2;
		double totalScore = createNReviews(storeName, descriptionName, offeringName, baseComment, reviewsNumber, 1);
		
		// Update initial review
		int newScore = 3;
		String newComment = "This is a new comment";
		Response updateRes = updateOfferingReview(USER_NAME, PASSWORD, storeName, descriptionName, offeringName,
				reviewId, newScore, newComment);
		assertThat(updateRes.getStatus()).isEqualTo(200);
		
		// Check that the review has been properly updated
		checkOfferingReview(USER_NAME, PASSWORD, storeName, descriptionName, offeringName,
				reviewId, newScore, newComment);
		
		// Get average score
		double average = (totalScore + newScore) / ((double) (reviewsNumber + 1));
		Offering reviewedOffering = getOffering(USER_NAME, PASSWORD, storeName, descriptionName, offeringName)
				.readEntity(Offering.class);
		assertThat(reviewedOffering.getAverageScore()).isEqualTo(average);
		
	}
	
	@Test
	public void testDeleteReviewComplex() {
		
		String usdlPath = defaultUSDLPath;
		String storeName = FIRST_STORE_NAME;
		String descriptionName = "default";
		String offeringName = DESCRIPTIONS_OFFERINGS.get(usdlPath).get(0).getName();
		int score = 5;
		String baseComment = "Basic comment";
		
		int reviewId = createOfferingAndReview(storeName, descriptionName, usdlPath, offeringName, 
				score, baseComment);
		
		// Create additional reviews
		int reviewsNumber = 2;
		double totalScore = createNReviews(storeName, descriptionName, offeringName, baseComment, reviewsNumber, 1);
		
		// Delete initial review
		Response deleteRes = deleteOfferingReview(USER_NAME, PASSWORD, storeName, descriptionName, 
				offeringName, reviewId);
		assertThat(deleteRes.getStatus()).isEqualTo(204);
		
		// Get average score
		double average = totalScore / reviewsNumber;
		Offering reviewdOffering = getOffering(USER_NAME, PASSWORD, storeName, descriptionName, offeringName)
				.readEntity(Offering.class);
		assertThat(reviewdOffering.getAverageScore()).isEqualTo(average);
		
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////// CATEGORIES //////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	
	private Response getCategories(String userName, String password) {		
		Client client = ClientBuilder.newClient();
		return client.target(endPoint + "/api/v2/category/")
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(userName, password)).get();
	}
	
	private Response getCategoryRecommendations(String userName, String password, String category, String orderBy) {		
		
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(endPoint + "/api/v2/category/" + category + "/offering");
		
		// Include order when parameter is not null. BY default, offerings are ordered by score
		if (orderBy != null) {
			target = target.queryParam("orderBy", orderBy);
		}
		
		return target.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(userName, password)).get();
	}
	
	@Test
	public void testGetCategories() {
		
		String usdlPath = defaultUSDLPath;
		Offering offering = DESCRIPTIONS_OFFERINGS.get(usdlPath).get(0);
		
		// Create one offering. This offering is contained in two categories
		Response createDescriptionResponse = createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, 
				"description", usdlPath, "");
		assertThat(createDescriptionResponse.getStatus()).isEqualTo(201);
		
		// Get categories
		Response getCategoriesResponse = getCategories(USER_NAME, PASSWORD);
		assertThat(getCategoriesResponse.getStatus()).isEqualTo(200);
		
		// Check that all the categories are included
		Categories categories = getCategoriesResponse.readEntity(Categories.class);
		List<Category> categoriesList = categories.getCategories();
		assertThat(categoriesList).containsAll(offering.getCategories());		
	}
	
	@Test
	public void testGetCategoryRecommendations() {
		
		String storeName = FIRST_STORE_NAME;
		String descriptionName = "description";
		String usdlPath = secondaryUSDLPath;
		
		// Create two offerings
		Response createDescriptionResponse = createDescription(USER_NAME, PASSWORD, storeName, 
				descriptionName, usdlPath, "");
		assertThat(createDescriptionResponse.getStatus()).isEqualTo(201);
		
		// Review both offerings. First one with 1 starts and second one with 5 starts
		List<Offering> descriptionOfferings = DESCRIPTIONS_OFFERINGS.get(usdlPath);
		assertThat(descriptionOfferings.size()).isEqualTo(2);	// Two offerings expected in this Description
		Offering highScoredOffering = descriptionOfferings.get(1);
		Offering lowScoredOffering = descriptionOfferings.get(0);
		
		Response createRating1Response = createOfferingReview(USER_NAME, PASSWORD, storeName, 
				descriptionName, lowScoredOffering.getName(), 1, "");
		assertThat(createRating1Response.getStatus()).isEqualTo(201);
		
		Response createRating2Response = createOfferingReview(USER_NAME, PASSWORD, storeName, 
				descriptionName, highScoredOffering.getName(), 5, "");
		assertThat(createRating2Response.getStatus()).isEqualTo(201);
		
		// Get the category in common between the two offerings
		Set<Category> commonCategories = new HashSet<>(descriptionOfferings.get(0).getCategories());
		commonCategories.retainAll(descriptionOfferings.get(1).getCategories());
		String categoryName = commonCategories.iterator().next().getName();
		
		// Get category recommendations (offerings should be ordered by score: second offering
		// should be the first one in the list)
		Response getRecommendationsResponse = getCategoryRecommendations(USER_NAME, PASSWORD, categoryName, null);
		assertThat(getRecommendationsResponse.getStatus()).isEqualTo(200);
		Offerings recommendations = getRecommendationsResponse.readEntity(Offerings.class);
		List<Offering> offeringsList = recommendations.getOfferings();
		
		// Set offerings description since offering.equals depends on it
		Store store = new Store();
		store.setName(storeName);
		
		Description description = new Description();
		description.setName(descriptionName);
		description.setStore(store);
		
		descriptionOfferings.get(0).setDescribedIn(description);
		descriptionOfferings.get(1).setDescribedIn(description);

		// Check that offerings has been returned in the correct order
		assertThat(offeringsList.get(0)).isEqualTo(highScoredOffering);
		assertThat(offeringsList.get(1)).isEqualTo(lowScoredOffering);

	}
	
	@Test
	public void testGetCategoriesInvalidOrder() {
		
		String usdlPath = defaultUSDLPath;
		Offering offering = DESCRIPTIONS_OFFERINGS.get(defaultUSDLPath).get(0);
		
		// Create one offering.
		Response createDescriptionResponse = createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, 
				"description", usdlPath, "");
		assertThat(createDescriptionResponse.getStatus()).isEqualTo(201);
		
		// Get offerings for one category with invalid order
		String categoryName = offering.getCategories().iterator().next().getName();
		String orderBy = "namea";
		Response getRecommendationsResponse = getCategoryRecommendations(USER_NAME, PASSWORD, categoryName, orderBy);
		assertThat(getRecommendationsResponse.getStatus()).isEqualTo(400);
		checkAPIError(getRecommendationsResponse, 400, null, 
				"Offerings cannot be ordered by " + orderBy + ".", ErrorType.BAD_REQUEST);
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// LAST VIEWED OFFERINGS ////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	
	private Response getLastViewedOfferings() {
		// Get all the offerings
		Client client = ClientBuilder.newClient();
		Response allOfferingsResponse = client.target(endPoint + "/api/v2/offering/lastViewed")
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(USER_NAME, PASSWORD))
				.get();
		
		return allOfferingsResponse;
	}

	private void testListViewedOfferingsInvalidParams(int offset, int max, String expectedMessage) {
		Client client = ClientBuilder.newClient();
		Response response = client.target(endPoint + "/api/v2/offering/lastViewed")
				.queryParam("offset", offset)
				.queryParam("max", max)
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(USER_NAME, PASSWORD))
				.get();
		
		checkAPIError(response, 400, null, expectedMessage, ErrorType.BAD_REQUEST);

	}
	
	@Test
	public void testListViewedOfferingsInvalidOffset() {
		testListViewedOfferingsInvalidParams(-1, 2, MESSAGE_INVALID_OFFSET_MAX);
	}
	
	@Test
	public void testListViewedOfferingsInvalidMax() {
		testListViewedOfferingsInvalidParams(1, 0, MESSAGE_INVALID_OFFSET_MAX); 
	}
	
	@Test
	public void testListViewedOfferings() {
		
		// Initialization
		String firstDescriptionName = "default";
		String secondDescriptionName = "secondary";
		Map<String, String> descriptions = new HashMap<>();
		descriptions.put(firstDescriptionName, defaultUSDLPath);
		descriptions.put(secondDescriptionName, secondaryUSDLPath);
		intializeStoresWithOfferings(descriptions);
		
		// Select one offering
		String descriptionWithOfferingToView = firstDescriptionName;
		Offering viewedOffering = DESCRIPTIONS_OFFERINGS.get(descriptions.get(firstDescriptionName)).get(0);
		
		// View the offerings in different stores (same offering in different stores)
		String[] stores = {FIRST_STORE_NAME, SECOND_STORE_NAME};
		for (String storeName: stores) {
			Response viewResponse = getOffering(USER_NAME, PASSWORD, storeName, 
					descriptionWithOfferingToView, viewedOffering.getName());
			assertThat(viewResponse.getStatus()).isEqualTo(200);
		}
		
		// Check that viewed offerings contains both offerings
		Response viewedOfferingsResponse = getLastViewedOfferings();
		assertThat(viewedOfferingsResponse.getStatus()).isEqualTo(200);
		List<Offering> viewedOfferings = viewedOfferingsResponse.readEntity(Offerings.class).getOfferings();
		assertThat(viewedOfferings.size()).isEqualTo(stores.length);
		
		for (int i = 0; i < viewedOfferings.size(); i++) {
			checkOfferingInList(stores[i], descriptionWithOfferingToView, viewedOffering.getUri(), viewedOfferings);
		}
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////// OFFERINGS VIEWED BY OTHERS //////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	
	private void testOfferingsViewedByOthersInvalidParams(int max, String expectedMessage) {
		Client client = ClientBuilder.newClient();
		Response response = client.target(endPoint + "/api/v2/offering/viewedByOthers")
				.queryParam("max", max)
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(USER_NAME, PASSWORD))
				.get();
		
		checkAPIError(response, 400, null, expectedMessage, ErrorType.BAD_REQUEST);

	}
	
	@Test
	public void testOfferingsViewedByOthersZeroIsNotAValidMax() {
		testOfferingsViewedByOthersInvalidParams(0, "max is not valid");
	}
	
	@Test
	public void testOfferingsViewedByOthersMaxIsToHigh() {
		testOfferingsViewedByOthersInvalidParams(21, "max cannot be higher than 20.");
	}
	
	// Core functionality is check in SeleniumIT...
	// TODO: Add extra test to check that max works as expected

}
