package com.votingsystem.frontendservice.feignController;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import resources.ResponseDTO;
import resources.voter.*;
import resources.voter.VoterPageResponseDTO;
import resources.voter.VoterResponseDTO;

@FeignClient(name = "VOTER-SERVICE")
public interface VoterFeignController {

  /**
   * to create the voter
   *
   * @param voterRequestDTO: contains the user id and constituency id
   * @return : return success if created successfully else return failure exception
   */
  @PostMapping("/voter")
  public ResponseDTO createVoter(@RequestBody VoterRequestDTO voterRequestDTO);

  /**
   * check if voter is registered in constituency or not
   *
   * @param token:   to validate request
   * @param voterId: voter id
   * @return : return the voter response if voter is present else return null
   */
  @GetMapping("/voter/{voterId}")
  public VoterResponseDTO isVoterRegisteredInConstituency(@CookieValue("Authorization") String token, @PathVariable(name = "voterId") long voterId);

  /**
   * to retrieve voters in constituency
   *
   * @param token:          to validate user
   * @param constituencyId: constituency id
   * @param pageable:       to give custom size
   * @return :return pageable object of voter response else return null
   */
  @GetMapping("voter/all/{constituencyId}")
  public VoterPageResponseDTO retrieveAllVotersInConstituency(@CookieValue("Authorization") String token, @PathVariable(name = "constituencyId") long constituencyId, Pageable pageable);

  /**
   * get the constituency of voter
   *
   * @param voterId: if of voter
   * @return :return the voter response if found else return null
   */
  @GetMapping("voter/{voterId}/constituency")
  public VoterResponseDTO getVoterConstituency(@PathVariable(name = "voterId") long voterId);

  /**
   * to cast the vote
   *
   * @param voteRequestDTO: contains the resource to cast vote
   * @return : return success response if vote cast successfully else return failure exception
   */
  @PostMapping("/votes")
  public ResponseDTO castVote(@CookieValue("Authorization") String token, @RequestBody VoteRequestDTO voteRequestDTO);

  @GetMapping("/votes/vote/cast")
  public boolean isVoteCasted(@RequestParam long voterId, @RequestParam long pollingId);
}
