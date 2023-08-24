package com.votingsystem.frontendservice.controller;

import CustomException.CommonException;
import com.votingsystem.frontendservice.feignController.UserController;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/ui")
@RequiredArgsConstructor
public class AdminController {

  private final UserController userController;

  @GetMapping("/admin-panel")
  public ModelAndView showLoginPage(@RequestParam long id, @RequestParam String role) {
    try {
      ModelAndView modelAndView = new ModelAndView("admin-panel");
      modelAndView.addObject("userId", id);
      modelAndView.addObject("userRole", role.toLowerCase());
      return modelAndView;
    } catch (CommonException | FeignException ex) {
      return new ModelAndView("error");
    }
  }

}
