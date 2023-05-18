package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Recipes;
import edu.ucsb.cs156.example.repositories.RecipesRepository;

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

@WebMvcTest(controllers = RecipesController.class)
@Import(TestConfig.class)
public class RecipesControllerTests extends ControllerTestCase {

        @MockBean
        RecipesRepository recipeRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/recipes/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/recipes/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/recipes/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/recipes?id=66"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/recipes/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/recipes/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/recipes/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                Recipes okonomiyaki = Recipes.builder()
                                .name("Okonomiyaki")
                                .mealType("snack")
                                .prepTime("15m")
                                .cookTime("10m")
                                .totalCalories("450")
                                .build();

                when(recipeRepository.findById(eq(7L))).thenReturn(Optional.of(okonomiyaki));

                // act
                MvcResult response = mockMvc.perform(get("/api/recipes?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(recipeRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(okonomiyaki);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(recipeRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/recipes?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(recipeRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Recipes with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_recipes() throws Exception {

                // arrange

                Recipes okonomiyaki = Recipes.builder()
                                .name("Okonomiyaki")
                                .mealType("snack")
                                .prepTime("15m")
                                .cookTime("10m")
                                .totalCalories("450")
                                .build();

                Recipes frittata = Recipes.builder()
                                .name("Frittata")
                                .mealType("breakfast")
                                .prepTime("30m")
                                .cookTime("30m")
                                .totalCalories("470")
                                .build();

                ArrayList<Recipes> expectedRecipe = new ArrayList<>();
                expectedRecipe.addAll(Arrays.asList(okonomiyaki, frittata));

                when(recipeRepository.findAll()).thenReturn(expectedRecipe);

                // act
                MvcResult response = mockMvc.perform(get("/api/recipes/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(recipeRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedRecipe);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_recipe() throws Exception {
                // arrange

                Recipes gyudon = Recipes.builder()
                                .name("gyudon")
                                .mealType("dinner")
                                .prepTime("20m")
                                .cookTime("30m")
                                .totalCalories("400")
                                .build();

                when(recipeRepository.save(eq(gyudon))).thenReturn(gyudon);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/recipes/post?name=gyudon&mealType=dinner&prepTime=20m&cookTime=30m&totalCalories=400")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(recipeRepository, times(1)).save(gyudon);
                String expectedJson = mapper.writeValueAsString(gyudon);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_recipe() throws Exception {
                // arrange

                Recipes gyudon = Recipes.builder()
                                .name("gyudon")
                                .mealType("dinner")
                                .prepTime("20m")
                                .cookTime("30m")
                                .totalCalories("400")
                                .build();

                when(recipeRepository.findById(eq(66L))).thenReturn(Optional.of(gyudon));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/recipes?id=66")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(recipeRepository, times(1)).findById(66L);
                verify(recipeRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Recipes with id 66L deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_recipe_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(recipeRepository.findById(eq(66L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/recipes?id=66")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(recipeRepository, times(1)).findById(66L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Recipes with id 66 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_recipe() throws Exception {
                // arrange

                Recipes gyudon = Recipes.builder()
                                .name("gyudon")
                                .mealType("dinner")
                                .prepTime("20m")
                                .cookTime("30m")
                                .totalCalories("400")
                                .build();

                Recipes gyudonEdited = Recipes.builder()
                                .name("gyudon")
                                .mealType("lunch")
                                .prepTime("20m")
                                .cookTime("30m")
                                .totalCalories("350")
                                .build();

                String requestBody = mapper.writeValueAsString(gyudonEdited);

                when(recipeRepository.findById(eq(662L))).thenReturn(Optional.of(gyudon));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/recipes?id=662")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(recipeRepository, times(1)).findById(662L);
                verify(recipeRepository, times(1)).save(gyudonEdited); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_recipe_that_does_not_exist() throws Exception {
                // arrange

                Recipes gyudonEdited = Recipes.builder()
                                .name("gyudon")
                                .mealType("lunch")
                                .prepTime("20m")
                                .cookTime("30m")
                                .totalCalories("350")
                                .build();


                String requestBody = mapper.writeValueAsString(gyudonEdited);

                when(recipeRepository.findById(eq(662L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/recipes?id=662")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(recipeRepository, times(1)).findById(662L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Recipes with id 662 not found", json.get("message"));

        }
}
