import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;

public class IoClient {
        private Socket socket;
        private Scanner scanner;

        public IoClient() {
            try {
                this.socket = new Socket("localhost", 9000);
                this.scanner = new Scanner(System.in);
                start();
            } catch (IOException e) {
                System.out.println("Server is not available");
            }
        }



        private void start() throws IOException {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                while (true) {
                    try {
//                        byte [] bytes = in.readAllBytes();
//                              String msg = new String (bytes, StandardCharsets.UTF_8);
                       byte inboundBytes = in.readByte();
                       char ch = ((char) inboundBytes);
                    System.out.println(ch);
                    } catch (IOException ex) {
                        System.out.println("Connection closed.");
                        break;
                    }
                }
            })
                    .start();

            while (true) {
                System.out.println("Please enter message");
                String outboundMessage = scanner.nextLine();
                out.writeBytes(outboundMessage);
            }
        }

    public static void main(String[] args) {
        new IoClient();
    }
}
