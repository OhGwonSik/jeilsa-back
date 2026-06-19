package com.jeil.delivery.system.service;

import java.util.List;

import com.jeil.delivery.deliverylogic.domain.CompanySearchCond;
import com.jeil.delivery.billing.domain.PagedResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.common.auth.common.enums.PermissionHierarchyLevel;
import com.jeil.delivery.configuration.error.DeleteCheckedException;
import com.jeil.delivery.configuration.error.InsertCheckedException;
import com.jeil.delivery.configuration.error.NoDataException;
import com.jeil.delivery.configuration.error.UpdateCheckedException;
import com.jeil.delivery.deliverylogic.mapper.DeliveryRouteMapper;
import com.jeil.delivery.domain.MemberDTO;
import com.jeil.delivery.system.domain.CompanyDTO;
import com.jeil.delivery.system.mapper.CompanyMapper;
import com.jeil.delivery.system.mapper.ShipperMapper;
import com.jeil.delivery.util.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyService {

    private final CompanyMapper companyMapper;
    private final ShipperMapper shipperMapper;
    private final DeliveryRouteMapper deliveryRouteMapper;

    public boolean checkBizNo(String bizNo) {
    	return companyMapper.checkBizNo(bizNo);
    }

	public List<CompanyDTO> selectCompanyList(CompanyDTO companyDTO) {
		MemberDTO memberDTO = SecurityUtils.getCustomUserDetails().getMemberDTO();
		Boolean isUser = PermissionHierarchyLevel.USER.getName().equals(memberDTO.getRoles());
		companyDTO.setIsUser(isUser);
		companyDTO.setUserCompanyId(memberDTO.getUserCompanyId());
		return companyMapper.selectCompanyList(companyDTO);
	}

	public int insertCompany(CompanyDTO companyDTO) {

		MemberDTO memberDTO = SecurityUtils.getCustomUserDetails().getMemberDTO();

		//shipper -> use_yn은 company의 del_yn에만 영향
		if ("Y".equals(companyDTO.getUseYn()) || !StringUtils.hasText(companyDTO.getUseYn())) {
		    companyDTO.setDelYn("N");
		}else {
			companyDTO.setDelYn("Y");
		}

		int count = companyMapper.insertCompany(companyDTO, memberDTO);

		if(count == 0) {
			throw new InsertCheckedException("insert Error");
		}

		// 화주업체인 경우 (화주테이블 insert, 배송코스테이블 insert) - Y인 경우 useYn은 무조건 있음.
		if("Y".equals(companyDTO.getShipperYn())) {

			if(companyDTO.getExcclcCompanyId() == 0 || companyDTO.getExcclcCompanyId() == null) {
				companyDTO.setExcclcCompanyId(companyDTO.getCompanyId());
			}

			count = shipperMapper.insertCompanyShipper(companyDTO, memberDTO);

			if(count == 0) {
				throw new InsertCheckedException("insert Error");
			}

			count = deliveryRouteMapper.insertCompanyAndDeliberyRoute(companyDTO, memberDTO);

			if(count == 0) {
				throw new InsertCheckedException("insert Error");
			}

		}

		return count;
	}

	// shipper use_yn에 따라 company del_yn
	// company내 shipperYn에 따른 shipper del_yn분기
	public int updateCompany(CompanyDTO companyDTO) {
		MemberDTO memberDTO = SecurityUtils.getCustomUserDetails().getMemberDTO();

		//변하기 전 값 조회(단일 값)
		CompanyDTO originCompanyInfo = companyMapper.selectCompanyById(companyDTO);

		if(originCompanyInfo == null) {
			throw new NoDataException();
		}

		String originShipperYn = originCompanyInfo.getShipperYn();

		//shipper -> use_yn은 company의 del_yn에만 영향
		if ("Y".equals(companyDTO.getUseYn()) || !StringUtils.hasText(companyDTO.getUseYn())) {
		    companyDTO.setDelYn("N");
		}else {
			companyDTO.setDelYn("Y");
		}

		int count = companyMapper.updateCompany(companyDTO, memberDTO);

		if(count == 0) {
			throw new UpdateCheckedException("update Error");
		}

		// N-> Y로 바뀐 경우, Y-> N으로 바뀐 경우, N->N인 경우 (아무것도안함), Y->Y인 겨웅
		if("Y".equals(originShipperYn) && "Y".equals(companyDTO.getShipperYn())) {

			if(companyDTO.getExcclcCompanyId() == null || companyDTO.getExcclcCompanyId() == 0) {
				companyDTO.setExcclcCompanyId(companyDTO.getCompanyId());
			}
			companyDTO.setDelYn("N");
			count = shipperMapper.updateShipper(companyDTO, memberDTO);

			if(count == 0) {
				throw new UpdateCheckedException("update Error");
			}

			companyDTO.setOrigDeliveryRouteId(originCompanyInfo.getDeliveryRouteId());
			//배송코스 수정
			count = deliveryRouteMapper.updateCompanyAndDeliveryRoute(companyDTO, memberDTO);

		}else if("N".equals(originShipperYn) && "Y".equals(companyDTO.getShipperYn())) {
			// N-> Y로 바뀐 경우 기본-> 화주 (insert)
			// 화주 데이터 존재 유무
			CompanyDTO existShipperInfo = shipperMapper.existShipperById(companyDTO);

			if(existShipperInfo == null) {

				if(companyDTO.getExcclcCompanyId() == null || companyDTO.getExcclcCompanyId() == 0) {
					companyDTO.setExcclcCompanyId(companyDTO.getCompanyId());
				}

				count = shipperMapper.insertCompanyShipper(companyDTO, memberDTO);

				if(count == 0) {
					throw new InsertCheckedException("insert Error");
				}

			}else {
				companyDTO.setShipperId(existShipperInfo.getShipperId());

				if(companyDTO.getExcclcCompanyId() == null || companyDTO.getExcclcCompanyId() == 0) {
					if(existShipperInfo.getExcclcCompanyId() == 0 || existShipperInfo.getExcclcCompanyId() == null) {
						companyDTO.setExcclcCompanyId(existShipperInfo.getCompanyId());
					}else {
						companyDTO.setExcclcCompanyId(existShipperInfo.getExcclcCompanyId());
					}
				}
				count = shipperMapper.updateShipper(companyDTO, memberDTO);

				if(count == 0) {
					throw new UpdateCheckedException("update Error");
				}
			}

			// 배송코스매핑 존재 유무
			CompanyDTO existCompanyAndDeliveryRegionInfo = deliveryRouteMapper.selectDeliveryRouteByCompany(companyDTO);

			if(existCompanyAndDeliveryRegionInfo == null) {
				count = deliveryRouteMapper.insertCompanyAndDeliberyRoute(companyDTO, memberDTO);

				if(count == 0) {
					throw new InsertCheckedException("insert Error");
				}

			}else {
				companyDTO.setOrigDeliveryRouteId((existCompanyAndDeliveryRegionInfo.getDeliveryRouteId()));
				deliveryRouteMapper.updateCompanyAndDeliveryRoute(companyDTO, memberDTO);

			}
		}else if("Y".equals(originShipperYn) && "N".equals(companyDTO.getShipperYn())) {
			// Y-> N로 바뀐 경우 화주-> 기본 (SOFT DELETE)

			count = shipperMapper.deleteShipper(companyDTO, memberDTO);

			if(count == 0) {
				throw new DeleteCheckedException("delete Error");
			}

			deliveryRouteMapper.deleteCompanyAndDeliveryRoute(companyDTO, memberDTO);

		}

		return count;
	}

	public int deleteCompany(CompanyDTO companyDTO) {
		MemberDTO memberDTO = SecurityUtils.getCustomUserDetails().getMemberDTO();
		int count = 0;

		//삭제할 회사, 화주, 회사_배송코스매핑 정보 저장
		count = companyMapper.insertCompanyDeleteBackup(companyDTO, memberDTO);
		if(count == 0) {
			throw new InsertCheckedException();
		}

		// 회사_배송코스매핑 삭제
		deliveryRouteMapper.deleteCompanyAndDeliveryRouteById(companyDTO);

		// 화주 삭제
		shipperMapper.deleteShipperById(companyDTO);

		// 회사 삭제
		count = companyMapper.deleteCompanyById(companyDTO);

		if(count == 0) {
			throw new DeleteCheckedException("delete Error");
		}

		return count;
	}

	public boolean checkCompanyByCompanyNm(String companyNm){
		CompanyDTO dto = new CompanyDTO();
		dto.setCompanyNm(companyNm);
		return companyMapper.checkCompanyByCompanyNm(dto);
	}

	public CompanyDTO selectCompanyByCompanyNm(String companyNm){
		CompanyDTO dto = new CompanyDTO();
		dto.setCompanyNm(companyNm);
		return companyMapper.selectCompanyByCompanyNm(dto);
	}

    public PagedResult<CompanyDTO> selectCompanyListPaged(CompanySearchCond cond) {
        MemberDTO memberDTO = SecurityUtils.getCustomUserDetails().getMemberDTO();
        Boolean isUser = PermissionHierarchyLevel.USER.getName().equals(memberDTO.getRoles());
        cond.setIsUser(isUser);
        cond.setUserCompanyId(memberDTO.getUserCompanyId());
        List<CompanyDTO> items = companyMapper.selectCompanyListPaged(cond);
        int total = companyMapper.countCompanyList(cond);
        return new PagedResult<>(items, total);
    }
}
