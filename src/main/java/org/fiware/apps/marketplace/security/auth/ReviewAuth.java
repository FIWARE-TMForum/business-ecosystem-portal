package org.fiware.apps.marketplace.security.auth;

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

import java.util.HashSet;
import java.util.Set;

import org.fiware.apps.marketplace.exceptions.UserNotFoundException;
import org.fiware.apps.marketplace.model.Review;
import org.fiware.apps.marketplace.model.ReviewableEntity;
import org.fiware.apps.marketplace.model.User;
import org.springframework.stereotype.Service;

@Service("ratingAuth")
public class ReviewAuth extends AbstractAuth<Review>{
	
	@Override
	protected User getEntityOwner(Review rating) {
		return rating.getUser();
	}

	@Override
	public boolean canCreate(Review entity) {
		// This method cannot be used
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Checks if a given user can review a given entity
	 * @param entity The entity that is going to be reviewed
	 * @param review The review itself
	 * @return true if the current user has not reviewed the entity yet. false otherwise
	 */
	public boolean canCreate(ReviewableEntity entity, Review review) {
		
		boolean canCreate = false;
		
		try {
			
			// Check if the user has created another review for the same entity
			Set<User> users = new HashSet<>();
			
			for (Review previousReviews: entity.getReviews()) {
				users.add(previousReviews.getUser());
			}
			
			if (!users.contains(getUserBo().getCurrentUser())) {
				canCreate = true;
			}
			
		} catch (UserNotFoundException ex) {
			// Nothing to do...
		}
		
		return canCreate;
	}
	
}
