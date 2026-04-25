package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for HelpRequests */
@Tag(name = "HelpRequests")
@RequestMapping("/api/helprequests")
@RestController
@Slf4j
public class HelpRequestController extends ApiController {

  @Autowired HelpRequestRepository helpRequestRepository;

  /**
   * List all HelpRequests
   *
   * @return an iterable of HelpRequest
   */
  @Operation(summary = "List all help requests")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<HelpRequest> allHelpRequests() {
    Iterable<HelpRequest> requests = helpRequestRepository.findAll();
    return requests;
  }

  /**
   * Create a new help request
   *
   * @param requesterEmail the email of the requester
   * @param teamId the team id
   * @param tableOrBreakoutRoom the table or breakout room
   * @param explanation the explanation of the issue
   * @param solved whether the issue has been solved
   * @param requestTime the time the request was made
   * @return the saved HelpRequest
   */
  @Operation(summary = "Create a new help request")
  @PreAuthorize("hasRole('ROLE_USER')")
  @PostMapping("/post")
  public HelpRequest postHelpRequest(
      @Parameter(name = "requesterEmail") @RequestParam String requesterEmail,
      @Parameter(name = "teamId") @RequestParam String teamId,
      @Parameter(name = "tableOrBreakoutRoom") @RequestParam String tableOrBreakoutRoom,
      @Parameter(name = "explanation") @RequestParam String explanation,
      @Parameter(name = "solved") @RequestParam boolean solved,
      @Parameter(
              name = "requestTime",
              description =
                  "date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)")
          @RequestParam("requestTime")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime requestTime) {

    log.info("requestTime={}", requestTime);

    HelpRequest helpRequest = new HelpRequest();
    helpRequest.setRequesterEmail(requesterEmail);
    helpRequest.setTeamId(teamId);
    helpRequest.setTableOrBreakoutRoom(tableOrBreakoutRoom);
    helpRequest.setExplanation(explanation);
    helpRequest.setSolved(solved);
    helpRequest.setRequestTime(requestTime);

    HelpRequest savedHelpRequest = helpRequestRepository.save(helpRequest);

    return savedHelpRequest;
  }

  /**
   * Get a single help request by id
   *
   * @param id the id of the help request
   * @return a HelpRequest
   */
  @Operation(summary = "Get a single help request")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public HelpRequest getById(@Parameter(name = "id") @RequestParam Long id) {
    HelpRequest helpRequest =
        helpRequestRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(HelpRequest.class, id));

    return helpRequest;
  }
}
