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

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.fiware.apps.marketplace.model.ErrorType;
import org.fiware.apps.marketplace.model.Review;
import org.fiware.apps.marketplace.model.Reviews;
import org.fiware.apps.marketplace.model.Store;
import org.fiware.apps.marketplace.model.Stores;
import org.junit.Before;
import org.junit.Test;

public class StoreServiceIT extends AbstractIT {
	
	private final static String USER_NAME = "marketplace";
	private final static String PASSWORD = "password1!a";
	private final static String EMAIL = "example@example.com";
	
	protected final static String MESSAGE_URL_IN_USE = "This URL is already in use.";
	protected final static String MESSAGE_NAME_IN_USE = "This name is already in use.";
	
	private static final String IMAGE_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAYAAADgdz34AAAACXBIWXMAAAsTAA"
			+ "ALEwEAmpwYAAADpElEQVRIDa1WS0ubURAdk/gAN75QCApGjIItSIOuRMSiIrhzIUj7J1pL7UZ/QEH6DxIhdK8uJFio+Fi4ykLsp"
			+ "oJ1YVqtulB8mzg9Z5r79YsIyaIDk/v4zj0z987MvSlTVXFSBkE/iLks5zB8gWYE+hLaAW2FUvah36FfoV+AT6MlPoQmh/E/Uvbz"
			+ "Y0dO7DNoHEpgKZoA7nmeJ4h+mcf7BPkrADKhUEiDwWBRcuKoWPMT+jrPF3BG0Irf8zcYF5AGAgEtLy83EkfGlnM4Eg/r60/5d0I"
			+ "DofwEPfcWjI+Pa19fnzf2f/P3R0dHdWJi4jHO7SRkZwXrPPMVeBt+eHiQhoYGyWQyUlFRIQsLC5JIJKSrq0taWloE3svp6ans7u"
			+ "7K5OSkDA8Py8XFhTQ3N8vZ2ZmAQ8DxC3wjcHzHBdgCyuOgd5FIRLEI30uTk5MThVO21nGAZx6r0YgwFQs+1tTU6PHxsbHf3d1pL"
			+ "pfT+/v7As1mszYm6OrqSjs7O43jUWLEaGDaGchng87MzBg5SYoJDVNSqZQZ8AWb4w80kKIB94Ee7O3t2aJSDOC8DctdRqNRkmZ9"
			+ "u1hhvrJCWYVsLMB1dXXWZ8CKCdfBggU3HA4THnRc6EfJEOEss4dyfX0tt7e31ufCYkKMI7y5uTE4duOWtXouMiUrKysFAZPDw0M"
			+ "DlGLAkSMWcnl5acaqq6udAaGBHxw1NjZKW1ub1NfXy9ramgEQA6E3TxniHHftvEbcrB7a29ultrbW1uNnnx0LMnag2AHPxHR9fR"
			+ "0cpcvQ0JCt4xXiDzINeGlKcnigTU1NBp6dnVVUrCIuXs67rOEcKlqXlpa0t7fX8L4iszH4LE2t0NxHGlhcXNTu7m4H0v7+fsVRF"
			+ "GwnmUxqVVWVh/F57c3BQMydr10V3B53MTAwoNvb2zo4OKg4T43H40Z+fn6uR0dH1uf1wIon3q1j36d/rwqgMWcPTIYfXTXPzc0p"
			+ "j4OklNXVVTO6sbGhW1tbistQY7GYEbrd+8j5NtgDFECa8br+hon3UGHmUKampqSnp0fGxsZkeXnZsompiKMQGLWb09WOLSj8mQb"
			+ "nDrmZpjl0+A5/Rv8tccxtVnE6nRZ4bPnN6gbGy3Ne6Tgawr1Cs4HIO+CS5MQ4R+95Dz3kjXxC+xtTH6FhEtBrxMMKsKOjQw4ODm"
			+ "w3m5ubghvXOIFlyzeAnpOcjtslZV7lAf6nkw9QAmpnXEI7D8yTj769aPhoAsvOyH/72/IH8JNvDJtE0dkAAAAASUVORK5CYII=";
	
	@Before
	public void setUp() {
		createUser(USER_NAME, EMAIL, PASSWORD);
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////// AUXILIAR //////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////

	private Response getStore(String userName, String password, String name) {
		Client client = ClientBuilder.newClient();
		return client.target(endPoint + "/api/v2/store/" + name).request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(userName, password)).get();
	}
	
