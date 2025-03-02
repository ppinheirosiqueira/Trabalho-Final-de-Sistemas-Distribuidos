/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package my_company.memoria.servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author pinhe
 */
public class MemoriaServidor implements JogoListener{

    private static MemoriaServidor instance;
    private static MenuServidor menu;
    private static ServerSocket serverSocket;
    private static boolean servidorAtivo = false;
    private static boolean multicastAtivo = false;
    private static Thread servidorThread;
    private static Thread multicastThread;
    private static List<Jogo> jogos = new ArrayList<>();
    private static final Map<Socket, PrintWriter> jogadores = new HashMap<>();
    private static Multicast multicast;

    public static void main(String[] args) {
        menu = new MenuServidor();
        menu.setVisible(true);
    }
    
    public static MemoriaServidor getInstance() {
        if (instance == null) {
            instance = new MemoriaServidor();
        }
        return instance;
    }
    
    public static int criarJogo(Socket cliente, int numPares){
        MemoriaServidor servidor = MemoriaServidor.getInstance(); // Obtém a instância única
        for (Jogo jogo : jogos) {
            if (jogo.contemJogador(cliente)) {
                return -1;
            }
        }
        
        Jogo novo_jogo = new Jogo(numPares, cliente, servidor);
        novo_jogo.conectarJogador(cliente);
        jogos.add(novo_jogo);

        return jogos.size() - 1;        
    }
    
