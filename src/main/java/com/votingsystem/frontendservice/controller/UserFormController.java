package com.votingsystem.frontendservice.controller;

import CustomException.CommonException;
import com.votingsystem.frontendservice.feignController.CandidateFeignController;
import com.votingsystem.frontendservice.feignController.ConstituencyFeignController;
import com.votingsystem.frontendservice.feignController.UserController;
import constants.Constants;
import constants.Data;
import constants.ReturnMessage;
import feign.FeignException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import resources.ResponseDTO;
import resources.candidate.PartyListResponseDTO;
import resources.constituency.ConstituencyListResponseDTO;
import resources.user.*;

import java.util.Base64;
import java.util.Objects;


@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/ui")
public class UserFormController {

  private final UserController userController;
  private final ConstituencyFeignController constituencyController;
  private final CandidateFeignController candidateController;

  @GetMapping("/add-user")
  public ModelAndView showAddUserForm(@CookieValue("Authorization") String token, @RequestParam(name = "message", required = false) String message, @RequestParam(name = "errorMessage", required = false) String errorMessage) {
    try {
      ModelAndView modelAndView = new ModelAndView("add-user");
      ValidateResponseDTO validateResponseDTO = userController.validateToken(token);
      if (!validateResponseDTO.getUserRole().equals(Constants.ADMIN)) {
        modelAndView.addObject(Data.MESSAGE.getValue(), "You are Authorized for this Action");
        return modelAndView;
      }
      ConstituencyListResponseDTO constituencyListResponseDTO = constituencyController.retrieveConstituencies();
      modelAndView.addObject(Data.USER.getValue(), new UserInviteDTO());
      modelAndView.addObject(Data.MESSAGE.getValue(), message);
      modelAndView.addObject(Data.ERROR_MESSAGE.getValue(), errorMessage);
      modelAndView.addObject(Data.HALKA_LIST.getValue(), constituencyListResponseDTO.getConstituencyResponseDTOList());
      modelAndView.addObject(Data.USERROLE.getValue(), validateResponseDTO.getUserRole().toLowerCase());
      PartyListResponseDTO partyList = candidateController.retrieveAllParties(false);
      ConstituencyListResponseDTO halkaList = constituencyController.retrieveConstituencies();
      modelAndView.addObject(Data.PARTY_LIST.getValue(), partyList.getPartyResponseDTOList());
      modelAndView.addObject(Data.HALKA_LIST.getValue(), halkaList.getConstituencyResponseDTOList());
      return modelAndView;
    } catch (CommonException | FeignException ex) {
      return new ModelAndView("error");
    }
  }

  @PostMapping("/add-user")
  public ModelAndView addUser(Model model, @CookieValue("Authorization") String token, @ModelAttribute("user") UserInviteDTO user) {
    try {
      ModelAndView modelAndView = new ModelAndView("redirect:/ui/add-user");
      ValidateResponseDTO validateResponseDTO = userController.validateToken(token);
      ResponseDTO responseDTO = userController.createUser(token, user);
      if (!validateResponseDTO.getUserRole().equals(Constants.ADMIN)) {
        modelAndView.addObject(Data.MESSAGE.getValue(), "You are Authorized for this Action");
        return modelAndView;
      }
      modelAndView.addObject(Data.USERROLE.getValue(), validateResponseDTO.getUserRole().toLowerCase());
      if (responseDTO.getResponseCode() != 0) {
        modelAndView.addObject(Data.ERROR_MESSAGE.getValue(), responseDTO.getErrorMessage());
      } else {
        modelAndView.addObject(Data.MESSAGE.getValue(), ReturnMessage.USER_ADDED.getValue());
      }
      return modelAndView;
    } catch (CommonException | FeignException ex) {
      return new ModelAndView("error");
    }
  }

  @GetMapping("/all-users")
  public String showUsersPage(@CookieValue("Authorization") String token, Model model, Pageable pageable) {
    try {
      ValidateResponseDTO validateResponseDTO = userController.validateToken(token);
      UserPageResponseDTO userListResponse = userController.getAllVoters(token, pageable);
      Page<UserResponseDTO> usersPage = userListResponse.getUserResponses();
      model.addAttribute("usersPage", usersPage);
      model.addAttribute(Data.USERROLE.getValue(), validateResponseDTO.getUserRole().toLowerCase());
      log.info("redirecting to all-users");
      return "all-users";
    } catch (CommonException | FeignException ex) {
      return "error";
    }
  }

