package com.votingsystem.frontendservice.controller;

import CustomException.CommonException;
import com.votingsystem.frontendservice.feignController.CandidateFeignController;
import com.votingsystem.frontendservice.feignController.ConstituencyFeignController;
import com.votingsystem.frontendservice.feignController.UserController;
import com.votingsystem.frontendservice.feignController.VoterFeignController;
import constants.Constants;
import constants.Data;
import constants.ReturnMessage;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import resources.ResponseDTO;
import resources.candidate.CandidatePageResponseDTO;
import resources.candidate.CandidateResponseDTO;
import resources.constituency.ConstituencyResponseDTO;
import resources.constituency.ConstituencyResultDTO;
import resources.constituency.PollingRequestDTO;
import resources.constituency.PoolingResponseDTO;
import resources.user.ValidateResponseDTO;
import resources.voter.VoteRequestDTO;
import resources.voter.VoterResponseDTO;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/ui")
public class PollStatusController {

  private final ConstituencyFeignController constituencyController;
  private final VoterFeignController voterFeignController;
  private final CandidateFeignController candidateFeignController;
  private final UserController userController;

  @GetMapping("/poll-status")
  public String getPollStatus(@CookieValue("Authorization") String token, Model model) {
    try {
      ValidateResponseDTO validateResponseDTO = userController.validateToken(token);
      LocalDateTime currentDateTime = LocalDateTime.now();
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
      PoolingResponseDTO poolingResponseDTO = constituencyController.isPoolingStarted();
      model.addAttribute(Data.TOKEN.getValue(), token);
      if (poolingResponseDTO == null) {
        model.addAttribute(Data.MESSAGE.getValue(), "No polling is created at the moment.");
      } else {
        model.addAttribute(Data.POLLING_ID.getValue(), poolingResponseDTO.getPoolingId());
        model.addAttribute(Data.POLLING_START_TIME.getValue(), poolingResponseDTO.getStartDate());
        model.addAttribute(Data.POLLING_END_TIME.getValue(), poolingResponseDTO.getEndDate());
        LocalDateTime pollingStartDate = LocalDateTime.parse(poolingResponseDTO.getStartDate(), formatter);
        LocalDateTime pollingEndDate = LocalDateTime.parse(poolingResponseDTO.getEndDate(), formatter);
        String message = null;
        String timeLeft = null;
        Duration duration = Duration.between(currentDateTime, pollingStartDate);
        if (currentDateTime.isBefore(pollingStartDate)) {
          long days = duration.toDays();
          duration = duration.minusDays(days);
          long hours = duration.toHours();
          duration = duration.minusHours(hours);
          long minutes = duration.toMinutes();
          model.addAttribute(Data.POLLING_STARTED.getValue(), false);
          message = "Polling has not started.";
          timeLeft = days + "d " + hours + "h " + minutes + "m ";
        } else if (currentDateTime.isBefore(pollingEndDate)) {
          long days = Math.abs(duration.toDays());
          duration = duration.minusDays(days);
          long hours = Math.abs(duration.toHours());
          duration = duration.minusHours(hours);
          long minutes = Math.abs(duration.toMinutes());
          model.addAttribute(Data.POLLING_STARTED.getValue(), true);
          message = "Polling is ongoing.";
          timeLeft = days + "d " + hours + "h " + minutes + "m ";
        } else if (currentDateTime.isAfter(pollingEndDate)) {
          message = "Polling Time was ended.";
          model.addAttribute(Data.POLLING_ENDED.getValue(), true);
        }
        model.addAttribute(Data.MESSAGE.getValue(), message);
        model.addAttribute(Data.TIME_LEFT.getValue(), timeLeft);
      }
      model.addAttribute(Data.USERROLE.getValue(), validateResponseDTO.getUserRole().toLowerCase());
      return "poll-status";
    } catch (CommonException | FeignException ex) {
      return "error";
    }
  }

