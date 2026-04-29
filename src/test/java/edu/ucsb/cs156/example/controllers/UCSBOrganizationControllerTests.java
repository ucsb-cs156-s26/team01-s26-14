package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = UCSBOrganizationController.class)
@Import(TestConfig.class)
public class UCSBOrganizationControllerTests extends ControllerTestCase {
  @MockitoBean UCSBOrganizationRepository ucsbOrganizationRepository;

  @MockitoBean UserRepository userRepository;

  // Authorization tests for /api/ucsbdiningcommons/admin/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/ucsborganization/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    UCSBOrganization org1 =
        UCSBOrganization.builder()
            .orgCode("Brundage")
            .orgTranslationShort("Brdg")
            .orgTranslation("B")
            .inactive(true)
            .build();

    UCSBOrganization org2 =
        UCSBOrganization.builder()
            .orgCode("Cheadle")
            .orgTranslationShort("Chdl")
            .orgTranslation("C")
            .inactive(false)
            .build();

    ArrayList<UCSBOrganization> expectedOrganizations = new ArrayList<>();
    expectedOrganizations.addAll(Arrays.asList(org1, org2));

    when(ucsbOrganizationRepository.findAll()).thenReturn(expectedOrganizations);

    mockMvc
        .perform(get("/api/ucsborganization/all"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].orgCode").value("Brundage"))
        .andExpect(jsonPath("$[0].orgTranslationShort").value("Brdg"))
        .andExpect(jsonPath("$[0].orgTranslation").value("B"))
        .andExpect(jsonPath("$[0].inactive").value(true))
        .andExpect(jsonPath("$[1].orgCode").value("Cheadle"))
        .andExpect(jsonPath("$[1].orgTranslationShort").value("Chdl"))
        .andExpect(jsonPath("$[1].orgTranslation").value("C"))
        .andExpect(jsonPath("$[1].inactive").value(false));

    verify(ucsbOrganizationRepository, times(1)).findAll();
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/ucsborganization/post")
                .param("orgCode", "Brundage")
                .param("orgTranslationShort", "Brdg")
                .param("orgTranslation", "B")
                .param("inactive", "true")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/ucsborganization/post")
                .param("orgCode", "Brundage")
                .param("orgTranslationShort", "Brdg")
                .param("orgTranslation", "B")
                .param("inactive", "true")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"ADMIN"})
  @Test
  public void logged_in_admin_users_can_post() throws Exception {
    when(ucsbOrganizationRepository.save(any(UCSBOrganization.class)))
        .thenAnswer(
            new org.mockito.stubbing.Answer<UCSBOrganization>() {
              @Override
              public UCSBOrganization answer(org.mockito.invocation.InvocationOnMock invocation)
                  throws Throwable {
                return invocation.getArgument(0);
              }
            });

    mockMvc
        .perform(
            post("/api/ucsborganization/post")
                .param("orgCode", "Brundage")
                .param("orgTranslationShort", "Brdg")
                .param("orgTranslation", "B")
                .param("inactive", "true")
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orgCode").value("Brundage"))
        .andExpect(jsonPath("$.orgTranslationShort").value("Brdg"))
        .andExpect(jsonPath("$.orgTranslation").value("B"))
        .andExpect(jsonPath("$.inactive").value(true));

    verify(ucsbOrganizationRepository, times(1)).save(any(UCSBOrganization.class));
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/ucsborganization").param("orgCode", "Brundage"))
        .andExpect(status().is(403)); // logged out users can't get by id
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    // arrange

    UCSBOrganization organization =
        UCSBOrganization.builder()
            .orgCode("Brundage")
            .orgTranslationShort("Brdg")
            .orgTranslation("B")
            .inactive(true)
            .build();

    when(ucsbOrganizationRepository.findById(eq("Brundage"))).thenReturn(Optional.of(organization));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsborganization").param("orgCode", "Brundage"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(ucsbOrganizationRepository, times(1)).findById(eq("Brundage"));
    String expectedJson = mapper.writeValueAsString(organization);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

    // arrange

    when(ucsbOrganizationRepository.findById(eq("munger-hall"))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsborganization").param("orgCode", "munger-hall"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert

    verify(ucsbOrganizationRepository, times(1)).findById(eq("munger-hall"));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("UCSBOrganization with id munger-hall not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_organization() throws Exception {
    // arrange

    UCSBOrganization existingOrganization =
        UCSBOrganization.builder()
            .orgCode("DSClub")
            .orgTranslationShort("DSC")
            .orgTranslation("Data Science Club")
            .inactive(false)
            .build();

    UCSBOrganization incomingOrganization =
        UCSBOrganization.builder()
            .orgCode("DifferentCode")
            .orgTranslationShort("DSC//")
            .orgTranslation("Data Science Club//")
            .inactive(true)
            .build();

    UCSBOrganization expectedOrganization =
        UCSBOrganization.builder()
            .orgCode("DSClub")
            .orgTranslationShort("DSC//")
            .orgTranslation("Data Science Club//")
            .inactive(true)
            .build();

    String requestBody = mapper.writeValueAsString(incomingOrganization);

    when(ucsbOrganizationRepository.findById(eq("DSClub")))
        .thenReturn(Optional.of(existingOrganization));

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/ucsborganization")
                    .param("orgCode", "DSClub")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById(eq("DSClub"));
    verify(ucsbOrganizationRepository, times(1)).save(expectedOrganization);

    String expectedJson = mapper.writeValueAsString(expectedOrganization);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_organization_that_does_not_exist() throws Exception {
    // arrange

    UCSBOrganization editedOrganization =
        UCSBOrganization.builder()
            .orgCode("DSClub")
            .orgTranslationShort("DSC//")
            .orgTranslation("Data Science Club//")
            .inactive(true)
            .build();

    String requestBody = mapper.writeValueAsString(editedOrganization);

    when(ucsbOrganizationRepository.findById(eq("DSClub"))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/ucsborganization")
                    .param("orgCode", "DSClub")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("DSClub");
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id DSClub not found", json.get("message"));
  }

  // Authorization tests for /api/ucsbdiningcommons/post
  // (Perhaps should also have these for put and delete)
  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_a_date() throws Exception {
    // arrange

    UCSBOrganization DSClub =
        UCSBOrganization.builder()
            .orgCode("DSClub")
            .orgTranslationShort("DSC")
            .orgTranslation("Data Science Club")
            .inactive(false)
            .build();

    when(ucsbOrganizationRepository.findById(eq("DSClub"))).thenReturn(Optional.of(DSClub));

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/ucsborganization").param("orgCode", "DSClub").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("DSClub");
    verify(ucsbOrganizationRepository, times(1)).delete(any());

    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id DSClub deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_tries_to_delete_non_existant_commons_and_gets_right_error_message()
      throws Exception {
    // arrange

    when(ucsbOrganizationRepository.findById(eq("DSClub"))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/ucsborganization").param("orgCode", "DSClub").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("DSClub");
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id DSClub not found", json.get("message"));
  }
}
