package org.fiware.apps.marketplace.helpers;

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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fiware.apps.marketplace.bo.CategoryBo;
import org.fiware.apps.marketplace.bo.ServiceBo;
import org.fiware.apps.marketplace.exceptions.CategoryNotFoundException;
import org.fiware.apps.marketplace.exceptions.ParseException;
import org.fiware.apps.marketplace.exceptions.ServiceNotFoundException;
import org.fiware.apps.marketplace.model.Category;
import org.fiware.apps.marketplace.model.Description;
import org.fiware.apps.marketplace.model.Offering;
import org.fiware.apps.marketplace.model.PriceComponent;
import org.fiware.apps.marketplace.model.PricePlan;
import org.fiware.apps.marketplace.model.Service;
import org.fiware.apps.marketplace.rdf.RdfHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.hp.hpl.jena.shared.JenaException;

public class OfferingResolverTest {

	private final static String DESCRIPTION_URI = "http://127.0.0.1:7777/usdl.rdf";

	@Mock private CategoryBo classificatioBoMock;
	@Mock private ServiceBo serviceBoMock;
	@Mock private RdfHelper rdfHelperMock;
	@InjectMocks private OfferingResolver offeringResolver;

	@Before 
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		this.offeringResolver = spy(offeringResolver);
		
		try {
			doReturn(rdfHelperMock).when(offeringResolver).getRdfHelper(any(Description.class));
		} catch (IOException e) {
			// Exception not expected
		}
	}

	private void checkOfferingBasic(Offering offering, String offeringName, String title,
			String description, String version, String imageUrl, String acquisitionUrl, String uri,
			Description describedIn) {

		assertThat(offering.getName()).isEqualTo(offeringName);
		assertThat(offering.getDisplayName()).isEqualTo(title);
		assertThat(offering.getDescription()).isEqualTo(description);
		assertThat(offering.getImageUrl()).isEqualTo(imageUrl);
		assertThat(offering.getVersion()).isEqualTo(version);
		assertThat(offering.getAcquisitionUrl()).isEqualTo(acquisitionUrl);
		assertThat(offering.getUri()).isEqualTo(uri);
		assertThat(offering.getDescribedIn()).isEqualTo(describedIn);
	}

	private void checkPricePlan(PricePlan pricePlan, String title, String description) {
		assertThat(pricePlan.getTitle()).isEqualTo(title);
		assertThat(pricePlan.getComment()).isEqualTo(description);
	}

	private void checkService(Service service, String title, String description) {
		assertThat(service.getDisplayName()).isEqualTo(title);
		assertThat(service.getComment()).isEqualTo(description);
	}

	private void checkPricePlanInSet(Set<PricePlan> pricePlans, String title, String description) {

		boolean found = false;
		Iterator<PricePlan> iterator = pricePlans.iterator();

		while (!found && iterator.hasNext()) {
			PricePlan pricePlan = iterator.next();

			if (pricePlan.getTitle().equals(title) && pricePlan.getComment().equals(description)) {
				found = true;
			}
		}

		assertThat(found).isTrue();
	}

	private void checkServiceInSet(Set<Service> services, String displayName, String description) {
		boolean found = false;
		Iterator<Service> iterator = services.iterator();

		while (!found && iterator.hasNext()) {
			Service service = iterator.next();

			if (service.getDisplayName().equals(displayName) && service.getComment().equals(description)) {
				found = true;
			}
		}

		assertThat(found).isTrue();
	}

	private void checkPriceComponent(PriceComponent priceComponent, String currency, String title, String unit,
			float value) {
		assertThat(priceComponent.getCurrency()).isEqualTo(currency);
		assertThat(priceComponent.getTitle()).isEqualTo(title);
		assertThat(priceComponent.getUnit()).isEqualTo(unit);
		assertThat(priceComponent.getValue()).isEqualTo(value);
	}

	private void initOffering(String uri, String title, String description, String version, String imageUrl,
			String acquisitionUrl) {

		String contextUri = "<" + uri + ">";
		List<String> offeringsUris = rdfHelperMock.queryUris("SELECT ?x WHERE { ?x a usdl:ServiceOffering . } ", "x");

		// Initialize offeringsUris (in case they are not) and add the new URI...
		offeringsUris = offeringsUris == null ? new ArrayList<String>() : offeringsUris;
		offeringsUris.add(contextUri);

		// Update mock
		when(rdfHelperMock.queryUris("SELECT ?x WHERE { ?x a usdl:ServiceOffering . } ", "x"))
				.thenReturn(offeringsUris);

		// Mock values for this offering
		when(rdfHelperMock.getLiteral(contextUri, "dcterms:title")).thenReturn(title);
		when(rdfHelperMock.getLiteral(contextUri, "dcterms:description")).thenReturn(description);
		when(rdfHelperMock.getLiteral(contextUri, "pav:version")).thenReturn(version);
		when(rdfHelperMock.getObjectUri(contextUri, "foaf:depiction")).thenReturn("<" + imageUrl + ">");
		when(rdfHelperMock.getObjectUri(contextUri, "gr:availableDeliveryMethods")).thenReturn(
				"<" + acquisitionUrl + ">");

	}
	
	private void addPricePlanToMap(String offeringUri, Map<String, List<Object>> pricePlan) {
		
		String contextOfferingUri = "<" + offeringUri + ">";
		List<Map<String, List<Object>>> offeringPricePlans = rdfHelperMock.getBlankNodesProperties(
				contextOfferingUri, "usdl:hasPricePlan");
		
		// Initialize pricePlansUris (in case they are not) and add the new URI...
		offeringPricePlans = offeringPricePlans == null ? 
				new ArrayList<Map<String, List<Object>>>() : offeringPricePlans;

		// Include the price plan
		offeringPricePlans.add(pricePlan);
		
		when(rdfHelperMock.getBlankNodesProperties(contextOfferingUri, "usdl:hasPricePlan"))
			.thenReturn(offeringPricePlans);

		
	}

	private void initPricePlan(String offeringUri, String title, String description, 
			List<PriceComponent> priceComponents) {

		String priceComponentPredicate = "hasPriceComponent";
		
		// Include price plan
		List<Object> titles = new ArrayList<>();
		titles.add(title);
		List<Object> descriptions = new ArrayList<>();
		descriptions.add(description);
		
		Map<String, List<Object>> pricePlan = new HashMap<>();
		pricePlan.put("title", titles);
		pricePlan.put("description", descriptions);
		
		// Initial list of price components (empty)
		List<Object> priceComponentsList = new ArrayList<>();
		pricePlan.put(priceComponentPredicate, priceComponentsList);
		
		for (PriceComponent priceComponent: priceComponents) {
			Map<String, List<Object>> priceComponentMap = new HashMap<>();
			
			// Title & Description
			List<Object> pcTitles = new ArrayList<>();
			pcTitles.add(priceComponent.getTitle());
			priceComponentMap.put("label", pcTitles);
			
			List<Object> pcDescriptions = new ArrayList<>();
			pcTitles.add(priceComponent.getComment());
			priceComponentMap.put("description", pcDescriptions);
			
			// Price
			Map<String, List<Object>> price = new HashMap<>();
			
			List<Object> currencies = new ArrayList<>();
			currencies.add(priceComponent.getCurrency());
			price.put("hasCurrency", currencies);
			
			List<Object> unitOfMeasurements = new ArrayList<>();
			unitOfMeasurements.add(priceComponent.getUnit());
			price.put("hasUnitOfMeasurement", unitOfMeasurements);
			
			List<Object> currenciesValues = new ArrayList<>();
			currenciesValues.add(new Float(priceComponent.getValue()).toString());
			price.put("hasCurrencyValue", currenciesValues);
			
			List<Object> prices = new ArrayList<>();
			prices.add(price);
			
			priceComponentMap.put("hasPrice", prices);	
			
			// Add the price Component to the 
			pricePlan.get(priceComponentPredicate).add(priceComponentMap);
		}
		
		addPricePlanToMap(offeringUri, pricePlan);
	}
	
	private void initPricePlan(String offeringUri, String title, String description) {
		initPricePlan(offeringUri, title, description, new ArrayList<PriceComponent>());
	}

	private void initService(String offeringUri, String serviceUri, String title, String description, 
			boolean existingService) {

		String contexServiceUri = "<" + serviceUri + ">";
		String contextOfferingUri = "<" + offeringUri + ">";
		List<String> servicesUris = rdfHelperMock.getObjectUris(contextOfferingUri, "usdl:includes");

		// Initialize pricePlansUris (in case they are not) and add the new URI...
		servicesUris = servicesUris == null ? new ArrayList<String>() : servicesUris;
		servicesUris.add(contexServiceUri);	

		try {
			if (existingService) {
				Service service = new Service();
				service.setDisplayName(title);
				service.setComment(description);
				service.setUri(serviceUri);
				Set<Category> classifications = new HashSet<>();
				service.setCategories(classifications);
				when(serviceBoMock.findByURI(serviceUri)).thenReturn(service);
			} else {
				doThrow(new ServiceNotFoundException("service not found")).when(serviceBoMock).findByURI(serviceUri);
			}
		} catch (ServiceNotFoundException e) {
			fail("Exception not expected", e);
			// Not expected...
		}

		// Update mock
		when(rdfHelperMock.getObjectUris(contextOfferingUri, "usdl:includes"))
				.thenReturn(servicesUris);

		// Mock values for this price plan 
		when(rdfHelperMock.getLiteral(contexServiceUri, "dcterms:title")).thenReturn(title);
		when(rdfHelperMock.getLiteral(contexServiceUri, "dcterms:description")).thenReturn(description);

	}

	private void initClassification(String serviceUri, List<String> classifications, boolean existing) {

		// Init mocks
		try {
			for (String classification: classifications) {

				if (existing) {
					Category c = new Category();
					c.setName(classification);
					when(classificatioBoMock.findByName(classification)).thenReturn(c);
				} else {
					doThrow(new CategoryNotFoundException("not found")).when(classificatioBoMock)
							.findByName(classification);
				}
			}
		} catch (Exception e) {
			fail("Exception not expected", e);
		}


		when(rdfHelperMock.getBlankNodesLabels("<" + serviceUri + ">", "usdl:hasClassification"))
				.thenReturn(classifications);
	}

	@Test
	public void testSimpleDescription() throws Exception {

		String offeringTitle = "cool offering";
		String offeringUri = "https://store.lab.fiware.org/offerings/offering1";
		String offeringName = offeringTitle.replaceAll(" ", "-");
		String offeringDesc = "a very long long description for the best offering";
		String offeringVersion = "1.2";
		String offeringImg = "https://store.lab.fiware.org/static/img1.png";
		String acquisitionUrl = "http://store.lab.fiware.org/offering/user/offering/1.0";
		String serviceUri = "https://store.lab.fiware.org/offering/offering1#service1";
		String serviceTitle = "Service 1";
		String serviceDescription = "Service 1 description";
		String serviceClassification = "wirecloud";
		String pricePlanTitle = "price plan";
		String pricePlanDesc = "a description for the price plan";
		String priceComponentTitle = "price component";
		String priceComponentCurrency = "EUR (€)";
		String priceComponentUnit = "bytes/month";
		float priceComponentValue = 1.23F;

		PriceComponent pricePlanComponent1 = new PriceComponent();
		pricePlanComponent1.setTitle(priceComponentTitle);
		pricePlanComponent1.setCurrency(priceComponentCurrency);
		pricePlanComponent1.setUnit(priceComponentUnit);
		pricePlanComponent1.setValue(priceComponentValue);
		
		List<PriceComponent> pricePlanComponents = new ArrayList<>();
		pricePlanComponents.add(pricePlanComponent1);

		// Used for comparisons
		Category c = new Category();
		c.setName(serviceClassification);

		// Mocking		
		initOffering(offeringUri, offeringTitle, offeringDesc, offeringVersion, offeringImg, acquisitionUrl);
		initService(offeringUri, serviceUri, serviceTitle, serviceDescription, false);
		initPricePlan(offeringUri, pricePlanTitle, pricePlanDesc, pricePlanComponents);
		List<String> classifications = new ArrayList<>();
		classifications.add(serviceClassification);
		initClassification(serviceUri, classifications, false);

		// Call the function
		Description description = mock(Description.class);
		when(description.getUrl()).thenReturn(DESCRIPTION_URI);
		List<Offering> offerings = offeringResolver.resolveOfferingsFromServiceDescription(description);

		// Check returned offering
		assertThat(offerings.size()).isEqualTo(1);
		Offering offering = offerings.get(0);
		checkOfferingBasic(offering, offeringName, offeringTitle, offeringDesc, offeringVersion, offeringImg, 
				acquisitionUrl, offeringUri, description);

		// Check services
		Set<Service> services = offering.getServices();
		assertThat(services.size()).isEqualTo(1);
		Service service = (Service) services.toArray()[0];
		checkService(service, serviceTitle, serviceDescription);
		assertThat(c).isIn(service.getCategories());

		// Check returned price plan
		Set<PricePlan> pricePlans = offering.getPricePlans();
		assertThat(pricePlans.size()).isEqualTo(1);
		PricePlan pricePlan = (PricePlan) pricePlans.toArray()[0];
		checkPricePlan(pricePlan, pricePlanTitle, pricePlanDesc);

		// Check price component
		Set<PriceComponent> priceComponents = pricePlan.getPriceComponents();
		assertThat(priceComponents.size()).isEqualTo(1);
		PriceComponent priceComponent = (PriceComponent) priceComponents.toArray()[0];
		checkPriceComponent(priceComponent, priceComponentCurrency, priceComponentTitle, priceComponentUnit, 
				priceComponentValue);

		// Check offering classifications
		assertThat(c).isIn(offering.getCategories());


	}

	@Test
	public void testDescriptionWithTwoOfferings() {

		try {
			String[] offeringsUris = {"https://store.lab.fiware.org/offerings/offering1",
			"https://store.lab.fiware.org/offerings/offering2"};
			String[] offeringsTitles = {"cool offering", "bad offering"};
			String[] offeringsDescs = {"a very long long description for the best offering", 
			"another long description" };
			String[] offeringsVersions = {"1.2", "2.4"};
			String[] offeringsImgs = {"https://store.lab.fiware.org/static/img1.png",
					"https://store.lab.fiware.org/static/img2.png"};
			String[] acquisitionUrls = {"https://store.lab.fiware.org/offeing/user/offering1/1.0",
					"https://store.lab.fiware.org/offering/user/offering2/2.0"};
	
			// Mocking		
			for (int i = 0; i < offeringsUris.length; i++) {
				initOffering(offeringsUris[i], offeringsTitles[i], offeringsDescs[i], 
						offeringsVersions[i], offeringsImgs[i], acquisitionUrls[i]);
			}
	
			// Call the function
			Description description = mock(Description.class);
			when(description.getUrl()).thenReturn(DESCRIPTION_URI);
			List<Offering> offerings = offeringResolver.resolveOfferingsFromServiceDescription(description);
	
			// Check the results
			assertThat(offerings.size()).isEqualTo(2);
	
			for (int i = 0; i < offerings.size(); i++) {
				checkOfferingBasic(offerings.get(i), offeringsTitles[i].replace(" ", "-"), offeringsTitles[i], 
						offeringsDescs[i], offeringsVersions[i], offeringsImgs[i], acquisitionUrls[i], 
						offeringsUris[i], description);
			}
		} catch (Exception e) {
			fail("Exception not expected", e);
		}
	}

	@Test
	public void testOfferingWithTwoPricePlans() {
		
		try {
			String offeringTitle = "cool offering";
			String offeringUri = "https://store.lab.fiware.org/offerings/offering1";
			String offeringDesc = "a very long long description for the best offering";
			String offeringVersion = "1.2";
			String offeringImg = "https://store.lab.fiware.org/static/img1.png";
			String acquisitionUrl = "http://store.lab.fiware.org/offering/user/offering/1.0";

			String[] pricePlansTitles = {"price plan 1", "price plan 2"};
			String[] pricePlansDescs = {"a description for price plan 1", "a brief desc for price plan 2" };
	
			// Init offerings and price plans
			initOffering(offeringUri, offeringTitle, offeringDesc, offeringVersion, offeringImg, acquisitionUrl);
			for (int i = 0; i < pricePlansTitles.length; i++) {
				initPricePlan(offeringUri, pricePlansTitles[i], pricePlansDescs[i]);
			}
	
			// Call the function
			Description description = mock(Description.class);
			when(description.getUrl()).thenReturn(DESCRIPTION_URI);
			List<Offering> offerings = offeringResolver.resolveOfferingsFromServiceDescription(description);
	
			// Check offering and its price plans
			assertThat(offerings.size()).isEqualTo(1);
			assertThat(offerings.get(0).getPricePlans()).hasSize(2);
	
			for (int i = 0; i < pricePlansTitles.length; i++) {
				checkPricePlanInSet(offerings.get(0).getPricePlans(), pricePlansTitles[i], pricePlansDescs[i]);
			}
		} catch (Exception ex) {
			fail("Exception not expected", ex);
		}
	}

	private void testOfferingWithTwoServices(boolean existingService) {
		try {
			String offeringTitle = "cool offering";
			String offeringUri = "https://store.lab.fiware.org/offerings/offering1";
			String offeringDesc = "a very long long description for the best offering";
			String offeringVersion = "1.2";
			String offeringImg = "https://store.lab.fiware.org/static/img1.png";
			String acquisitionUrl = "http://store.lab.fiware.org/offering/user/offering/1.0";

			String[] servicesUris = {"https://store.lab.fiware.org/offerings/offering1#service1", 
			"https://store.lab.fiware.org/offerings/offering1#service2"};
			String[] srevicesTitles = {"service 1", "service 2"};
			String[] servicesDescs = {"a description for service 1", "a brief desc for service 2" };
	
			// Init offerings and price plans
			initOffering(offeringUri, offeringTitle, offeringDesc, offeringVersion, offeringImg, acquisitionUrl);
			for (int i = 0; i < servicesUris.length; i++) {
				initService(offeringUri, servicesUris[i], srevicesTitles[i], servicesDescs[i], existingService);
			}
	
			// Call the function
			Description description = mock(Description.class);
			when(description.getUrl()).thenReturn(DESCRIPTION_URI);
			List<Offering> offerings = offeringResolver.resolveOfferingsFromServiceDescription(description);
	
			// Check offering and its services
			assertThat(offerings.size()).isEqualTo(1);
			assertThat(offerings.get(0).getServices()).hasSize(2);
	
			for (int i = 0; i < srevicesTitles.length; i++) {
				checkServiceInSet(offerings.get(0).getServices(), srevicesTitles[i], servicesDescs[i]);
			}
		} catch (Exception ex) {
			fail("Exception not expected", ex);
		}
	}
	
	@Test
	public void testOfferingWithTwoExistingServices() {
		testOfferingWithTwoServices(true);
	}
	
	@Test
	public void testOfferingWithTwoNonExistingServices() {
		testOfferingWithTwoServices(false);
	}

	private void testOfferingWithTwoClassifications(List<String> classifications, 
			boolean existingClassification) {
		
		try {
			String offeringTitle = "cool offering";
			String offeringUri = "https://store.lab.fiware.org/offerings/offering1";
			String offeringDesc = "a very long long description for the best offering";
			String offeringVersion = "1.2";
			String offeringImg = "https://store.lab.fiware.org/static/img1.png";
			String acquisitionUrl = "http://store.lab.fiware.org/offering/user/offering/1.0";

			String serviceUri = "https://store.lab.fiware.org/offerings/offering1#service1";
			String serviceTitle = "service 1";
			String serviceDesc = "a description for service 1";
	
			initOffering(offeringUri, offeringTitle, offeringDesc, offeringVersion, offeringImg, acquisitionUrl);
			initService(offeringUri, serviceUri, serviceTitle, serviceDesc, false);
			initClassification(serviceUri, classifications, existingClassification);
	
			// Call the function
			Description description = mock(Description.class);
			when(description.getUrl()).thenReturn(DESCRIPTION_URI);
			List<Offering> offerings = offeringResolver.resolveOfferingsFromServiceDescription(description);
	
			// Check offering and its services
			assertThat(offerings.size()).isEqualTo(1);
			assertThat(offerings.get(0).getServices()).hasSize(1);
	
			Offering offering = offerings.get(0);
			Service service = (Service) offering.getServices().toArray()[0];
			checkService(service, serviceTitle, serviceDesc);
	
			// Check classifications
			for (String classification: classifications) {
				Category c = new Category();
				c.setName(classification);
	
				assertThat(c).isIn(service.getCategories());
				assertThat(c).isIn(offering.getCategories());	
			}
		} catch (Exception ex) {
			fail("Exception not expected", ex);
		}
	}

	@Test
	public void testOfferingTwoClassificationsDiffClassificationsNonExisting() {
		// displayName == name
		String classification1 = "class1";
		String classification2 = "class2";

		List<String> classifications = new ArrayList<>();
		classifications.add(classification1);
		classifications.add(classification2);

		testOfferingWithTwoClassifications(classifications, false);
	}
	
	@Test
	public void testOfferingTwoClassificationsDiffClassificationsExisting() {
		// displayName == name
		String classification1 = "class1";
		String classification2 = "class2";

		List<String> classifications = new ArrayList<>();
		classifications.add(classification1);
		classifications.add(classification2);

		testOfferingWithTwoClassifications(classifications, true);
	}
	
	@Test
	public void testOfferingTwoClassificationsSameClassificationsNonExisting() {
		// displayName == name
		String classification = "class1";

		List<String> classifications = new ArrayList<>();
		classifications.add(classification);
		classifications.add(classification);

		testOfferingWithTwoClassifications(classifications, false);
	}
	
	@Test
	public void testOfferingTwoClassificationsSameClassificationsExisting() {
		// displayName == name
		String classification = "class1";

		List<String> classifications = new ArrayList<>();
		classifications.add(classification);
		classifications.add(classification);

		testOfferingWithTwoClassifications(classifications, true);
	}
	
	private void testException(Exception exception, String expectedMessage) {
		// Configure mock
		try {
			doThrow(exception).when(offeringResolver).getRdfHelper(any(Description.class));
		} catch (Exception ex) {
			// Nothing to do...
		}
		
		// Call the function
		try {
			Description description = mock(Description.class);
			offeringResolver.resolveOfferingsFromServiceDescription(description);
		} catch (ParseException ex) {
			assertThat(ex.getMessage()).isEqualTo(expectedMessage);
		}	

	}
	
	@Test
	public void testUSDLNotFound() {
		testException(new FileNotFoundException(), "The file does not exist"); 
	}
	
	@Test
	public void testHostUnreachable() {
		testException(new ConnectException(), "The host cannot be reached"); 
	}
	
	@Test
	public void testInvalidRDF() {
		testException(new JenaException(), "The file does not contains a valid USDL file"); 
	}
	
	private void testOfferingInvalid(String url, String title, String version, String imageUrl,
			String acquisitionUrl, String expectedMessage) {
		
		// Mocking
		initOffering(url, title, "", version, imageUrl, acquisitionUrl);
		
		// Call the function
		Description description = mock(Description.class);
		when(description.getUrl()).thenReturn(DESCRIPTION_URI);
		
		try {
			offeringResolver.resolveOfferingsFromServiceDescription(description);
		} catch (ParseException ex) {
			assertThat(ex.getMessage()).isEqualTo(expectedMessage);
		}

	}
	
	@Test
	public void testOfferingNameNotFound() {
		String url = "http://fiware.org";		
		testOfferingInvalid(url, "", "", "", "", "Name for entity " + url + " cannot be retrieved");
	}
	
	@Test
	public void testOfferingVersionNotFound() {
		String url = "http://fiware.org";
		String offeringName = "Offering Name";
		testOfferingInvalid(url, offeringName, "", "", "", "Version for offering " + offeringName
				+ " cannot be retrieved");
	}
	
	@Test
	public void testOfferingImageURLNotFound() {
		String url = "http://fiware.org";
		String offeringName = "Offering Name";
		testOfferingInvalid(url, offeringName, "1.0", "", "", "Image URL for offering " + offeringName
				+ " cannot be retrieved");
	}
	
	@Test
	public void testOfferingAcquisitionURLNotFound() {
		String url = "http://fiware.org";
		String offeringName = "Offering Name";
		testOfferingInvalid(url, offeringName, "1.0", url, "", "Acquisition URL for offering " + offeringName
				+ " cannot be retrieved");
	}
	
	private void testOfferingPricePlanInvalid(Map<String, List<Object>> pricePlan, String expectedMessage) {
		
		// Mocking
		String url = "http://fiware.org";
		String offeringName = "Offering Name";
		
		initOffering(url, offeringName, "", "1.0", url, url);
		addPricePlanToMap(url, pricePlan);
		
		// Call the function
		Description description = mock(Description.class);
		when(description.getUrl()).thenReturn(DESCRIPTION_URI);
		
		try {
			offeringResolver.resolveOfferingsFromServiceDescription(description);
			failBecauseExceptionWasNotThrown(ParseException.class);
		} catch (ParseException ex) {
			assertThat(ex.getMessage()).isEqualTo(String.format(expectedMessage, offeringName));
		} 
	}
	
	@Test
	public void testOfferingPricePlanNameNotIncluded() {
		testOfferingPricePlanInvalid(new HashMap<String, List<Object>>(), "Offering %s contains a price plan "
				+ "without title");
	}
	
	@Test
	public void testOfferingPricePlanNameEmpty() {
		// Configure price plan
		Map<String, List<Object>> pricePlan = new HashMap<String, List<Object>>();
		List<Object> titles = new ArrayList<>();
		pricePlan.put("title", titles);
		
		// test
		testOfferingPricePlanInvalid(pricePlan, "Offering %s contains a price plan without title");
	}
	
	@Test
	public void testOfferingPricePlanNameBlank() {
		// Configure price plan
		Map<String, List<Object>> pricePlan = new HashMap<String, List<Object>>();
		List<Object> titles = new ArrayList<>();
		titles.add("");
		pricePlan.put("title", titles);
		
		// test
		testOfferingPricePlanInvalid(pricePlan, "Offering %s contains a price plan without title");
	}
	
	private void testOfferingPriceComponentInvalid(Map<String, List<Object>> priceComponent, String message) {
		// Configure price plan
		Map<String, List<Object>> pricePlan = new HashMap<String, List<Object>>();
		
		List<Object> titles = new ArrayList<>();
		titles.add("a");
		pricePlan.put("title", titles);
		
		List<Object> priceComponents = new ArrayList<>();
		priceComponents.add(priceComponent);
		pricePlan.put("hasPriceComponent", priceComponents);
		
		// test
		testOfferingPricePlanInvalid(pricePlan, message);
	}
	
	@Test
	public void testOfferingPriceComponentLabelMissing() {		
		testOfferingPriceComponentInvalid(new HashMap<String, List<Object>>(), "Offering %s contains a price "
				+ "component without title");
	}
	
	@Test
	public void testOfferingPriceComponentLabelEmpty() {
		// Configure price component
		Map<String, List<Object>> priceComponent = new HashMap<>();
		List<Object> labels = new ArrayList<>();
		priceComponent.put("label", labels);
		
		// test
		testOfferingPriceComponentInvalid(priceComponent, "Offering %s contains a price component without title");
	}
	
	@Test
	public void testOfferingPriceComponentLabelBkank() {
		Map<String, List<Object>> priceComponent = new HashMap<>();
		List<Object> labels = new ArrayList<>();
		labels.add("");
		priceComponent.put("label", labels);
				
		// test
		testOfferingPriceComponentInvalid(priceComponent, "Offering %s contains a price component without title");
	}
	
	@Test
	public void testOfferingPriceComponentSpecificationMissing() {
		Map<String, List<Object>> priceComponent = new HashMap<>();
		List<Object> labels = new ArrayList<>();
		labels.add("a");
		priceComponent.put("label", labels);
				
		// test
		testOfferingPriceComponentInvalid(priceComponent, "Offering %s contains a price component without "
				+ "price specification");
	}
	
	@Test
	public void testOfferingPriceComponentSpecificationEmpty() {
		Map<String, List<Object>> priceComponent = new HashMap<>();
		List<Object> labels = new ArrayList<>();
		labels.add("a");
		priceComponent.put("label", labels);
		priceComponent.put("hasPrice", new ArrayList<Object>());
				
		// test
		testOfferingPriceComponentInvalid(priceComponent, "Offering %s contains a price component without "
				+ "price specification");
	}
	
	private void testOfferingPriceComponentInvalidSpecification(List<Object> currencies, List<Object> units, List<Object> values, 
			String message) {
		
		Map<String, List<Object>> priceComponent = new HashMap<>();
		List<Object> labels = new ArrayList<>();
		labels.add("a");
		priceComponent.put("label", labels);
		
		List<Object> prices = new ArrayList<Object>();
		Map<String, List<Object>> price = new HashMap<>();
		price.put("hasCurrency", currencies);
		price.put("hasUnitOfMeasurement", units);
		price.put("hasCurrencyValue", values);
		
		prices.add(price);
		priceComponent.put("hasPrice", prices);
		
		// test
		testOfferingPriceComponentInvalid(priceComponent, message);
	}
	
	@Test
	public void testOfferingPriceComponentCurrencyMissing() {		
		testOfferingPriceComponentInvalidSpecification(null, null, null, 
				"Offering %s contains a price component without currency");
	}
	
	@Test
	public void testOfferingPriceComponentEmptyCurrency() {		
		testOfferingPriceComponentInvalidSpecification(new ArrayList<>(), null, null, 
				"Offering %s contains a price component without currency");
	}
	
	@Test
	public void testOfferingPriceComponentBlankCurrency() {
		List<Object> currencies = new ArrayList<>();
		currencies.add("");
		
		testOfferingPriceComponentInvalidSpecification(currencies, null, null,
				"Offering %s contains a price component without currency");
	}
	
	@Test
	public void testOfferingPriceComponentMissingUnit() {
		List<Object> currencies = new ArrayList<>();
		currencies.add("EUR");
		
		testOfferingPriceComponentInvalidSpecification(currencies, null, null,
				"Offering %s contains a price component without unit of measurement");
	}
	
	@Test
	public void testOfferingPriceComponentEmptyUnit() {
		List<Object> currencies = new ArrayList<>();
		currencies.add("EUR");
		
		testOfferingPriceComponentInvalidSpecification(currencies, new ArrayList<>(), null,
				"Offering %s contains a price component without unit of measurement");
	}
	
	@Test
	public void testOfferingPriceComponentBlankUnit() {
		List<Object> currencies = new ArrayList<>();
		currencies.add("EUR");
		List<Object> units = new ArrayList<>();
		units.add("");
		
		testOfferingPriceComponentInvalidSpecification(currencies, units, null, 
				"Offering %s contains a price component without unit of measurement");
	}
	
	@Test
	public void testOfferingPriceComponentValueMissing() {
		List<Object> currencies = new ArrayList<>();
		currencies.add("EUR");
		List<Object> units = new ArrayList<>();
		units.add("month");
		
		testOfferingPriceComponentInvalidSpecification(currencies, units, null,
				"Offering %s contains a price component without value");
	}
	
	@Test
	public void testOfferingPriceComponentValueEmpty() {
		List<Object> currencies = new ArrayList<>();
		currencies.add("EUR");
		List<Object> units = new ArrayList<>();
		units.add("month");
		
		testOfferingPriceComponentInvalidSpecification(currencies, units, new ArrayList<>(), 
				"Offering %s contains a price component without value");
	}
	
	@Test
	public void testOfferingPriceComponentValueBlank() {
		List<Object> currencies = new ArrayList<>();
		currencies.add("EUR");
		List<Object> units = new ArrayList<>();
		units.add("month");
		List<Object> values = new ArrayList<>();
		values.add("");
		
		testOfferingPriceComponentInvalidSpecification(currencies, units, values, 
				"Offering %s contains a price component without value");
	}
	
	@Test
	public void testOfferingPriceComponentInvalidValue() {
		List<Object> currencies = new ArrayList<>();
		currencies.add("EUR");
		List<Object> units = new ArrayList<>();
		units.add("month");
		List<Object> values = new ArrayList<>();
		values.add("4a");
		
		testOfferingPriceComponentInvalidSpecification(currencies, units, values, 
				"Offering %s contains a price component with an invalid currency value");
	}
	
}
