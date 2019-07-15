package com.stakx.test.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

@Configuration
public class MapKeyReplacement {

	@Autowired
	private MappingMongoConverter mongoConverter;

	// Converts . into a mongo friendly char
	@PostConstruct
	public void setUpMongoEscapeCharacterConversion() {
		mongoConverter.setMapKeyDotReplacement("_");
	}
}
