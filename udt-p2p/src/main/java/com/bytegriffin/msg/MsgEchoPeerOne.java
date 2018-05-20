package com.bytegriffin.msg;

import java.net.InetSocketAddress;

import com.bytegriffin.Config;

/**
 * UDT Message Flow Peer
 * <p>
 * Sends one message when a connection is open and echoes back any received data
 * to the other peer.
 */
public class MsgEchoPeerOne extends MsgEchoPeerBase {

    public MsgEchoPeerOne(final InetSocketAddress self, final InetSocketAddress peer, final int messageSize) {
        super(self, peer, messageSize);
    }

    public static void main(final String[] args) throws Exception {
        final int messageSize = 64 * 1024;
        final InetSocketAddress self = new InetSocketAddress(Config.hostOne, Config.portOne);
        final InetSocketAddress peer = new InetSocketAddress(Config.hostTwo, Config.portTwo);
        new MsgEchoPeerOne(self, peer, messageSize).run();
    }
}