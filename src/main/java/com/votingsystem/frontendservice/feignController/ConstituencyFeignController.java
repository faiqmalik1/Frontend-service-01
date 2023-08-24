package com.votingsystem.frontendservice.feignController;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import resources.ResponseDTO;
import resources.constituency.*;

@FeignClient(name = "CONSTITUENCY-SERVICE")
public interface ConstituencyFeignController {

  /**
   * to create new constituency
   *
   * @param constituencyRequestDTO:contains the resources to create a new constituency
   * @return :return success response if everything works fine else throw error
   */
  @PostMapping("/constituency")
  public ResponseDTO createConstituency(@RequestBody ConstituencyRequestDTO constituencyRequestDTO);

  /**
   * to retrieve constituency by name
   *
   * @param halkaName: name of constituency
   * @return :return constituency if found else return null
   */
  @GetMapping("/constituency/name/{halkaName}")
  public ConstituencyResponseDTO retrieveConstituencyByName(@PathVariable(name = "halkaName") String halkaName);

  /**
   * to retrieve constituency by id
   *
   * @param constituencyId:id of constituency
   * @return :return constituency if found else return null
   */
  @GetMapping("/constituency/id/{constituencyId}")
  public ConstituencyResponseDTO retrieveConstituencyById(@PathVariable(name = "constituencyId") long constituencyId);

  /**
   * to retrieve all the constituencies
   *
   * @return :return the list of available constituencies if found else return null
   */
  @GetMapping("/constituency")
  public ConstituencyListResponseDTO retrieveConstituencies();

  /**
   * retrieve all the polling
   *
   * @param pageable: to give custom size to get pageable object
   * @return :object having polling pageable object else return null
   */
  @GetMapping("/polling")
  public PoolingPageResponseDTO retrievePooling(Pageable pageable);

  /**
   * to retrieve started polling or the last polling
   *
   * @return : return the polling response else return null
   */
  @GetMapping("polling/status")
  public PoolingResponseDTO isPoolingStarted();

  /**
   * to create the polling
   *
   * @param pollingRequestDTO: contains the resources to create polling
   * @return : success response if created else return failure response
   */
  @PostMapping("/polling")
  public ResponseDTO createPooling(@RequestBody PollingRequestDTO pollingRequestDTO);

  @GetMapping("/polling/{pollingId}")
  public PoolingResponseDTO retrievePolling(@PathVariable long pollingId);

}