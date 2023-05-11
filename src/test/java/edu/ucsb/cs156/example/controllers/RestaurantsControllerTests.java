package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Restaurants;
import edu.ucsb.cs156.example.repositories.RestaurantsRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = RestaurantsController.class)
@Import(TestConfig.class)
public class RestaurantsControllerTests extends ControllerTestCase {

        @MockBean
        RestaurantsRepository restaurantRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/restaurants/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/restaurants/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/restaurants/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/restaurants?code=freebirds"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/restaurants/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/restaurants/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/restaurants/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                Restaurants rest = Restaurants.builder()
                                .name("Freebirds")
                                .code("freebirds")
                                .cuisine("burritos")
                                .location("Isla Vista")
                                .build();

                when(restaurantRepository.findById(eq("freebirds"))).thenReturn(Optional.of(rest));

                // act
                MvcResult response = mockMvc.perform(get("/api/restaurants?code=freebirds"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(restaurantRepository, times(1)).findById(eq("freebirds"));
                String expectedJson = mapper.writeValueAsString(rest);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(restaurantRepository.findById(eq("munger-hall"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/restaurants?code=munger-hall"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(restaurantRepository, times(1)).findById(eq("munger-hall"));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Restaurants with id munger-hall not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_restaurants() throws Exception {

                // arrange

                Restaurants freebirds = Restaurants.builder()
                                .name("Freebirds")
                                .code("freebirds")
                                .cuisine("burritos")
                                .location("Isla Vista")
                                .build();

                Restaurants canes = Restaurants.builder()
                                .name("Canes")
                                .code("canes")
                                .cuisine("chicken")
                                .location("Oxnard")
                                .build();

                ArrayList<Restaurants> expectedRest = new ArrayList<>();
                expectedRest.addAll(Arrays.asList(freebirds, canes));

                when(restaurantRepository.findAll()).thenReturn(expectedRest);

                // act
                MvcResult response = mockMvc.perform(get("/api/restaurants/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(restaurantRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedRest);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_rest() throws Exception {
                // arrange

                Restaurants hummusrepublic = Restaurants.builder()
                                .name("Hummus Republic")
                                .code("hummusrepublic")
                                .cuisine("mediterranian bowls")
                                .location("Isla Vista")
                                .build();

                when(restaurantRepository.save(eq(hummusrepublic))).thenReturn(hummusrepublic);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/restaurants/post?name=Hummus Republic&code=hummusrepublic&cuisine=mediterranian bowls&location=Isla Vista&")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(restaurantRepository, times(1)).save(hummusrepublic);
                String expectedJson = mapper.writeValueAsString(hummusrepublic);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange

                Restaurants finneys = Restaurants.builder()
                                .name("Finneys")
                                .code("finneys")
                                .cuisine("bar food")
                                .location("Santa Barbara")
                                .build();

                when(restaurantRepository.findById(eq("finneys"))).thenReturn(Optional.of(finneys));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/restaurants?code=finneys")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(restaurantRepository, times(1)).findById("finneys");
                verify(restaurantRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Restaurants with id finneys deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_rest_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(restaurantRepository.findById(eq("munger-hall"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/restaurants?code=munger-hall")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(restaurantRepository, times(1)).findById("munger-hall");
                Map<String, Object> json = responseToJson(response);
                assertEquals("Restaurants with id munger-hall not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_rest() throws Exception {
                // arrange

                Restaurants freebirdsOrig = Restaurants.builder()
                                .name("Freebirds")
                                .code("freebirds")
                                .cuisine("burritos")
                                .location("Isla Vista")
                                .build();

                Restaurants freebirdsEdited = Restaurants.builder()
                                .name("Not Freebirds")
                                .code("freebirds")
                                .cuisine("asain")
                                .location("antarctica")
                                .build();

                String requestBody = mapper.writeValueAsString(freebirdsEdited);

                when(restaurantRepository.findById(eq("freebirds"))).thenReturn(Optional.of(freebirdsOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/restaurants?code=freebirds")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(restaurantRepository, times(1)).findById("freebirds");
                verify(restaurantRepository, times(1)).save(freebirdsEdited); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_rest_that_does_not_exist() throws Exception {
                // arrange

                Restaurants editedRest = Restaurants.builder()
                                .name("Munger Hall")
                                .code("munger-hall")
                                .cuisine("asain")
                                .location("antarctica")
                                .build();


                String requestBody = mapper.writeValueAsString(editedRest);

                when(restaurantRepository.findById(eq("munger-hall"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/restaurants?code=munger-hall")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(restaurantRepository, times(1)).findById("munger-hall");
                Map<String, Object> json = responseToJson(response);
                assertEquals("Restaurants with id munger-hall not found", json.get("message"));

        }
}
