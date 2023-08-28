package com.votingsystem.frontendservice.controller;

import CustomException.CommonException;
import com.votingsystem.frontendservice.feignController.CandidateFeignController;
import com.votingsystem.frontendservice.feignController.ConstituencyFeignController;
import com.votingsystem.frontendservice.feignController.UserController;
import constants.Constants;
import constants.Data;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import resources.candidate.PartyListResponseDTO;
import resources.candidate.PartyResponseDTO;
import resources.constituency.ConstituencyListResponseDTO;
import resources.constituency.ConstituencyRequestDTO;
import resources.constituency.ConstituencyResponseDTO;
import resources.user.ValidateResponseDTO;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/ui")
public class ConstituencyController {

  private final ConstituencyFeignController constituencyFeignController;
  private final CandidateFeignController candidateFeignController;
  private final UserController userController;

  @GetMapping("/constituencies")
  public String constituencies(@CookieValue("Authorization") String token, Model model) {
    try {
      ValidateResponseDTO validateResponseDTO = userController.validateToken(token);
      ConstituencyListResponseDTO constituencyListResponseDTO = constituencyFeignController.retrieveConstituencies();
      if (constituencyListResponseDTO != null && constituencyListResponseDTO.getConstituencyResponseDTOList() != null && !constituencyListResponseDTO.getConstituencyResponseDTOList().isEmpty()) {
        List<ConstituencyResponseDTO> constituencies = constituencyListResponseDTO.getConstituencyResponseDTOList();
        model.addAttribute(Data.CONSTITUENCIES.getValue(), constituencies);
        PartyListResponseDTO partyListResponseDTO = candidateFeignController.retrieveAllParties(true);
        if (partyListResponseDTO != null && partyListResponseDTO.getPartyResponseDTOList() != null && !partyListResponseDTO.getPartyResponseDTOList().isEmpty()) {
          List<PartyResponseDTO> parties = partyListResponseDTO.getPartyResponseDTOList();
          model.addAttribute(Data.PARTIES.getValue(), parties);
        }
      } else {
        constituencyFeignController.createConstituency();
        candidateFeignController.createParty();
        constituencyListResponseDTO = constituencyFeignController.retrieveConstituencies();
        List<ConstituencyResponseDTO> constituencies = constituencyListResponseDTO.getConstituencyResponseDTOList();
        model.addAttribute(Data.CONSTITUENCIES.getValue(), constituencies);
        PartyListResponseDTO partyListResponseDTO = candidateFeignController.retrieveAllParties(true);
        List<PartyResponseDTO> parties = partyListResponseDTO.getPartyResponseDTOList();
        model.addAttribute(Data.PARTIES.getValue(), parties);
      }
      model.addAttribute(Data.USERROLE.getValue(), validateResponseDTO.getUserRole().toLowerCase());
      return "all-constituencies";
    } catch (CommonException | FeignException ex) {
      return "error";
    }
  }

  @PostMapping("/add-constituency")
  public ModelAndView addConstituency(@CookieValue("Authorization") String token, @RequestParam("constituencyName") String constituencyName) {
    try {
      ModelAndView modelAndView = new ModelAndView("all-constituencies");
      ValidateResponseDTO validateResponseDTO = userController.validateToken(token);
      if (!validateResponseDTO.getUserRole().equalsIgnoreCase(Constants.ADMIN)) {
        modelAndView.addObject(Data.MESSAGE.getValue(), "You are not Authorized for this action");
      }
      ConstituencyRequestDTO request = new ConstituencyRequestDTO();
      request.setHalkaName(constituencyName);
      constituencyFeignController.createConstituency(request);
      List<ConstituencyResponseDTO> constituencies = constituencyFeignController.retrieveConstituencies().getConstituencyResponseDTOList();
      List<PartyResponseDTO> parties = candidateFeignController.retrieveAllParties(true).getPartyResponseDTOList();
      modelAndView.addObject(Data.CONSTITUENCIES.getValue(), constituencies);
      modelAndView.addObject(Data.PARTIES.getValue(), parties);
      modelAndView.addObject(Data.USERROLE.getValue(), validateResponseDTO.getUserRole().toLowerCase());
      return modelAndView;
    } catch (CommonException | FeignException ex) {
      return new ModelAndView("error");
    }
  }

}