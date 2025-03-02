/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package my_company.memoria.jogador;

import java.net.Socket;
import java.io.*;

/**
 *
 * @author pinhe
 */
public class MemoriaJogador {
    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter saida;
    private Thread threadRecebimento;
    private boolean conectado = false;
    private MenuCliente menu;
    
    public MemoriaJogador(MenuCliente menu){
        this.menu = menu;
    }
    
    public boolean conectarAoServidor(String ip, int porta) {
        try {
            socket = new Socket(ip, porta);
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            saida = new PrintWriter(socket.getOutputStream(), true);
            conectado = true;

            System.out.println("Conectado ao servidor!");
            
            threadRecebimento = new Thread(this::escutarMensagens);
            threadRecebimento.start();
            menu.setModo(true);

            return true;
        } catch (IOException e) {
            System.out.println("Erro ao conectar: " + e.getMessage());
            return false;
        }
    }

    public int getPorta(){return this.socket.getLocalPort();}
    
    public void enviarMensagem(String mensagem) {
        if (saida != null) {
            saida.println(mensagem);
        }
    }
    
    private void escutarMensagens() {
        try {
            String mensagem;
            while (conectado && (mensagem = entrada.readLine()) != null) {
                processarMensagem(mensagem);
            }
        } catch (IOException e) {
            if (conectado) {
                System.out.println("Erro ao receber mensagem: " + e.getMessage());
            }
        } finally {
            fecharConexao();
        }
    }
    
    private void processarMensagem(String mensagem) {
        if (mensagem.startsWith("Jogo Criado com Sucesso") || mensagem.startsWith("Entrando no jogo") || mensagem.startsWith(("Atualizar painel do jogo"))){
            menu.trocarPainel("EsperaJogo");
            menu.chamarMetodoDoPainelAtual(mensagem);
        }
        else if (mensagem.startsWith("Não há jogos") || mensagem.startsWith("Jogo")){
            menu.chamarMetodoDoPainelAtual(mensagem);
        }
        else if(mensagem.startsWith("Jogo cheio")){
            menu.chamarMetodoDoPainelAtual(mensagem);
        }
        else if(mensagem.startsWith("Tabuleiro")){
            menu.trocarPainel("Jogo");
            menu.chamarMetodoDoPainelAtual(mensagem);
        }
        else if(mensagem.startsWith("Acabou")){
            menu.chamarMetodoDoPainelAtual(mensagem);
            menu.trocarPainel("EntrarJogo");
        }
    }

    public void fecharConexao() {
        try {
            if (socket != null) socket.close();
            if (entrada != null) entrada.close();
            if (saida != null) saida.close();
        } catch (IOException e) {
            System.out.println("Erro ao fechar conexão: " + e.getMessage());
        }
    }
}
