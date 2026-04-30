package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = RecommendationRequestsController.class)
@Import(TestConfig.class)
public class RecommendationRequestControllerTests extends ControllerTestCase {

  @MockitoBean RecommendationRequestRepository recommendationRequestRepository;

  @MockitoBean UserRepository userRepository;

  // Authorization tests for /api/recommendationrequests/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/recommendationrequests/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/recommendationrequests/all")).andExpect(status().is(200)); // logged
  }

  // Authorization tests for /api/recommendationrequests

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/recommendationrequests?id=1"))
        .andExpect(status().is(403)); // logged out users can't get by id
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_by_id() throws Exception {
    // arrange
    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
    LocalDateTime ldt2 = LocalDateTime.parse("2022-03-03T00:00:00");

    RecommendationRequest request1 =
        RecommendationRequest.builder()
            .id(1L)
            .requesterEmail("test1@example.com")
            .professorEmail("prof1@example.com")
            .explanation("Need recommendation for grad school")
            .dateRequested(ldt1)
            .dateNeeded(ldt2)
            .done(false)
            .build();

    when(recommendationRequestRepository.findById(1L)).thenReturn(java.util.Optional.of(request1));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequests?id=1"))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(1L);
    String expectedJson = mapper.writeValueAsString(request1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_cannot_get_by_id_when_not_found() throws Exception {
    // arrange
    when(recommendationRequestRepository.findById(1L)).thenReturn(java.util.Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequests?id=1"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(1L);
    String responseString = response.getResponse().getContentAsString();
    assertTrue(responseString.contains("EntityNotFoundException"));
  }

  // Authorization tests for /api/recommendationrequests/post

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/recommendationrequests/post")
                .param("requesterEmail", "test@example.com")
                .param("professorEmail", "professor@example.com")
                .param("explanation", "Need recommendation for grad school")
                .param("dateRequested", "2022-01-03T00:00:00")
                .param("dateNeeded", "2022-03-03T00:00:00")
                .param("done", "false")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/recommendationrequests/post")
                .param("requesterEmail", "test@example.com")
                .param("professorEmail", "professor@example.com")
                .param("explanation", "Need recommendation for grad school")
                .param("dateRequested", "2022-01-03T00:00:00")
                .param("dateNeeded", "2022-03-03T00:00:00")
                .param("done", "false")
                .with(csrf()))
        .andExpect(status().is(403)); // only admins can post
  }

  // Tests with mocks for database actions

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_recommendation_requests() throws Exception {

    // arrange
    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
    LocalDateTime ldt2 = LocalDateTime.parse("2022-03-03T00:00:00");

    RecommendationRequest request1 =
        RecommendationRequest.builder()
            .requesterEmail("test1@example.com")
            .professorEmail("prof1@example.com")
            .explanation("Need recommendation for grad school")
            .dateRequested(ldt1)
            .dateNeeded(ldt2)
            .done(false)
            .build();

    LocalDateTime ldt3 = LocalDateTime.parse("2022-02-03T00:00:00");
    LocalDateTime ldt4 = LocalDateTime.parse("2022-04-03T00:00:00");

    RecommendationRequest request2 =
        RecommendationRequest.builder()
            .requesterEmail("test2@example.com")
            .professorEmail("prof2@example.com")
            .explanation("Need recommendation for job")
            .dateRequested(ldt3)
            .dateNeeded(ldt4)
            .done(true)
            .build();

    ArrayList<RecommendationRequest> expectedRequests = new ArrayList<>();
    expectedRequests.addAll(Arrays.asList(request1, request2));

    when(recommendationRequestRepository.findAll()).thenReturn(expectedRequests);

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequests/all"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(recommendationRequestRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedRequests);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_recommendation_request() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
    LocalDateTime ldt2 = LocalDateTime.parse("2022-03-03T00:00:00");

    RecommendationRequest request1 =
        RecommendationRequest.builder()
            .requesterEmail("test@example.com")
            .professorEmail("professor@example.com")
            .explanation("Need recommendation for grad school")
            .dateRequested(ldt1)
            .dateNeeded(ldt2)
            .done(false)
            .build();

    when(recommendationRequestRepository.save(eq(request1))).thenReturn(request1);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/recommendationrequests/post")
                    .param("requesterEmail", "test@example.com")
                    .param("professorEmail", "professor@example.com")
                    .param("explanation", "Need recommendation for grad school")
                    .param("dateRequested", "2022-01-03T00:00:00")
                    .param("dateNeeded", "2022-03-03T00:00:00")
                    .param("done", "false")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).save(request1);
    String expectedJson = mapper.writeValueAsString(request1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // Authorization tests for /api/recommendationrequests (PUT)

  @Test
  public void logged_out_users_cannot_update() throws Exception {
    mockMvc
        .perform(
            put("/api/recommendationrequests?id=1")
                .contentType("application/json")
                .content(
                    """
                    {
                        "id": 1,
                        "requesterEmail": "test@example.com",
                        "professorEmail": "professor@example.com",
                        "explanation": "Updated explanation",
                        "dateRequested": "2022-01-03T00:00:00",
                        "dateNeeded": "2022-03-03T00:00:00",
                        "done": false
                    }
                    """)
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_update() throws Exception {
    mockMvc
        .perform(
            put("/api/recommendationrequests?id=1")
                .contentType("application/json")
                .content(
                    """
                    {
                        "id": 1,
                        "requesterEmail": "test@example.com",
                        "professorEmail": "professor@example.com",
                        "explanation": "Updated explanation",
                        "dateRequested": "2022-01-03T00:00:00",
                        "dateNeeded": "2022-03-03T00:00:00",
                        "done": false
                    }
                    """)
                .with(csrf()))
        .andExpect(status().is(403)); // only admins can update
  }

  // Tests with mocks for database actions

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_update_a_recommendation_request() throws Exception {
    // arrange
    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
    LocalDateTime ldt2 = LocalDateTime.parse("2022-03-03T00:00:00");
    LocalDateTime ldt3 = LocalDateTime.parse("2023-06-15T00:00:00");
    LocalDateTime ldt4 = LocalDateTime.parse("2023-09-01T00:00:00");

    RecommendationRequest originalRequest =
        RecommendationRequest.builder()
            .requesterEmail("original@example.com")
            .professorEmail("originalProf@example.com")
            .explanation("Original explanation")
            .dateRequested(ldt1)
            .dateNeeded(ldt2)
            .done(false)
            .build();

    RecommendationRequest updatedRequest =
        RecommendationRequest.builder()
            .requesterEmail("updated@example.com")
            .professorEmail("updatedProf@example.com")
            .explanation("Updated explanation")
            .dateRequested(ldt3)
            .dateNeeded(ldt4)
            .done(true)
            .build();

    String requestBody = mapper.writeValueAsString(updatedRequest);

    when(recommendationRequestRepository.findById(eq(1L)))
        .thenReturn(java.util.Optional.of(originalRequest));
    when(recommendationRequestRepository.save(any(RecommendationRequest.class)))
        .thenReturn(updatedRequest);

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/recommendationrequests")
                    .param("id", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(1L);
    verify(recommendationRequestRepository, times(1)).save(any(RecommendationRequest.class));
    String responseString = response.getResponse().getContentAsString();
    assertTrue(responseString.contains("\"requesterEmail\":\"updated@example.com\""));
    assertTrue(responseString.contains("\"professorEmail\":\"updatedProf@example.com\""));
    assertTrue(responseString.contains("\"explanation\":\"Updated explanation\""));
    assertTrue(responseString.contains("\"done\":true"));
    assertTrue(responseString.contains("\"dateRequested\":\"2023-06-15T00:00:00\""));
    assertTrue(responseString.contains("\"dateNeeded\":\"2023-09-01T00:00:00\""));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_cannot_update_when_not_found() throws Exception {
    // arrange
    RecommendationRequest updatedRequest =
        RecommendationRequest.builder()
            .requesterEmail("updated@example.com")
            .professorEmail("updatedProf@example.com")
            .explanation("Updated explanation")
            .done(true)
            .build();

    String requestBody = mapper.writeValueAsString(updatedRequest);
    when(recommendationRequestRepository.findById(eq(1L))).thenReturn(java.util.Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/recommendationrequests")
                    .param("id", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(1L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("RecommendationRequest with id 1 not found", json.get("message"));
  }

  // Authorization tests for /api/recommendationrequests (DELETE)

  @Test
  public void logged_out_users_cannot_delete() throws Exception {
    mockMvc
        .perform(delete("/api/recommendationrequests?id=1").with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_delete() throws Exception {
    mockMvc
        .perform(delete("/api/recommendationrequests?id=1").with(csrf()))
        .andExpect(status().is(403)); // only admins can delete
  }

  // Tests with mocks for database actions

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_delete_a_recommendation_request() throws Exception {
    // arrange
    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
    LocalDateTime ldt2 = LocalDateTime.parse("2022-03-03T00:00:00");

    RecommendationRequest request1 =
        RecommendationRequest.builder()
            .id(1L)
            .requesterEmail("test@example.com")
            .professorEmail("professor@example.com")
            .explanation("Need recommendation for grad school")
            .dateRequested(ldt1)
            .dateNeeded(ldt2)
            .done(false)
            .build();

    when(recommendationRequestRepository.findById(1L)).thenReturn(java.util.Optional.of(request1));
    doNothing().when(recommendationRequestRepository).delete(request1);

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/recommendationrequests?id=1").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(1L);
    verify(recommendationRequestRepository, times(1)).delete(request1);
    String responseString = response.getResponse().getContentAsString();
    assertTrue(responseString.contains("RecommendationRequest with id 1 deleted"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_cannot_delete_when_not_found() throws Exception {
    // arrange
    when(recommendationRequestRepository.findById(1L)).thenReturn(java.util.Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/recommendationrequests?id=1").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(1L);
    String responseString = response.getResponse().getContentAsString();
    assertTrue(responseString.contains("EntityNotFoundException"));
  }
}
