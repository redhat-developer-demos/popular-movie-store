/*
 *  Copyright (c) 2017 Kamesh Sampath<kamesh.sampath@hotmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.workspace7.moviestore.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.AdvancedCache;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.stream.CacheCollectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.workspace7.moviestore.config.MovieStoreProps;
import org.workspace7.moviestore.data.Movie;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @author kameshs
 */
@Component
@Slf4j
public class MovieDBHelper {

    public static final String POPULAR_MOVIES_CACHE = "popular-movies-cache";

    final RestTemplate restTemplate;

    final MovieStoreProps movieStoreProps;

    final AdvancedCache<Object, Object> moviesCache;

    @Autowired
    public MovieDBHelper(RestTemplate restTemplate, MovieStoreProps movieStoreProps,
                         EmbeddedCacheManager cacheManager) {
        this.restTemplate = restTemplate;
        this.movieStoreProps = movieStoreProps;
        this.moviesCache = cacheManager
            .getCache(POPULAR_MOVIES_CACHE).getAdvancedCache();
    }

    /**
     * This method queries the external API and caches the movies, for the demo purpose we just query only first page
     *
     * @return - the status code of the invocation
     */
    protected int queryAndCache() {

        if (this.moviesCache.isEmpty()) {

            log.info("No movies exist in cache, loading cache ..");

            UriComponentsBuilder moviesUri = UriComponentsBuilder
                .fromUriString(movieStoreProps.getApiEndpointUrl() + "/movie/popular")
                .queryParam("api_key", movieStoreProps.getApiKey());

            final URI requestUri = moviesUri.build().toUri();

            log.info("Request URI:{}", requestUri);

            ResponseEntity<String> response = restTemplate.getForEntity(requestUri, String.class);

            log.info("Response Status:{}", response.getStatusCode());

            Map<String, Movie> movieMap = new HashMap<>();

            if (200 == response.getStatusCode().value()) {
                String jsonBody = response.getBody();
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    JsonNode root = objectMapper.readTree(jsonBody);
                    JsonNode results = root.path("results");
                    results.elements().forEachRemaining(movieNode -> {
                        String id = movieNode.get("id").asText();
                        Movie movie = Movie.builder()
                            .id(id)
                            .overview(movieNode.get("overview").asText())
                            .popularity(movieNode.get("popularity").floatValue())
                            .posterPath("http://image.tmdb.org/t/p/w92" + movieNode.get("poster_path").asText())
                            .logoPath("http://image.tmdb.org/t/p/w45" + movieNode.get("poster_path").asText())
                            .title(movieNode.get("title").asText())
                            .price(ThreadLocalRandom.current().nextDouble(1.0, 10.0))
                            .build();
                        movieMap.put(id, movie);
                    });
                } catch (IOException e) {
                    log.error("Error reading response:", e);
                }

                log.debug("Got {} movies", movieMap);
                moviesCache.putAll(movieMap);
            }
            return response.getStatusCode().value();
        } else {
            log.info("Cache already loaded with movies ... will use cache");
            return 200;
        }
    }

    public Movie query(String movieId) {
        return (Movie) moviesCache.get(movieId);
    }

    /**
     * @return
     */
    public List<Movie> getAll() {
        if (moviesCache.isEmpty()) {
            queryAndCache();
        } else {
            log.info("Loading movies from cache");
        }

        List<Movie> movies = moviesCache.entrySet().stream()
            .map(longMovieEntry -> longMovieEntry.getValue())
            .map(o -> Movie.class.cast(o))
            .collect(CacheCollectors.serializableCollector(() -> Collectors.toList()));

        log.info("Loaded {} movies from cache", movies.size());

        return movies;
    }
}

