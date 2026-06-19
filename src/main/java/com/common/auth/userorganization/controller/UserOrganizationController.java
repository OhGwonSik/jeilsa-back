package com.common.auth.userorganization.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.auth.userorganization.service.UserOrganizationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/user-organizations")
@RequiredArgsConstructor
public class UserOrganizationController {
    //----- DI Fields -----//
    private final UserOrganizationService userOrganizationService;
}
