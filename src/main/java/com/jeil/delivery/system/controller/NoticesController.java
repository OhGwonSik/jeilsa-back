package com.jeil.delivery.system.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jeil.delivery.system.domain.NoticesVO;
import com.jeil.delivery.system.service.NoticesService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notices")
public class NoticesController {

	private final NoticesService noticesService;

	@GetMapping("/list")
	@PreAuthorize("@permissionHelper.hasMinLevel('user', 'notices', 'read')")
	public List<NoticesVO> selectNoticesList(NoticesVO noticesVO) {
		return noticesService.selectNoticesList(noticesVO);
	}

	@PostMapping("/insert")
	@PreAuthorize("@permissionHelper.hasMinLevel('owner', 'notices', 'create')")
	public int insertNotices(@RequestBody NoticesVO noticesVO) {
		return noticesService.insertNotices(noticesVO);
	}

	@PutMapping("/update")
	@PreAuthorize("@permissionHelper.hasMinLevel('owner', 'notices', 'update')")
	public int updateNotices(@RequestBody NoticesVO noticesVO) {
		return noticesService.updateNotices(noticesVO);
	}

	@DeleteMapping("/delete")
	@PreAuthorize("@permissionHelper.hasMinLevel('owner', 'notices', 'delete')")
	public int deleteNotices(@RequestBody NoticesVO noticesVO) {
		return noticesService.deleteNotices(noticesVO);
	}
}
