package com.schmeisky;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

@Configuration
public class ThymeleafConfiguration extends WebMvcConfigurerAdapter {

    @Bean
    public TemplateResolver templateResolver() {
        final ServletContextTemplateResolver resolver = new ServletContextTemplateResolver();   
        resolver.setPrefix("/WEB-INF/");     
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");      
        resolver.setOrder(1);    
        return resolver;       



    }

    @Bean
    public SpringTemplateEngine templateEngine() {
        final SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templateResolver());
        return engine;    
    }

    @Bean
    public ViewResolver viewResolver() {
        final ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine());
        resolver.setCache(false);
        resolver.setOrder(1);
        return resolver;
    }
}
