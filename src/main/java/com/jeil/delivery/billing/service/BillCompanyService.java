package com.jeil.delivery.billing.service;

import com.common.auth.common.util.SecurityUtil;
import com.jeil.delivery.billing.domain.BillCompanyVO;
import com.jeil.delivery.billing.mapper.BillCompanyMapper;
import com.jeil.delivery.configuration.error.DeleteCheckedException;
import com.jeil.delivery.configuration.error.InsertCheckedException;
import com.jeil.delivery.configuration.error.UpdateCheckedException;
import com.jeil.delivery.system.domain.CodeVO;
import com.jeil.delivery.system.domain.CompanyDTO;
import com.jeil.delivery.system.mapper.ShipperMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BillCompanyService {

	private final BillCompanyMapper billCompanyMapper;
	private final ShipperMapper shipperMapper;

	public List<BillCompanyVO> selectBillCompanyList(BillCompanyVO billCompanyVO) {
		return billCompanyMapper.selectBillCompanyList(billCompanyVO);
	}

	public List<CodeVO> selectBillCompanyCodeList(CodeVO codeVO){
		return billCompanyMapper.selectBillCompanyCodeList(codeVO);
	}

	public List<CodeVO> selectBillCompanyCodeDetailList(CodeVO codeVO){
		return billCompanyMapper.selectBillCompanyCodeDetailList(codeVO);
	}

	public int insertBillCompany(BillCompanyVO billCompanyVO) {
		Integer currentMemberId = SecurityUtil.getCurrentMemberId();
		billCompanyVO.setChgId(currentMemberId);
		billCompanyVO.setRegId(currentMemberId);
		int count = billCompanyMapper.insertBillCompany(billCompanyVO);

		if(count == 0) {
			throw new InsertCheckedException("insert Error");
		}

		return count;
	}

	public int updateBillCompany(BillCompanyVO billCompanyVO) {
		Integer currentMemberId = SecurityUtil.getCurrentMemberId();
		billCompanyVO.setChgId(currentMemberId);
		int count = billCompanyMapper.updateBillCompany(billCompanyVO);

		if(count == 0) {
			throw new UpdateCheckedException("update Error");
		}

		return count;
	}

	public int deleteBillCompany(BillCompanyVO billCompanyVO) {
		Integer currentMemberId = SecurityUtil.getCurrentMemberId();

		CompanyDTO companyDTO = new CompanyDTO();
		companyDTO.setBillCompanyId(billCompanyVO.getBillCompanyId());
		boolean exist = shipperMapper.checkCompanyByBillCompanyId(companyDTO);
		if(exist) {
			throw new DeleteCheckedException("해당 공급자를 사용 중인 업체가 있어 삭제할 수 없습니다.");
		}

		billCompanyVO.setChgId(currentMemberId);
		int count = billCompanyMapper.deleteBillCompany(billCompanyVO);

		if(count == 0) {
			throw new DeleteCheckedException("delete Error");
		}

		return count;
	}
}
