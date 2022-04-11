import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class NioServer {
    private ByteBuffer byteBuffer = ByteBuffer.allocate(256);
    private Selector selector;

    public static void main(String[] args) throws IOException {
        new NioServer().start();
    }

    public void start() throws IOException {
        selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind(new InetSocketAddress("localhost", 9000));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server started");

        while (true) {
            selector.select();
            System.out.println("New selector event");
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                if (selectionKey.isAcceptable()) {
                    System.out.println("New selector acceptable event");
                    register(selector, serverSocket);
                }

                if (selectionKey.isReadable()) {
                    System.out.println("New selector readable event");
                    readMessage(selectionKey);
                }
                iterator.remove();
            }
        }
    }

    public void register(Selector selector, ServerSocketChannel serverSocket) throws IOException {
        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        System.out.println("New client is connected");
    }

    public void readMessage(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        StringBuilder sb = new StringBuilder();
        byteBuffer.clear();
        int bytesRead = 0;
        while ((bytesRead = client.read(byteBuffer)) > 0) {
            byteBuffer.flip();
            byte[] bytes = new byte[byteBuffer.limit()];
            byteBuffer.get(bytes);
            sb.append(new String(bytes));
//            while (byteBuffer.hasRemaining()) {
//                System.out.print((char) byteBuffer.get());
//            }
            byteBuffer.clear();
//            bytesRead = client.read(byteBuffer);
        }
        String msg;
        if (bytesRead < 0) {
            msg = key.attachment() + ": покинул чат \n";
            client.close();
        } else {
            msg = sb.toString();
        }
        System.out.println(msg);
        broadCastMessage(msg);
    }

    private void broadCastMessage(String msg) throws IOException {
        ByteBuffer msgBuff = ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8));
        for (SelectionKey key : selector.keys()) {
            if (key.isValid() && key.channel() instanceof SocketChannel) {
                SocketChannel client = (SocketChannel) key.channel();
                client.write(msgBuff);
                msgBuff.rewind();
            }
        }
    }
}