  @PostMapping("/set-polling-dates")
  public ResponseEntity<Map<String, String>> setPollingDates(
          @CookieValue("Authorization") String token,
          @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime startDate,
          @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime endDate) {
    try {
      ValidateResponseDTO validateResponseDTO = userController.validateToken(token);
      Map<String, String> responseMap = new HashMap<>();
      PollingRequestDTO pollingRequestDTO = new PollingRequestDTO();
      if (!validateResponseDTO.getUserRole().equals(Constants.ADMIN)) {
        responseMap.put(Data.MESSAGE.getValue(), "You are not Authorized for this Action");
        responseMap.put("code", "red");
      }
      Instant startInstant = startDate.atZone(ZoneId.systemDefault()).toInstant();
      Instant endInstant = endDate.atZone(ZoneId.systemDefault()).toInstant();
      pollingRequestDTO.setStartDate(Date.from(startInstant));
      pollingRequestDTO.setEndDate(Date.from(endInstant));
      ResponseDTO responseDTO = constituencyController.createPooling(pollingRequestDTO);

      if (responseDTO.getResponseCode() == 0) {
        responseMap.put(Data.MESSAGE.getValue(), "Polling dates have been updated successfully.");
        responseMap.put("code", "green");
      } else {
        responseMap.put(Data.MESSAGE.getValue(), responseDTO.getErrorMessage());
        responseMap.put("code", "red");
      }

      return ResponseEntity.ok(responseMap);
    } catch (CommonException | FeignException ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping("/candidate-cards")
  public ModelAndView getCandidateCards(@CookieValue("Authorization") String token, @RequestParam("pollingId") Long pollingId, Pageable pageable) {
    try {
      ValidateResponseDTO validateResponseDTO = userController.validateToken(token);
      if (voterFeignController.isVoteCasted(validateResponseDTO.getUserId(), pollingId)) {
        ModelAndView modelAndView = new ModelAndView("poll-status");
        PoolingResponseDTO poolingResponseDTO = constituencyController.retrievePolling(pollingId);
        modelAndView.addObject(Data.POLLING_ID.getValue(), poolingResponseDTO.getPoolingId());
        modelAndView.addObject(Data.POLLING_START_TIME.getValue(), poolingResponseDTO.getStartDate());
        modelAndView.addObject(Data.POLLING_END_TIME.getValue(), poolingResponseDTO.getEndDate());
        modelAndView.addObject(Data.POLLING_ENDED.getValue(), true);
        modelAndView.addObject(Data.USERROLE.getValue(), validateResponseDTO.getUserRole().toLowerCase());
        modelAndView.addObject(Data.MESSAGE.getValue(), "You have already casted a Vote");
        return modelAndView;
      }
      ModelAndView modelAndView = new ModelAndView("votes");
      modelAndView.addObject(Data.USERROLE.getValue(), validateResponseDTO.getUserRole().toLowerCase());
      modelAndView.addObject(Data.POLLING_ID.getValue(), pollingId);
      VoterResponseDTO voterResponseDTO = voterFeignController.getVoterConstituency(validateResponseDTO.getUserId());
      if (voterResponseDTO != null) {
        CandidatePageResponseDTO candidatePageResponseDTO = candidateFeignController.retrieveAllCandidatesInConstituency(token, voterResponseDTO.getConstitutionId(), pageable);
        if (candidatePageResponseDTO != null) {
          candidatePageResponseDTO.getCandidateResponsePage().map(candidateResponse -> {
            if (candidateResponse != null) {
              if (candidateResponse.getImage() != null) {
                candidateResponse.setBase64Image(Base64.getEncoder().encodeToString(candidateResponse.getImage()));
              }
              return candidateResponse;
            }
            return null;
          });
          modelAndView.addObject(Data.CANDIDATES.getValue(), candidatePageResponseDTO.getCandidateResponsePage());
        } else {
          modelAndView.addObject(Data.CANDIDATES.getValue(), null);
        }
      }
      return modelAndView;
    } catch (CommonException | FeignException ex) {
      return new ModelAndView("error");
    }
  }

  @GetMapping("/votes")
  public String votes(@RequestParam("pollingId") Long pollingId,
                      @RequestParam("candidateId") Long candidateId,
                      @CookieValue("Authorization") String token,
                      Model model) {
    try {
      ValidateResponseDTO validateResponseDTO = userController.validateToken(token);
      VoteRequestDTO voteRequestDTO = new VoteRequestDTO();
      voteRequestDTO.setCandidateId(candidateId);
      voteRequestDTO.setPoolingId(pollingId);
      voteRequestDTO.setVoterId(validateResponseDTO.getUserId());
      ResponseDTO responseDTO = voterFeignController.castVote(token, voteRequestDTO);
      if (responseDTO.getResponseCode() == -1) {
        model.addAttribute(Data.MESSAGE.getValue(), responseDTO.getErrorMessage());
      } else {
        model.addAttribute("voted", true);
      }
      model.addAttribute(Data.USERROLE.getValue(), validateResponseDTO.getUserRole().toLowerCase());
      model.addAttribute(Data.POLLING_ID.getValue(), pollingId);
      model.addAttribute(Data.MESSAGE.getValue(), ReturnMessage.VOTE_CASTED);
      return "redirect:/ui/poll-status";
    } catch (CommonException | FeignException ex) {
      return "error";
    }
  }

  @GetMapping("/poll-results/results")
  public ModelAndView getPollResultsResults(@CookieValue("Authorization") String token, Pageable pageable) {
    try {
      ModelAndView modelAndView = new ModelAndView("polling-results");
      ValidateResponseDTO validateResponseDTO = userController.validateToken(token);
      VoterResponseDTO voterResponseDTO = voterFeignController.getVoterConstituency(validateResponseDTO.getUserId());
      CandidatePageResponseDTO allCandidatesPage = candidateFeignController.retrieveAllCandidatesInConstituency(token, voterResponseDTO.getConstitutionId(), pageable);
      modelAndView.addObject(Data.USERROLE.getValue(), validateResponseDTO.getUserRole().toLowerCase());
      if (allCandidatesPage != null) {
        List<CandidateResponseDTO> allCandidates = new ArrayList<>(allCandidatesPage.getCandidateResponsePage().getContent());
        modelAndView.addObject(Data.ALL_CANDIDATES.getValue(), allCandidates);

        allCandidates.sort(Comparator.comparingInt(CandidateResponseDTO::getVotes).reversed());

        CandidateResponseDTO specialCandidate = allCandidates.get(0);
        if (specialCandidate != null) {
          if (specialCandidate.getVotes() != 0) {
            modelAndView.addObject(Data.WINNING_CANDIDATE.getValue(), specialCandidate);
          } else {
            modelAndView.addObject(Data.WINNING_MESSAGE.getValue(), "No Winning Candidate");
          }
        }
        int currentPage = allCandidatesPage.getCandidateResponsePage().getNumber() + 1;
        int totalPages = allCandidatesPage.getCandidateResponsePage().getTotalPages();
        List<Integer> pageSizes = Arrays.asList(5, 10, 20); // You can customize this

        modelAndView.addObject("currentPage", currentPage);
        modelAndView.addObject("totalPages", totalPages);
        modelAndView.addObject("pageSizes", pageSizes);
        modelAndView.addObject("currentSize", 100);
        return modelAndView;
      } else {
        modelAndView.addObject(Data.MESSAGE.getValue(), "No Candidate From Your Constituency");
      }
      return modelAndView;
    } catch (CommonException | FeignException ex) {
      return new ModelAndView("error");
    }
  }

  @PostMapping("/submit-form")
  public ModelAndView submitForm(@CookieValue("Authorization") String token, @ModelAttribute("constituencyResult") ConstituencyResultDTO constituencyResultDTO, Pageable pageable) {
    try {
      ModelAndView model = new ModelAndView("results");
      ValidateResponseDTO validateResponseDTO = userController.validateToken(token);
      List<ConstituencyResponseDTO> constituencies = constituencyController.retrieveConstituencies().getConstituencyResponseDTOList();
      model.addObject(Data.CONSTITUENCIES.getValue(), constituencies);
      long constituencyId = Long.parseLong(constituencyResultDTO.getConstituencyId());
      long pollingId = Long.parseLong(constituencyResultDTO.getPollingId());
      CandidatePageResponseDTO allCandidatesPage = candidateFeignController.retrieveAllCandidatesInConstituency(token, constituencyId, pageable);
      if (allCandidatesPage != null) {
        List<CandidateResponseDTO> allCandidates = new ArrayList<>(allCandidatesPage.getCandidateResponsePage().getContent());
        allCandidates.sort(Comparator.comparingInt(CandidateResponseDTO::getVotes).reversed());
        model.addObject(Data.ALL_CANDIDATES.getValue(), allCandidates);
      } else {
        model.addObject(Data.MESSAGE.getValue(), ReturnMessage.CANDIDATE_NOT_EXISTS.getValue());
      }
      model.addObject(Data.USER.getValue(), new ConstituencyResultDTO());
      model.addObject(Data.POLLING_ID.getValue(), pollingId);
      model.addObject(Data.USERROLE.getValue(), validateResponseDTO.getUserRole().toLowerCase());
      return model;
    } catch (CommonException | FeignException ex) {
      return new ModelAndView("error");
    }
  }

  @GetMapping("/select-constituency")
  public ModelAndView showSelectConstituencyPage(@RequestParam long pollingId, @CookieValue("Authorization") String token) {
    try {
      ModelAndView model = new ModelAndView("results");
      ValidateResponseDTO validateResponseDTO = userController.validateToken(token);
      List<ConstituencyResponseDTO> constituencies = constituencyController.retrieveConstituencies().getConstituencyResponseDTOList();
      model.addObject(Data.CONSTITUENCIES.getValue(), constituencies);
      model.addObject(Data.POLLING_ID.getValue(), pollingId);
      model.addObject(Data.USER.getValue(), new ConstituencyResultDTO());
      model.addObject(Data.USERROLE.getValue(), validateResponseDTO.getUserRole().toLowerCase());
      return model;
    } catch (CommonException | FeignException ex) {
      return new ModelAndView("error");
    }
  }

}