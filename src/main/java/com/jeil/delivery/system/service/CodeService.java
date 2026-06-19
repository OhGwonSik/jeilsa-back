package com.jeil.delivery.system.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jeil.delivery.configuration.error.DeleteCheckedException;
import com.jeil.delivery.configuration.error.InsertCheckedException;
import com.jeil.delivery.configuration.error.UpdateCheckedException;
import com.jeil.delivery.system.domain.CodeVO;
import com.jeil.delivery.system.mapper.CodeMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CodeService {

	private final CodeMapper codeMapper;

	public List<CodeVO> selectCodeList(String type, String parentsCodeVal) {
		return codeMapper.selectCodeList(type, parentsCodeVal);
	}

	public int insertCode(CodeVO codeVO) {
		int count = codeMapper.insertCode(codeVO);

		if(count == 0) {
			throw new InsertCheckedException("insert Error");
		}

		return count;
	}

	public int updateCode(CodeVO codeVO) {
		int count = codeMapper.updateCode(codeVO);

		if(count == 0) {
			throw new UpdateCheckedException("update Error");
		}

		return count;
	}

	public int deleteCode(CodeVO codeVO) {
		int count = codeMapper.deleteCode(codeVO);

		if(count == 0) {
			throw new DeleteCheckedException("delete Error");
		}

		return count;
	}
}
