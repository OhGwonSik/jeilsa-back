package com.jeil.delivery.system.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.jeil.delivery.system.domain.CodeVO;
import com.jeil.delivery.system.service.CodeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CodeController {

	private final CodeService codeService;

	@GetMapping("/code/list")
	public List<CodeVO> selectCodeList(@RequestParam("type") String type
									 , @RequestParam(value = "parentsCodeVal", required = false) String parentsCodeVal) {
		return codeService.selectCodeList(type, parentsCodeVal);
	}

	@PostMapping("/code/insert")
	public int insertCode(CodeVO codeVO) {
		return codeService.insertCode(codeVO);
	}

	@PutMapping("/code/update")
	public int updateCode(CodeVO codeVO) {
		return codeService.updateCode(codeVO);
	}

	@DeleteMapping("/code/delete")
	public int deleteCode(CodeVO codeVO) {
		return codeService.deleteCode(codeVO);
	}
}
