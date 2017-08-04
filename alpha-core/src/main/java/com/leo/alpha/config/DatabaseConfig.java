package com.leo.alpha.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.sql.SQLException;

/**
 * @author leo
 */

@Configuration
@EnableTransactionManagement
public class DatabaseConfig {
    
	private static Logger logger=LoggerFactory.getLogger(DatabaseConfig.class);
	
	@Autowired
	private DataSourceProperties properties;
	
	@Bean
	public DruidDataSource dataSource(){
		DruidDataSource dataSource=new DruidDataSource();
		dataSource.setDriverClassName(properties.getDriverClassName());
		dataSource.setUrl(properties.getUrl());
		dataSource.setUsername(properties.getUsername());
		dataSource.setPassword(properties.getPassword());
		dataSource.setInitialSize(properties.getInitPoolSize());
		dataSource.setMaxActive(properties.getMaxPoolSize());
		dataSource.setMinIdle(properties.getMinPoolSize());
		//配置获取连接等待超时的时间
		dataSource.setMaxWait(60000l);
		//配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
		dataSource.setTimeBetweenEvictionRunsMillis(60000);
		// 配置一个连接在池中最小生存的时间，单位是毫秒
		dataSource.setMinEvictableIdleTimeMillis(300000);
		dataSource.setValidationQuery(properties.getValidationQuery());
		dataSource.setTestWhileIdle(true);
		dataSource.setTestOnBorrow(false);
		dataSource.setTestOnReturn(false);
		
		/**
		 * 打开PSCache，并且指定每个连接上PSCache的大小（Oracle使用）
		 */
//		dataSource.setPoolPreparedStatements(true);
		dataSource.setMaxPoolPreparedStatementPerConnectionSize(20);
		
		//配置监控统计拦截的filters 
		try {
			dataSource.setFilters("stat");
		} catch (SQLException e) {
			logger.error("",e);
		}
		return dataSource;
	}
	
    @Bean
    public DataSourceTransactionManager txManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource());
        /**
         * mybatis配置
         */
        Resource resource=new ClassPathResource("mybatis-config.xml");
        sessionFactory.setConfigLocation(resource);
//		Resource resource2=new ClassPathResource("com/leo/alpha/mapper/ResourceMapper.xml");
//		Resource[] rlist=new Resource[1];
//		rlist[0]=resource2;
//        sessionFactory.setMapperLocations(rlist);
        return sessionFactory.getObject();
    }
    
    
    @Bean
    public SqlSessionTemplate sqlSessionTemplate() throws Exception{
    	SqlSessionTemplate template=new SqlSessionTemplate(sqlSessionFactory(),ExecutorType.SIMPLE);
    	return template;
    }
    
//  @Bean(name="dataSourcejdbc")
//  public DynamicRoutingDataSource dataSourcejdbc(){
//  	DynamicRoutingDataSource dynamic=new DynamicRoutingDataSource();
//  	DruidDataSource defaultDataSource=dataSource();
//  	defaultDataSource.setUrl("jdbc:mysql://localhost:3306/leo?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&allowMultiQueries=true");
//  	//设置默认 数据源
//  	dynamic.setDefaultTargetDataSource(defaultDataSource);
//  	
//  	Map<Object, Object> targetDataSources=new HashMap<Object, Object>();//多数据源map
//  	
//  	DruidDataSource adminDataSource=dataSource();
//  	adminDataSource.setUrl("jdbc:mysql://localhost:3306/leo?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&allowMultiQueries=true");
//  	targetDataSources.put(DynamicRoutingDataSource.MASTER_DATA_SOURCE, adminDataSource);
//  	
//  	dynamic.setTargetDataSources(targetDataSources);
//  	return dynamic;
//  }

}
