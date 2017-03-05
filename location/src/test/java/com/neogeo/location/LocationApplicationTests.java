package com.neogeo.location;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.neogeo.location.repository.LocationRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class LocationApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private LocationRepository locationRepository;

	@Before
	public void deleteAllBeforeTests() throws Exception {
		locationRepository.deleteAll();
	}

	@Test
	public void shouldReturnRepositoryIndex() throws Exception {

		mockMvc.perform(get("/")).andDo(print()).andExpect(status().isOk())
		.andExpect(jsonPath("$._links.locations").exists());
	}

	@Test
	public void shouldCreateEntity() throws Exception {

		mockMvc.perform(post("/locations").content(
				"{\"addressess\":[\"Avenida Pedro Álvares Cabral\"], "+
						"\"location\":{\"type\": \"Point\", \"coordinates\": [-23.5874162, -46.6576336]}, "+
				"\"names\": [\"Parque Ibirapuera\", \"parque\", \"ibirapuera\", \"Pq Ibirapuera\", \"Ibira\"], \"enabled\": true}"))
		.andExpect(status().isCreated())
		.andExpect(header().string("Location", containsString("locations/")));
	}

	@Test
	public void shouldRetrieveEntity() throws Exception {

		MvcResult mvcResult = mockMvc.perform(post("/locations").content(
				"{\"addressess\":[\"Avenida Pedro Álvares Cabral\"], "+
						"\"location\":{\"type\": \"Point\", \"coordinates\": [-23.5874162, -46.6576336]}, "+
				"\"names\": [\"Parque Ibirapuera\", \"parque\", \"ibirapuera\", \"Pq Ibirapuera\", \"Ibira\"], \"enabled\": true}"))
				.andExpect(status().isCreated()).andReturn();

		String location = mvcResult.getResponse().getHeader("Location").replace("http://localhost", "");
		mockMvc.perform(get(location)).andExpect(status().isOk())
		.andExpect(jsonPath("$.addressess").value("Avenida Pedro Álvares Cabral"))
		.andExpect(jsonPath("$.location.type").value("Point"))
		.andExpect(jsonPath("$.location.x").value(-23.5874162))
		.andExpect(jsonPath("$.location.y").value(-46.6576336))
		.andExpect(jsonPath("$.names").value(Matchers.containsInAnyOrder("Parque Ibirapuera", "parque", "ibirapuera", "Pq Ibirapuera", "Ibira")))
		.andExpect(jsonPath("$.enabled").value(true));
	}

	@Test
	public void shouldQueryEntityByProximity() throws Exception {

		mockMvc.perform(post("/locations").content(
				"{\"addressess\":[\"Avenida Pedro Álvares Cabral\"], "+
						"\"location\":{\"type\": \"Point\", \"coordinates\": [-23.5874162, -46.6576336]}, "+
				"\"names\": [\"Parque Ibirapuera\", \"parque\", \"ibirapuera\", \"Pq Ibirapuera\", \"Ibira\"], \"enabled\": true}"))
		.andExpect(status().isCreated());

		mockMvc.perform(
				get("/locations/search/findByLocationNearAndEnabled?latitude={latitude}&longitude={longitude}&distance={distance}", -46.6576336, -23.5874162, 1))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.[0].addressess").value("Avenida Pedro Álvares Cabral"));
	}
	
	@Test
	public void shouldQueryEntityByName() throws Exception {

		mockMvc.perform(post("/locations").content(
				"{\"addressess\":[\"Avenida Pedro Álvares Cabral\"], "+
						"\"location\":{\"type\": \"Point\", \"coordinates\": [-23.5874162, -46.6576336]}, "+
				"\"names\": [\"Parque Ibirapuera\", \"parque\", \"ibirapuera\", \"Pq Ibirapuera\", \"Ibira\"], \"enabled\": true}"))
		.andExpect(status().isCreated());

		mockMvc.perform(
				get("/locations/search/findByName?name={name}", "Ibira"))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.[0].addressess").value("Avenida Pedro Álvares Cabral"));
	}

	@Test
	public void shouldUpdateEntity() throws Exception {

		MvcResult mvcResult = mockMvc.perform(post("/locations").content(
				"{\"addressess\":[\"Avenida Pedro Álvares Cabral\"], "+
						"\"location\":{\"type\": \"Point\", \"coordinates\": [-23.5874162, -46.6576336]}, "+
				"\"names\": [\"Parque Ibirapuera\", \"parque\", \"ibirapuera\", \"Pq Ibirapuera\", \"Ibira\"], \"enabled\": true}"))
				.andExpect(status().isCreated()).andReturn();

		String location = mvcResult.getResponse().getHeader("Location").replace("http://localhost", "");

		mockMvc.perform(put(location).contentType(MediaType.APPLICATION_JSON).content(
				"{\"addressess\": [\"Avenida Pedro Álvares Cabral\", \"Av. Pedro Álvares Cabral\"], "+
						"\"location\":{\"type\": \"Point\", \"coordinates\": [-23.5874162, -46.6576336]}, "+
				"\"names\": [\"Parque Ibirapuera\", \"parque\", \"ibirapuera\", \"Pq Ibirapuera\", \"Ibira\"], \"enabled\": true}"))
		.andExpect(status().isNoContent());

		mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(
				jsonPath("$.addressess").value(Matchers.containsInAnyOrder("Avenida Pedro Álvares Cabral", "Av. Pedro Álvares Cabral")));
	}

	@Test
	public void shouldPartiallyUpdateEntity() throws Exception {

		MvcResult mvcResult = mockMvc.perform(post("/locations").content(
				"{\"addressess\": [\"Avenida Pedro Álvares Cabral\"], "+
						"\"location\":{\"type\": \"Point\", \"coordinates\": [-23.5874162, -46.6576336]}, "+
				"\"names\": [\"Parque Ibirapuera\", \"parque\", \"ibirapuera\", \"Pq Ibirapuera\", \"Ibira\"], \"enabled\": true}"))
				.andExpect(status().isCreated()).andReturn();

		String location = mvcResult.getResponse().getHeader("Location").replace("http://localhost", "");

		mockMvc.perform(patch(location).contentType(MediaType.APPLICATION_JSON)
				.content("{\"addressess\": [\"Av Pedro Álvares Cabral\"]}"))
		.andExpect(status().isNoContent());

		mockMvc.perform(get(location)).andExpect(status().isOk())
		.andExpect(jsonPath("$.addressess").value("Av Pedro Álvares Cabral"));
	}
	

	@Test
	public void shouldDeleteEntity() throws Exception {

		MvcResult mvcResult = mockMvc.perform(post("/locations").content(
				"{\"addressess\": [\"Avenida Pedro Álvares Cabral\"], "+
						"\"location\":{\"type\": \"Point\", \"coordinates\": [-23.5874162, -46.6576336]}, "+
				"\"names\": [\"Parque Ibirapuera\", \"parque\", \"ibirapuera\", \"Pq Ibirapuera\", \"Ibira\"], \"enabled\": true}"))
				.andExpect(status().isCreated()).andReturn();

		String location = mvcResult.getResponse().getHeader("Location").replace("http://localhost", "");
		
		mockMvc.perform(delete(location)).andExpect(status().isNoContent());

		mockMvc.perform(get(location)).andExpect(status().isOk())
		.andExpect(jsonPath("$.enabled").value(false));
	}
}
