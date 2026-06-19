package com.common.auth.audit.service;

import com.common.auth.audit.component.AuditTargetCache;
import com.common.auth.audit.constants.AuditConstants;
import com.common.auth.audit.domain.SqlRecord;
import com.common.auth.audit.dto.AuditLogDTO;
import com.common.auth.audit.dto.AuditTableMetaDTO;
import com.common.auth.audit.dto.KeyValueDTO;
import com.common.auth.audit.dto.PkConditionDTO;
import com.common.auth.audit.mapper.AuditMapper;
import com.common.auth.audit.util.AuditUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Values;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 공통 감사 추적 서비스 
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {
    
    private final AuditMapper auditMapper;

    /**
     * 감사 로그 삽입
     */
    public void insertAuditLog(AuditLogDTO log) {
        if (log == null) {
            return;
        }
        auditMapper.insertAuditLog(log);
    }

    /**
     * 감사 테이블 메타데이터 조회
     */
    public List<AuditTableMetaDTO> getAuditTableMetaList() {
        return auditMapper.getAuditTableMetaList();
    }

    /**
     * DML 감사 레코드 처리 (INSERT | UPDATE | DELETE) 
     */
    public void processAuditRecord(SqlRecord sqlRecord, AuditTargetCache auditTargetCache, String transactionStatus) {
        String tableName = sqlRecord.getTableName();
        Map<String, Integer> targetIdsByColumn = auditTargetCache.getTargetIdsByColumns(tableName);
        
        if (targetIdsByColumn.isEmpty()) {
            return;
        }
        
        extractChangedRowsUnified(sqlRecord, auditTargetCache)
            .forEach(row -> createAuditLogsForRow(sqlRecord, row, targetIdsByColumn, transactionStatus, auditTargetCache));
    }

    /**
     * pre-image 스냅샷 조회 (UPDATE/DELETE 전용) - JSQLParser + 바인딩 매핑 기반
     */
    public List<KeyValueDTO> capturePreImageSnapshot(SqlRecord sqlRecord, AuditTargetCache auditTargetCache) {
        String tableName = sqlRecord.getTableName();
        List<String> auditableColumns = auditTargetCache.getAuditableColumns(tableName);
        BoundSql boundSql = sqlRecord.getBoundSql();
        if (boundSql == null) {
            return AuditConstants.EMPTY_KEYVALUE_LIST;
        }

        String sql = boundSql.getSql();
        List<Object> boundValues = sqlRecord.getParameterValues() == null
                ? List.of()
                : sqlRecord.getParameterValues().stream().map(KeyValueDTO::getValue).toList();

        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            SqlCommandType cmdType = sqlRecord.getCommandType();

            if (cmdType == SqlCommandType.UPDATE && statement instanceof Update updateStatement) {
                int startIndex = countJdbcParametersInUpdateSets(updateStatement);
                List<PkConditionDTO> whereConds = buildWhereConditions(updateStatement.getWhere(), startIndex, boundValues);
                if (!whereConds.isEmpty()) {
                    return auditMapper.selectRowColumns(tableName, auditableColumns, whereConds);
                }
            } else if (cmdType == SqlCommandType.DELETE && statement instanceof Delete deleteStatement) {
                List<PkConditionDTO> whereConds = buildWhereConditions(deleteStatement.getWhere(), 0, boundValues);
                if (!whereConds.isEmpty()) {
                    return auditMapper.selectRowColumns(tableName, auditableColumns, whereConds);
                }
            }
        } catch (JSQLParserException parseException) {
            log.debug("JSQLParser pre-image parse failed for table {}", tableName, parseException);
        }

        return AuditConstants.EMPTY_KEYVALUE_LIST;
    }

    // UPDATE 후조회 (SET/WHERE 기반 PK 또는 WHERE 기반 조건) → post-image로 사용
    private List<KeyValueDTO> fetchPostImageForUpdate(SqlRecord sqlRecord,
                                                      Map<String,Integer> targetIdsByColumn) {
        String tableName = sqlRecord.getTableName();
        List<String> auditableColumns = new ArrayList<>(targetIdsByColumn.keySet());
        BoundSql boundSql = sqlRecord.getBoundSql();
        if (boundSql == null) {
            return AuditConstants.EMPTY_KEYVALUE_LIST;
        }
        String sql = boundSql.getSql();
        List<Object> boundValues = sqlRecord.getParameterValues() == null
                ? List.of()
                : sqlRecord.getParameterValues().stream().map(KeyValueDTO::getValue).toList();
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (!(statement instanceof Update updateStatement)) {
                return AuditConstants.EMPTY_KEYVALUE_LIST;
            }
            int startIndex = countJdbcParametersInUpdateSets(updateStatement);
            List<PkConditionDTO> whereConds = buildWhereConditions(updateStatement.getWhere(), startIndex, boundValues);
            if (!whereConds.isEmpty()) {
                return auditMapper.selectRowColumns(tableName, auditableColumns, whereConds);
            }
        } catch (JSQLParserException parseException) {
            log.debug("UPDATE post-image parse failed for table {}", tableName, parseException);
        }
        return AuditConstants.EMPTY_KEYVALUE_LIST;
    }

    /**
     * UPDATE SET 절의 JdbcParameter 개수 카운트 (WHERE 바인딩 인덱스 시작점 계산)
     */
    private int countJdbcParametersInUpdateSets(Update updateStmt) {
        int count = 0;
        if (updateStmt == null || updateStmt.getUpdateSets() == null) {
            return 0;
        }
        for (UpdateSet set : updateStmt.getUpdateSets()) {
            ExpressionList<?> exprList = set.getValues();
            if (exprList == null) {
                continue;
            }
            for (Expression valueExpr : exprList) {
                if (valueExpr instanceof JdbcParameter) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * WHERE 조건으로부터 컬럼-값 조건 리스트 구성 (Equals AND 조합 지원)
     */
    private List<PkConditionDTO> buildWhereConditions(Expression whereExpr, int startIndex, List<Object> boundValues) {
        List<PkConditionDTO> conditions = new ArrayList<>();
        int[] indexHolder = new int[] { startIndex };
        collectConditions(whereExpr, indexHolder, boundValues, conditions);
        return conditions;
    }

    private void collectConditions(Expression expression, int[] indexHolder, List<Object> boundValues, List<PkConditionDTO> out) {
        if (expression == null) {
            return;
        }

        if (expression instanceof AndExpression andExpression) {
            collectConditions(andExpression.getLeftExpression(), indexHolder, boundValues, out);
            collectConditions(andExpression.getRightExpression(), indexHolder, boundValues, out);
            return;
        }

        if (expression instanceof EqualsTo equalsTo) {
            handleEqualsNode(equalsTo, indexHolder, boundValues, out);
        }
    }

    private void handleEqualsNode(EqualsTo equalsTo, int[] indexHolder, List<Object> boundValues, List<PkConditionDTO> out) {
        Expression leftExpression = equalsTo.getLeftExpression();
        Expression rightExpression = equalsTo.getRightExpression();

        if (leftExpression instanceof Column leftColumn && rightExpression instanceof JdbcParameter) {
            Object value = nextBoundValue(indexHolder, boundValues);
            addCondition(out, leftColumn.getColumnName(), value);
            return;
        }
        if (rightExpression instanceof Column rightColumn && leftExpression instanceof JdbcParameter) {
            Object value = nextBoundValue(indexHolder, boundValues);
            addCondition(out, rightColumn.getColumnName(), value);
            return;
        }
        if (leftExpression instanceof Column leftSideColumn) {
            String literal = rightExpression == null ? null : rightExpression.toString();
            addCondition(out, leftSideColumn.getColumnName(), literal);
            return;
        }
        if (rightExpression instanceof Column rightSideColumn) {
            String literal = leftExpression == null ? null : leftExpression.toString();
            addCondition(out, rightSideColumn.getColumnName(), literal);
        }
    }

    private String sanitizeColumnName(String columnName) {
        String trimmed = columnName.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed.replaceAll(AuditConstants.REGEX_TRIM_QUOTED_IDENTIFIER, "");
    }

    private Object nextBoundValue(int[] indexHolder, List<Object> boundValues) {
        int valueIndex = indexHolder[0];
        Object value = null;
        if (boundValues != null && valueIndex >= 0 && valueIndex < boundValues.size()) {
            value = boundValues.get(valueIndex);
        }
        indexHolder[0] = valueIndex + 1;
        return value;
    }

    private void addCondition(List<PkConditionDTO> out, String columnName, Object value) {
        if (columnName == null) {
            return;
        }
        String cleanName = sanitizeColumnName(columnName);
        Object coerced = coerceUuidIfString(value);
        String columnType = coerced instanceof Integer ? AuditConstants.Integer_TYPE : null;
        out.add(new PkConditionDTO(cleanName, coerced, columnType));
    }

    /**
     * 변경 행 추출 (JSQLParser 우선, PK Fallback)
     */
    private List<List<KeyValueDTO>> extractChangedRowsUnified(SqlRecord sqlRecord, AuditTargetCache auditTargetCache) {
        List<List<KeyValueDTO>> rows = extractWithJSQLParser(sqlRecord, auditTargetCache);
        if (rows.isEmpty()) {
            return AuditConstants.EMPTY_ROWS_LIST;
        }
        return rows;
    }

    /**
     * JSQLParser 기반 행 추출 
     */
    private List<List<KeyValueDTO>> extractWithJSQLParser(SqlRecord sqlRecord, AuditTargetCache auditTargetCache) {
        try {
            BoundSql boundSql = sqlRecord.getBoundSql();
            MappedStatement mappedStatement = sqlRecord.getMappedStatement();
            
            if (boundSql == null || mappedStatement == null) {
                return AuditConstants.EMPTY_ROWS_LIST;
            }
            
            String sql = boundSql.getSql();
            List<String> auditableColumns = auditTargetCache.getAuditableColumns(sqlRecord.getTableName());
            
            // SQL 파싱 (단순화)
            Statement statement = CCJSqlParserUtil.parse(sql);
            
            // SQL 명령 타입에 따른 처리
            return switch (sqlRecord.getCommandType()) {
                case INSERT ->
                        extractInsertRowsFromParameters(statement, sqlRecord.getParameterValues(), auditableColumns);
                case UPDATE -> {
                        List<Object> boundValues = sqlRecord.getParameterValues() == null
                                ? List.of()
                                : sqlRecord.getParameterValues().stream().map(KeyValueDTO::getValue).toList();
                        yield extractUpdateRowsFromParameters(statement, boundValues, auditableColumns,
                                                             sqlRecord.getTableName(), auditTargetCache);
                }
                case DELETE -> AuditConstants.EMPTY_ROWS_LIST;
                default -> AuditConstants.EMPTY_ROWS_LIST;
            };
            
        } catch (JSQLParserException parseException) {
            log.debug("JSQLParser failed for table {}", sqlRecord.getTableName(), parseException);
            return AuditConstants.EMPTY_ROWS_LIST;
        }
    }

    /**
     * INSERT 행 추출 (JSQLParser 기반 배치 지원)
     */
    private List<List<KeyValueDTO>> extractInsertRowsFromParameters(Statement statement, List<KeyValueDTO> extractedParams, 
                                                                   List<String> auditableColumns) {
        if (!(statement instanceof Insert insert) || insert.getColumns() == null || insert.getColumns().isEmpty()) {
            return AuditConstants.EMPTY_ROWS_LIST;
        }
        
        List<Object> boundValues = extractedParams == null ? List.of() : extractedParams.stream().map(KeyValueDTO::getValue).toList();
        List<List<KeyValueDTO>> rows = new ArrayList<>();

        Object items = insert.getValues();
        if (items instanceof Values valuesClause) {
            // JSQLParser를 사용한 멀티 INSERT 감지
            if (isMultiRowInsert(valuesClause)) {
                return processMultiRowInsert(insert.getColumns(), valuesClause, boundValues, auditableColumns);
            }
            
            // 단일 INSERT 처리
            ExpressionList<?> single = valuesClause.getExpressions();
            if (single != null) {
                int[] indexHolder = new int[] { 0 };
                List<KeyValueDTO> row = buildInsertRow(insert.getColumns(), single, indexHolder, boundValues, auditableColumns);
                if (!row.isEmpty()) {
                    rows.add(row);
                }
                return rows;
            }
        }
        
        // 기존 로직 유지
        if (!(items instanceof ExpressionList<?> itemsList)) {
            return AuditConstants.EMPTY_ROWS_LIST;
        }
        
        int[] indexHolder = new int[] { 0 };
        List<Expression> topLevel = new ArrayList<>();
        for (Expression valueExpression : itemsList) {
            topLevel.add(valueExpression);
        }

        if (!topLevel.isEmpty() && topLevel.get(0) instanceof ExpressionList<?>) {
            for (Expression topLevelExpression : topLevel) {
                if (topLevelExpression instanceof ExpressionList<?> rowList) {
                    List<KeyValueDTO> row = buildInsertRow(insert.getColumns(), rowList, indexHolder, boundValues, auditableColumns);
                    if (!row.isEmpty()) {
                        rows.add(row);
                    }
                }
            }
            return rows;
        }

        List<KeyValueDTO> row = buildInsertRow(insert.getColumns(), itemsList, indexHolder, boundValues, auditableColumns);
        if (!row.isEmpty()) {
            rows.add(row);
        }
        return rows;
    }

    /**
     * JSQLParser를 사용한 멀티 INSERT 감지
     */
    private boolean isMultiRowInsert(Values valuesClause) {
        try {
            ExpressionList<?> expressions = valuesClause.getExpressions();
            if (expressions == null || expressions.isEmpty()) {
                return false;
            }
            
            // 첫 번째 요소가 ExpressionList인지 확인 (멀티 행)
            Expression firstExpression = expressions.get(0);
            return firstExpression instanceof ExpressionList<?>;
        } catch (Exception e) {
            log.debug("Failed to detect multi-row insert", e);
            return false;
        }
    }

    /**
     * 멀티 행 INSERT 처리
     */
    private List<List<KeyValueDTO>> processMultiRowInsert(List<Column> columns, Values valuesClause, 
                                                        List<Object> boundValues, List<String> auditableColumns) {
        List<List<KeyValueDTO>> rows = new ArrayList<>();
        
        try {
            ExpressionList<?> expressions = valuesClause.getExpressions();
            if (expressions == null) {
                return rows;
            }
            
            // 각 행을 개별 처리
            for (Expression expression : expressions) {
                if (expression instanceof ExpressionList<?> rowValues) {
                    int[] indexHolder = new int[] { 0 };
                    List<KeyValueDTO> row = buildInsertRow(columns, rowValues, indexHolder, boundValues, auditableColumns);
                    if (!row.isEmpty()) {
                        rows.add(row);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Failed to process multi-row insert", e);
        }
        
        return rows;
    }

    

	private List<KeyValueDTO> buildInsertRow(List<Column> columns,
										  ExpressionList<?> valueList,
										  int[] indexHolder,
										  List<Object> boundValues,
										  List<String> auditableColumns) {
        List<KeyValueDTO> row = new ArrayList<>();
        List<Expression> expressions = new ArrayList<>();
        for (Expression valueExpressionItem : valueList) {
            expressions.add(valueExpressionItem);
        }
        int pairCount = Math.min(columns.size(), expressions.size());
            for (int columnValuePairIndex = 0; columnValuePairIndex < pairCount; columnValuePairIndex++) {
                Column column = columns.get(columnValuePairIndex);
            Expression expression = expressions.get(columnValuePairIndex);
            String columnName = sanitizeColumnName(column.getColumnName());

            if (!AuditUtil.containsIgnoreCase(auditableColumns, columnName)) {
                // JdbcParameter면 바운드 인덱스 소비
                consumeIfJdbcParameter(expression, indexHolder, boundValues);
                    continue;
            }

            Object value = extractValueForExpression(expression, indexHolder, boundValues);
                    if (value != null) {
                row.add(new KeyValueDTO(columnName, value));
            }
        }
        return row;
    }

    private void consumeIfJdbcParameter(Expression expression, int[] indexHolder, List<Object> boundValues) {
        if (expression instanceof JdbcParameter) {
            nextBoundValue(indexHolder, boundValues);
        } else if (expression instanceof Function function && function.getParameters() != null) {
            ExpressionList<?> parameters = function.getParameters();
            for (Expression parameterExpression : parameters) {
                consumeIfJdbcParameter(parameterExpression, indexHolder, boundValues);
            }
        }
    }

    private Object extractValueForExpression(Expression expression, int[] indexHolder, List<Object> boundValues) {
        if (expression instanceof JdbcParameter) {
            Object value = nextBoundValue(indexHolder, boundValues);
            log.debug("Original value: {} (type: {})", value, value != null ? value.getClass().getSimpleName() : "null");
            
            // Integer 타입 처리: 문자열로 감싸진 Integer를 실제 Integer 객체로 변환
            Object coercedValue = coerceUuidIfString(value);
            log.debug("Coerced value: {} (type: {})", coercedValue, coercedValue != null ? coercedValue.getClass().getSimpleName() : "null");
            
            return coercedValue;
        } else if (expression instanceof Function function && function.getParameters() != null) {
            ExpressionList<?> parameters = function.getParameters();
            for (Expression parameterExpression : parameters) {
                Object extractedValue = extractValueForExpression(parameterExpression, indexHolder, boundValues);
                if (extractedValue != null) {
                    return extractedValue;
                }
            }
            return function.toString();
        }
        return expression == null ? null : expression.toString();
    }

    /**
     * UPDATE 행 추출 (파라미터 재사용)
     */
    private List<List<KeyValueDTO>> extractUpdateRowsFromParameters(Statement statement, List<Object> parameterValues,
                                                                   List<String> auditableColumns, String tableName, 
                                                                   AuditTargetCache auditTargetCache) {
        if (!(statement instanceof Update update) || update.getUpdateSets() == null || update.getUpdateSets().isEmpty()) {
            return AuditConstants.EMPTY_ROWS_LIST;
        }
        
        List<String> pkColumns = auditTargetCache.getPkColumns(tableName);
        
        return extractUpdateColumns(update.getUpdateSets(), parameterValues, auditableColumns, pkColumns);
    }

    /**
     * UPDATE SET절 컬럼 추출
     */
    private List<List<KeyValueDTO>> extractUpdateColumns(List<UpdateSet> updateSets, 
                                                        List<Object> parameterValues,
                                                        List<String> auditableColumns, 
                                                        List<String> pkColumns) {
        List<KeyValueDTO> updateRow = new ArrayList<>();
        int paramIndex = 0;

        for (UpdateSet updateSet : updateSets) {
            paramIndex = processUpdateSet(updateSet, parameterValues, auditableColumns, pkColumns, updateRow, paramIndex);
        }

        return updateRow.isEmpty() ? AuditConstants.EMPTY_ROWS_LIST : List.of(updateRow);
    }

    private int processUpdateSet(UpdateSet updateSet,
                                 List<Object> parameterValues,
                                 List<String> auditableColumns,
                                 List<String> pkColumns,
                                 List<KeyValueDTO> updateRow,
                                 int startIndex) {
            List<Column> columns = updateSet.getColumns();
        ExpressionList<?> valueList = updateSet.getValues();
        if (valueList == null) {
            return startIndex;
        }

        int paramIndex = startIndex;
        List<Expression> assignedValueExpressions = new ArrayList<>();
        for (Expression assignedValueExpression : valueList) {
            assignedValueExpressions.add(assignedValueExpression);
        }
        int maxColumnValuePairs = Math.min(columns.size(), assignedValueExpressions.size());
        for (int columnValuePairIndex = 0; columnValuePairIndex < maxColumnValuePairs; columnValuePairIndex++) {
            Column column = columns.get(columnValuePairIndex);
            Expression assignedValueExpression = assignedValueExpressions.get(columnValuePairIndex);
            paramIndex = processUpdatePair(column, assignedValueExpression, parameterValues, auditableColumns, pkColumns, updateRow, paramIndex);
        }
        return paramIndex;
    }

    private int processUpdatePair(Column column,
                                  Expression valueExpression,
                                  List<Object> parameterValues,
                                  List<String> auditableColumns,
                                  List<String> pkColumns,
                                  List<KeyValueDTO> updateRow,
                                  int paramIndex) {
        String columnName = sanitizeColumnName(column.getColumnName());
        boolean isAuditable = AuditUtil.containsIgnoreCase(auditableColumns, columnName);
        boolean isPrimaryKey = AuditUtil.containsIgnoreCase(pkColumns, columnName);

        if (valueExpression instanceof JdbcParameter) {
            if (isAuditable && !isPrimaryKey && paramIndex < parameterValues.size()) {
                Object boundValue = parameterValues.get(paramIndex);
                if (boundValue != null) {
                    // Integer 타입 처리: 문자열로 감싸진 Integer를 실제 Integer 객체로 변환
                    Object coercedValue = coerceUuidIfString(boundValue);
                    updateRow.add(new KeyValueDTO(columnName, coercedValue));
                }
            }
            return paramIndex + 1; // 항상 소비
        }

        if (!isAuditable || isPrimaryKey) {
            return paramIndex;
        }

        String literalValue = valueExpression == null ? null : valueExpression.toString();
        if (literalValue != null && !literalValue.isBlank()) {
            updateRow.add(new KeyValueDTO(columnName, literalValue));
        }
        return paramIndex;
    }

    /**
     * 행별 감사 로그 생성 
     */
    private void createAuditLogsForRow(SqlRecord sqlRecord, List<KeyValueDTO> changedRow,
                                       Map<String,Integer> targetIdsByColumn, String transactionStatus,
                                       AuditTargetCache auditTargetCache) {
        SqlCommandType cmdType = sqlRecord.getCommandType();
        List<KeyValueDTO> oldRow = (cmdType == SqlCommandType.DELETE || cmdType == SqlCommandType.UPDATE) 
                ? sqlRecord.getOldRow() : AuditConstants.EMPTY_KEYVALUE_LIST;

        // INSERT/UPDATE: NEW_VALUE 추출 후 COMMIT 시점 post-image로 보정 (값이 다르면 조회값으로 덮어씀)
        List<KeyValueDTO> effectiveNewRow = changedRow;
        if (cmdType == SqlCommandType.INSERT) {
            // 감사대상/PK 커버 여부 확인 후 PK→파라미터 기반 순으로 후조회, 컬럼별 비교 보정
            List<KeyValueDTO> params = sqlRecord.getParameterValues();
            boolean allCovered = areAllAuditColumnsInParams(params, targetIdsByColumn.keySet());
            List<String> pkColumns = auditTargetCache.getPkColumns(sqlRecord.getTableName());
            boolean pkCovered = areAllAuditColumnsInParams(params, new HashSet<>(pkColumns));

            if (!allCovered || !pkCovered) {
                List<KeyValueDTO> postImage = fetchPostImageForInsert(sqlRecord, auditTargetCache, targetIdsByColumn);
                if (postImage.isEmpty()) {
                    postImage = fetchPostImageForInsertEnriched(sqlRecord, auditTargetCache, targetIdsByColumn, changedRow);
                }
                if (!postImage.isEmpty()) {
                    effectiveNewRow = reconcileWithPostImage(changedRow, postImage, targetIdsByColumn.keySet());
                }
            }
        } else if (cmdType == SqlCommandType.UPDATE) {
            List<KeyValueDTO> postImage = fetchPostImageForUpdate(sqlRecord, targetIdsByColumn);
            if (!postImage.isEmpty()) {
                effectiveNewRow = reconcileWithPostImage(changedRow, postImage, targetIdsByColumn.keySet());
            }
        }
        
        // 각 컬럼별 감사 처리
        List<KeyValueDTO> finalNewRow = effectiveNewRow;
        targetIdsByColumn.entrySet().forEach(entry -> processColumnAudit(entry, sqlRecord, oldRow, finalNewRow, cmdType, transactionStatus));
    }

    // 감사대상 컬럼이 모두 파라미터에 포함되어 있는지 검사
    private boolean areAllAuditColumnsInParams(List<KeyValueDTO> params, Set<String> auditableColumns) {
        if (auditableColumns == null || auditableColumns.isEmpty()) {
        return true;
    }
        if (params == null || params.isEmpty()) {
            return false;
        }
        
        return auditableColumns.stream()
                .allMatch(column -> isColumnCoveredWithValue(params, column));
    }

    // 특정 컬럼이 파라미터에 값과 함께 포함되어 있는지 검사
    private boolean isColumnCoveredWithValue(List<KeyValueDTO> params, String column) {
        return params.stream()
                .filter(keyValue -> keyValue != null && keyValue.getKey() != null)
                .anyMatch(keyValue -> keyValue.getKey().equalsIgnoreCase(column) && keyValue.getValue() != null);
    }

    // post-image와 비교하여 값이 다르면 조회값으로 덮어쓰기
    private List<KeyValueDTO> reconcileWithPostImage(List<KeyValueDTO> primaryRow,
                                                     List<KeyValueDTO> postImageRow,
                                                     Set<String> auditableColumns) {
        if (primaryRow == null) {
            primaryRow = AuditConstants.EMPTY_KEYVALUE_LIST;
        }
        if (postImageRow == null || auditableColumns == null || auditableColumns.isEmpty()) {
            return primaryRow;
        }
        List<KeyValueDTO> reconciled = new ArrayList<>();
        for (String column : auditableColumns) {
            String primaryVal = getColumnValue(primaryRow, column);
            String postVal = getColumnValue(postImageRow, column);
            if (postVal != null && (primaryVal == null || !postVal.equals(primaryVal))) {
                reconciled.add(new KeyValueDTO(column, postVal));
            } else if (primaryVal != null) {
                reconciled.add(new KeyValueDTO(column, primaryVal));
            }
        }
        return reconciled;
    }

    // INSERT 후 확정값 조회 - selectRowByPrimaryKey 재사용
    private List<KeyValueDTO> fetchPostImageForInsert(SqlRecord sqlRecord, AuditTargetCache auditTargetCache,
                                                     Map<String,Integer> targetIdsByColumn) {
        List<String> pkColumns = auditTargetCache.getPkColumns(sqlRecord.getTableName());
        if (pkColumns.isEmpty()) {
            return AuditConstants.EMPTY_KEYVALUE_LIST;
        }

        BoundSql boundSql = sqlRecord.getBoundSql();
        if (boundSql == null) {
            return AuditConstants.EMPTY_KEYVALUE_LIST;
        }

        String sql = boundSql.getSql();
        List<Object> boundValues = sqlRecord.getParameterValues() == null
                ? List.of()
                : sqlRecord.getParameterValues().stream().map(KeyValueDTO::getValue).toList();

        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (!(statement instanceof Insert insert) || insert.getColumns() == null || insert.getColumns().isEmpty()) {
                return AuditConstants.EMPTY_KEYVALUE_LIST;
            }

            int[] indexHolder = new int[] { 0 };
			// 단건 또는 다건 모두 첫 row 기준으로 PK 추출
            Object items = insert.getValues();
            if (items instanceof Values valuesClause) {
                ExpressionList<?> single = valuesClause.getExpressions();
                if (single != null) {
                    List<Object> pkValues = extractPkFromInsertRow(insert.getColumns(), single, indexHolder, boundValues, pkColumns);
                    pkValues.replaceAll(this::coerceUuidIfString);
                    if (!pkValues.isEmpty() && pkValues.stream().anyMatch(Objects::nonNull)) {
                        return selectRowByPrimaryKey(sqlRecord.getTableName(), new ArrayList<>(targetIdsByColumn.keySet()), pkColumns, pkValues);
                    }
                    return AuditConstants.EMPTY_KEYVALUE_LIST;
                }
            }
            if (!(items instanceof ExpressionList<?> itemsList)) {
                return AuditConstants.EMPTY_KEYVALUE_LIST;
            }
            List<Expression> topLevel = new ArrayList<>();
			for (Expression valueExpression : itemsList) {
				topLevel.add(valueExpression);
            }
            if (!topLevel.isEmpty() && topLevel.get(0) instanceof ExpressionList<?> firstRow) {
                List<Object> pkValues = extractPkFromInsertRow(insert.getColumns(), firstRow, indexHolder, boundValues, pkColumns);
                for (int valueIndex = 0; valueIndex < pkValues.size(); valueIndex++) {
                    pkValues.set(valueIndex, coerceUuidIfString(pkValues.get(valueIndex)));
                }
                if (!pkValues.isEmpty() && pkValues.stream().anyMatch(Objects::nonNull)) {
                    return selectRowByPrimaryKey(sqlRecord.getTableName(), new ArrayList<>(targetIdsByColumn.keySet()), pkColumns, pkValues);
                }
                return AuditConstants.EMPTY_KEYVALUE_LIST;
            }
            List<Object> pkValues = extractPkFromInsertRow(insert.getColumns(), itemsList, indexHolder, boundValues, pkColumns);
            pkValues.replaceAll(this::coerceUuidIfString);
            if (!pkValues.isEmpty() && pkValues.stream().anyMatch(Objects::nonNull)) {
                return selectRowByPrimaryKey(sqlRecord.getTableName(), new ArrayList<>(targetIdsByColumn.keySet()), pkColumns, pkValues);
            }
        } catch (JSQLParserException parseException) {
            log.debug("JSQLParser post-image PK parse failed for table {}", sqlRecord.getTableName(), parseException);
        }

        return AuditConstants.EMPTY_KEYVALUE_LIST;
    }

    private List<Object> extractPkFromInsertRow(List<Column> columns,
                                               ExpressionList<?> valueList,
                                               int[] indexHolder,
                                               List<Object> boundValues,
                                               List<String> pkColumns) {
        if (columns == null || columns.isEmpty() || valueList == null) {
            return List.of();
        }
        List<Object> pkValuesOrdered = new ArrayList<>(Collections.nCopies(pkColumns.size(), null));

        List<Expression> valueExpressions = new ArrayList<>();
        for (Expression valueExpression : valueList) {
            valueExpressions.add(valueExpression);
        }
        int pairCount = Math.min(columns.size(), valueExpressions.size());
        for (int columnValuePairIndex = 0; columnValuePairIndex < pairCount; columnValuePairIndex++) {
            int targetIndex = indexOfIgnoreCase(pkColumns, sanitizeColumnName(columns.get(columnValuePairIndex).getColumnName()));
            Expression expression = valueExpressions.get(columnValuePairIndex);
            if (targetIndex < 0) {
                // PK가 아니더라도 JdbcParameter면 바운드 인덱스는 소비 필요
                consumeIfJdbcParameter(expression, indexHolder, boundValues);
                continue;
            }
            Object value = getPkValue(expression, indexHolder, boundValues);
            pkValuesOrdered.set(targetIndex, value);
        }
        return pkValuesOrdered;
    }

    private Object getPkValue(Expression expression, int[] indexHolder, List<Object> boundValues) {
        if (expression instanceof JdbcParameter) {
            return nextBoundValue(indexHolder, boundValues);
        }
        return null;
    }

    private int indexOfIgnoreCase(List<String> list, String value) {
        if (list == null || value == null) {
            return -1;
        }
        for (int candidateIndex = 0; candidateIndex < list.size(); candidateIndex++) {
            if (value.equalsIgnoreCase(list.get(candidateIndex))) {
                return candidateIndex;
            }
        }
        return -1;
    }

    private boolean isLikelyUuidString(String s) {
        if (s == null) {
            return false;
        }
        String text = s.trim();
        if (text.startsWith("\"") && text.endsWith("\"")) {
            text = text.substring(1, text.length() - 1);
        }
        if (text.startsWith("{") && text.endsWith("}")) {
            text = text.substring(1, text.length() - 1);
        }
        return text.matches(AuditConstants.REGEX_Integer_CANONICAL);
    }

    private Object coerceUuidIfString(Object value) {
        if (value instanceof CharSequence charSequenceValue) {
            final String trimmed = charSequenceValue.toString().trim();
            log.debug("Checking if value is Integer: {}", trimmed);
            
            if (isLikelyUuidString(trimmed)) {
                try {
                    String cleaned = trimmed.replace("\"", "").replace("{", "").replace("}", "");
                    log.debug("Cleaned Integer string: {}", cleaned);
                    Integer uuid = Integer.valueOf(cleaned);
                    log.debug("Successfully converted to Integer: {}", uuid);
                    return uuid;
                } catch (IllegalArgumentException ignored) {
                    log.debug("Failed to convert to Integer, returning original value: {}", value);
                    return value;
                }
            } else {
                log.debug("Value is not a Integer pattern: {}", trimmed);
            }
        }
        log.debug("Value is not CharSequence or not Integer, returning as-is: {}", value);
        return value;
    }

    // INSERT post-image 확장: PK가 없거나 실패 시 changedRow/params 기반 조건으로 조회
    private List<KeyValueDTO> fetchPostImageForInsertEnriched(SqlRecord sqlRecord, AuditTargetCache auditTargetCache,
                                                              Map<String, Integer> targetIdsByColumn, List<KeyValueDTO> changedRow) {
        // 1) PK로 우선 시도
        List<KeyValueDTO> byPk = fetchPostImageForInsert(sqlRecord, auditTargetCache, targetIdsByColumn);
        if (!byPk.isEmpty()) {
            return byPk;
        }

        // 2) changedRow 값들로 조건 구성
        List<PkConditionDTO> conditions = buildConditionsFromRow(changedRow);
        if (!conditions.isEmpty()) {
            return auditMapper.selectRowColumns(sqlRecord.getTableName(), new ArrayList<>(targetIdsByColumn.keySet()), conditions);
        }

        // 3) 파라미터 전체에서 감사대상 컬럼만 non-null로 조건 구성
        List<PkConditionDTO> paramConds = buildConditionsFromParams(sqlRecord.getParameterValues(), targetIdsByColumn.keySet());
        if (!paramConds.isEmpty()) {
            return auditMapper.selectRowColumns(sqlRecord.getTableName(), new ArrayList<>(targetIdsByColumn.keySet()), paramConds);
        }
            return AuditConstants.EMPTY_KEYVALUE_LIST;
        }

    private List<PkConditionDTO> buildConditionsFromRow(List<KeyValueDTO> row) {
        List<PkConditionDTO> list = new ArrayList<>();
        if (row == null) {
            return list;
        }
        for (KeyValueDTO keyValue : row) {
            if (keyValue == null || keyValue.getKey() == null || keyValue.getValue() == null) {
                continue;
            }
            String clean = sanitizeColumnName(keyValue.getKey());
            if (shouldSkipCondition(keyValue.getValue())) {
                continue;
            }
            Object coerced = coerceUuidIfString(keyValue.getValue());
            String type = coerced instanceof Integer ? AuditConstants.Integer_TYPE : null;
            list.add(new PkConditionDTO(clean, coerced, type));
        }
        return list;
    }

    private List<PkConditionDTO> buildConditionsFromParams(List<KeyValueDTO> params, Set<String> auditableColumns) {
        List<PkConditionDTO> list = new ArrayList<>();
        if (params == null || auditableColumns == null || auditableColumns.isEmpty()) {
            return list;
        }
            for (KeyValueDTO keyValue : params) {
            if (keyValue == null || keyValue.getKey() == null || keyValue.getValue() == null) {
                continue;
            }
            if (!AuditUtil.containsIgnoreCase(new ArrayList<>(auditableColumns), keyValue.getKey())) {
                continue;
            }
            String clean = sanitizeColumnName(keyValue.getKey());
            if (shouldSkipCondition(keyValue.getValue())) {
                continue;
            }
            Object coerced = coerceUuidIfString(keyValue.getValue());
            String type = coerced instanceof Integer ? AuditConstants.Integer_TYPE : null;
            list.add(new PkConditionDTO(clean, coerced, type));
        }
        return list;
    }

    // 후보정 WHERE 조건에서 함수형/비-Integer 문자열 등을 제외
    private boolean shouldSkipCondition(Object value) {
        if (value == null) {
            return true;
        }
        // 함수 호출 형태나 표현식 같은 값은 제외
        if (value instanceof CharSequence charSequenceValue) {
            final String rawText = charSequenceValue.toString();
            if (rawText.indexOf('(') >= 0 || rawText.indexOf(')') >= 0 || rawText.contains("::")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 컬럼별 감사 처리 (조건부 최적화)
     */
    private void processColumnAudit(Map.Entry<String, Integer> entry, SqlRecord sqlRecord,
                                   List<KeyValueDTO> oldRow, List<KeyValueDTO> changedRow,
                                   SqlCommandType cmdType, String transactionStatus) {
        String columnName = entry.getKey();
        Integer targetId = entry.getValue();
        // UPDATE일 때, 실제 변경 SET 대상 컬럼만 기록 (변경되지 않은 컬럼은 스킵)
        if (cmdType == SqlCommandType.UPDATE && getColumnValue(changedRow, columnName) == null) {
            return;
        }
        
        // 정책에 따른 old/new 값 추출
        String oldValue = (cmdType == SqlCommandType.UPDATE || cmdType == SqlCommandType.DELETE)
                ? AuditUtil.normalizeJsonValue(getColumnValue(oldRow, columnName)) : null;
        String newValue = (cmdType == SqlCommandType.INSERT || cmdType == SqlCommandType.UPDATE)
                ? AuditUtil.normalizeJsonValue(getColumnValue(changedRow, columnName)) : null;
        
        // 성능 최적화: 변경 없는 UPDATE는 스킵
        if (cmdType == SqlCommandType.UPDATE && 
            ((oldValue == null && newValue == null) || (oldValue != null && oldValue.equals(newValue)))) {
            return;
        }
        
        AuditLogDTO auditLog = createAuditLog(sqlRecord, columnName, targetId, 
                                            cmdType, oldValue, newValue, transactionStatus, oldRow, changedRow);
        insertAuditLog(auditLog);
    }

    /**
     * 감사 로그 DTO 생성
     */
    private AuditLogDTO createAuditLog(SqlRecord sqlRecord, String columnName, Integer targetId,
                                      SqlCommandType cmdType, String oldValue, String newValue, 
                                      String transactionStatus, List<KeyValueDTO> oldRow, List<KeyValueDTO> changedRow) {
        Integer memberId = sqlRecord.getActorUserId();

        // 컬럼 값의 타입 정보 설정
        String oldValueType = getColumnValueType(oldRow, columnName);
        String newValueType = getColumnValueType(changedRow, columnName);

        // 원본 Object 타입 값 설정
        Object oldValueObject = getColumnValueObject(oldRow, columnName);
        Object newValueObject = getColumnValueObject(changedRow, columnName);

        return AuditLogDTO.builder()
                .targetId(targetId)
                .tableName(sqlRecord.getTableName())
                .tableColumn(columnName)
                .operation(cmdType.name())
                .transactionStatus(transactionStatus)
                .valueOld(oldValueObject)
                .valueNew(newValueObject)
                .valueOldType(oldValueType)
                .valueNewType(newValueType)
                .userId(memberId)
                .requestIp(sqlRecord.getRequestIp())
                .regId(memberId)
                .chgId(memberId)
                .delYn("N")
                .build();
    }

    /**
     * 컬럼 값 조회 (Stream 최적화)
     */
    public String getColumnValue(List<KeyValueDTO> row, String columnName) {
        if (row == null || columnName == null) {
            return null;
        }
        
        return row.stream()
                .filter(column -> column != null && column.getKey() != null)
                .filter(column -> column.getKey().equalsIgnoreCase(columnName))
                .map(KeyValueDTO::getValue)
                .filter(Objects::nonNull)
                .map(Object::toString)  
                .findFirst()
                .orElse(null);
    }

    /**
     * 컬럼 값의 타입 정보 조회
     */
    private String getColumnValueType(List<KeyValueDTO> row, String columnName) {
        if (row == null || columnName == null) {
            return null;
        }
        
        return row.stream()
                .filter(column -> column != null && column.getKey() != null)
                .filter(column -> column.getKey().equalsIgnoreCase(columnName))
                .map(KeyValueDTO::getValue)
                .filter(Objects::nonNull)
                .map(value -> {
                    if (value instanceof Integer) return AuditConstants.Integer_TYPE;
                    if (value instanceof Boolean) return "BOOLEAN";
                    if (value instanceof Number) return "NUMERIC";
                    return "TEXT";
                })
                .findFirst()
                .orElse("TEXT");
    }

    /**
     * 컬럼 값 조회 (Object 타입)
     */
    private Object getColumnValueObject(List<KeyValueDTO> row, String columnName) {
        if (row == null || columnName == null) {
            return null;
        }
        
        return row.stream()
                .filter(column -> column != null && column.getKey() != null)
                .filter(column -> column.getKey().equalsIgnoreCase(columnName))
                .map(KeyValueDTO::getValue)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    /**
     * PK 기반 행 조회
     */
    public List<KeyValueDTO> selectRowByPrimaryKey(String tableName, List<String> columns, 
                                                  List<String> pkColumns, List<Object> pkValues) {
        if (columns == null || columns.isEmpty() || pkColumns == null || 
            pkColumns.isEmpty() || pkValues == null || pkValues.isEmpty()) {
            return List.of();
        }
        
        List<PkConditionDTO> pkConditions = buildPkConditions(pkColumns, pkValues);
        return auditMapper.selectRowColumns(tableName, columns, pkConditions);
    }

    /**
     * PK 조건 구성 (Stream 최적화)
     */
    private List<PkConditionDTO> buildPkConditions(List<String> pkColumns, List<Object> pkValues) {
        int minSize = Math.min(pkColumns.size(), pkValues.size());
        List<PkConditionDTO> list = new ArrayList<>();
        for (int positionIndex = 0; positionIndex < minSize; positionIndex++) {
            Object coerced = coerceUuidIfString(pkValues.get(positionIndex));
            String columnType = coerced instanceof Integer ? AuditConstants.Integer_TYPE : null;
            list.add(new PkConditionDTO(pkColumns.get(positionIndex), coerced, columnType));
        }
        return list;
    }
}
