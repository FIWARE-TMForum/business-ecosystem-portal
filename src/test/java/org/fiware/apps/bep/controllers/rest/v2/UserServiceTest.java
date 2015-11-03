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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.fiware.apps.bep.bo.UserBo;
import org.fiware.apps.bep.controllers.rest.v2.UserService;
import org.fiware.apps.bep.exceptions.NotAuthorizedException;
import org.fiware.apps.bep.exceptions.UserNotFoundException;
import org.fiware.apps.bep.exceptions.ValidationException;
import org.fiware.apps.bep.model.ErrorType;
import org.fiware.apps.bep.model.User;
import org.fiware.apps.bep.model.Users;
import org.hibernate.HibernateException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


public class UserServiceTest {

	@Mock private UserBo userBoMock;
	@InjectMocks private UserService userRegistrationService;

	// User to be created...
	private User user;
	
	// URI info
	private UriInfo uri;

	// Other useful constants
	private static final String OFFSET_MAX_INVALID = "offset and/or max are not valid";
	private static final String VALIDATION_ERROR = "Validation Error";
	private static final String USER_NAME = "example-name";
	private static final String PASSWORD = "12345678";
	private static final String EMAIL = "example@example.com";
	private static final String COMPANY = "Example";
	private static final String DISPLAY_NAME = "Example Name";
	private static final String PATH = "/api/user";
	
	private static final ConstraintViolationException VIOLATION_EXCEPTION = 
			new ConstraintViolationException("", new SQLException(), "");

	///////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////// BASIC METHODS ////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////

	@Before 
	public void setUp() throws UserNotFoundException {
		MockitoAnnotations.initMocks(this);
		
		User loggedUser = mock(User.class);
		when(loggedUser.getUserName()).thenReturn(USER_NAME);
		when(userBoMock.getCurrentUser()).thenReturn(loggedUser);
	}

	@Before
	public void generateValidUser() {
		user = new User();
		user.setPassword(PASSWORD);
		user.setEmail(EMAIL);
		user.setCompany(COMPANY);
		user.setDisplayName(DISPLAY_NAME);
	}
	
