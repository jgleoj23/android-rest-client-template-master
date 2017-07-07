package com.codepath.apps.restclienttemplate;

import com.codepath.apps.restclienttemplate.model.Tweet;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

/**
 * @author Joseph Gardi
 */
public class TweetListMapper {
    private ObjectMapper objectMapper = new ObjectMapper();

    private Function<String, List<Tweet>> fromString = new Function<String, List<Tweet>>() {
        @Override
        public List<Tweet> apply(@NonNull String json) throws Exception {
            return objectMapper.readValue(json,
                                          new TypeReference<List<Tweet>>() {});
        }
    };

    public Function<String, List<Tweet>> getMapper() {
        return fromString;
    }
}
