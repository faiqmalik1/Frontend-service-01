package com.votingsystem.frontendservice.controller;

import CustomException.CommonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.votingsystem.frontendservice.feignController.CandidateFeignController;
import com.votingsystem.frontendservice.feignController.ConstituencyFeignController;
import com.votingsystem.frontendservice.feignController.UserController;
import com.votingsystem.frontendservice.feignController.VoterFeignController;
import constants.Constants;
import constants.Data;
import constants.ReturnMessage;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import resources.candidate.CandidatePageResponseDTO;
import resources.candidate.CandidateResponseDTO;
import resources.user.ValidateResponseDTO;
import resources.voter.VoterPageResponseDTO;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/ui")
public class CandidateController {

  private final CandidateFeignController candidateFeignController;
  private final VoterFeignController voterController;
  private final UserController userController;

  @GetMapping("/show-candidates")
  public ModelAndView showCandidates(@CookieValue("Authorization") String token, Pageable pageable) {
    try {
      ModelAndView model = new ModelAndView("request-candidates");
      ValidateResponseDTO validateResponseDTO = userController.validateToken(token);
      CandidatePageResponseDTO candidatePageResponseDTO = candidateFeignController.retrieveCandidates(token, false, pageable);
      if (candidatePageResponseDTO != null) {
        model.addObject(Data.CANDIDATES.getValue(), candidatePageResponseDTO.getCandidateResponsePage());
      } else {
        model.addObject(Data.MESSAGE.getValue(), ReturnMessage.NO_CANDIDATE_FOUND.getValue());
      }
      model.addObject(Data.USERROLE.getValue(), validateResponseDTO.getUserRole().toLowerCase());
      return model;
    } catch (CommonException | FeignException ex) {
      return new ModelAndView("error");
    }
  }

  @GetMapping("/approved-candidates")
  public ModelAndView showApprovedCandidates(@CookieValue("Authorization") String token, Pageable pageable) {
    try {
      ModelAndView modelAndView = new ModelAndView("approved-candidates");
      ValidateResponseDTO validateResponseDTO = userController.validateToken(token);
      CandidatePageResponseDTO candidatePageResponseDTO = candidateFeignController.retrieveCandidates(token, true, pageable);
      if (candidatePageResponseDTO != null) {
        modelAndView.addObject("candidates", candidatePageResponseDTO.getCandidateResponsePage());
      } else {
        modelAndView.addObject(Data.MESSAGE.getValue(), ReturnMessage.NO_CANDIDATE_FOUND.getValue());
      }
      modelAndView.addObject("userRole", validateResponseDTO.getUserRole().toLowerCase());
      return modelAndView;
    } catch (CommonException | FeignException ex) {
      return new ModelAndView("error");
    }
  }

  @PostMapping("/approve-candidate/{candidateId}")
  public ModelAndView approveCandidate(@CookieValue("Authorization") String token, @PathVariable long candidateId) {
    try {
      ModelAndView modelAndView = new ModelAndView("redirect:/ui/request-candidates");
      ValidateResponseDTO validateResponseDTO = userController.validateToken(token);
      if (!validateResponseDTO.getUserRole().equalsIgnoreCase(Constants.ADMIN)) {
        modelAndView.addObject(Data.MESSAGE.getValue(), "You are not Authorized for this action");
        return modelAndView;
      }
      candidateFeignController.approveCandidate(candidateId);
      userController.updateUserRole(token, candidateId, Data.CANDIDATE.getValue());
      return modelAndView;
    } catch (CommonException | FeignException ex) {
      return new ModelAndView("error");
    }
  }

  @PostMapping("/decline-candidate/{candidateId}")
  public ModelAndView declineCandidate(@CookieValue("Authorization") String token, @PathVariable Long candidateId) {
    try {
      ModelAndView modelAndView = new ModelAndView("redirect:/ui/request-candidates");
      ValidateResponseDTO validateResponseDTO = userController.validateToken(token);
      if (!validateResponseDTO.getUserRole().equalsIgnoreCase(Constants.ADMIN)) {
        modelAndView.addObject(Data.MESSAGE.getValue(), "You are not Authorized for this action");
        return modelAndView;
      }
      candidateFeignController.declineCandidate(candidateId);
      return modelAndView;
    } catch (CommonException | FeignException ex) {
      return new ModelAndView("error");
    }
  }


  @GetMapping("/constituency-voter")
  public String retrieveVoterInConstituency(Model model, @CookieValue("Authorization") String token, Pageable pageable) {
    try {
      ValidateResponseDTO validateResponseDTO = userController.validateToken(token);
      CandidateResponseDTO candidateResponseDTO = candidateFeignController.retrieveCandidateDetail(token, validateResponseDTO.getUserId());
      VoterPageResponseDTO voterPageResponseDTO = voterController.retrieveAllVotersInConstituency(token, candidateResponseDTO.getConstituencyId(), pageable);
      if (voterPageResponseDTO != null) {
        model.addAttribute(Data.VOTERS.getValue(), voterPageResponseDTO.getVoterResponseList());
      } else {
        model.addAttribute(Data.MESSAGE.getValue(), ReturnMessage.VOTER_NOT_EXISTS.getValue());
      }
      return "voter-list";
    } catch (CommonException | FeignException ex) {
      return "error";
    }
  }

  @GetMapping("/candidate-panel")
  public ModelAndView showLoginPage(@CookieValue("Authorization") String token) {
    try {
      ValidateResponseDTO validateResponseDTO = userController.validateToken(token);
      ModelAndView modelAndView = new ModelAndView("candidate-panel");
      modelAndView.addObject(Data.USERROLE.getValue(), validateResponseDTO.getUserRole().toLowerCase());
      if (validateResponseDTO.getProfile() == null) {
        modelAndView.addObject(Data.PROFILE.getValue(), false);
      }
      return modelAndView;
    } catch (CommonException | FeignException ex) {
      return new ModelAndView("error");
    }
  }

}