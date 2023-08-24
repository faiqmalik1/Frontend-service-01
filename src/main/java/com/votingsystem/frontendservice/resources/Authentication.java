package com.votingsystem.frontendservice.resources;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class Authentication {
  public static String parseRequestToToken(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    String token = null;
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if ("token".equals(cookie.getName())) {
          token = cookie.getValue();
          break;
        }
      }
    }
    return token;
  }
}
