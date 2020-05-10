package com.itmo.server;

import com.itmo.app.Application;
import com.itmo.utils.HandlerThread;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.*;

public class Server {
    private DatagramChannel channel;
    private byte[] buffer;
    private static final int DEFAULT_BUFFER_SIZE = 65536;

    public Server() {
        buffer = new byte[DEFAULT_BUFFER_SIZE];
    }

    //модуль приёма соединений
    public void connect(int port) throws IOException {
        SocketAddress address = new InetSocketAddress(port);
        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.bind(address);
        setByeMessage("Сервер закончил работу...");
    }

    //чтение полученных данных и отправка ответа
    public void run(Application application) throws IOException {
        try {
            Callable<SocketAddress> task = getTask();
            ExecutorService service = Executors.newFixedThreadPool(2);
            while (true) {
                Future<SocketAddress> result = service.submit(task);
                SocketAddress socketAddress = result.get();
                byte[] copyData = new byte[buffer.length];
                System.arraycopy(buffer, 0, copyData, 0, buffer.length);
                new HandlerThread(application, copyData, socketAddress, channel).start();
            }
        } catch (ClassCastException e) {
            System.out.println("Сервер ожидал команду, а получил что-то не то...");
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            System.out.println("Сервер завершает работу...");
            channel.close();
        }
    }

    private void setByeMessage(String message){
        Runtime.getRuntime().addShutdownHook(new Thread(() -> System.out.println(message)));
    }

    private Callable<SocketAddress> getTask(){
        return () -> {
            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
            SocketAddress socketAddress;
            do {
                socketAddress = channel.receive(byteBuffer);
            } while (socketAddress == null);
            return socketAddress;
        };
    }
}