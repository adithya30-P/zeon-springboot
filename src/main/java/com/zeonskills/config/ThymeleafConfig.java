package com.zeonskills.config;

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThymeleafConfig {

    /**
     * Registers the Thymeleaf Layout Dialect, which gives us layout:decorate
     * and layout:fragment - the direct equivalents of Jinja2's
     * {% extends %} and {% block %} used throughout the original templates.
     */
    @Bean
    public LayoutDialect layoutDialect() {
        return new LayoutDialect();
    }
}
