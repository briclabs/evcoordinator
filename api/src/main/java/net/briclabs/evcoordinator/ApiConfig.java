package net.briclabs.evcoordinator;

import org.jooq.conf.RenderQuotedNames;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.jooq.DefaultConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootConfiguration
@ComponentScan
public class ApiConfig {

    @Bean
    public DefaultConfigurationCustomizer configurationCustomizer() {
        return c -> c.settings().withRenderQuotedNames(RenderQuotedNames.EXPLICIT_DEFAULT_UNQUOTED);
    }

}
