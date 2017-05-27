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

package org.workspace7.moviestore.controller;

import lombok.extern.slf4j.Slf4j;
import org.infinispan.AdvancedCache;
import org.infinispan.health.HealthStatus;
import org.infinispan.spring.provider.SpringEmbeddedCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.session.MapSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.workspace7.moviestore.data.Movie;
import org.workspace7.moviestore.data.MovieCart;
import org.workspace7.moviestore.data.MovieCartItem;
import org.workspace7.moviestore.utils.MovieDBHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author kameshs
 */
@Controller
@Slf4j
public class HomeController {


    final MovieDBHelper movieDBHelper;

    final SpringEmbeddedCacheManager cacheManager;

    @Autowired
    public HomeController(SpringEmbeddedCacheManager cacheManager, MovieDBHelper movieDBHelper) {
        this.cacheManager = cacheManager;
        this.movieDBHelper = movieDBHelper;
    }

    @GetMapping("/")
    public ModelAndView home(ModelAndView modelAndView, HttpServletRequest request) {

        final String hostname = System.getenv().getOrDefault("HOSTNAME", "unknown");
        log.info("Request served by HOST {} ", hostname);

        HttpSession session = request.getSession(false);

        List<Movie> movies = movieDBHelper.getAll();

        List<MovieCartItem> movieList = movies.stream()
            .map((Movie movie) -> MovieCartItem.builder()
                .movie(movie)
                .quantity(1)
                .total(0)
                .build())
            .collect(Collectors.toList());

        if (session != null) {
            AdvancedCache<String, Object> sessionCache = (AdvancedCache<String, Object>)
                cacheManager.getCache("moviestore-sessions-cache").getNativeCache();

            Optional<MapSession> mapSession = Optional.ofNullable((MapSession) sessionCache.get(session.getId()));

            log.info("Session already exists, retrieving values from session {}", mapSession);

            int cartCount = 0;

            if (mapSession.isPresent()) {

                MovieCart movieCart = mapSession.get().getAttribute(ShoppingCartController.SESSION_ATTR_MOVIE_CART);

                if (movieCart != null) {

                    log.info("Movie Cart:{} for session id:{}", movieCart, session.getId());

                    final Map<String, Integer> movieItems = movieCart.getMovieItems();

                    movieList = movieList.stream()
                        .map(movieCartItem -> {
                            Movie movie = movieCartItem.getMovie();
                            String movieId = movie.getId();
                            if (movieItems.containsKey(movieId)) {
                                int quantity = movieItems.get(movieId);
                                movieCartItem.setQuantity(quantity);
                            } else {
                                movieCartItem.setQuantity(1);
                            }
                            return movieCartItem;

                        }).collect(Collectors.toList());

                    cartCount = movieItems.size();
                }
            }
            modelAndView.addObject("cartCount", cartCount);
            modelAndView.addObject("movies", movieList);
        } else {
            log.info("New Session");
            modelAndView.addObject("movies", movieList);
        }
        modelAndView.setViewName("home");
        modelAndView.addObject("hostname", hostname);
        return modelAndView;
    }


    @PostMapping("/logout")
    public ModelAndView clear(ModelAndView modelAndView, HttpServletRequest request) {
        final String hostname = System.getenv().getOrDefault("HOSTNAME", "unknown");
        List<Movie> movies = movieDBHelper.getAll();

        List<MovieCartItem> movieList = movies.stream()
            .map((Movie movie) -> MovieCartItem.builder()
                .movie(movie)
                .quantity(0)
                .total(0)
                .build())
            .collect(Collectors.toList());

        HttpSession session = request.getSession(false);

        if (session != null) {
            log.info("Invalidating session:{}", session.getId());
            session.invalidate();
        }

        log.info("New Session");
        modelAndView.addObject("movies", movieList);
        modelAndView.setViewName("home");
        modelAndView.addObject("hostname", hostname);
        return modelAndView;
    }

    @GetMapping("/healthz")
    public ResponseEntity healthz() {

        HealthStatus healthStatus = cacheManager.getNativeCacheManager()
            .getHealth().getClusterHealth().getHealthStatus();

        if (healthStatus == HealthStatus.HEALTHY) {
            log.info("HEALTHY");
            return new ResponseEntity(HttpStatus.OK);
        }

        return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
