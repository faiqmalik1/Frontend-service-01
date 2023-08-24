package com.votingsystem.frontendservice.controller;

import CustomException.CommonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.votingsystem.frontendservice.feignController.CandidateFeignController;
import com.votingsystem.frontendservice.feignController.ConstituencyFeignController;
import com.votingsystem.frontendservice.feignController.UserController;
import constants.Constants;
import constants.Data;
import constants.ReturnMessage;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import resources.ResponseDTO;
import resources.candidate.CandidateRequestDTO;
import resources.candidate.PartyListResponseDTO;
import resources.candidate.PartyResponseDTO;
import resources.constituency.ConstituencyListResponseDTO;
import resources.user.ValidateResponseDTO;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/ui")
public class PartyManagementController {

  private final CandidateFeignController candidateController;
  private final ConstituencyFeignController constituencyController;
  private final ObjectMapper mapper;
  private final UserController userController;

  @GetMapping("/party-management")
  public ModelAndView showPartyManagementPage(@CookieValue("Authorization") String token, @RequestParam long partyId) {
    try {
      ValidateResponseDTO validateResponseDTO = userController.validateToken(token);
      ModelAndView modelAndView = new ModelAndView("become_candidate");
      PartyListResponseDTO partyList = candidateController.retrievePartiesById(partyId);
      ConstituencyListResponseDTO halkaList = constituencyController.retrieveConstituencies();
      modelAndView.addObject(Data.PARTY_LIST.getValue(), partyList.getPartyResponseDTOList());
      modelAndView.addObject(Data.HALKA_LIST.getValue(), halkaList.getConstituencyResponseDTOList());
      modelAndView.addObject(Data.USERROLE.getValue(), validateResponseDTO.getUserRole());
      return modelAndView;
    } catch (CommonException | FeignException ex) {
      return new ModelAndView("error");
    }
  }

  @PostMapping("/party-management/confirm")
  public ModelAndView confirmPartySelection(@RequestParam("halkaId") Long halkaId, @RequestParam("partyId") Long partyId, @CookieValue("Authorization") String token) throws JsonProcessingException {
    ModelAndView modelAndView = new ModelAndView("become_candidate");
    try {
      ValidateResponseDTO validateResponseDTO = userController.validateToken(token);
      if (!validateResponseDTO.getUserRole().equals(Constants.VOTER)) {
        modelAndView.addObject(Data.MESSAGE.getValue(), "You are not eligible for Candidate Ship");
        modelAndView.addObject("code", 1);
        return modelAndView;
      }
      CandidateRequestDTO candidateRequestDTO = new CandidateRequestDTO();
      candidateRequestDTO.setPartyId(partyId);
      candidateRequestDTO.setParty_name(null);
      candidateRequestDTO.setConstituencyId(halkaId);
      candidateRequestDTO.setUserId(validateResponseDTO.getUserId());
      candidateRequestDTO.setPost("MNA");
      ResponseDTO responseDTO = candidateController.registerUserAsCandidate(token, mapper.writeValueAsString(candidateRequestDTO));
      modelAndView.addObject(Data.USERROLE.getValue(), validateResponseDTO.getUserRole());
      if (responseDTO.getResponseCode() == 0) {
        modelAndView.addObject(Data.MESSAGE.getValue(), "Your request has been sent to admin, Wait for Approval!");
        modelAndView.addObject("code", 1);
      } else {
        modelAndView.addObject(Data.MESSAGE.getValue(), responseDTO.getErrorMessage());
        modelAndView.addObject("code", 1);
      }
      return modelAndView;
    } catch (CommonException ex) {
      String errorMessage = ex.getMessage();
      modelAndView.addObject(Data.MESSAGE.getValue(), errorMessage);
      return modelAndView;
    } catch (FeignException ex) {
      modelAndView.addObject(Data.MESSAGE.getValue(), ReturnMessage.INVALID_REQUEST.getValue());
      return modelAndView;
    }
  }

  @PostMapping("/party-management/create-party")
  public ModelAndView createParty(@CookieValue("Authorization") String token, @RequestParam("partyName") String partyName, @RequestPart("partyLogo") MultipartFile partyLogo) {
    try {

      ValidateResponseDTO validateResponseDTO = userController.validateToken(token);
      PartyResponseDTO partyResponseDTO = candidateController.createParty(validateResponseDTO.getUserId(), partyName, partyLogo);
      ModelAndView modelAndView = new ModelAndView("become_candidate");
      if (!validateResponseDTO.getUserRole().equals(Constants.VOTER)) {
        modelAndView.addObject(Data.MESSAGE.getValue(), "You are not eligible for Candidate Ship");
        modelAndView.addObject("code", 1);
        return modelAndView;
      }
      modelAndView.addObject(Data.MESSAGE.getValue(), partyResponseDTO.getErrorMessage());
      modelAndView.addObject("code", partyResponseDTO.getResponseCode());
      modelAndView.addObject(Data.USERROLE.getValue(), validateResponseDTO.getUserRole());
      PartyListResponseDTO partyList = candidateController.retrievePartiesById(partyResponseDTO.getPartyId());
      ConstituencyListResponseDTO halkaList = constituencyController.retrieveConstituencies();
      modelAndView.addObject(Data.PARTY_LIST.getValue(), partyList.getPartyResponseDTOList());
      modelAndView.addObject(Data.HALKA_LIST.getValue(), halkaList.getConstituencyResponseDTOList());
      return modelAndView;
    } catch (CommonException | FeignException ex) {
      ModelAndView modelAndView = new ModelAndView("error");
      modelAndView.addObject(Data.MESSAGE.getValue(), ex.getMessage());
      return modelAndView;
    }
  }

}