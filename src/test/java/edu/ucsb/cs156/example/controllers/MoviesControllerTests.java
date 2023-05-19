package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Movies;
import edu.ucsb.cs156.example.repositories.MoviesRepository;

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

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = MoviesController.class)
@Import(TestConfig.class)
public class MoviesControllerTests extends ControllerTestCase {

        @MockBean
        MoviesRepository moviesRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/movies/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/movies/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/movies/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/movies?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/movies/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/movies/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/movies/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {
                Movies movies = Movies.builder()
                                .title("Title1")
                                .starring("Starring1")
                                .director("Director1")
                                .build();

                when(moviesRepository.findById(eq(7L))).thenReturn(Optional.of(movies));

                // act
                MvcResult response = mockMvc.perform(get("/api/movies?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(moviesRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(movies);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(moviesRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/movies?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(moviesRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Movies with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_movies() throws Exception {

                // arrange
                Movies movies1 = Movies.builder()
                                .title("Title1")
                                .starring("Starring1")
                                .director("Director1")
                                .build();

                Movies movies2 = Movies.builder()
                                .title("Title2")
                                .starring("Starring2")
                                .director("Director2")
                                .build();

                ArrayList<Movies> expectedDates = new ArrayList<>();
                expectedDates.addAll(Arrays.asList(movies1, movies2));

                when(moviesRepository.findAll()).thenReturn(expectedDates);

                // act
                MvcResult response = mockMvc.perform(get("/api/movies/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(moviesRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedDates);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_movies() throws Exception {
                // arrange
                Movies movies1 = Movies.builder()
                                .title("Title1")
                                .starring("Starring1")
                                .director("Director1")
                                .build();

                when(moviesRepository.save(eq(movies1))).thenReturn(movies1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/movies/post?title=Title1&starring=Starring1&director=Director1")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(moviesRepository, times(1)).save(movies1);
                String expectedJson = mapper.writeValueAsString(movies1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange
                Movies movies1 = Movies.builder()
                                .title("Title1")
                                .starring("Starring1")
                                .director("Director1")
                                .build();

                when(moviesRepository.findById(eq(15L))).thenReturn(Optional.of(movies1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/movies?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(moviesRepository, times(1)).findById(15L);
                verify(moviesRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Movies with id 15 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_movies_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(moviesRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/movies?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(moviesRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Movies with id 15 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_movies() throws Exception {
                // arrange
                Movies moviesOrig = Movies.builder()
                                .title("Title1")
                                .starring("Starring1")
                                .director("Director1")
                                .build();

                Movies moviesEdited = Movies.builder()
                                .title("Asia")
                                .starring("Starring1ro")
                                .director("Asiatotoro")
                                .build();

                String requestBody = mapper.writeValueAsString(moviesEdited);

                when(moviesRepository.findById(eq(67L))).thenReturn(Optional.of(moviesOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/movies?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(moviesRepository, times(1)).findById(67L);
                verify(moviesRepository, times(1)).save(moviesEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_movies_that_does_not_exist() throws Exception {
                // arrange

                Movies moviesEdited = Movies.builder()
                                .title("Asia")
                                .starring("Starring1ro")
                                .director("Asiatotoro")
                                .build();

                String requestBody = mapper.writeValueAsString(moviesEdited);

                when(moviesRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/movies?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(moviesRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Movies with id 67 not found", json.get("message"));

        }
}