  @GetMapping("/login")
  public ModelAndView showLoginPage(@RequestParam(value = "message", required = false) String message) {
    ModelAndView modelAndView = new ModelAndView("login");
    if (message != null) {
      modelAndView.addObject("message", message);
    }
    return modelAndView;
  }

  @PostMapping("/login")
  public String processLogin(HttpServletResponse httpServletResponse, @RequestParam("userCNIC") String userCNIC,
                             @RequestParam("userPassword") String userPassword,
                             Model model) {
    try {
      LoginRequestDTO loginRequestDTO = new LoginRequestDTO(userCNIC, userPassword);
      LoginResponseDTO loginResponseDTO = userController.loginUser(loginRequestDTO);
      if (Objects.equals(loginResponseDTO.getResponseCode(), Constants.SUCCESS_CODE)) {
        Cookie cookie = new Cookie(Data.AUTHORIZATION.getValue(), loginResponseDTO.getToken());
        cookie.setMaxAge(3600);
        cookie.setPath("/");
        httpServletResponse.addCookie(cookie);
        ValidateResponseDTO validateResponseDTO = userController.validateToken(loginResponseDTO.getToken());
        if (validateResponseDTO.getUserRole() != null) {
          if (validateResponseDTO.getUserRole().toLowerCase().contains("admin")) {
            return "redirect:/ui/admin-panel?id=" + validateResponseDTO.getUserId() + "&role=" + validateResponseDTO.getUserRole();
          } else if (validateResponseDTO.getUserRole().toLowerCase().contains("candidate")) {
            return "redirect:/ui/candidate-panel";
          } else if (validateResponseDTO.getUserRole().toLowerCase().contains("voter")) {
            return "redirect:/ui/voter-panel";
          }
        }
      }
      model.addAttribute(Data.MESSAGE.getValue(), loginResponseDTO.getErrorMessage());
      return "redirect:/ui/login?message=" + loginResponseDTO.getErrorMessage();
    } catch (CommonException | FeignException ex) {
      return "error";
    }
  }

  @GetMapping("/voter-panel")
  public ModelAndView showUserPage(@CookieValue("Authorization") String token) {
    try {
      ModelAndView modelAndView = new ModelAndView("voter-panel");
      ValidateResponseDTO validateResponseDTO = userController.validateToken(token);
      modelAndView.addObject(Data.USERROLE.getValue(), validateResponseDTO.getUserRole().toLowerCase());
      if (validateResponseDTO.getProfile() == null) {
        modelAndView.addObject(Data.PROFILE.getValue(), false);
      }
      return modelAndView;
    } catch (CommonException | FeignException ex) {
      return new ModelAndView("error");
    }
  }

  @GetMapping("/profile")
  public String showProfilePage(Model model, @CookieValue("Authorization") String token) {
    try {
      ValidateResponseDTO validateResponseDTO = userController.validateToken(token);
      UserResponseDTO user = userController.getUser(token, validateResponseDTO.getUserId());
      if (user.getImg() != null) {
        String base64Image = Base64.getEncoder().encodeToString(user.getImg());
        model.addAttribute("base64Image", base64Image);
      }
      model.addAttribute(Data.USER.getValue(), user);
      model.addAttribute(Data.USERROLE.getValue(), validateResponseDTO.getUserRole().toLowerCase());
      return "profile";
    } catch (CommonException | FeignException ex) {
      return "error";
    }
  }

  @PostMapping("/upload-image")
  public String uploadImage(@RequestParam("image") MultipartFile image, @CookieValue("Authorization") String token) {
    try {
      if (!image.isEmpty()) {
        userController.uploadProfile(token, image);
      }
      return "redirect:/ui/profile";
    } catch (CommonException | FeignException ex) {
      return "error";
    }
  }

  @GetMapping("/become-candidate")
  public String becomeCandidate(@CookieValue("Authorization") String token) {
    return "redirect:/ui/party-management";
  }

}
