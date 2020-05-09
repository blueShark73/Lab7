package com.itmo.utils;

import com.itmo.server.Response;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

@AllArgsConstructor
public class SenderThread extends Thread {
    private DatagramChannel datagramChannel;
    private SocketAddress socketAddress;
    private Response response;

    @Override
    public void run() {
        try {
            byte[] data = new SerializationManager<Response>().writeObject(response);
            ByteBuffer byteBuffer = ByteBuffer.wrap(data);
            datagramChannel.send(byteBuffer, socketAddress);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
