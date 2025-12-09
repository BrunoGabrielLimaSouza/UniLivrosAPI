package com.unilivros.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.modelmapper.config.Configuration.AccessLevel;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.getConfiguration()
                // 🛑 ESTA É A LINHA MÁGICA: Ignora propriedades nulas na origem (DTO)
                .setSkipNullEnabled(true)
                // Opcional: Define correspondência estrita para evitar erros de ambiguidade
                .setMatchingStrategy(MatchingStrategies.STRICT)
                // Opcional: Permite acessar campos privados se necessário
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(AccessLevel.PRIVATE);

        return modelMapper;
    }
}
