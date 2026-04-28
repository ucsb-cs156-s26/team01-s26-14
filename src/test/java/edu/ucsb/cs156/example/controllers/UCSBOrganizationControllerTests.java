package edu.ucsb.cs156.example.controllers;

import static org.mockito.ArgumentMatchers.any;
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
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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
}
