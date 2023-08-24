package com.votingsystem.frontendservice.feignController;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import resources.ResponseDTO;
import resources.candidate.*;

@FeignClient(name = "CANDIDATE-SERVICE")
public interface CandidateFeignController {

  /**
   * returns the list of parties available
   *
   * @param detail: to get parties detail including party image and creation date
   * @return :list of parties
   */
  @GetMapping("/candidate/party")
  public PartyListResponseDTO retrieveAllParties(@RequestParam(value = "detail", required = true) boolean detail);

  /**
   * to register the user as candidate and to update its role
   *
   * @param token             : to validate user request
   * @param candidateRequest: json containing data for creating candidate
   * @return :success response if every thing goes fine else failure response
   */
  @PostMapping("/candidate/register")
  public ResponseDTO registerUserAsCandidate(@CookieValue("Authorization") String token, @RequestParam(value = "candidate") String candidateRequest);

  /**
   * it will retrieve candidate with its details
   *
   * @param token:       token to validate request
   * @param candidateId: id of candidate
   * @return :return candidate response if candidate present else return null
   */
  @GetMapping("/candidate/{candidateId}")
  public CandidateResponseDTO retrieveCandidateDetail(@CookieValue("Authorization") String token, @PathVariable(name = "candidateId") long candidateId);

  @GetMapping("/candidate/party-management/party-management")
  public String showPartyManagementPage(Model model);

  /**
   * approve the request of voter to become candidate
   *
   * @param id: id of candidate
   * @return :return the success response if everything goes fine else return failure reason
   */
  @PostMapping("candidate/{candidate_id}/approve")
  public ResponseDTO approveCandidate(@PathVariable("candidate_id") long id);

  /**
   * decline the request of voter to become candidate
   *
   * @param id: id of candidate
   * @return :return the success response if everything goes fine else return failure reason
   */
  @PostMapping("candidate/{candidate_id}/decline")
  public ResponseDTO declineCandidate(@PathVariable("candidate_id") long id);

  /**
   * it will retrieve the list of candidate having basic info of each candidate
   *
   * @param token:    to validate the request
   * @param status:   to find active or inactive candidate list
   * @param pageable: to retrieve the custom size pageable objects of candidates
   * @return :return the object having pageable object of candidates if present else return null
   */
  @GetMapping("/candidate/candidates")
  public CandidatePageResponseDTO retrieveCandidates(@CookieValue("Authorization") String token, @RequestParam(value = "status", required = true) boolean status, Pageable pageable);

  /**
   * to create a party
   *
   * @param candidateId: id of candidate who want to create party
   * @param name:        name of party to be created
   * @param partyLogo:   image/logo of party-08-21T14:11:57.905+05:00  WARN 35934 --- [nio-3000-exec-5] .w.s.m.s.DefaultHandlerExceptionResolver : Resolved [org.springframework.web.bind.MissingRequestHeaderException: Required request header 'Authorization' for method parameter type String is not present]
   * @return :return the success response if everything goes fine else return failure reason
   */
  @PostMapping(value = "candidate/{candidateId}/party/{name}", consumes = {"multipart/form-data"}, headers = "Content-Type= multipart/form-data")
  public PartyResponseDTO createParty(@PathVariable long candidateId, @PathVariable String name, @RequestPart(value = "partyLogo", required = true) MultipartFile partyLogo);

  /**
   * it will retrieve the pageable object of candidates who are in specific constituency
   *
   * @param token:          to validate request
   * @param constituencyId: id of constituency
   * @param pageable:       to give custom size of pageable object
   * @return :will return the object having list of candidate else return null
   */
  @GetMapping("/candidate/{constituencyId}/constituency")
  public CandidatePageResponseDTO retrieveAllCandidatesInConstituency(@CookieValue("Authorization") String token, @PathVariable("constituencyId") long constituencyId, Pageable pageable);

  /**
   * to retrieve the basic info of candidate
   *
   * @param candidateId: id of candidate
   * @return :return the candidate basic response else return null
   */
  @GetMapping("/candidate/{candidateId}/get")
  public CandidateBasicResponseDTO retrieveCandidate(@PathVariable(name = "candidateId") long candidateId);

  @GetMapping("/candidate/party/{partyId}")
  public PartyListResponseDTO retrievePartiesById(@PathVariable long partyId);

}