package com.common.auth.common.aop;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.common.auth.audit.util.AuditUtil;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;

@Slf4j
@Component
public class AuditSqlTableResolver {

    private final Map<String, String> statementIdToTable = new ConcurrentHashMap<>();

    public String resolve(MappedStatement mappedStatement, Object parameterObject, SqlCommandType commandType) {
        String mappedStatementId = mappedStatement.getId();
        String cached = statementIdToTable.get(mappedStatementId);
        if (cached != null) {
            return cached;
        }
        try {
            String sqlSource = mappedStatement.getBoundSql(parameterObject).getSql();
            String resolvedTableName = parseTableWithJSqlParser(sqlSource, commandType);
            if (resolvedTableName != null) {
                statementIdToTable.put(mappedStatementId, resolvedTableName);
            }
            return resolvedTableName;
        } catch (JSQLParserException exception) {
            log.debug("Failed to resolve table via JSQLParser for statement: {}", mappedStatementId, exception);
            return null;
        } catch (RuntimeException exception) {
            log.debug("Failed to resolve table for statement: {}", mappedStatementId, exception);
            return null;
        }
    }

    private String parseTableWithJSqlParser(String sql, SqlCommandType type) throws JSQLParserException {
        if (sql == null) {
            return null;
        }
        Statement stmt = CCJSqlParserUtil.parse(sql);
        return switch (type) {
            case INSERT -> (stmt instanceof Insert insert)
                    ? AuditUtil.normalizeName(insert.getTable().getFullyQualifiedName()) : null;
            case UPDATE -> (stmt instanceof Update update && update.getTable() != null)
                    ? AuditUtil.normalizeName(update.getTable().getFullyQualifiedName()) : null;
            case DELETE -> (stmt instanceof Delete delete)
                    ? AuditUtil.normalizeName(delete.getTable().getFullyQualifiedName()) : null;
            default -> null;
        };
    }
}