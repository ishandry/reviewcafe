package lnu.ishchuk.reviewcafe.config;

import graphql.language.IntValue;
import graphql.schema.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

@Configuration
public class GraphQlScalarConfig {

    @Bean
    public RuntimeWiringConfigurer ratingScalar() {
        GraphQLScalarType rating = GraphQLScalarType.newScalar()
                .name("Rating")
                .description("Integer rating from 1 to 5")
                .coercing(new Coercing<Integer, Integer>() {
                    @Override
                    public Integer serialize(Object dataFetcherResult) {
                        return toRating(dataFetcherResult);
                    }

                    @Override
                    public Integer parseValue(Object input) {
                        return toRating(input);
                    }

                    @Override
                    public Integer parseLiteral(Object input) {
                        if (input instanceof IntValue) {
                            return toRating(((IntValue) input).getValue().intValue());
                        }
                        throw new CoercingParseLiteralException("Expected AST type 'IntValue'");
                    }

                    private Integer toRating(Object input) {
                        Integer value = (input instanceof Integer) ? (Integer) input :
                                Integer.valueOf(input.toString());
                        if (value < 1 || value > 5)
                            throw new CoercingSerializeException("Rating must be between 1 and 5");
                        return value;
                    }
                })
                .build();

        return wiringBuilder -> wiringBuilder.scalar(rating);
    }
}
