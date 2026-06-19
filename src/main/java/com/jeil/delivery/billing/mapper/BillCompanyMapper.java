package com.jeil.delivery.billing.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.jeil.delivery.billing.domain.BillCompanyDTO;
import com.jeil.delivery.billing.domain.BillCompanyVO;
import com.jeil.delivery.billing.domain.TaxInvoiceExcelDTO;
import com.jeil.delivery.system.domain.CodeVO;

@Mapper
public interface BillCompanyMapper {

    public List<BillCompanyVO> selectBillCompanyList(BillCompanyVO billCompanyVO);

    public BillCompanyDTO selectBillCompanyInfo(TaxInvoiceExcelDTO taxInvoiceExcelDTO);

    public List<CodeVO> selectBillCompanyCodeList(CodeVO codeVO);

    public List<CodeVO> selectBillCompanyCodeDetailList(CodeVO codeVO);

    public int insertBillCompany(BillCompanyVO billCompanyVO);

    public int updateBillCompany(BillCompanyVO billCompanyVO);

    public int deleteBillCompany(BillCompanyVO billCompanyVO);
}
