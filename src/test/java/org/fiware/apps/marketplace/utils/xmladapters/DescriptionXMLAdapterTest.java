package org.fiware.apps.marketplace.utils.xmladapters;

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

import static org.assertj.core.api.Assertions.assertThat;

import org.fiware.apps.marketplace.model.Description;
import org.fiware.apps.marketplace.model.MinifiedDescription;
import org.fiware.apps.marketplace.model.Store;
import org.junit.Test;


public class DescriptionXMLAdapterTest {
	
	private DescriptionXMLAdapter adapter = new DescriptionXMLAdapter();
	
	@Test
	public void testUnmarshall() throws Exception {
		
		Store store = new Store();
		store.setName("store");
		
		MinifiedDescription minDescription = new MinifiedDescription();
		minDescription.setName("description");
		minDescription.setStore(store);
		
		Description description = adapter.unmarshal(minDescription);
		assertThat(description.getName()).isEqualTo(minDescription.getName());
		assertThat(description.getStore().getName()).isEqualTo(store.getName());
	}
	
	@Test
	public void testMarshallValidValue() throws Exception {
		Store store = new Store();
		store.setName("store");
		
		Description description = new Description();
		description.setName("description");
		description.setStore(store);
		
		MinifiedDescription minDescription = adapter.marshal(description);
		assertThat(minDescription.getName()).isEqualTo(description.getName());
		assertThat(minDescription.getStore().getName()).isEqualTo(store.getName());
	}
	

}
