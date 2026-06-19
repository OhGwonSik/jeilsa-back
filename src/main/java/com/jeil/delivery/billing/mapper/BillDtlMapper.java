package com.jeil.delivery.billing.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jeil.delivery.billing.domain.BillDTO;
import com.jeil.delivery.billing.domain.BillDtlVO;
import com.jeil.delivery.billing.domain.BillVO;
import com.jeil.delivery.billing.domain.CompanyInvoiceByKindCdDTO;
import com.jeil.delivery.billing.domain.TaxInvoiceExcelDTO;
import com.jeil.delivery.domain.MemberDTO;

@Mapper
public interface BillDtlMapper {

	public List<BillDTO> selectBillDtlByTransportData(@Param("param") BillVO billVO);

    public List<BillDtlVO> selectBillHistoryDtlList(BillDtlVO billDtlVO);

    public List<TaxInvoiceExcelDTO> selectTaxInvoiceExcelData(TaxInvoiceExcelDTO taxInvoiceExcelDTO);

    public List<BillDTO> selectCompanyBillDataList(TaxInvoiceExcelDTO taxInvoiceExcelDTO);

    public List<CompanyInvoiceByKindCdDTO> selectCompanyBillDataListByKindCd(TaxInvoiceExcelDTO taxInvoiceExcelDTO);

    public BillDTO selectCompanyDetailInvoicePage(TaxInvoiceExcelDTO taxInvoiceExcelDTO);

    public int insertBillDtl(@Param("param") List<BillDTO> billDtlList, @Param("memberInfo")  MemberDTO memberDTO);

    public int insertBillDtlReCalculation(@Param("param") BillDTO billDTO, @Param("memberInfo")  MemberDTO memberDTO);

    public int insertBillDtlReCalculationAll(@Param("param") BillDTO billDTO, @Param("memberInfo")  MemberDTO memberDTO);

    public int updateShipmentSettleStatus(@Param("param") BillVO bilShistoryVO, @Param("memberInfo") MemberDTO memberDTO);

    public int updateBillHistoryDtl(BillDtlVO billHistoryDtlVO);

    public int deleteBillHistoryDtl(BillDtlVO billHistoryDtlVO);
}
