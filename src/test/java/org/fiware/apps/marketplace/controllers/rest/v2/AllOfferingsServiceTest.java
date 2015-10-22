package org.fiware.apps.marketplace.controllers.rest.v2;

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
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.List;

import javax.ws.rs.core.Response;

import org.fiware.apps.marketplace.bo.OfferingBo;
import org.fiware.apps.marketplace.exceptions.NotAuthorizedException;
import org.fiware.apps.marketplace.model.ErrorType;
import org.fiware.apps.marketplace.model.Offering;
import org.fiware.apps.marketplace.model.Offerings;
import org.fiware.apps.marketplace.model.User;
import org.hibernate.QueryException;
import org.hibernate.exception.SQLGrammarException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AllOfferingsServiceTest {
	
	@Mock private OfferingBo offeringBoMock;
	@InjectMocks private AllOfferingsService allOfferingsService;
	
	private static final String OFFSET_MAX_INVALID = "offset and/or max are not valid";
	
	@Before 
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testListAllOfferingNotAllowed() throws NotAuthorizedException {
		String userName = "example-user";
		
		// Mocks
		User user = mock(User.class);
		when(user.getUserName()).thenReturn(userName);
		Exception e = new NotAuthorizedException("list offerings");
		doThrow(e).when(offeringBoMock).getOfferingsPage(anyInt(), anyInt(), anyString(), anyBoolean());

		// Call the method
		Response res = allOfferingsService.listOfferings(0, 100, "name", false);

		// Assertions
		GenericRestTestUtils.checkAPIError(res, 403, ErrorType.FORBIDDEN, 
				e.getMessage());
	}
	
	private void testListAllOfferingsInvalidParams(int offset, int max, String orderBy, boolean desc) {
		// Call the method
		Response res = allOfferingsService.listOfferings(offset, max, orderBy, desc);

		// Assertions
		GenericRestTestUtils.checkAPIError(res, 400, ErrorType.BAD_REQUEST, 
				String.format(OFFSET_MAX_INVALID, offset, max));
	}
	
	@Test
	public void testListAllOfferingsInvalidOffset() {
		testListAllOfferingsInvalidParams(-1, 100, "name", true);
	}
	
	@Test
	public void testListAllOfferingsInvalidMax() {
		testListAllOfferingsInvalidParams(0, -1, "name", true);
	}
	
	@Test
	public void testListAllOfferingsInvalidOffsetMax() {
		testListAllOfferingsInvalidParams(-1, -1, "name", true);
	}
	
	@Test
	public void testListAllOfferingsGetNoErrors() throws NotAuthorizedException {
		@SuppressWarnings("unchecked")
		List<Offering> oferrings = mock(List.class);
		
		// Mocks
		when(offeringBoMock.getOfferingsPage(anyInt(), anyInt(), anyString(), anyBoolean())).
				thenReturn(oferrings);
		
		// Call the method
		int offset = 0;
		int max = 100;
		String orderBy = "name";
		boolean desc = true;
		Response res = allOfferingsService.listOfferings(offset, max, orderBy, desc);
		
		// Verify
		verify(offeringBoMock).getOfferingsPage(offset, max, orderBy, desc);
		
		// Assertions
		assertThat(res.getStatus()).isEqualTo(200);
		assertThat(((Offerings) res.getEntity()).
				getOfferings()).isEqualTo(oferrings);
	}
	
	@Test
	public void testListBookmarkedOfferingsGetNoErrors() throws NotAuthorizedException {
		@SuppressWarnings("unchecked")
		List<Offering> oferrings = mock(List.class);
		
		// Mocks
		when(offeringBoMock.getBookmarkedOfferingsPage(anyInt(), anyInt(), anyString(), anyBoolean())).
				thenReturn(oferrings);
		
		// Call the method
		int offset = 0;
		int max = 100;
		String orderBy = "averageScore";
		boolean desc = false;
		Response res = allOfferingsService.listBookmarked(offset, max, orderBy, desc);
		
		// Verify
		verify(offeringBoMock).getBookmarkedOfferingsPage(offset, max, orderBy, desc);
		
		// Assertions
		assertThat(res.getStatus()).isEqualTo(200);
		assertThat(((Offerings) res.getEntity()).
				getOfferings()).isEqualTo(oferrings);
	}
	
	@Test
	public void testListAllOfferingsException() throws NotAuthorizedException {
		// Mocks
		String exceptionMsg = "exception";
		doThrow(new RuntimeException("", new Exception(exceptionMsg)))
				.when(offeringBoMock).getOfferingsPage(anyInt(), anyInt(), anyString(), anyBoolean());

		// Call the method
		int offset = 0;
		int max = 100;
		String orderBy = "describedIn.registrationDate";
		boolean desc = false;
		Response res = allOfferingsService.listOfferings(offset, max, orderBy, desc);
		
		// Verify
		verify(offeringBoMock).getOfferingsPage(offset, max, orderBy, desc);
		
		// Check exception
		GenericRestTestUtils.checkAPIError(res, 500, ErrorType.INTERNAL_SERVER_ERROR, exceptionMsg);
	}
	
	@Test
	public void testListBookmarkedOfferingsException() throws NotAuthorizedException {
		// Mocks
		String exceptionMsg = "exception";
		doThrow(new RuntimeException("", new Exception(exceptionMsg)))
				.when(offeringBoMock).getBookmarkedOfferingsPage(anyInt(), anyInt(), anyString(), anyBoolean());

		// Call the method
		int offset = 0;
		int max = 100;
		String orderBy = "name";
		boolean desc = true;
		Response res = allOfferingsService.listBookmarked(offset, max, orderBy, desc);
		
		// Verify
		verify(offeringBoMock).getBookmarkedOfferingsPage(offset, max, orderBy, desc);
		
		// Check exception
		GenericRestTestUtils.checkAPIError(res, 500, ErrorType.INTERNAL_SERVER_ERROR, exceptionMsg);
	}
	
	// Exceptions that can be thrown when orderBy is invalid
	private void testListSQLException(Exception exception) throws NotAuthorizedException {
		// Mocks
		doThrow(exception).when(offeringBoMock).getOfferingsPage(anyInt(), anyInt(), anyString(), anyBoolean());

		// Call the method
		int offset = 0;
		int max = 100;
		String orderBy = "name";
		boolean desc = true;
		Response res = allOfferingsService.listOfferings(offset, max, orderBy, desc);
		
		// Verify
		verify(offeringBoMock).getOfferingsPage(offset, max, orderBy, desc);
		
		// Check exception
		GenericRestTestUtils.checkAPIError(res, 400, ErrorType.BAD_REQUEST, 
				"Offerings cannot be ordered by " + orderBy + ".");
	}
	
	@Test
	public void testListSQLGrammarException() throws NotAuthorizedException {
		testListSQLException(new SQLGrammarException("", new SQLException()));
	}
	
	@Test
	public void testListQueryException() throws NotAuthorizedException {
		testListSQLException(new QueryException(""));
	}
	
	private void testListLastViewedOfferingsInvalidParams(int offset, int max) {
		// Call the method
		Response res = allOfferingsService.lastVisited(offset, max);

		// Assertions
		GenericRestTestUtils.checkAPIError(res, 400, ErrorType.BAD_REQUEST, 
				String.format(OFFSET_MAX_INVALID, offset, max));
	}
	
	@Test
	public void testListLastViewedOfferingsInvalidOffset() {
		testListLastViewedOfferingsInvalidParams(-1, 100);
	}
	
	@Test
	public void testListLastViewedOfferingsInvalidMax() {
		testListLastViewedOfferingsInvalidParams(0, -1);
	}
	
	@Test
	public void testListLastViewedOfferingsInvalidOffsetMax() {
		testListLastViewedOfferingsInvalidParams(-1, -1);
	}
	
	@Test
	public void testListLastViewedNotAllowed() throws NotAuthorizedException {
		String userName = "example-user";
		
		// Mocks
		User user = mock(User.class);
		when(user.getUserName()).thenReturn(userName);
		Exception e = new NotAuthorizedException("list offerings");
		doThrow(e).when(offeringBoMock).getLastViewedOfferingsPage(anyInt(), anyInt());

		// Call the method
		Response res = allOfferingsService.lastVisited(0, 100);

		// Assertions
		GenericRestTestUtils.checkAPIError(res, 403, ErrorType.FORBIDDEN, 
				e.getMessage());

	}
	
	@Test
	public void testListLastViewedGetNoErrors() throws NotAuthorizedException {
		@SuppressWarnings("unchecked")
		List<Offering> oferrings = mock(List.class);
		
		// Mocks
		when(offeringBoMock.getLastViewedOfferingsPage(anyInt(), anyInt())).
				thenReturn(oferrings);
		
		// Call the method
		int offset = 0;
		int max = 100;
		Response res = allOfferingsService.lastVisited(offset, max);
		
		// Verify
		verify(offeringBoMock).getLastViewedOfferingsPage(offset, max);
		
		// Assertions
		assertThat(res.getStatus()).isEqualTo(200);
		assertThat(((Offerings) res.getEntity()).
				getOfferings()).isEqualTo(oferrings);
	}
		
	@Test
	public void testViewedByOthersInvalidMax() {
		// Call the method
		Response res = allOfferingsService.viewedByOthers(0);

		// Assertions
		GenericRestTestUtils.checkAPIError(res, 400, ErrorType.BAD_REQUEST, "max is not valid");
	}
		
	@Test
	public void testViewedByOthersdNotAllowed() throws NotAuthorizedException {
		String userName = "example-user";
		
		// Mocks
		User user = mock(User.class);
		when(user.getUserName()).thenReturn(userName);
		Exception e = new NotAuthorizedException("list offerings");
		doThrow(e).when(offeringBoMock).getOfferingsViewedByOtherUsers(anyInt());

		// Call the method
		Response res = allOfferingsService.viewedByOthers(20);

		// Assertions
		GenericRestTestUtils.checkAPIError(res, 403, ErrorType.FORBIDDEN, 
				e.getMessage());

	}
	
	@Test
	public void testViewedByOthersGetNoErrors() throws NotAuthorizedException {
		@SuppressWarnings("unchecked")
		List<Offering> oferrings = mock(List.class);
		
		// Mocks
		when(offeringBoMock.getOfferingsViewedByOtherUsers(anyInt())).
				thenReturn(oferrings);
		
		// Call the method
		int max = 20;
		Response res = allOfferingsService.viewedByOthers(max);
		
		// Verify
		verify(offeringBoMock).getOfferingsViewedByOtherUsers(max);
		
		// Assertions
		assertThat(res.getStatus()).isEqualTo(200);
		assertThat(((Offerings) res.getEntity()).getOfferings()).isEqualTo(oferrings);
	}


}
