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

package org.workspace7.moviestore.listeners;

import lombok.extern.slf4j.Slf4j;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryExpired;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryInvalidated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryExpiredEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryInvalidatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryRemovedEvent;

/**
 * @author kameshs
 */
@Listener
@Slf4j
public class SessionsCacheListener {

    @CacheEntryCreated
    public void sessionCreated(CacheEntryCreatedEvent event) {
        log.info("Session with id {} created with data {} ", event.getKey(), event.getValue());
    }

    @CacheEntryInvalidated
    public void sessionInvalidated(CacheEntryInvalidatedEvent event) {
        log.info("Session with id {} invalidated ", event.getKey());
    }

    @CacheEntryRemoved
    public void sessionEvicted(CacheEntryRemovedEvent event) {
        log.info("Session with id {} evicted ", event.getKey());
    }

    @CacheEntryExpired
    public void sessionExpired(CacheEntryExpiredEvent event) {
        log.info("Session with id {} expired ", event.getKey());
    }
}
