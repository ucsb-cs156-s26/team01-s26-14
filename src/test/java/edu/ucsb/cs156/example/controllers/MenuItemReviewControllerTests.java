package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = MenuItemReviewController.class)
@Import(TestConfig.class)
public class MenuItemReviewControllerTests extends ControllerTestCase {

  @MockitoBean MenuItemReviewRepository menuItemReviewRepository;

  @MockitoBean UserRepository userRepository;

  // Authorization tests for /api/ucsbdates/admin/all
  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/menuitemreview/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/menuitemreview/all")).andExpect(status().is(200)); // logged
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/menuitemreview/post")
                .param("itemId", "217")
                .param("reviewerEmail", "liljit@ucsb.edu")
                .param("stars", "1")
                .param("dateReviewed", "2026-04-29T22:15:50")
                .param("comments", "mid")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/menuitemreview/post")
                .param("itemId", "217")
                .param("reviewerEmail", "liljit@ucsb.edu")
                .param("stars", "1")
                .param("dateReviewed", "2026-04-29T22:15:50")
                .param("comments", "mid")
                .with(csrf()))
        .andExpect(status().is(403)); // only admins can post
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_menuitemreviews() throws Exception {

    // arrange
    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

    MenuItemReview menuItemReview1 =
        MenuItemReview.builder()
            .itemId(12)
            .reviewerEmail("liljit@gmail.com")
            .stars(2)
            .dateReviewed(ldt1)
            .comments("eh")
            .build();

    LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");

    MenuItemReview menuItemReview2 =
        MenuItemReview.builder()
            .itemId(22)
            .reviewerEmail("liljit@gmail.com")
            .stars(5)
            .dateReviewed(ldt2)
            .comments("peak")
            .build();
    ArrayList<MenuItemReview> expectedMenuItemReviews = new ArrayList<>();
    expectedMenuItemReviews.addAll(Arrays.asList(menuItemReview2, menuItemReview1));

    when(menuItemReviewRepository.findAll()).thenReturn(expectedMenuItemReviews);

    // act
    MvcResult response =
        mockMvc.perform(get("/api/menuitemreview/all")).andExpect(status().isOk()).andReturn();

    // assert

    verify(menuItemReviewRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedMenuItemReviews);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_menuitemreview() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

    MenuItemReview menuItemReview1 =
        MenuItemReview.builder()
            .itemId(12)
            .reviewerEmail("liljit@gmail.com")
            .stars(5)
            .dateReviewed(ldt1)
            .comments("eh")
            .build();

    when(menuItemReviewRepository.save(eq(menuItemReview1))).thenReturn(menuItemReview1);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/menuitemreview/post")
                    .param("itemId", "12")
                    .param("reviewerEmail", "liljit@gmail.com")
                    .param("stars", "5")
                    .param("dateReviewed", "2022-01-03T00:00:00")
                    .param("comments", "eh")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(menuItemReviewRepository, times(1)).save(menuItemReview1);
    String expectedJson = mapper.writeValueAsString(menuItemReview1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }
}
