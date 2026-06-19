package com.jeil.delivery.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jeil.delivery.domain.MemberDTO;
import com.jeil.delivery.security.CustomUserDetails;


@RestController
public class HealthCheckController {

	@GetMapping("/health")
	public String healthCheck() {

		return "OK";
	}

	@GetMapping("/health1")
	public String healthCheck1() {

		return "안녕";
	}

	@GetMapping("/health2")
	public ResponseEntity<Map<String, Object>> healthCheck2() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("message", "안녕안녕");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("randomNumber", new Random().nextInt(100));
        return ResponseEntity.ok(response);
	}

	@GetMapping("/health3")
	public ResponseEntity<Map<String, Object>> healthCheck3() throws Exception {

		throw new Exception("헬로우");

	}

	@GetMapping("/health4")
	public ResponseEntity<Map<String, Object>> healthCheck4() throws Exception {
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

	    Map<String, Object> response = new HashMap<>();

	    if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
	        MemberDTO member = userDetails.getMemberDTO(); // 또는 getMemberDTO()

	        response.put("status", "OK");
	        response.put("memberId", member.getMemberId());
	        response.put("name", member.getName());
	        response.put("email", member.getEmail());
	    } else {
	        response.put("status", "UNAUTHORIZED");
	        response.put("message", "인증되지 않은 사용자입니다.");
	    }

	    return ResponseEntity.ok(response);
	}
}