	private void checkStore(String userName, String password, String name, String displayName, 
			String url, String comment) {
		
		Store store = getStore(userName, password, name).readEntity(Store.class);
		
		assertThat(store.getName()).isEqualTo(name);
		assertThat(store.getDisplayName()).isEqualTo(displayName);
		assertThat(store.getUrl()).isEqualTo(url);
		assertThat(store.getComment()).isEqualTo(comment);
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////// CREATE ///////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////

	@Test
	public void testCreation() {
		
		String storeDisplayName = "WMarket";
		String storeName = "wmarket";
		String storeUrl = "https://store.lab.fiware.org";
		String storeComment = "Example Comment";
		
		Response response = createStore(USER_NAME, PASSWORD, storeDisplayName, storeUrl, storeComment, null);
		assertThat(response.getStatus()).isEqualTo(201);
		assertThat(response.getHeaderString("Location")).isEqualTo(endPoint + "/api/v2/store/" + storeName);
		
		// Check that the store actually exists
		checkStore(USER_NAME, PASSWORD, storeName, storeDisplayName, storeUrl, storeComment);
	}
	
	@Test
	public void testCreationWithImage() throws Exception {
		
		String storeDisplayName = "WMarket";
		String storeName = "wmarket";
		
		Response createResponse = createStore(USER_NAME, PASSWORD, storeDisplayName, "http://store.com", 
				null, IMAGE_BASE64);
		assertThat(createResponse.getStatus()).isEqualTo(201);
		assertThat(createResponse.getHeaderString("Location")).isEqualTo(endPoint + "/api/v2/store/" + storeName);
		
		// Retrieve the Store. Check if imagePath is not null
		Response getResponse = getStore(USER_NAME, PASSWORD, storeName);
		Store retrievedStore = getResponse.readEntity(Store.class);
		assertThat(retrievedStore.getImagePath()).isEqualTo("media/store/" + storeName + ".png");
		
		URL url = new URL(endPoint + "/" + retrievedStore.getImagePath());
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		assertThat(conn.getResponseCode()).isEqualTo(200);		
	}
	
	private void testCreationInvalidField(String displayName, String url, String comment, String invalidField,
			String message) {

		Response response = createStore(USER_NAME, PASSWORD, displayName, url, comment, null);
		checkAPIError(response, 400, invalidField, message, ErrorType.VALIDATION_ERROR);
	}
	
	@Test
	public void testCreationDisplayNameInvalid() {
		testCreationInvalidField("Wstore!", "https://store.lab.fiware.org", "", "displayName", 
				MESSAGE_INVALID_DISPLAY_NAME);
	}
	
	@Test
	public void testCreationDisplayNameTooShort() {
		testCreationInvalidField("a", "https://store.lab.fiware.org", "", "displayName", 
				String.format(MESSAGE_TOO_SHORT, 3));
	}
	
	@Test
	public void testCreationDisplayNameTooLong() {
		testCreationInvalidField("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz"
				+ "abcdefghijklmnopqrstuvw", "https://store.lab.fiware.org", "", "displayName", 
				String.format(MESSAGE_TOO_LONG, 100));
	}
	
	@Test
	public void testCreationURLInvalid() {
		testCreationInvalidField("Wstore", "https:/store.lab.fiware.org", "", "url", 
				MESSAGE_INVALID_URL);
	}
	
	@Test
	public void testCreationCommentTooLong() {
		testCreationInvalidField("Wstore", "https://store.com", "12345678901234567890123456789012345678901234567890"
				+ "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456"
				+ "7890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", 
				"comment", String.format(MESSAGE_TOO_LONG, 200));
	}
	
	private void testCreationFieldAlreayExists(String displayName1, String displayName2, String url1, String url2,
			String field, String expectedMessage) {

		createStore(USER_NAME, PASSWORD, displayName1, url1, "", null);
		Response response = createStore(USER_NAME, PASSWORD, displayName2, url2, "", null);
		
		checkAPIError(response, 400, field, expectedMessage, ErrorType.VALIDATION_ERROR);

	}
	
	@Test
	public void testCreationDisplayNameAlreadyExists() {		
		String displayName = "Wmarket";
		
		// name is based on display name and name is checked before display name...
		testCreationFieldAlreayExists(displayName, displayName, "https://store.com", "http://market.com", 
				"displayName", MESSAGE_NAME_IN_USE);
	}
	
	@Test
	public void testCreationURLAlreadyExists() {
		String url = "https://store.lab.fiware.org";
		testCreationFieldAlreayExists("wstore1", "wstore2", url, url, "url", MESSAGE_URL_IN_USE);
	}
	
	@Test
	public void testDeleteUserWithStore() {
		String name = "wstore";
		String url = "https://store.lab.fiware.org";
		
		Response createStoreResponse = createStore(USER_NAME, PASSWORD, name, url, null, null);
		assertThat(createStoreResponse.getStatus()).isEqualTo(201);
		checkStore(USER_NAME, PASSWORD, name, name, url, null);
		
		// Delete user
		Response deleteUserResponse = deleteUser(USER_NAME, PASSWORD, USER_NAME);
		assertThat(deleteUserResponse.getStatus()).isEqualTo(204);
		
		// Create another user to be able to check the store
		String newUserName = USER_NAME + "a";
		String email = "new_email__@example.com";
		Response createUserResponse = createUser(newUserName, email, PASSWORD);
		assertThat(createUserResponse.getStatus()).isEqualTo(201);
		
		// Check that the Store does not exist anymore
		Response getStoreResponse = getStore(newUserName, PASSWORD, name);
		checkAPIError(getStoreResponse, 404, null, String.format(MESSAGE_STORE_NOT_FOUND, name), 
				ErrorType.NOT_FOUND);
		
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////// UPDATE ///////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	
	@Test
	public void testUpdate() {
		// Create Store
		String name = "wstore";
		String displayName = "WStore";
		String url = "https://store.lab.fiware.org";
		String comment = "commnet1";
		
		Response createStoreResponse = createStore(USER_NAME, PASSWORD, displayName, url, comment, null);
		assertThat(createStoreResponse.getStatus()).isEqualTo(201);
		
		// Update the store
		String newDisplayName = "Wstore 1";
		String newUrl = "https://store.testbed.fiware.org";
		String newComment = "comment2";
		
		Response updateStoreResponse = updateStore(USER_NAME, PASSWORD, name, newDisplayName, newUrl, newComment, 
				null);
		assertThat(updateStoreResponse.getStatus()).isEqualTo(200);
		
		// Check that the store has been updated
		checkStore(USER_NAME, PASSWORD, name, newDisplayName, newUrl, newComment);
	}
	
	private void testUpdateInvalidField(String newDisplayName, String newUrl, String newComment, 
			String invalidField, String message) {
		
		String name = "wstore";
		String displayName = "Wstore";
		String url = "https://store.lab.fiware.org";
		String comment = "";
		
		Response createResponse = createStore(USER_NAME, PASSWORD, displayName, url, comment, null);
		assertThat(createResponse.getStatus()).isEqualTo(201);

		Response updateResponse = updateStore(USER_NAME, PASSWORD, name, newDisplayName, newUrl, newComment, null);
		checkAPIError(updateResponse, 400, invalidField, message, ErrorType.VALIDATION_ERROR);
	}
	
	@Test
	public void testUpdateDisplayNameInvalid() {
		testUpdateInvalidField("Wstore!", "https://store.lab.fiware.org", "", "displayName", 
				MESSAGE_INVALID_DISPLAY_NAME);
	}
	
	@Test
	public void testUpdateDisplayNameTooShort() {
		testUpdateInvalidField("a", "https://store.lab.fiware.org", "", "displayName", 
				String.format(MESSAGE_TOO_SHORT, 3));
	}
	
	@Test
	public void testUpdateDisplayNameTooLong() {
		testUpdateInvalidField("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz"
				+ "abcdefghijklmnopqrstuvw", "https://store.lab.fiware.org", "", "displayName", 
				String.format(MESSAGE_TOO_LONG, 100));
	}
	
	@Test
	public void testUpdateURLInvalid() {
		testUpdateInvalidField("Wstore", "https:/store.lab.fiware.org", "", "url", 
				MESSAGE_INVALID_URL);
	}
	
	@Test
	public void testUpdateCommentTooLong() {
		testUpdateInvalidField("Wstore", "https://store.com", "12345678901234567890123456789012345678901234567890"
				+ "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456"
				+ "7890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", 
				"comment", String.format(MESSAGE_TOO_LONG, 200));
	}
	
	/**
	 * This methods creates two stores, based on the different parameters and tries to updates the second
	 * store based on updatedDisplayName and updatedURL. However, it's expected that one of these parameters
	 * has been used to create the first Store, so an error should arise. Error details should be contained in
	 * field an expectedMessage.
	 * @param nameStore1 The name of the first store
	 * @param urlStore1 The URL of the first store
	 * @param nameStore2 The name of the second store (the one to be updated). This name is used to modify the
	 * store so this is not a displayName but the name (the one without spaces, ...) 
	 * @param urlStore2 The URL of the second store (the one to be updated)
	 * @param updatedDisplayName The new display name to be set in the second store
	 * @param updatedURL The new URL to be set in the second store
	 * @param field The field that is repeated
	 * @param expectedMessage Expected error message
	 */
	private void testUpdateFieldAlreayExists(
			String nameStore1,String urlStore1, 
			String nameStore2, String urlStore2,
			String updatedDisplayName, String updatedURL,
			String field, String expectedMessage) {
		
		Response createStore1Response = createStore(USER_NAME, PASSWORD, nameStore1, urlStore1, "", null);
		Response createStore2Response = createStore(USER_NAME, PASSWORD, nameStore2, urlStore2, "", null);
		assertThat(createStore1Response.getStatus()).isEqualTo(201);
		assertThat(createStore2Response.getStatus()).isEqualTo(201);
		
		Response updateResponse = updateStore(USER_NAME, PASSWORD, nameStore2, updatedDisplayName, updatedURL, null, null);
		checkAPIError(updateResponse, 400, field, expectedMessage, ErrorType.VALIDATION_ERROR);

	}
	
	@Test
	public void testUpdateDisplayNameAlreadyExists() {		
		String displayName = "wmarket";
		
		testUpdateFieldAlreayExists(
				displayName, "http://store1.com", 
				"wmarket-2", "http://store2.com", 
				displayName, "http://store3.com", 
				"displayName", MESSAGE_NAME_IN_USE);	
	}
	
	@Test
	public void testUpdateURLAlreadyExists() {
		String url = "https://store.lab.fiware.org";

		testUpdateFieldAlreayExists(
				"wamrket", url, 
				"wmarket-2", "http://store2.com", 
				"wmarket-2", url, 
				"url", MESSAGE_URL_IN_USE);	
	}
	
	@Test
	public void testUpdateNonExisting() {
		
		String displayName = "store-1";
		Response createStoreResponse = createStore(USER_NAME, PASSWORD, displayName, "http://store.com", null, null);
		assertThat(createStoreResponse.getStatus()).isEqualTo(201);
		
		// Update non-existing store
		String storeToBeUpdated = displayName + "a";  	//This ID is supposed not to exist
		Response updateStoreResponse = updateStore(USER_NAME, PASSWORD, storeToBeUpdated, "new display", null, 
				null, null);
		checkAPIError(updateStoreResponse, 404, null, String.format(MESSAGE_STORE_NOT_FOUND, storeToBeUpdated), 
				ErrorType.NOT_FOUND);	
	}
	
	@Test
	public void testUpdateWithAnotherUser() {
		
		String storeName = "store-1";
		Response createStoreResponse = createStore(USER_NAME, PASSWORD, storeName, "http://store.com", null, null);
		assertThat(createStoreResponse.getStatus()).isEqualTo(201);

		// Create another user
		String newUserName = USER_NAME + "a";
		String email = "new_email__@example.com";
		createUser(newUserName, email, PASSWORD);
		
		//Update store with the new user
		Response updateStoreResponse = updateStore(newUserName, PASSWORD, storeName, "new display", null, 
				null, null);
		checkAPIError(updateStoreResponse, 403, null, String.format(MESSAGE_NOT_AUTHORIZED, "update store " 
				+ storeName), ErrorType.FORBIDDEN);	

	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////// DELETE ///////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	
	private Response deleteStore(String authUserName, String authPassword, String storeName) {
				
		Client client = ClientBuilder.newClient();
		Response response = client.target(endPoint + "/api/v2/store/" + storeName)
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(authUserName, authPassword))
				.delete();
		
		return response;

	}
	
	@Test
	public void testDelete() {
		
		String name = "wstore";
		String displayName = "Wstore";
		String url = "https://store.lab.fiware.org";
		String comment = "comment";
		
		// Create the store
		Response createStore = createStore(USER_NAME, PASSWORD, displayName, url, comment, "");
		assertThat(createStore.getStatus()).isEqualTo(201);
		
		// Delete the store
		Response deleteStoreResponse = deleteStore(USER_NAME, PASSWORD, name);
		assertThat(deleteStoreResponse.getStatus()).isEqualTo(204);
	}
	
	@Test
	public void testDeleteNonExisting() {
		
		String displayName = "store-1";
		Response createStoreResponse = createStore(USER_NAME, PASSWORD, displayName, "http://store.com", null, null);
		assertThat(createStoreResponse.getStatus()).isEqualTo(201);
		
		// Delete non-existing store
		String storeToBeDeleted = displayName + "a";  	//This ID is supposed not to exist
		Response deleteStoreResponse = deleteStore(USER_NAME, PASSWORD, storeToBeDeleted);
		checkAPIError(deleteStoreResponse, 404, null, String.format(MESSAGE_STORE_NOT_FOUND, storeToBeDeleted), 
				ErrorType.NOT_FOUND);	
	}
	
	@Test
	public void testDeleteWithAnotherUser() {
		
		String storeName = "store-1";
		Response createStoreResponse = createStore(USER_NAME, PASSWORD, storeName, "http://store.com", null, null);
		assertThat(createStoreResponse.getStatus()).isEqualTo(201);

		// Create another user
		String newUserName = USER_NAME + "a";
		String email = "new_email__@example.com";
		createUser(newUserName, email, PASSWORD);
		
		//Delete user
		Response deleteStoreResponse = deleteStore(newUserName, PASSWORD, storeName);
		checkAPIError(deleteStoreResponse, 403, null, String.format(MESSAGE_NOT_AUTHORIZED, "delete store " 
				+ storeName), ErrorType.FORBIDDEN);	

	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////// LIST STORES /////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	
	@Test
	public void testListAllStores() {
		
		final int STORES_CREATED = 6;
		
		// Create some stores
		String displayNamePattern = "Store %d";
		String urlPattern = "https://store%d.lab.fiware.org";
		
		for (int i = 0; i < STORES_CREATED; i++) {
			createStore(USER_NAME, PASSWORD, String.format(displayNamePattern, i), 
					String.format(urlPattern, i), null, null);
		}
		
		// Get all stores
		Client client = ClientBuilder.newClient();
		Response response = client.target(endPoint + "/api/v2/store/")
				.queryParam("orderBy", "name")	// Order by name
				.queryParam("desc", false)
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(USER_NAME, PASSWORD)).get();
		
		// Check the response
		assertThat(response.getStatus()).isEqualTo(200);
		Stores stores = response.readEntity(Stores.class);
		assertThat(stores.getStores().size()).isEqualTo(STORES_CREATED);
		
		// Users are supposed to be returned in order
		for (int i = 0; i < STORES_CREATED; i++) {
			assertThat(stores.getStores().get(i).getDisplayName()).isEqualTo(String.format(displayNamePattern, i));
		}
	}
	
	private void testListSomeStores(int offset, int max) {
		
		final int STORES_CREATED = 10;
		
		// Create some stores
		String displayNamePattern = "Store %d";
		String urlPattern = "https://store%d.lab.fiware.org";
		
		for (int i = 0; i < STORES_CREATED; i++) {
			createStore(USER_NAME, PASSWORD, String.format(displayNamePattern, i), 
					String.format(urlPattern, i), null, null);
		}
				
		// Get some stores
		Client client = ClientBuilder.newClient();
		Response response = client.target(endPoint + "/api/v2/store/")
				.queryParam("offset", offset)
				.queryParam("max", max)
				.queryParam("orderBy", "name")	// Order by name
				.queryParam("desc", false)
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(USER_NAME, PASSWORD))
				.get();
		
		// Check the response
		int expectedElements = offset + max > STORES_CREATED ? STORES_CREATED - offset : max;
		assertThat(response.getStatus()).isEqualTo(200);
		Stores stores = response.readEntity(Stores.class);
		assertThat(stores.getStores().size()).isEqualTo(expectedElements);
		
		// Stores are supposed to be returned in order
		for (int i = offset; i < offset + expectedElements; i++) {
			assertThat(stores.getStores().get(i - offset).getDisplayName())
					.isEqualTo(String.format(displayNamePattern, i));
		}
	}
	
	@Test
	public void testListSomeStoresMaxInRange() {
		testListSomeStores(3, 4);
	}
	
	@Test
	public void testListSomeStoresMaxNotInRange() {
		testListSomeStores(5, 7);
	}
	
	private void testListStoresInvalidParams(int offset, int max, String orderBy, String expectedMessage) {
		Client client = ClientBuilder.newClient();
		Response response = client.target(endPoint + "/api/v2/store/")
				.queryParam("offset", offset)
				.queryParam("max", max)
				.queryParam("orderBy", orderBy)
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(USER_NAME, PASSWORD))
				.get();
		
		checkAPIError(response, 400, null, expectedMessage, ErrorType.BAD_REQUEST);

	}
	
