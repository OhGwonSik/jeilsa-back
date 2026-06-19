package com.jeil.delivery.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.jeil.delivery.system.domain.CodeVO;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CodeMapper {

	List<CodeVO> selectCodeList(@Param("type") String type, @Param("parentsCodeVal") String parentsCodeVal);

	public int insertCode(CodeVO codeVO);

	public int updateCode(CodeVO codeVO);

	public int deleteCode(CodeVO codeVO);
}
