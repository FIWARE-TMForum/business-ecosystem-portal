package org.fiware.apps.marketplace.dao;

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

import java.util.List;

import org.fiware.apps.marketplace.exceptions.StoreNotFoundException;
import org.fiware.apps.marketplace.model.Store;

public interface StoreDao {

	////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////// CRUD ///////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new store in the database
	 * @param store The store to be created
	 */
	public void save(Store store);

	/**
	 * Updates an existing store
	 * @param store The updated store
	 */
	public void update(Store store);

	/**
	 * Deletes an existing store
	 * @param store The store to be deleted
	 */
	public void delete(Store store);

	/**
	 * Returns a store based on its name
	 * @param name The name of the store to be retrieved
	 * @return The store with the given name
	 * @throws StoreNotFoundException If it does not exist an store with the given name
	 */
	public Store findByName(String name) throws StoreNotFoundException;

	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// VERIFICATIONS ///////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Checks if a given name is in use
	 * @param name The name to be checked
	 * @return true if the name is not in use. false otherwise
	 */
	public boolean isNameAvailable(String name);

	/**
	 * Checks if a given display name is in use
	 * @param displayName The display name to be checked
	 * @return true if the display name is not in use. false otherwise
	 */
	public boolean isDisplayNameAvailable(String displayName);

	/**
	 * Checks if a given URL is in use
	 * @param url The URL to be checked
	 * @return true if the URL is not in use. false otherwise
	 */
	public boolean isURLAvailable(String url);


	////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////// LIST ///////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Returns all the stores contained in the database
	 * @return All the stores contained in the database
	 */
	public List<Store> getAllStores();

	/**
	 * Returns a sublist of all the stores contained in the database
	 * @param offset The first store to be retrieved
	 * @param max The max number of stores to be returned
	 * @param orderBy The field that will be used to order the returned stores
	 * @param desc true to sort results in reverse order
	 * @return A sublist of all the stores contained in the database
	 */
	public List<Store> getStoresPage(int offset, int max, String orderBy, boolean desc);

}
