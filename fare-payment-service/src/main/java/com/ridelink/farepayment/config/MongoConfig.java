package com.ridelink.farepayment.config;

import java.math.BigDecimal;
import java.util.List;

import org.bson.types.Decimal128;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import org.springframework.core.convert.converter.Converter;

@Configuration
public class MongoConfig {

    /**
     * Maps BigDecimal <-> Decimal128 so money values are stored numerically
     * (sortable/aggregatable) instead of the default String mapping.
     */
    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(List.of(
                new BigDecimalToDecimal128Converter(),
                new Decimal128ToBigDecimalConverter()));
    }

    @WritingConverter
    static class BigDecimalToDecimal128Converter implements Converter<BigDecimal, Decimal128> {
        public Decimal128 convert(BigDecimal source) { return new Decimal128(source); }
    }

    @ReadingConverter
    static class Decimal128ToBigDecimalConverter implements Converter<Decimal128, BigDecimal> {
        public BigDecimal convert(Decimal128 source) { return source.bigDecimalValue(); }
    }
}