	@Test
	public void testListStoresInvalidOffset() {
		testListStoresInvalidParams(-1, 2, "id", MESSAGE_INVALID_OFFSET_MAX);
	}
	
	@Test
	public void testListStoresInvalidMax() {
		testListStoresInvalidParams(1, 0, "id", MESSAGE_INVALID_OFFSET_MAX);
	}
	
	@Test
	public void testListStoresInvalidOrderBy() {
		String orderBy = "namea";
		testListStoresInvalidParams(1, 1, orderBy, "Stores cannot be ordered by " + orderBy + ".");
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////// REVIEWS ///////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	
	private int createStoreAndReview(String storeName, String storeUrl, int score, String comment) {
		
		// Create the store and review
		createStore(USER_NAME, PASSWORD, storeName, storeUrl);
		Response res = createStoreReview(USER_NAME, PASSWORD, storeName, score, comment);
		assertThat(res.getStatus()).isEqualTo(201);
		
		// Get store and its average score
		Store reviewdStore = getStore(USER_NAME, PASSWORD, storeName).readEntity(Store.class);
		assertThat(reviewdStore.getAverageScore()).isEqualTo(score);
		
		String[] urlParts = res.getLocation().getPath().split("/");
		int reviewId = Integer.parseInt(urlParts[urlParts.length - 1]); 
		assertThat(res.getLocation().getPath()).endsWith("/api/v2/store/" + storeName + "/review/" + reviewId);
		
		return reviewId;
	}
	
	@Test
	public void testReviewStore() {
		
		String storeName = "store";
		String storeUrl = "http://store.lab.fiware.org";
		int score = 5;
		String comment = "Basic comment";
		
		int reviewId = createStoreAndReview(storeName, storeUrl, score, comment);
		checkStoreReview(USER_NAME, PASSWORD, storeName, reviewId, score, comment);
	}
	
	@Test
	public void testUserCannotReviewOneStoreTwice() {
		
		String storeName = "store";
		String storeUrl = "http://store.lab.fiware.org";
		int score = 5;
		String comment = "Basic comment";
		
		createStoreAndReview(storeName, storeUrl, score, comment);

		// Create another review for the same store with the same user
		Response res = createStoreReview(USER_NAME, PASSWORD, storeName, score, comment);
		checkAPIError(res, 403, null, "You are not authorized to review Store. An entity can only be reviewed once", 
				ErrorType.FORBIDDEN);
	}
	
	@Test
	public void testReviewStoreInvalid() {
		
		String storeName = "store";
		String storeUrl = "http://store.lab.fiware.org";
		int score = 7;
		String comment = "Basic comment";
		
		createStore(USER_NAME, PASSWORD, storeName, storeUrl);
		Response res = createStoreReview(USER_NAME, PASSWORD, storeName, score, comment);
		checkAPIError(res, 400, "score", MESSAGE_INVALID_SCORE, ErrorType.VALIDATION_ERROR);		
		
	}
	
	@Test
	public void testUpdateReview() {
		
		String storeName = "store";
		String storeUrl = "http://store.lab.fiware.org";
		int score = 5;
		String comment = "Basic comment";

		int reviewId = createStoreAndReview(storeName, storeUrl, score, comment);
		
		// Update review
		int newScore = 3;
		String newComment = "This is a new comment";
		Response updateRes = updateStoreReview(USER_NAME, PASSWORD, storeName, 
				reviewId, newScore, newComment);
		assertThat(updateRes.getStatus()).isEqualTo(200);
		
		// Check updated review
		checkStoreReview(USER_NAME, PASSWORD, storeName, reviewId, newScore, newComment);
	}
	
	@Test
	public void testDeleteReview() {
		
		String storeName = "store";
		String storeUrl = "http://store.lab.fiware.org";
		int score = 5;
		String comment = "Basic comment";
		
		int reviewId = createStoreAndReview(storeName, storeUrl, score, comment);
		Response deleteResponse = deleteStoreReview(USER_NAME, PASSWORD, storeName, reviewId);
		assertThat(deleteResponse.getStatus()).isEqualTo(204);
		
		// Get review should return 404
		Response getResponse = getStoreReview(USER_NAME, PASSWORD, storeName, reviewId);
		assertThat(getResponse.getStatus()).isEqualTo(404);
		
		// Average score should be zero when the last review is deleted
		Store reviewStore = getStore(USER_NAME, PASSWORD, storeName).readEntity(Store.class);
		assertThat(reviewStore.getAverageScore()).isEqualTo(0);
		
	}
	
	private String getCommentFromBase(String baseComment, int score) {
		return baseComment + " " + score;
	}
	
	private double createNReviews(String storeName, String baseComment, int nReviews, int initialScore) {
		
		double totalScore = 0;
		String alphabet = "abcdefghijklmonpqrstuvwxyz";
		
		for (int score = initialScore; score < nReviews + initialScore; score++) {
			
			// Create user for review
			char userSuffix = alphabet.charAt(score % alphabet.length());
			String userName = "userforrating" + userSuffix;
			String email = "rating" + userSuffix + "@example.com";
			Response createUserRes = createUser(userName, email, PASSWORD);
			assertThat(createUserRes.getStatus()).isEqualTo(201);
			
			// Review the offering with the new user (one user can only review one store once)
			String comment = getCommentFromBase(baseComment, score);
			Response res = createStoreReview(userName, PASSWORD, storeName, score, comment);
			assertThat(res.getStatus()).isEqualTo(201);
			
			totalScore += score;
		}

		return totalScore;
	}
	
	@Test
	public void testCreateNReviews() {
		
		String storeName = "store";
		String storeUrl = "http://store.lab.fiware.org";
		String baseComment = "Basic comment";
		
		// Create store
		createStore(USER_NAME, PASSWORD, storeName, storeUrl);
		
		// Create reviews
		int reviewsNumber = 4;
		int initialScore = 1;
		double totalScore = createNReviews(storeName, baseComment, reviewsNumber, initialScore);
		
		// Get average score
		double average = totalScore / ((double) reviewsNumber);
		Store reviewedStore = getStore(USER_NAME, PASSWORD, storeName).readEntity(Store.class);
		assertThat(reviewedStore.getAverageScore()).isEqualTo(average);
		
		// Get reviews list and check values
		Reviews reviews = getStoreReviews(USER_NAME, PASSWORD, storeName, 0, 100, "id", false)
				.readEntity(Reviews.class);
		List<Review> reviewsList = reviews.getReviews();
		assertThat(reviewsList).hasSize((int) reviewsNumber);
		
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
		reviews = getStoreReviews(USER_NAME, PASSWORD, storeName, 1, 2, "score", true)
				.readEntity(Reviews.class);
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
		
		String storeName = "store";
		String storeUrl = "http://store.lab.fiware.org";
		int score = 5;
		String baseComment = "Basic comment";
		
		int reviewId = createStoreAndReview(storeName, storeUrl, score, baseComment);

		// Create additional reviews
		int reviewsNumber = 2;
		double totalScore = createNReviews(storeName, baseComment, reviewsNumber, 1);
		
		// Update initial review
		int newScore = 3;
		String newComment = "This is a new comment";
		Response updateRes = updateStoreReview(USER_NAME, PASSWORD, storeName, 
				reviewId, newScore, newComment);
		assertThat(updateRes.getStatus()).isEqualTo(200);
		
		// Check that the review has been properly updated
		checkStoreReview(USER_NAME, PASSWORD, storeName, reviewId, newScore, newComment);
		
		// Get average score
		double average = (totalScore + newScore) / ((double) (reviewsNumber + 1));
		Store reviewdStore = getStore(USER_NAME, PASSWORD, storeName).readEntity(Store.class);
		assertThat(reviewdStore.getAverageScore()).isEqualTo(average);
		
	}
	
	@Test
	public void testDeleteReviewComplex() {
		
		String storeName = "store";
		String storeUrl = "http://store.lab.fiware.org";
		int score = 5;
		String baseComment = "Basic comment";
		
		int reviewId = createStoreAndReview(storeName, storeUrl, score, baseComment);

		// Create additional reviews
		int reviewsNumber = 2;
		double totalScore = createNReviews(storeName, baseComment, reviewsNumber, 1);
		
		// Delete initial review
		Response deleteRes = deleteStoreReview(USER_NAME, PASSWORD, storeName, reviewId);
		assertThat(deleteRes.getStatus()).isEqualTo(204);
		
		// Get average score
		double average = totalScore / reviewsNumber;
		Store reviewdStore = getStore(USER_NAME, PASSWORD, storeName).readEntity(Store.class);
		assertThat(reviewdStore.getAverageScore()).isEqualTo(average);
		
	}
	
	/**
	 * There was a bug that makes /api/v2/store/ to return several times the same store in the 
	 * same request when the store has been reviewed more than once. This test checks that this
	 * bug is fixed.
	 */
	@Test
	public void testGetAllStoresWhenOneStoresHasBeenReviewedSomeTimes() {
		
		String storeName = "store";
		String storeUrl = "http://store.lab.fiware.org";
		String baseComment = "Basic comment";
		
		// Create store
		createStore(USER_NAME, PASSWORD, storeName, storeUrl);
		
		// Create reviews
		int reviewsNumber = 4;
		int start = 1;
		createNReviews(storeName, baseComment, reviewsNumber, start);
		
		// Get all stores
		Client client = ClientBuilder.newClient();
		Response getAllStoresRes = client.target(endPoint + "/api/v2/store/")
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(USER_NAME, PASSWORD))
				.get();
		assertThat(getAllStoresRes.getStatus()).isEqualTo(200);
		
		// Only one store should be returned
		Stores stores = getAllStoresRes.readEntity(Stores.class);
		assertThat(stores.getStores()).hasSize(1);
	}
	
