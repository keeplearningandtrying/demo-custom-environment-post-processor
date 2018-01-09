package com.example.demo.env;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;

/**
 * A demo {@link EnvironmentPostProcessor} that maps that advanced mail properties
 * from OS env variables. If at least one property is set, an additional
 * {@link PropertySource} is added to the {@link Environment}.
 *
 * @author Stephane Nicoll
 */
public class MailEnvironmentPostProcessor implements EnvironmentPostProcessor {

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment,
			SpringApplication springApplication) {
		PropertySource<?> osEnvironmentPropertySource = environment.getPropertySources()
				.get(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
		// Could use PropertySourceUtils.getSubProperties() to extract common properties
		PropertySource<?> mailPropertySource = new MailPropertiesMapper(
				osEnvironmentPropertySource).map();
		if (mailPropertySource != null) {
			environment.getPropertySources().addAfter(
					StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
					mailPropertySource);
		}
	}


	private static final class MailPropertiesMapper {

		private final PropertySource<?> propertySource;

		private MailPropertiesMapper(PropertySource<?> propertySource) {
			this.propertySource = propertySource;
		}

		PropertySource map() {
			Map<String, Object> content = new LinkedHashMap<>();
			map(content, key("SMTP_STARTTLS_ENABLE"),
					"spring.mail.properties.mail.smtp.starttls.enable");
			map(content, key("SMTP_CONNECTION_TIMEOUT"),
					"spring.mail.properties.mail.smtp.connectiontimeout");
			// more mapping
			return !content.isEmpty() ? new MapPropertySource("mailProperties", content)
					: null;
		}

		private void map(Map<String, Object> content, String key, String targetKey) {
			if (this.propertySource.containsProperty(key)) {
				content.put(targetKey, this.propertySource.getProperty(key));
			}
		}

		private static String key(String suffix) {
			return "ACME_MAIL_" + suffix;
		}

	}

}
