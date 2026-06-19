package com.jeil.delivery.configuration.aop;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import com.jeil.delivery.configuration.db.DataSourceConfig;

@Configuration
@EnableAspectJAutoProxy
@EnableTransactionManagement
public class TransactionAOP {
	private static final int TX_METHOD_TIMEOUT = 300;
	private static final String AOP_POINTCUT_EXPRESSION = "execution(* com.jeil.delivery..*Service.*(..))";
	private static final String OPERATION_TX_MANAGER = "operationTxManager";

	@Bean(name = OPERATION_TX_MANAGER)
	public DataSourceTransactionManager operationTxManager(
			@Qualifier(DataSourceConfig.OPERATION_DATASOURCE) final DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}

	@Bean
	public TransactionInterceptor operationTxInterceptor(DataSourceTransactionManager transactionManager) {
		TransactionInterceptor txAdvice = new TransactionInterceptor();
		Properties txAttributes = new Properties();
		List<RollbackRuleAttribute> rollbackRules = new ArrayList<>();
		rollbackRules.add(new RollbackRuleAttribute(Exception.class));
		DefaultTransactionAttribute readOnlyAttribute = new DefaultTransactionAttribute(TransactionDefinition.PROPAGATION_REQUIRED);
		RuleBasedTransactionAttribute writeAttribute = new RuleBasedTransactionAttribute(TransactionDefinition.PROPAGATION_REQUIRES_NEW, rollbackRules);

		readOnlyAttribute.setReadOnly(true);
		readOnlyAttribute.setTimeout(TX_METHOD_TIMEOUT);

		writeAttribute.setTimeout(TX_METHOD_TIMEOUT);

		String readOnlyTransactionAttributesDefinition = readOnlyAttribute.toString();
		String writeTransactionAttributesDefinition = writeAttribute.toString();

		txAttributes.setProperty("select*", readOnlyTransactionAttributesDefinition);
		txAttributes.setProperty("get*", readOnlyTransactionAttributesDefinition);
		txAttributes.setProperty("search*", readOnlyTransactionAttributesDefinition);
		txAttributes.setProperty("find*", readOnlyTransactionAttributesDefinition);
		txAttributes.setProperty("count*", readOnlyTransactionAttributesDefinition);

		txAttributes.setProperty("*", writeTransactionAttributesDefinition);

		txAdvice.setTransactionAttributes(txAttributes);
		txAdvice.setTransactionManager(transactionManager);

		return txAdvice;
	}

	@Bean
	public Advisor operationTxAdviceAdvisor(@Qualifier(OPERATION_TX_MANAGER)DataSourceTransactionManager transactionManager) {
		AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();

		pointcut.setExpression(AOP_POINTCUT_EXPRESSION);

		return new DefaultPointcutAdvisor(pointcut, operationTxInterceptor(transactionManager));
	}
}