	@Test
	public void testGetStoresOrderedByScore() {
		
		// storesNames.length must be 4 at the most
		String[] storeNames = new String[]{"AStore", "BStore", "CStore", "DStore"};
		int nStores = storeNames.length;
		
		for (int i = 0; i < storeNames.length; i++) {
			String storeUrl = "http://store" + i + ".com";
			
			// Ensure that name order is opposite to score order
			// AStore -> 1, BStore -> 2,...
			createStoreAndReview(storeNames[i], storeUrl, i + 1, "");
		}
		
		// Get some stores
		Client client = ClientBuilder.newClient();
		Response response = client.target(endPoint + "/api/v2/store/")
				.queryParam("orderBy", "averageScore")	// Order by average score
				.queryParam("desc", true)				// Descending
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(USER_NAME, PASSWORD))
				.get();
		
		// Check the response
		assertThat(response.getStatus()).isEqualTo(200);
		Stores stores = response.readEntity(Stores.class);
		assertThat(stores.getStores().size()).isEqualTo(nStores);
		
		// Users are supposed to be returned in order
		for (int i = 0; i < stores.getStores().size(); i++) {
			Store store = stores.getStores().get(i);
			assertThat(store.getDisplayName()).isEqualTo(storeNames[nStores - i - 1]);
		}
	}
	
	/**
	 * Test if this bug has been fixed:
	 * When a user is deleted, and this used has reviewed one or more entities, the average
	 * score of these entities is not recalculated. In addition, the system throws an 
	 * unexpected exception under certain circumstances.
	 */
	@Test
	public void testDeleteUserWithReviews() {
		
		String storeName = "store";
		String storeUrl = "http://store.lab.fiware.org";
		int firstUserScore = 5;
		String comment = "Basic comment";
		
		int reviewId = createStoreAndReview(storeName, storeUrl, firstUserScore, comment);
		checkStoreReview(USER_NAME, PASSWORD, storeName, reviewId, firstUserScore, comment);

		// Create a new user and review the store with this user
		String userName = "userforrating";
		String email = "rating@example.com";
		Response createUserRes = createUser(userName, email, PASSWORD);
		assertThat(createUserRes.getStatus()).isEqualTo(201);
		
		// Review the store with the new user
		int secondUserScore = 3;
		Response createReviewRes = createStoreReview(userName, PASSWORD, storeName, secondUserScore, comment);
		assertThat(createReviewRes.getStatus()).isEqualTo(201);
		
		// Check the average score
		Response getStoreRes = getStore(USER_NAME, PASSWORD, storeName);
		assertThat(getStoreRes.getStatus()).isEqualTo(200);
		Store store = getStoreRes.readEntity(Store.class);
		double averageScore = (((double) firstUserScore) + ((double) secondUserScore)) / 2D;
		assertThat(store.getAverageScore()).isEqualTo(averageScore);
		
		// Remove the user
		Response deleteUserRes = deleteUser(userName, PASSWORD, userName);
		assertThat(deleteUserRes.getStatus()).isEqualTo(204);
		
		// Get store
		getStoreRes = getStore(USER_NAME, PASSWORD, storeName);
		assertThat(getStoreRes.getStatus()).isEqualTo(200);
		store = getStoreRes.readEntity(Store.class);
		assertThat(store.getAverageScore()).isEqualTo(firstUserScore);
	}
}
