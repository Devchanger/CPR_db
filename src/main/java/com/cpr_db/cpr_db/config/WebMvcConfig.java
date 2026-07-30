package com.cpr_db.cpr_db.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

<<<<<<< HEAD
import java.nio.file.Path;
import java.nio.file.Paths;

=======
>>>>>>> 193e2be (feat: complete all missing backend API endpoints)
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:/opt/cpr-db/uploads/");
<<<<<<< HEAD
=======
        registry.addResourceHandler("/uploads/images/**")
                .addResourceLocations("file:/opt/cpr-db/uploads/images/");
        registry.addResourceHandler("/uploads/videos/**")
                .addResourceLocations("file:/opt/cpr-db/uploads/videos/");
>>>>>>> 193e2be (feat: complete all missing backend API endpoints)
    }
}
