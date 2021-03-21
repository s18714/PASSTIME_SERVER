package zad1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Server {
    private Thread thread;
    private final StringBuilder serverLog;
    private final InetSocketAddress inetSocketAddress;
    private final Map<SocketChannel, ClientLog> clientLog;

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    public Server(String host, int port) {
        inetSocketAddress = new InetSocketAddress(host, port);
        clientLog = new HashMap<>();
        serverLog = new StringBuilder();
        createThread();
    }

    private void createThread() {
        thread = new Thread(() -> {
            try {
                selector = Selector.open();

                serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.bind(inetSocketAddress);
                serverSocketChannel.configureBlocking(false);

                serverSocketChannel.register(selector, serverSocketChannel.validOps(), null);

                while (!thread.isInterrupted()) {
                    selector.select();

                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();

                        if (key.isAcceptable()) {
                            SocketChannel clientSocket = serverSocketChannel.accept();
                            clientSocket.configureBlocking(false);
                            clientSocket.register(selector, SelectionKey.OP_READ);
                        }

                        if (key.isReadable()) {
                            SocketChannel clientSocket = (SocketChannel) key.channel();
                            ByteBuffer buffer = ByteBuffer.allocate(256);
                            clientSocket.read(buffer);

                            String clientRequest = new String(buffer.array()).trim();

                            String clientResponse = requestHandler(clientSocket, clientRequest).toString();

                            CharBuffer charBuffer = CharBuffer.wrap(clientResponse);
                            ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
                            clientSocket.write(byteBuffer);
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private StringBuilder requestHandler(SocketChannel clientSocket, String str) {
        StringBuilder response = new StringBuilder();

        if (str.matches("login .+")) {
            clientLog.put(clientSocket, new ClientLog(str.substring(6)));
            clientLog.get(clientSocket).fullClientLog.append("=== ").append(clientLog.get(clientSocket).clientId)
                    .append(" log start ===\n").append("logged in\n");
            serverLog.append(clientLog.get(clientSocket).clientId).append(" logged in at ").append(LocalTime.now())
                    .append("\n");
            response.append("logged in");
        } else if (str.matches(".*\\d{4}-\\d{2}-\\d{2}.*")) {
            String[] parts = str.split(" ");
            clientLog.get(clientSocket).fullClientLog.append("Request: ").append(str).append("\n").append("Result:\n")
                    .append(Time.passed(parts[0], parts[1])).append("\n");
            serverLog.append(clientLog.get(clientSocket).clientId).append(" request at ").append(LocalTime.now())
                    .append(": \"").append(str).append("\"").append("\n");
            response.append(Time.passed(parts[0], parts[1]));
        } else if (str.contains("bye")) {
            clientLog.get(clientSocket).fullClientLog.append("logged out\n").append("=== ")
                    .append(clientLog.get(clientSocket).clientId).append(" log end ===\n");
            serverLog.append(clientLog.get(clientSocket).clientId).append(" logged out at ").append(LocalTime.now())
                    .append("\n");
            if (str.equals("bye and log transfer")) {
                response.append(clientLog.get(clientSocket).fullClientLog);
            } else {
                response.append("logged out");
            }
            clientLog.remove(clientSocket);
        }
        return response;
    }

    public void startServer() {
        thread.start();
    }

    public void stopServer() {
        try {
            thread.interrupt();
            Thread.sleep(300);
            serverSocketChannel.close();
            selector.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    String getServerLog() {
        return serverLog.toString();
    }
}