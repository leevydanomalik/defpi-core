/**
 * File ConnectionManagerTest.java
 *
 * Copyright 2017 TNO
 */
package org.flexiblepower.orchestrator;

import java.util.UUID;

import org.flexiblepower.proto.ServiceProto.ConnectionMessage;
import org.junit.Assert;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

/**
 * ConnectionManagerTest
 *
 * @author coenvl
 * @version 0.1
 * @since Apr 19, 2017
 */
@Slf4j
public class ConnectionManagerTest {

    @Test
    public void tryConnect() {
        final UUID id = UUID.randomUUID();

        final ConnectionMessage connection = ConnectionMessage.newBuilder()
                .setConnectionId(id.toString())
                .setMode(ConnectionMessage.ModeType.CREATE)
                .setTargetAddress("tcp://localhost:5051")
                .setListenPort(5025)
                .setReceiveHash("eefc3942366e0b12795edb10f5358145694e45a7a6e96144299ff2e1f8f5c252")
                .setSendHash("eefc3942366e0b12795edb10f5358145694e45a7a6e96144299ff2e1f8f5c252")
                .build();

        Assert.assertEquals(0, ConnectionManager.sendStartSession("localhost", connection));
    }

}