package org.fiware.apps.marketplace.controllers.web;

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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response.Status;

import org.fiware.apps.marketplace.bo.DescriptionBo;
import org.fiware.apps.marketplace.bo.StoreBo;
import org.fiware.apps.marketplace.bo.UserBo;
import org.fiware.apps.marketplace.exceptions.UserNotFoundException;
import org.fiware.apps.marketplace.exceptions.ValidationException;
import org.fiware.apps.marketplace.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;


public abstract class AbstractController {

	@Autowired private UserBo userBo;
	@Autowired private StoreBo storeBo;
	@Autowired private DescriptionBo descriptionBo;

	private String applicationName = "TM Forum Portal";

	protected ModelAndView buildErrorView(Status status, String content) {

		ModelMap model = new ModelMap();

		// Add the view title
		model.addAttribute("title", getContextName());

		// Add the HTTP status
		model.addAttribute("statusCode", status.getStatusCode());
		model.addAttribute("reasonPhrase", status.getReasonPhrase());

		// Add the truly reason
		model.addAttribute("content", content);

		return new ModelAndView("core.error", model);
	}

	protected String getContextName() {
		return applicationName;
	}

	protected User getCurrentUser() throws UserNotFoundException {
		return userBo.getCurrentUser();
	}

	protected UserBo getUserBo() {
		return userBo;
	}

	protected StoreBo getStoreBo() {
		return storeBo;
	}

	protected DescriptionBo getDescriptionBo() {
		return descriptionBo;
	}

	protected void checkPasswordConfirmation(String password, String passwordConfirm) throws ValidationException {
		if (!password.equals(passwordConfirm)) {
			throw new ValidationException("passwordConfirm", "Passwords do not match.");
		}
	}

	protected void addFlashMessage(HttpServletRequest request, ModelMap model) {
		HttpSession session = request.getSession();

		synchronized (session) {
			String flashMessage = (String) session.getAttribute("flashMessage");

			if (flashMessage != null) {
				model.addAttribute("message", flashMessage);
				session.removeAttribute("flashMessage");
			}
		}
	}

	protected void setFlashMessage(HttpServletRequest request, String message) {
		HttpSession session = request.getSession();

		synchronized (session) {
			session.setAttribute("flashMessage", message);
		}
	}

}
