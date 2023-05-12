package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Books;
import edu.ucsb.cs156.example.repositories.BooksRepository;

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

@WebMvcTest(controllers = BooksController.class)
@Import(TestConfig.class)
public class BooksControllerTests extends ControllerTestCase {

        @MockBean
        BooksRepository booksRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/books/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/books/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/books/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/books?code=Dracula"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/books/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/books/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/books/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                Books book = Books.builder()
                                .name("The Lord of the RIngs")
                                .title("lotr")
                                .genre("fantasy")
                                .setting("Middle Earth")
                                .build();

                when(booksRepository.findById(eq("lotr"))).thenReturn(Optional.of(book));

                // act
                MvcResult response = mockMvc.perform(get("/api/books?code=lotr"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(booksRepository, times(1)).findById(eq("lotr"));
                String expectedJson = mapper.writeValueAsString(book);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(booksRepository.findById(eq("potato"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/books?code=potato"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(booksRepository, times(1)).findById(eq("potato"));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Books with id potato not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_ucsbdiningcommons() throws Exception {

                // arrange

                Books lotr = Books.builder()
                                .name("The Lord of the RIngs")
                                .title("lotr")
                                .genre("fantasy")
                                .setting("Middle Earth")
                                .build();

                Books jeeves = Books.builder()
                                .name("Jeeves and Wooster")
                                .title("jeeves-and-wooster")
                                .genre("comedy")
                                .setting("England")
                                .build();

                ArrayList<Books> expectedBooks = new ArrayList<>();
                expectedBooks.addAll(Arrays.asList(lotr, dlg));

                when(booksRepository.findAll()).thenReturn(expectedBooks);

                // act
                MvcResult response = mockMvc.perform(get("/api/books/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(booksRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedBooks);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_commons() throws Exception {
                // arrange

                Books pimpernel = Books.builder()
                                .name("The Scarlet Pimpernel")
                                .code("pimpernel")
                                .genre("romance")
                                .setting("France")
                                .build();

                when(booksRepository.save(eq(pimpernel))).thenReturn(pimpernel);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/books/post?name=The-Scarlet-Pimpernel&code=pimpernel&genre=romance&setting=France")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(booksRepository, times(1)).save(pimpernel);
                String expectedJson = mapper.writeValueAsString(pimpernel);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange

                Books spare = Books.builder()
                                .name("Spare")
                                .code("spare")
                                .genre("autobiography")
                                .setting("Britain")
                                .build();

                when(booksRepository.findById(eq("spare"))).thenReturn(Optional.of(spare));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/books?code=spare")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(booksRepository, times(1)).findById("spare");
                verify(booksRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Books with id spare deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_book_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(booksRepository.findById(eq("potato"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/books?code=potato")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(booksRepository, times(1)).findById("potato");
                Map<String, Object> json = responseToJson(response);
                assertEquals("Books with id potato not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_commons() throws Exception {
                // arrange

                Books lotrOrig = Books.builder()
                                .name("The Lord of the Rings")
                                .title("lotr")
                                .genre("fantasy")
                                .setting("Middle Earth")
                                .build();
                Books lotrEdited = Books.builder()
                                .name("The Fellowship of the Ring")
                                .code("lotr")
                                .genre("fantasy")
                                .setting("The Shire")
                                .build();

                String requestBody = mapper.writeValueAsString(lotrEdited);

                when(booksRepository.findById(eq("lotr"))).thenReturn(Optional.of(lotrOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/books?code=lotr")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(booksRepository, times(1)).findById("lotr");
                verify(booksRepository, times(1)).save(lotrEdited); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_commons_that_does_not_exist() throws Exception {
                // arrange

                Books editedBook = Books.builder()
                                .name("potato")
                                .code("potato")
                                .genre(root)
                                .setting("Gaffer's Garden")
                                .build();

                String requestBody = mapper.writeValueAsString(editedBook);

                when(booksRepository.findById(eq("potato"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/books?code=potato")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(booksRepository, times(1)).findById("potato");
                Map<String, Object> json = responseToJson(response);
                assertEquals("Books with id potato not found", json.get("message"));

        }
}
