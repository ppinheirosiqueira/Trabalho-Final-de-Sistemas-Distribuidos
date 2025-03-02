/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package my_company.memoria.servidor;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Multicast {
    private MulticastSocket multicastSocket;
    private InetAddress group;

    public Multicast() {
        try {
            multicastSocket = new MulticastSocket();
            group = InetAddress.getByName("230.0.0.0");
        } catch (IOException e) {
            System.err.println("Erro ao inicializar o MulticastSender: " + e.getMessage());
        }
    }

    public void enviarMensagem(String mensagem) {
        try {
            byte[] buffer = mensagem.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, 7777);
            multicastSocket.send(packet);
            System.out.println("Mensagem multicast enviada");
        } catch (IOException e) {
            System.err.println("Erro ao enviar mensagem multicast: " + e.getMessage());
        }
    }
}