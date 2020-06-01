
/*
 *   OVERVIEW:
 *   Manda i messaggi sulla porta passata in input
*/

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class UDPsender  {
    public static void send(String message, int port) throws IOException {

        DatagramSocket socket = new DatagramSocket();
        InetAddress group = InetAddress.getByName("230.0.0.0");

        byte[] msg = message.getBytes();
        DatagramPacket packet = new DatagramPacket(msg, msg.length, group, port);

        socket.send(packet);
        socket.close();
    }
}
