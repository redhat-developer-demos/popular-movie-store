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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.AdvancedCache;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.stream.CacheCollectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.MapSession;
import org.springframework.web.bind.annotation.*;
import org.workspace7.moviestore.data.MovieCart;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author kameshs
 */
@RestController
@Slf4j
public class SessionsController {


    final EmbeddedCacheManager cacheManager;

    @Autowired
    public SessionsController(EmbeddedCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, value = "/sessions", produces = "application/json")
    public @ResponseBody
    String sessions(HttpServletRequest request) {

        final String hostname = System.getenv().getOrDefault("HOSTNAME", "unknown");

        ObjectMapper sessions = new ObjectMapper();

        ObjectNode rootNode = sessions.createObjectNode().put("hostName", hostname);

        String jsonResponse = "{\"message\":\"NO SESSIONS AVAILABLE\"}";

        try {

            AdvancedCache<Object, Object> sessionCache = cacheManager
                .getCache("moviestore-sessions-cache").getAdvancedCache();

            if (sessionCache != null && !sessionCache.isEmpty()) {

                ArrayNode sessionsArray = rootNode.arrayNode();

                Map<Object, Object> sessionsCacheMap = sessionCache.entrySet()
                    .stream()
                    .collect(CacheCollectors.serializableCollector(() ->
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

                sessionsCacheMap.forEach((s, o) -> {

                    MapSession mapSession = (MapSession) o;

                    log.debug("Session Controller Map Session Id {} value : {}", s, mapSession);

                    if (log.isDebugEnabled()) {
                        StringBuilder debugMessage = new StringBuilder();

                        mapSession.getAttributeNames().forEach(key -> {
                            debugMessage.append("Attribute :" + s + " Value: " + mapSession.getAttribute(key));
                        });

                        log.debug("Map Session Attributes : {}", debugMessage);
                    }

                    MovieCart movieCart = mapSession.getAttribute(ShoppingCartController.SESSION_ATTR_MOVIE_CART);

                    if (movieCart != null) {

                        ObjectNode movieCartNode = sessions.createObjectNode();
                        movieCartNode.put("sessionId", mapSession.getId());
                        movieCartNode.put("orderId", movieCart.getOrderId());

                        ArrayNode movieItemsNode = movieCartNode.arrayNode();

                        movieCart.getMovieItems().forEach((movieId, qty) -> {
                            ObjectNode movieItem = movieItemsNode.addObject();
                            movieItem.put("movieId", movieId);
                            movieItem.put("orderQuantity", qty);
                        });

                        movieCartNode.set("movies", movieItemsNode);

                        sessionsArray.add(movieCartNode);
                    }
                });
                rootNode.set("sessions", sessionsArray);
            }
            jsonResponse = sessions.writeValueAsString(rootNode);
        } catch (Exception e) {
            log.error("Error building JSON response for sesisons", e);
        }

        return jsonResponse;
    }

}
