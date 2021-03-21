/**
 * @author Kryzhanivskyi Denys S18714
 */

package zad1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class Client {
    private final InetSocketAddress inetSocketAddress;
    private SocketChannel channel;
    private final String clientId;

    public Client(String host, int port, String id) {
        this.inetSocketAddress = new InetSocketAddress(host, port);
        this.clientId = id;
    }

    public void connect() {
        try {
            channel = SocketChannel.open(inetSocketAddress);
            channel.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String send(String req) {
        StringBuilder response = new StringBuilder();
        try {
            ByteBuffer buffer = ByteBuffer.wrap(req.getBytes());
            channel.write(buffer);
            buffer.clear();

            int bytesRead = channel.read(buffer);
            while (bytesRead == 0) {
                Thread.sleep(10);
                bytesRead = channel.read(buffer);
            }

            while (bytesRead != 0) {
                buffer.flip();
                CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer);
                response.append(charBuffer);
                buffer.clear();
                bytesRead = channel.read(buffer);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return response.toString();
    }

    public String getClientId() {
        return clientId;
    }
}