	@Before
	public void setUpUri() {
		uri = mock(UriInfo.class);
		when(uri.getPath()).thenReturn(PATH);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////// CREATE ///////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////

	@Test
	public void testCreateUserNotAllowed() throws Exception {
		// Mocks
		Exception e = new NotAuthorizedException("create user");
		doThrow(e).when(userBoMock).save(user);

		// Call the method
		Response res = userRegistrationService.createUser(uri, user);

		// Assertions
		GenericRestTestUtils.checkAPIError(res, 403, ErrorType.FORBIDDEN, e.getMessage());

		// Verify mocks
		verify(userBoMock).save(user);

	}

	@Test
	public void testCreateUserNoErrors() throws Exception {
		// Mocks
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				invocation.getArgumentAt(0, User.class).setUserName(USER_NAME);
				return null;
			}
		}).when(userBoMock).save(user);

		//Call the method
		Response res = userRegistrationService.createUser(uri, user);

		// Verify mocks
		verify(userBoMock).save(user);
		
		// Check
		assertThat(res.getStatus()).isEqualTo(201);
		assertThat(res.getHeaders().get("Location").get(0).toString()).isEqualTo(PATH + "/" + USER_NAME);
		// Done by the BO
		// assertThat(user.getRegistrationDate()).isNotNull();
		assertThat(user.getUserName()).isEqualTo(USER_NAME);
		assertThat(user.getDisplayName()).isEqualTo(DISPLAY_NAME);
		assertThat(user.getEmail()).isEqualTo(EMAIL);
		assertThat(user.getCompany()).isEqualTo(COMPANY);
		assertThat(user.getPassword()).isEqualTo(PASSWORD);
	}
	
	@Test
	public void testCreateUserValidationError() throws Exception {
		// Mock
		String field = "afield";
		doThrow(new ValidationException(field, VALIDATION_ERROR)).when(userBoMock).save(user);

		// Call the method
		Response res = userRegistrationService.createUser(uri, user);

		// Check
		verify(userBoMock).save(user);
		GenericRestTestUtils.checkAPIError(res, 400, ErrorType.VALIDATION_ERROR, VALIDATION_ERROR, field);
	}
	
	private void testCreateUserHibernateException(Exception ex, String message)  throws Exception {
		// Mock
		doThrow(ex).when(userBoMock).save(isA(User.class));

		// Call the method
		Response res = userRegistrationService.createUser(uri, user);

		// Checks
		verify(userBoMock).save(user);
		GenericRestTestUtils.checkAPIError(res, 400, ErrorType.BAD_REQUEST, message);
	}

	@Test
	public void testCreateUserAlreadyExists() throws Exception {
		testCreateUserHibernateException(VIOLATION_EXCEPTION, 
				"The user and/or the email introduced are already registered in the system");
	}
	
	@Test
	public void testCreateUserOtherDataException() throws Exception {
		Exception exception = new HibernateException(new Exception("too much content"));
		testCreateUserHibernateException(exception, exception.getCause().getMessage());
	}
	
	@Test
	public void testCreteUpdateNotKnowException() throws Exception {
		// Mock
		String exceptionMsg = "SERVER ERROR";
		doThrow(new RuntimeException("", new Exception(exceptionMsg))).when(userBoMock).save(user);

		// Call the method
		Response res = userRegistrationService.createUser(uri, user);

		// Checks
		verify(userBoMock).save(user);
		GenericRestTestUtils.checkAPIError(res, 500, ErrorType.INTERNAL_SERVER_ERROR, exceptionMsg);
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////// UPDATE ///////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////

	@Test
	public void testUpdateUserNotAllowed() throws Exception {
		// Mocks
		when(userBoMock.findByName(USER_NAME)).thenReturn(user);
		Exception e = new NotAuthorizedException("update user");
		doThrow(e).when(userBoMock).update(USER_NAME, user);

		// Call the method
		Response res = userRegistrationService.updateUser(USER_NAME, user);

		// Assertions
		GenericRestTestUtils.checkAPIError(res, 403, ErrorType.FORBIDDEN, e.getMessage());

		// Verify mocks
		verify(userBoMock).update(USER_NAME, user);
	}
	
	@Test
	public void testUpdateUserValidationError() 
			throws ValidationException, UserNotFoundException, NotAuthorizedException {
		
		String field = "aField";
		
		// Mock
		doThrow(new ValidationException(field, VALIDATION_ERROR)).when(userBoMock).update(USER_NAME, user);

		// Call the method
		Response res = userRegistrationService.updateUser(USER_NAME, user);

		// Check
		verify(userBoMock).update(USER_NAME, user);
		
		//checkMocksUpdateNotCalled();
		GenericRestTestUtils.checkAPIError(res, 400, ErrorType.VALIDATION_ERROR, VALIDATION_ERROR, field);
	}
	
	
	@Test
	public void testUpdateUserNonExistingUser() throws Exception {
		String userNotFoundMsg = "user_name does not exist";
		
		// Mock
		doThrow(new UserNotFoundException(userNotFoundMsg))
				.when(userBoMock).update(USER_NAME, user);
		
		// Call the method
		Response res = userRegistrationService.updateUser(USER_NAME, user);
		
		// Checks
		verify(userBoMock).update(USER_NAME, user);		
		GenericRestTestUtils.checkAPIError(res, 404, ErrorType.NOT_FOUND, userNotFoundMsg);
	}
	
	private void testUpdateUserHibernateException(Exception ex, String message)  {
		
		// Mock
		try {
			doThrow(ex).when(userBoMock).update(USER_NAME, user);
		} catch (Exception e) {
			fail("Exception not expected", e);
		}

		// Call the method
		Response res = userRegistrationService.updateUser(USER_NAME, user);

		// Checks
		try {
			verify(userBoMock).update(USER_NAME, user);
			GenericRestTestUtils.checkAPIError(res, 400, ErrorType.BAD_REQUEST, message);
		} catch (Exception e) {
			// It isn't going to happen
		}
		

	}
	
	@Test
	public void testUpdateUserViolationIntegration() throws ValidationException {
		testUpdateUserHibernateException(VIOLATION_EXCEPTION, 
				"The user and/or the email introduced are already registered in the system");
	}
	
	@Test
	public void testUpdateUserOtherDataException() throws ValidationException {
		HibernateException exception = new HibernateException(new Exception("Too much content"));
		testUpdateUserHibernateException(exception, exception.getCause().getMessage());
	}
	
	@Test
	public void testUpdateUserNotKnowException() throws Exception {
		// Mock
		String exceptionMsg = "SERVER ERROR";
		doThrow(new RuntimeException("", new Exception(exceptionMsg))).when(userBoMock).update(USER_NAME, user);
		
		// Call the method
		Response res = userRegistrationService.updateUser(USER_NAME, user);

		// Checks
		verify(userBoMock).update(USER_NAME, user);
		GenericRestTestUtils.checkAPIError(res, 500, ErrorType.INTERNAL_SERVER_ERROR, exceptionMsg);
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////// DELETE ///////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	
	private void testDeleteException(String userName, int statusCode, ErrorType type, String message) {
		// Call the method
		Response res = userRegistrationService.deleteUser(userName);

		// Assertions
		GenericRestTestUtils.checkAPIError(res, statusCode, type, message);

		// Verify mocks
		try {
			verify(userBoMock).delete(userName);
		} catch (Exception ex) {
			fail("Exception not expected", ex);
		}

	}
	
	@Test
	public void testDeleteUserNotAllowed() throws Exception {
		// Mocks
		when(userBoMock.findByName(USER_NAME)).thenReturn(user);
		Exception e = new NotAuthorizedException("delete user");
		doThrow(e).when(userBoMock).delete(USER_NAME);
		
		testDeleteException(USER_NAME, 403, ErrorType.FORBIDDEN, e.getMessage());
	}
	
	@Test
	public void testDeleteUserNotExisting() throws Exception {
		// Configure Mocks
		String msg = "User user_name not found";
		doThrow(new UserNotFoundException(msg)).when(userBoMock).delete(USER_NAME);
		
		testDeleteException(USER_NAME, 404, ErrorType.NOT_FOUND, msg);
	}
	
	@Test
	public void testDeleteUserException() throws Exception {
		// Configure Mocks
		String exceptionMsg = "DB is down!";
		doThrow(new RuntimeException("", new Exception(exceptionMsg))).when(userBoMock).delete(USER_NAME);

		testDeleteException(USER_NAME, 500, ErrorType.INTERNAL_SERVER_ERROR, exceptionMsg);
	}
	
	@Test
	public void testDeleteUserNoErrors() throws Exception {
		// Call the method
		Response res = userRegistrationService.deleteUser(USER_NAME);

		// Assertions
		assertThat(res.getStatus()).isEqualTo(204);
		
		// Verify mocks
		verify(userBoMock).delete(USER_NAME);
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////// FIND ////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////

	@Test
	public void testGetUserNotAllowed() throws Exception {
		// Mocks
		when(userBoMock.findByName(USER_NAME)).thenReturn(user);
		Exception e = new NotAuthorizedException("get user");
		doThrow(e).when(userBoMock).findByName(USER_NAME);

		// Call the method
		Response res = userRegistrationService.getUser(USER_NAME);

		// Assertions
		GenericRestTestUtils.checkAPIError(res, 403, ErrorType.FORBIDDEN, e.getMessage());
	}
	
	@Test
	public void testGetUserNoErrors() throws Exception {
		// Mocks
		when(userBoMock.findByName(USER_NAME)).thenReturn(user);

		// Call the method
		Response res = userRegistrationService.getUser(USER_NAME);

		// Assertions
		assertThat(res.getStatus()).isEqualTo(200);
		assertThat((User) res.getEntity()).isEqualTo(user);
	}
	
	@Test
	public void testGetUserUserNotFound() throws Exception {
		// Mocks
		String msg = "User user_name not found";
		doThrow(new UserNotFoundException(msg)).when(userBoMock).findByName(USER_NAME);
		
		// Call the method
		Response res = userRegistrationService.getUser(USER_NAME);
		
		// Assertions
		GenericRestTestUtils.checkAPIError(res, 404, ErrorType.NOT_FOUND, msg);
	}
	
	@Test
	public void testGetUserException() throws Exception {
		// Mocks
		String exceptionMsg = "DB is down!";
		doThrow(new RuntimeException("", new Exception(exceptionMsg)))
				.when(userBoMock).findByName(USER_NAME);

		// Call the method
		Response res = userRegistrationService.getUser(USER_NAME);

		// Assertions
		GenericRestTestUtils.checkAPIError(res, 500, ErrorType.INTERNAL_SERVER_ERROR, exceptionMsg);
		
		// Verify
		verify(userBoMock).findByName(USER_NAME);
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////// LIST ////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	
	@Test
	public void testListUsersNotAllowed() throws NotAuthorizedException {
		// Mocks
		Exception e = new NotAuthorizedException("list users");
		doThrow(e).when(userBoMock).getUsersPage(anyInt(), anyInt());

		// Call the method
		Response res = userRegistrationService.listUsers(0, 100);

		// Assertions
		GenericRestTestUtils.checkAPIError(res, 403, ErrorType.FORBIDDEN, e.getMessage());
	}
	
	private void testListUsersInvalidParams(int offset, int max) {
		// Call the method
		Response res = userRegistrationService.listUsers(offset, max);

		// Assertions
		GenericRestTestUtils.checkAPIError(res, 400, ErrorType.BAD_REQUEST, OFFSET_MAX_INVALID);
	}
	
	@Test
	public void testListUsersInvalidOffset() {
		testListUsersInvalidParams(-1, 100);
	}
	
	@Test
	public void testListUsersInvalidMax() {
		testListUsersInvalidParams(0, -1);
	}
	
	@Test
	public void testListUsersInvalidOffsetMax() {
		testListUsersInvalidParams(-1, -1);
	}
	
	@Test
	public void testListUsersGetNoErrors() throws NotAuthorizedException {
		List<User> users = new ArrayList<User>();
		for (int i = 0; i < 3; i++) {
			User user = new User();
			user.setId(i);
			users.add(user);
		}
		
		// Mocks
		when(userBoMock.getUsersPage(anyInt(), anyInt())).thenReturn(users);
		
		// Call the method
		int offset = 0;
		int max = 100;
		Response res = userRegistrationService.listUsers(offset, max);
		
		// Verify
		verify(userBoMock).getUsersPage(offset, max);
		
		// Assertations
		assertThat(res.getStatus()).isEqualTo(200);
		assertThat(((Users) res.getEntity()).getUsers()).isEqualTo(users);
	}
	
	@Test
	public void testListUsersException() throws NotAuthorizedException {
		// Mocks
		String exceptionMsg = "exception";
		doThrow(new RuntimeException("", new Exception(exceptionMsg)))
				.when(userBoMock).getUsersPage(anyInt(), anyInt());

		// Call the method
		int offset = 0;
		int max = 100;
		Response res = userRegistrationService.listUsers(offset, max);
		
		// Verify
		verify(userBoMock).getUsersPage(offset, max);
		
		// Check exception
		GenericRestTestUtils.checkAPIError(res, 500, ErrorType.INTERNAL_SERVER_ERROR, exceptionMsg);
	}
	
}
