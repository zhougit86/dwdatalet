package com.yh.dwdatalink.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;


@Configuration
@MapperScan(basePackages = "com.yh.dwdatalink.mapper", sqlSessionTemplateRef = "datalinkSqlSessionTemplate")
public class DataSourceConfig {

    private final String mybatisConfig = "classpath:mybatis/mysql-datalink-mybatis-config.xml";
    private final String sqlmap = "classpath:sqlmap/mysqldatalink/*.xml";

    @Bean(name = "datalinkDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.datalink")
    @Primary
    public DataSource datalinkDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "datalinkSqlSessionFactory")
    @Primary
    public SqlSessionFactory datasourcelinkSqlSessionFactory(@Qualifier("datalinkDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setConfigLocation(new PathMatchingResourcePatternResolver().getResource(mybatisConfig));
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(sqlmap));
        bean.setDataSource(dataSource);
        //保证jar模式运行
        bean.setVfs(SpringBootVFS.class);
        return bean.getObject();
    }

    @Bean(name = "datalinkTransactionManager")
    @Primary
    public DataSourceTransactionManager datalinkTransactionManager(@Qualifier("datalinkDataSource") DataSource dataSource) {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager(dataSource);
        return dataSourceTransactionManager;
    }

    @Bean(name = "datalinkSqlSessionTemplate")
    @Primary
    public SqlSessionTemplate datalinkSqlSessionTemplate(@Qualifier("datalinkSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        SqlSessionTemplate sqlSessionTemplate = new SqlSessionTemplate(sqlSessionFactory);
        return sqlSessionTemplate;
    }

}

