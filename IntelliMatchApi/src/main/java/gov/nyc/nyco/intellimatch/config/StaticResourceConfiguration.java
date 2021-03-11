package gov.nyc.nyco.intellimatch.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

@Configuration
@EnableEncryptableProperties
@AutoConfigureAfter(DispatcherServletAutoConfiguration.class)
public class StaticResourceConfiguration implements WebMvcConfigurer  {
	@Value("${spring.web.resources.static-locations}")
	private String myExternalFilePathStatic;
	
	@Value("${spring.web.resources.additional-locations}")
	private String myExternalFilePathWeb;
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
	     registry.addResourceHandler("/static/**").addResourceLocations(myExternalFilePathStatic);
	     registry.addResourceHandler("/web/**").addResourceLocations(myExternalFilePathWeb);
	}

}