    public static void iniciarServidor(int porta) {
        if (servidorAtivo) return; // Evita iniciar duas vezes

        servidorAtivo = true;
        servidorThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(porta);
                System.out.println("Servidor iniciado na porta " + porta);

                while (servidorAtivo) {
                    Socket cliente = serverSocket.accept();
                    System.out.println("Novo cliente conectado: " + cliente);
                    PrintWriter saida = new PrintWriter(cliente.getOutputStream(), true);
                    jogadores.put(cliente, saida);
                    try {
                        Thread.sleep(1000); // Necessário ou ele retorna uma mensagem antes do cliente atualizar o panel
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    enviarMensagem(cliente, getJogos());
                    new Thread(() -> atenderCliente(cliente)).start();
                }
            } catch (IOException e) {
                if (servidorAtivo) {
                    System.out.println("Erro no servidor: " + e.getMessage());
                }
            } finally {
                pararServidor();
            }
        });
        servidorThread.start();
    }

    private static String getJogos(){
        String saida = "";
        int i = 0;
        for (Jogo jogo : jogos) {
            saida = saida + "Jogo " + (i+1) + ": " + jogo.getNumPares() + " pares e " + jogo.getQtdJogadores() + " jogadores^^^";
            i++;
        }
        if (saida.equals("")) saida = "Não há jogos";
        return saida;
    }
    
    private static void atenderCliente(Socket cliente) {
        try {
            BufferedReader entrada = new BufferedReader(new InputStreamReader(cliente.getInputStream()));

            String mensagem;
            while ((mensagem = entrada.readLine()) != null) {
                processarMensagem(mensagem, cliente);
            }
        } catch (IOException e) {
            System.out.println("Erro ao comunicar com o cliente: " + e.getMessage());
            for (Jogo jogo : jogos){
                if(jogo.contemJogador(cliente)) jogo.desconectarJogador(cliente);
            }
        } finally {
            try {
                cliente.close();
            } catch (IOException e) {
                System.out.println("Erro ao fechar conexão do cliente: " + e.getMessage());
            }
        }
    }

    private static void processarMensagem(String mensagem, Socket cliente) {
        System.out.println(mensagem);
        if (mensagem.startsWith("Criar Jogo")){
            String[] palavras = mensagem.split("-");
            int indice = criarJogo(cliente, Integer.parseInt(palavras[palavras.length - 1].strip()));
            if (indice != -1){
                enviarMensagem(cliente, "Jogo Criado com Sucesso - " + jogos.get(indice).getJogadores());
            }
        }
        else if(mensagem.startsWith("Entrando no jogo")){
            String[] palavras = mensagem.split(" "); 
            boolean adicionou = jogos.get(Integer.parseInt(palavras[palavras.length - 1])).conectarJogador(cliente);
            if (adicionou) enviarMensagem(cliente, "Entrando no jogo - " + jogos.get(Integer.parseInt(palavras[palavras.length - 1])).getJogadores());
            else enviarMensagem(cliente, "Jogo cheio");
        }
        else if(mensagem.startsWith("Começar Jogo")){
            for (Jogo jogo : jogos){
                if (cliente == jogo.getCriador()){
                    String[] palavras = mensagem.split("-");
                    jogo.setTempo(Integer.parseInt(palavras[palavras.length - 1].strip()) * 1000);
                    jogo.comecarJogo();
                    break;
                }
            }
        }
        else if(mensagem.startsWith("Movimento")){
            Jogo aux = null;
            for (Jogo jogo : jogos){
                if (jogo.contemJogador(cliente)){
                    aux = jogo;
                    break;
                }
            }
            String[] partes = mensagem.split("-");
            aux.checkMovimento(Integer.parseInt(partes[1].strip()), Integer.parseInt(partes[2].strip()), cliente);
        }
        else System.out.println("Mensagem recebida de " + cliente + ": " + mensagem);
    }

    public static void enviarMensagem(Socket cliente, String mensagem) {
        PrintWriter saida = jogadores.get(cliente);
        if (saida != null) {
            saida.println(mensagem);
        }
    }
    
    public static void pararServidor() {
        servidorAtivo = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.out.println("Erro ao fechar o servidor: " + e.getMessage());
        }
    }

    public static boolean isServidorAtivo() {
        return servidorAtivo;
    }
    
    public static boolean isMulticastAtivo(){
        return multicastAtivo;
    }
    
    public static void deletarJogo(Jogo jogo) {
        jogos.remove(jogo);
        System.out.println("Jogo removido com sucesso.");
    }

    @Override
    public void onJogoTerminado(Jogo jogo) {
        if (multicast != null){multicast.enviarMensagem(jogo.getCriador().getPort() + "^^^Jogo terminado - vencedor:" + jogo.getVencedor().getPort());}
        deletarJogo(jogo);
    }
    
    @Override
    public void onJogoAtualizado(Jogo jogo) {
        if (multicast != null) multicast.enviarMensagem(jogo.getCriador().getPort() + "^^^" + jogo.montarPontuacao() + "^^^" + jogo.montarTabuleiro());
    }
    
    public static void iniciarMulticast() {
        if (multicast != null) {
            System.out.println("Multicast já está ativo.");
            return;
        }

        multicast = new Multicast();
        multicastThread = new Thread(new MulticastThread(multicast));
        multicastThread.start();
        multicastAtivo = true;
        System.out.println("Multicast iniciado.");
    }

    public static void pararMulticast() {
        if (multicastThread != null && multicastThread.isAlive()) {
            multicastThread.interrupt(); // Interrompe a thread de informações
        }
        if (multicast != null) {
            multicast = null;
            multicastAtivo = false;
            System.out.println("Multicast parado.");
        } else {
            System.out.println("Multicast não está ativo.");
        }
    }
    
    public static String getJogosAtivos() {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Jogo jogo : jogos) {
            if (jogo.getComecou()){
                sb.append("Jogo criador porta: ").append(jogo.getCriador().getPort()).append(" -> ")
                  .append(jogo.checkTotalAchado()).append("/").append(jogo.getNumPares()).append(" pares achados e ")
                  .append(jogo.getQtdJogadores()).append(" jogadores^^^");
                i++;
            }
        }
        if (sb.length() == 0) {return "Não há jogos começados para ver";}
        return "Jogos Disponiveis ***" + sb.toString();
    }
}
