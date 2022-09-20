package com.pyd.Config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @ Program: vueblog
 * @ Description: swagger配置
 * @ Author: fei tao
 * @ Create: 2022-4-20 10:56
 */
@Configuration
@EnableSwagger2
@EnableKnife4j
@Import(BeanValidatorPluginsConfiguration.class)
public class Knife4jConfiguration {
    @Bean(value = "defaultApi2")
    public Docket defaultApi2() {
        Docket docket=new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                //分组名称
                .groupName("文档管理系统接口文档")
                .select()
                //这里指定Controller扫描包路径(项目路径也行)
                .apis(RequestHandlerSelectors.basePackage("com.pyd.controller"))
                .paths(PathSelectors.any())//选any表示给这个controller包下所有的接口都生成文档
                .build();
        return docket;
    }
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("接口文档")
                .description("api接口描述")
                .termsOfServiceUrl("http://localhost:8080/")
                //文档制作人、个人主页地址、邮箱
                .contact(new Contact("pyd", "", ""))
                .version("1.0.0")
                .build();
    }

}