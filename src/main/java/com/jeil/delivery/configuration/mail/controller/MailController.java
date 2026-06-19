package com.jeil.delivery.configuration.mail.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jeil.delivery.billing.domain.BillVO;
import com.jeil.delivery.configuration.mail.service.MailService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/mail")
@RequiredArgsConstructor
public class MailController {

	private final MailService mailService;

	@PostMapping("/resend")
	@PreAuthorize("@permissionHelper.hasMinLevel('owner','mail','create')")
	public Boolean invoiceMailSend(@RequestBody BillVO baseBillVO) {
		return mailService.invoiceMailSend(baseBillVO);
	}
}
