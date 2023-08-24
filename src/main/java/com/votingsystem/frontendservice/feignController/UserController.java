package com.votingsystem.frontendservice.feignController;

import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import resources.ResponseDTO;
import resources.user.*;

@FeignClient(name = "USER-SERVICE")
public interface UserController {

  /**
   * create the user
   *
   * @param userInviteDTO: contains the resources to create the user
   * @return :return success if creation successful else return failure reason
   */
  @PostMapping("/users")
  public ResponseDTO createUser(@CookieValue("Authorization") String token, @RequestBody UserInviteDTO userInviteDTO);

  /**
   * upload the user image
   *
   * @param token:to        validate user
   * @param image:multipart image
   * @return :return success message if image saved else throw failure exception
   */

  @PostMapping(value = "/users/profile", consumes = {"multipart/form-data"}, headers = "Content-Type= multipart/form-data")
  public ResponseDTO uploadProfile(@CookieValue("Authorization") String token, @RequestPart("image") MultipartFile image);

  /**
   * retrieve user by id
   *
   * @param id:id of user
   * @return :return user response else return null
   */
  @GetMapping("users/{id}")
  public UserResponseDTO getUser(@CookieValue("Authorization") String token, @PathVariable("id") long id);

  /**
   * retrieve all users who are voter
   *
   * @param pageable: to give custom size to pageable object
   * @return :return pageable object of voters
   */
  @GetMapping("users/voters")
  public UserPageResponseDTO getAllVoters(@CookieValue("Authorization") String token, Pageable pageable);

  /**
   * login user
   *
   * @param loginRequestDTO: contains the resources to login user
   * @return :if found return user if and roles else throw failure reason exception
   */
  @PutMapping(value = "users/login", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String login(@RequestBody LoginRequestDTO loginRequestDTO);

  /**
   * find user role
   *
   * @param userId: id of user
   * @return :role of user if found else return null
   */
  @GetMapping("/users/user/{userId}/role")
  public String userRole(@CookieValue("Authorization") String token, @PathVariable long userId);

  /**
   * login function for user which creates the jwt token and return token
   *
   * @param loginRequestDTO: contains the resources for login
   * @return : return the jwt token else return failure exception
   */
  @PostMapping(value = "/users/login")
  public LoginResponseDTO loginUser(@RequestBody LoginRequestDTO loginRequestDTO);

  /**
   * function to validate the user token
   *
   * @param token: token to be validated containing user id
   * @return :return the user response containing the role, name and id of user if user found else return null
   */
  @GetMapping(value = "/users/validate")
  public ValidateResponseDTO validateToken(@CookieValue("Authorization") String token);

  /**
   * to update the user role
   *
   * @param token:    token of user to be validates
   * @param userId:   id of user whose role is to be updated
   * @param roleName: role name
   * @return : success response of role updated successfully else failure exception
   */
  @PostMapping(value = "/users/{userId}/role/{roleName}")
  public ResponseDTO updateUserRole(@CookieValue("Authorization") String token, @PathVariable long userId, @PathVariable String roleName);

}
