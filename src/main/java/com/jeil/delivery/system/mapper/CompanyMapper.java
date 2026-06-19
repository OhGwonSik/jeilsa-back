package com.jeil.delivery.system.mapper;

import java.util.List;

import com.jeil.delivery.deliverylogic.domain.CompanySearchCond;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jeil.delivery.billing.domain.TaxInvoiceExcelDTO;
import com.jeil.delivery.domain.MemberDTO;
import com.jeil.delivery.system.domain.CompanyDTO;

@Mapper
public interface CompanyMapper {

	public boolean checkBizNo(String bizNo);

	public List<CompanyDTO> selectCompanyList(CompanyDTO companyDTO);

	public CompanyDTO selectCompanyById(CompanyDTO companyDTO);

	public boolean checkCompanyByCompanyNm(CompanyDTO companyDTO);

	public CompanyDTO selectCompanyByCompanyNm(CompanyDTO companyDTO);

	public CompanyDTO selectInvoiceCompanyById(TaxInvoiceExcelDTO taxInvoiceExcelDTO);

	public int insertCompany(@Param("param") CompanyDTO companyDTO, @Param("memberInfo") MemberDTO memberDTO);

	public int updateCompany(@Param("param") CompanyDTO companyDTO, @Param("memberInfo") MemberDTO memberDTO);

	public int deleteCompany(@Param("param") CompanyDTO companyDTO, @Param("memberInfo") MemberDTO memberDTO);

	public int insertCompanyDeleteBackup(@Param("param") CompanyDTO companyDTO, @Param("memberInfo") MemberDTO memberDTO);

	public int deleteCompanyById(CompanyDTO companyDTO);

    List<CompanyDTO> selectCompanyListPaged(CompanySearchCond cond);

    int countCompanyList(CompanySearchCond cond);
}
