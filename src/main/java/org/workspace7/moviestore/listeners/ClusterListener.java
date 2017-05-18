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
import org.infinispan.notifications.cachemanagerlistener.annotation.ViewChanged;
import org.infinispan.notifications.cachemanagerlistener.event.ViewChangedEvent;

import java.util.concurrent.CountDownLatch;

/**
 * @author kameshs
 */
@Listener
@Slf4j
public class ClusterListener {

    CountDownLatch clusterFormedLatch = new CountDownLatch(1);
    CountDownLatch shutdownLatch = new CountDownLatch(1);
    private final int expectedNodes;

    public ClusterListener(int expectedNodes) {
        this.expectedNodes = expectedNodes;
    }

    @ViewChanged
    public void viewChanged(ViewChangedEvent event) {
        log.info("PMS:::View changed: {} ", event.getNewMembers());
        if (event.getCacheManager().getMembers().size() == expectedNodes) {
            clusterFormedLatch.countDown();
        } else if (event.getNewMembers().size() < event.getOldMembers().size()) {
            shutdownLatch.countDown();
        }
    }
}
