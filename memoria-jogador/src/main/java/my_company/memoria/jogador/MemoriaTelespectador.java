/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package my_company.memoria.jogador;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MemoriaTelespectador implements Runnable {
    private MulticastSocket multicastSocket;
    private InetAddress group;
    private boolean conectado = false;
    private MenuCliente menu;

    public MemoriaTelespectador(MenuCliente menu){
        this.menu = menu;
    }
    
    public boolean conectar() {
        try {
            multicastSocket = new MulticastSocket(7777);
            group = InetAddress.getByName("230.0.0.0");
            multicastSocket.joinGroup(group);

            conectado = true;
            System.out.println("Conectado ao grupo multicast.");

            new Thread(this).start();
            menu.setModo(false);
            return true;
        } catch (IOException e) {
            System.err.println("Erro ao conectar ao grupo multicast: " + e.getMessage());
            return false;
        }
    }

    public void desconectar() {
        if (multicastSocket != null && conectado) {
            try {
                multicastSocket.leaveGroup(group);
                multicastSocket.close();
                conectado = false;
                System.out.println("Desconectado do grupo multicast.");
            } catch (IOException e) {
                System.err.println("Erro ao desconectar do grupo multicast: " + e.getMessage());
            }
        }
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];

        while (conectado) {
            try {
                // Recebe uma mensagem do grupo multicast
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(packet);

                // Processa a mensagem recebida
                String mensagem = new String(packet.getData(), 0, packet.getLength());
                processarMensagem(mensagem);
            } catch (IOException e) {
                if (conectado) {
                    System.err.println("Erro ao receber mensagem multicast: " + e.getMessage());
                }
            }
        }
    }

    private void processarMensagem(String mensagem) {
        menu.chamarMetodoDoPainelAtual(mensagem);
    }
}