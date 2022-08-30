package fi.bizhop.kirves;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfiguration implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods("HEAD", "GET", "POST", "PUT", "PATCH", "DELETE")
                .allowedOrigins("http://localhost:1234", "http://localhost:8080", "https://kirvespeli.herokuapp.com", "https://kirvespeli-web.fly.dev")
                .allowedHeaders("*");
    }
}
