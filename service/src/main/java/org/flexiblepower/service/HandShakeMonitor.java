/**
 * File ConnectionMonitor.java
 *
 * Copyright 2017 TNO
 */
package org.flexiblepower.service;

import org.flexiblepower.exceptions.SerializationException;
import org.flexiblepower.proto.ConnectionProto.ConnectionHandshake;
import org.flexiblepower.proto.ConnectionProto.ConnectionState;
import org.flexiblepower.serializers.ProtobufMessageSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ.Socket;

/**
 * ConnectionMonitor
 *
 * @author coenvl
 * @version 0.1
 * @since Aug 23, 2017
 */
public class HandShakeMonitor {

    private static final Logger log = LoggerFactory.getLogger(HandShakeMonitor.class);
    private static final int MAX_RECEIVE_TRIES = 100;

    private final Socket publishSocket;
    private final Socket subscribeSocket;
    private final String connectionId;
    private final ProtobufMessageSerializer serializer;

    public HandShakeMonitor(final Socket publishSocket, final Socket subscribeSocket, final String connectionId) {
        this.publishSocket = publishSocket;
        this.subscribeSocket = subscribeSocket;
        this.connectionId = connectionId;

        // Add Protobuf serializer for ConnectionHandshake messages
        this.serializer = new ProtobufMessageSerializer();
        this.serializer.addMessageClass(ConnectionHandshake.class);
    }

    public boolean shakeHands(final ConnectionState currentState) {
        // Send the handshake
        final ConnectionHandshake initHandshakeMessage = ConnectionHandshake.newBuilder()
                .setConnectionId(this.connectionId)
                .setConnectionState(currentState)
                .build();

        final byte[] sendData;
        try {
            sendData = this.serializer.serialize(initHandshakeMessage);
        } catch (final SerializationException e) {
            // This should not happen
            throw new RuntimeException("Exception while serializing message: " + initHandshakeMessage, e);
        }

        if (!this.publishSocket.send(sendData)) {
            // Failed sending handshake
            HandShakeMonitor.log.warn("Failed to send handshake");
            return false;
        }

        // Receive the HandShake
        for (int i = 0; i < HandShakeMonitor.MAX_RECEIVE_TRIES; i++) {
            try {
                ManagedConnection.log.trace("Listening for handshake..");
                final byte[] recvData = this.subscribeSocket.recv();

                if (recvData == null) {
                    // Timeout occured, try again
                    continue;
                }

                final ConnectionHandshake handShakeMessage = (ConnectionHandshake) this.serializer
                        .deserialize(recvData);

                if (handShakeMessage.getConnectionId().equals(this.connectionId)) {
                    ManagedConnection.log.debug("Received acknowledge string: {}", handShakeMessage);
                    // We are done
                    return true;
                } else {
                    ManagedConnection.log
                            .warn("Invalid Connection ID in Handshake message : " + handShakeMessage.getConnectionId());
                    continue;
                }
            } catch (final SerializationException e) {
                // Maybe it was a handshake?
                HandShakeMonitor.log.warn("Received unexpected message while listening for handshake: {}",
                        e.getMessage());
                HandShakeMonitor.log.trace(e.getMessage(), e);
                continue;
            } catch (final Exception e) {
                // The subscribeSocket is closed, probably the session was suspended before it was running
                ManagedConnection.log.warn("Exception while receiving from socket: {}", e.getMessage());
                HandShakeMonitor.log.trace(e.getMessage(), e);
                return false;
            }
        }
        // Still no luck
        return false;
    }

}