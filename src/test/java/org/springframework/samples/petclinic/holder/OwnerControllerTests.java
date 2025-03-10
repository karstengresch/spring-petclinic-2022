/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.holder;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.util.List;

import org.assertj.core.util.Lists;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test class for {@link HolderController}
 *
 * @author Colin But
 */
@WebMvcTest(HolderController.class)
class HolderControllerTests {

	private static final int TEST_OWNER_ID = 1;

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private HolderRepository holders;

	private Holder george() {
		Holder george = new Holder();
		george.setId(TEST_OWNER_ID);
		george.setFirstName("George");
		george.setLastName("Franklin");
		george.setAddress("110 W. Liberty St.");
		george.setCity("Madison");
		george.setTelephone("6085551023");
		Pet max = new Pet();
		PetType dog = new PetType();
		dog.setName("dog");
		max.setType(dog);
		max.setName("Max");
		max.setBirthDate(LocalDate.now());
		george.addPet(max);
		max.setId(1);
		return george;
	};

	@BeforeEach
	void setup() {

		Holder george = george();
		given(this.holders.findByLastName(eq("Franklin"), any(Pageable.class)))
			.willReturn(new PageImpl<Holder>(Lists.newArrayList(george)));

		given(this.holders.findAll(any(Pageable.class))).willReturn(new PageImpl<Holder>(Lists.newArrayList(george)));

		given(this.holders.findById(TEST_OWNER_ID)).willReturn(george);
		Visit visit = new Visit();
		visit.setDate(LocalDate.now());
		george.getPet("Max").getVisits().add(visit);

	}

	@Test
	void testInitCreationForm() throws Exception {
		mockMvc.perform(get("/holders/new"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("holder"))
			.andExpect(view().name("holders/createOrUpdateHolderForm"));
	}

	@Test
	void testProcessCreationFormSuccess() throws Exception {
		mockMvc
			.perform(post("/holders/new").param("firstName", "Joe")
				.param("lastName", "Bloggs")
				.param("address", "123 Caramel Street")
				.param("city", "London")
				.param("telephone", "01316761638"))
			.andExpect(status().is3xxRedirection());
	}

	@Test
	void testProcessCreationFormHasErrors() throws Exception {
		mockMvc
			.perform(post("/holders/new").param("firstName", "Joe").param("lastName", "Bloggs").param("city", "London"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("holder"))
			.andExpect(model().attributeHasFieldErrors("holder", "address"))
			.andExpect(model().attributeHasFieldErrors("holder", "telephone"))
			.andExpect(view().name("holders/createOrUpdateHolderForm"));
	}

	@Test
	void testInitFindForm() throws Exception {
		mockMvc.perform(get("/holders/find"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("holder"))
			.andExpect(view().name("holders/findHolders"));
	}

	@Test
	void testProcessFindFormSuccess() throws Exception {
		Page<Holder> tasks = new PageImpl<Holder>(Lists.newArrayList(george(), new Holder()));
		Mockito.when(this.holders.findByLastName(anyString(), any(Pageable.class))).thenReturn(tasks);
		mockMvc.perform(get("/holders?page=1"))
			.andExpect(status().isOk())
			.andExpect(view().name("holders/holdersList"));
	}

	@Test
	void testProcessFindFormByLastName() throws Exception {
		Page<Holder> tasks = new PageImpl<Holder>(Lists.newArrayList(george()));
		Mockito.when(this.holders.findByLastName(eq("Franklin"), any(Pageable.class))).thenReturn(tasks);
		mockMvc.perform(get("/holders?page=1").param("lastName", "Franklin"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/holders/" + TEST_OWNER_ID));
	}

	@Test
	void testProcessFindFormNoHoldersFound() throws Exception {
		Page<Holder> tasks = new PageImpl<Holder>(Lists.newArrayList());
		Mockito.when(this.holders.findByLastName(eq("Unknown Surname"), any(Pageable.class))).thenReturn(tasks);
		mockMvc.perform(get("/holders?page=1").param("lastName", "Unknown Surname"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrors("holder", "lastName"))
			.andExpect(model().attributeHasFieldErrorCode("holder", "lastName", "notFound"))
			.andExpect(view().name("holders/findHolders"));

	}

	@Test
	void testInitUpdateHolderForm() throws Exception {
		mockMvc.perform(get("/holders/{holderId}/edit", TEST_OWNER_ID))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("holder"))
			.andExpect(model().attribute("holder", hasProperty("lastName", is("Franklin"))))
			.andExpect(model().attribute("holder", hasProperty("firstName", is("George"))))
			.andExpect(model().attribute("holder", hasProperty("address", is("110 W. Liberty St."))))
			.andExpect(model().attribute("holder", hasProperty("city", is("Madison"))))
			.andExpect(model().attribute("holder", hasProperty("telephone", is("6085551023"))))
			.andExpect(view().name("holders/createOrUpdateHolderForm"));
	}

	@Test
	void testProcessUpdateHolderFormSuccess() throws Exception {
		mockMvc
			.perform(post("/holders/{holderId}/edit", TEST_OWNER_ID).param("firstName", "Joe")
				.param("lastName", "Bloggs")
				.param("address", "123 Caramel Street")
				.param("city", "London")
				.param("telephone", "01616291589"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/holders/{holderId}"));
	}

	@Test
	void testProcessUpdateHolderFormUnchangedSuccess() throws Exception {
		mockMvc.perform(post("/holders/{holderId}/edit", TEST_OWNER_ID))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/holders/{holderId}"));
	}

	@Test
	void testProcessUpdateHolderFormHasErrors() throws Exception {
		mockMvc
			.perform(post("/holders/{holderId}/edit", TEST_OWNER_ID).param("firstName", "Joe")
				.param("lastName", "Bloggs")
				.param("address", "")
				.param("telephone", ""))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("holder"))
			.andExpect(model().attributeHasFieldErrors("holder", "address"))
			.andExpect(model().attributeHasFieldErrors("holder", "telephone"))
			.andExpect(view().name("holders/createOrUpdateHolderForm"));
	}

	@Test
	void testShowHolder() throws Exception {
		mockMvc.perform(get("/holders/{holderId}", TEST_OWNER_ID))
			.andExpect(status().isOk())
			.andExpect(model().attribute("holder", hasProperty("lastName", is("Franklin"))))
			.andExpect(model().attribute("holder", hasProperty("firstName", is("George"))))
			.andExpect(model().attribute("holder", hasProperty("address", is("110 W. Liberty St."))))
			.andExpect(model().attribute("holder", hasProperty("city", is("Madison"))))
			.andExpect(model().attribute("holder", hasProperty("telephone", is("6085551023"))))
			.andExpect(model().attribute("holder", hasProperty("pets", not(empty()))))
			.andExpect(model().attribute("holder", hasProperty("pets", new BaseMatcher<List<Pet>>() {

				@Override
				public boolean matches(Object item) {
					@SuppressWarnings("unchecked")
					List<Pet> pets = (List<Pet>) item;
					Pet pet = pets.get(0);
					if (pet.getVisits().isEmpty()) {
						return false;
					}
					return true;
				}

				@Override
				public void describeTo(Description description) {
					description.appendText("Max did not have any visits");
				}
			})))
			.andExpect(view().name("holders/holderDetails"));
	}

}
