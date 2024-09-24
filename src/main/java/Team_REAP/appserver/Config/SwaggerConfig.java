package Team_REAP.appserver.Config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.ExternalDocumentation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.customizers.OpenApiCustomizer;

@Configuration
@EnableWebMvc
public class SwaggerConfig implements WebMvcConfigurer {

    private static final String SERVICE_NAME = "Make Project";
    private static final String API_VERSION = "V1";
    private static final String API_DESCRIPTION = "MakeProject API TEST";
    private static final String API_URL = "http://localhost:8080/";

    // Springdoc OpenAPI 설정
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("v1-definition")
                .pathsToMatch("/**") // 모든 경로를 포함하도록 설정
                .addOpenApiCustomizer(openApiCustomizer())
                .build();
    }

    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> openApi.info(new Info()
                .title(SERVICE_NAME)
                .version(API_VERSION)
                .description(API_DESCRIPTION)
                .termsOfService(API_URL)
                .license(new License().name("License Name").url("http://example.com")));
    }

    // 리소스 핸들러 설정
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");

        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}
