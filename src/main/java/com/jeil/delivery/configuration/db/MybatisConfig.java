package com.jeil.delivery.configuration.db;

import javax.sql.DataSource;

import org.apache.ibatis.session.LocalCacheScope;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@MapperScan(basePackages={
		"com.jeil.delivery",
},sqlSessionFactoryRef=MybatisConfig.OPERATION_SESSION_FACTORY)

public class MybatisConfig {
	// OPERRATION
	public static final String OPERATION_MYBATIS_SESSION_CONFIG = "operationMybatisSessionConfig";
	public static final String OPERATION_SESSION_FACTORY = "operationSessionFactory";
	public static final String OPERATION_SESSION_TEMPLATE = "operationSessionTemplate";

	@Primary
	@Bean(name = OPERATION_SESSION_FACTORY, destroyMethod = "")
	public SqlSessionFactory operationSessionFactory(
			@Qualifier(DataSourceConfig.OPERATION_DATASOURCE) final DataSource dataSource,
			final ApplicationContext applicationContext) throws Exception {
		SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
		sqlSessionFactoryBean.setDataSource(dataSource);
		sqlSessionFactoryBean.setMapperLocations(applicationContext.getResources("classpath:mapper/**/*.xml"));

		operationMybatisSessionConfig().setLocalCacheScope(LocalCacheScope.STATEMENT);
		operationMybatisSessionConfig().setCacheEnabled(false);
		operationMybatisSessionConfig().setJdbcTypeForNull(JdbcType.VARCHAR);
		sqlSessionFactoryBean.setConfiguration(operationMybatisSessionConfig());
		return sqlSessionFactoryBean.getObject();
	}

	@Primary
	@Bean(name = OPERATION_SESSION_TEMPLATE)
	public SqlSessionTemplate operationSessionTemplate(
			@Qualifier(OPERATION_SESSION_FACTORY) SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}

	//===============================
	// MYBATIS_SESSION_CONFIG
	//===============================
	@Bean(name = OPERATION_MYBATIS_SESSION_CONFIG)
	public org.apache.ibatis.session.Configuration operationMybatisSessionConfig(){
		org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
		configuration.setMapUnderscoreToCamelCase(true);
		return configuration;
	}
}
