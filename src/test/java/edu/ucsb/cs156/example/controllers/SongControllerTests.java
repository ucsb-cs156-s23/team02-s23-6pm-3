package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Song;
import edu.ucsb.cs156.example.repositories.SongRepository;

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

@WebMvcTest(controllers = SongController.class)
@Import(TestConfig.class)
public class SongControllerTests extends ControllerTestCase {

        @MockBean
        SongRepository songRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/song/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/song/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/song/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/song?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/song/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/song/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/song/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {
                Song song = Song.builder()
                                .title("Africa")
                                .artist("Toto")
                                .album("africa's toto")
                                .duration(4.5d)
                                .build();

                when(songRepository.findById(eq(7L))).thenReturn(Optional.of(song));

                // act
                MvcResult response = mockMvc.perform(get("/api/song?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(songRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(song);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(songRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/song?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(songRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Song with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_song() throws Exception {

                // arrange
                Song song1 = Song.builder()
                                .title("Africa")
                                .artist("Toto")
                                .album("africa's toto")
                                .duration(4.5d)
                                .build();

                Song song2 = Song.builder()
                                .title("Pride")
                                .artist("Kendrick")
                                .album("DAMN.")
                                .duration(3.2d)
                                .build();

                ArrayList<Song> expectedDates = new ArrayList<>();
                expectedDates.addAll(Arrays.asList(song1, song2));

                when(songRepository.findAll()).thenReturn(expectedDates);

                // act
                MvcResult response = mockMvc.perform(get("/api/song/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(songRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedDates);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_song() throws Exception {
                // arrange
                Song song1 = Song.builder()
                                .title("Africa")
                                .artist("Toto")
                                .album("africastoto")
                                .duration(4.5d)
                                .build();

                when(songRepository.save(eq(song1))).thenReturn(song1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/song/post?title=Africa&artist=Toto&album=africastoto&duration=4.5")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(songRepository, times(1)).save(song1);
                String expectedJson = mapper.writeValueAsString(song1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange
                Song song1 = Song.builder()
                                .title("Africa")
                                .artist("Toto")
                                .album("africastoto")
                                .duration(4.5d)
                                .build();

                when(songRepository.findById(eq(15L))).thenReturn(Optional.of(song1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/song?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(songRepository, times(1)).findById(15L);
                verify(songRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Song with id 15 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_song_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(songRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/song?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(songRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Song with id 15 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_song() throws Exception {
                // arrange
                Song songOrig = Song.builder()
                                .title("Africa")
                                .artist("Toto")
                                .album("africastoto")
                                .duration(4.5d)
                                .build();

                Song songEdited = Song.builder()
                                .title("Asia")
                                .artist("Totoro")
                                .album("Asiatotoro")
                                .duration(4.2d)
                                .build();

                String requestBody = mapper.writeValueAsString(songEdited);

                when(songRepository.findById(eq(67L))).thenReturn(Optional.of(songOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/song?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(songRepository, times(1)).findById(67L);
                verify(songRepository, times(1)).save(songEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_song_that_does_not_exist() throws Exception {
                // arrange

                Song songEdited = Song.builder()
                                .title("Asia")
                                .artist("Totoro")
                                .album("Asiatotoro")
                                .duration(4.2d)
                                .build();

                String requestBody = mapper.writeValueAsString(songEdited);

                when(songRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/song?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(songRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Song with id 67 not found", json.get("message"));

        }
}
