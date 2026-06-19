package com.common.auth.audit.component;

import com.common.auth.audit.constants.AuditConstants;
import com.common.auth.audit.dto.AuditTableMetaDTO;
import com.common.auth.audit.service.AuditLogService;
import com.common.auth.audit.util.AuditUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 감사 대상 메타데이터 캐시
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditTargetCache {

	private final ObjectProvider<AuditLogService> auditLogServiceProvider;
	
	private final Map<String, List<String>> pkColumnsByTable = new ConcurrentHashMap<>();
	private final Map<String, Map<String,Integer>> targetIdsByTableColumn = new ConcurrentHashMap<>();

	/**
	 * 컨텍스트 갱신 시 캐시 자동 업데이트
	 */
	@EventListener
	public void onContextRefreshed(ContextRefreshedEvent event) {
		refreshCache();
	}

	/**
	 * 캐시 갱신 (성능 최적화)
	 */
	public void refreshCache() {
		try {
			AuditLogService auditService = auditLogServiceProvider.getIfAvailable();
			if (auditService == null) {
				log.warn("AuditLogService not available for cache refresh");
				return;
			}

			List<AuditTableMetaDTO> metaDataList = auditService.getAuditTableMetaList();
			Map<String, List<String>> newPkColumns = new HashMap<>();
			Map<String, Map<String,Integer>> newTargetIds = new HashMap<>();

			processMetaData(metaDataList, newPkColumns, newTargetIds);
			updateCacheAtomically(newPkColumns, newTargetIds);
		} catch (RuntimeException runtimeException) {
			log.debug("AuditTargetCache Exception Occured", runtimeException);
		}
	}

	/**
	 * 메타데이터 처리
	 */
	private void processMetaData(List<AuditTableMetaDTO> metaDataList, 
								Map<String, List<String>> newPkColumns,
								Map<String, Map<String,Integer>> newTargetIds) {
		// null 안전성 강화  
		metaDataList.stream()
				.filter(meta -> meta != null && meta.getTableName() != null && meta.getTableColumn() != null)
				.forEach(meta -> {
					String tableName = meta.getTableName();
					String columnName = meta.getTableColumn();
					
                    newTargetIds.computeIfAbsent(tableName, tableKey -> new HashMap<>())
                               .put(columnName, meta.getTargetId());
					
					if (!newPkColumns.containsKey(tableName) && meta.getPkColumnsCsv() != null) {
						newPkColumns.put(tableName, AuditUtil.parsePkColumns(meta.getPkColumnsCsv()));
					}
				});
	}

	/**
	 * 원자적 캐시 업데이트
	 */
	private void updateCacheAtomically(Map<String, List<String>> newPkColumns, 
									  Map<String, Map<String,Integer>> newTargetIds) {
		boolean pkChanged = !pkColumnsByTable.equals(newPkColumns);
		boolean targetChanged = !targetIdsByTableColumn.equals(newTargetIds);
		
		if (!pkChanged && !targetChanged) {
			return;
		}
		
		pkColumnsByTable.clear();
		pkColumnsByTable.putAll(newPkColumns);
		
		targetIdsByTableColumn.clear();
		newTargetIds.forEach((table, columnMap) -> 
			targetIdsByTableColumn.put(table, new ConcurrentHashMap<>(columnMap))
		);
	}

	/**
	 * 테이블 PK 컬럼 조회
	 */
	public List<String> getPkColumns(String tableName) {
		return tableName == null ? AuditConstants.EMPTY_STRING_LIST 
								 : pkColumnsByTable.getOrDefault(tableName, AuditConstants.EMPTY_STRING_LIST);
	}

	/**
	 * 감사 대상 컬럼 목록 조회
	 */
	public List<String> getAuditableColumns(String tableName) {
		if (tableName == null) {
			return AuditConstants.EMPTY_STRING_LIST;
		}
		Map<String,Integer> columnMap = targetIdsByTableColumn.get(tableName);
		return columnMap == null ? AuditConstants.EMPTY_STRING_LIST : List.copyOf(columnMap.keySet());
	}

	/**
	 * 컬럼별 타겟ID 매핑 조회
	 */
	public Map<String,Integer> getTargetIdsByColumns(String tableName) {
		if (tableName == null) {
			return AuditConstants.EMPTY_Integer_MAP;
		}
		Map<String,Integer> columnMap = targetIdsByTableColumn.get(tableName);
		return columnMap == null ? AuditConstants.EMPTY_Integer_MAP : Map.copyOf(columnMap);
	}

	/**
	 * 특정 컬럼의 타겟ID 조회
	 */
	public Integer getTargetId(String tableName, String columnName) {
		if (tableName == null || columnName == null) {
			return null;
		}
		return targetIdsByTableColumn.getOrDefault(tableName, AuditConstants.EMPTY_Integer_MAP)
				.get(columnName);
	}

	/**
	 * 테이블이 감사 대상인지 확인
	 */
	public boolean isAuditableTable(String tableName) {
		return tableName != null && targetIdsByTableColumn.containsKey(tableName);
	}
}