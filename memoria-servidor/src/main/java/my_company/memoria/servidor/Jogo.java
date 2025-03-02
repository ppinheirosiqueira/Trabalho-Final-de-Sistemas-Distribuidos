/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package my_company.memoria.servidor;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.PrintWriter;

/**
 *
 * @author pinhe
 */
public class Jogo {
    private Map<Socket, Integer> pontuacaoJogadores = new HashMap<>();
    private int[][] jogo;
    private int numPares;
    private List<Socket> ordemJogadores = new ArrayList<>();
    private int turno;
    private boolean comecou;
    private final Socket criador;
    private int espera = 3000;
    private List<int[]> movimentos = new ArrayList<>();
    private JogoListener listener; // Único listener
    
    public Jogo(int numeroPares, Socket criador, MemoriaServidor pai){
        this.numPares = numeroPares;
        this.listener = pai;
        this.comecou = false;
        this.criador = criador;
        gerarJogo();
    }
    
    public int getNumPares(){return numPares;}
    
    public boolean getComecou(){return this.comecou;}
    
    public Socket getCriador() {return criador;}
    
    public void setTempo(int novoTempo){ this.espera = novoTempo;}
    
    public int getQtdJogadores(){return ordemJogadores.size();}
        
    public String getJogadores(){
        String jogadores = "";
        int i = 1;
        for (Socket jogador : pontuacaoJogadores.keySet()) {
            jogadores = jogadores + "Jogador " + i + ": porta: " + jogador.getPort() + "^^^";
            i++;
        }
        return jogadores;
    }
    
    public void comecarJogo(){
        this.comecou = true;
        this.turno = 0;
        atualizarTabuleiro(true);
    }
    
    public boolean contemJogador(Socket cliente) {
        return pontuacaoJogadores.containsKey(cliente);
    }
    
    public boolean conectarJogador(Socket cliente){
        if (pontuacaoJogadores.size() >= 4) {
            System.out.println("Limite de jogadores atingido. Cliente não pode entrar: " + cliente);
            return false;
        }
        if (comecou == true){
            System.out.println("O jogo já começou! Novos jogadores não podem entrar.");
            return false;
        }
        if (!pontuacaoJogadores.containsKey(cliente)) {
            pontuacaoJogadores.put(cliente, 0);
            ordemJogadores.add(cliente);
            System.out.println("Novo jogador adicionado: " + cliente);
            String textoJogadores = getJogadores();
            for (Socket jogador : pontuacaoJogadores.keySet()) { // Atualiza os paineis de outros jogadores
                if (!jogador.equals(cliente)) {
                    try {
                        PrintWriter saida = new PrintWriter(jogador.getOutputStream(), true);
                        if (jogador == criador) saida.println("Jogo Criado com Sucesso - " + textoJogadores);
                        else saida.println("Atualizar painel do jogo - " + textoJogadores);
                    } catch (Exception e) {
                        System.err.println("Erro ao enviar dados para " + jogador + ": " + e.getMessage());
                    }
                }
            }
            return true;
        } else {
            System.out.println("Jogador já está na lista.");
            return false;
        }
    }
    
    public void desconectarJogador(Socket cliente){
        if (this.comecou == true){
            this.numPares = this.numPares - pontuacaoJogadores.get(cliente);
            ordemJogadores.remove(cliente);
            pontuacaoJogadores.remove(cliente);
            if (pontuacaoJogadores.isEmpty()){
                listener.onJogoTerminado(this);
                return;    
            }
            if (turno >= ordemJogadores.size()) turno = ordemJogadores.size() - 1;
            atualizarTabuleiro(true);
            return;
        }
        else if(this.criador == cliente){
            listener.onJogoTerminado(this);
        }
        String textoJogadores = getJogadores();
        for (Socket jogador : pontuacaoJogadores.keySet()) { // Atualiza os paineis de outros jogadores
            try {
                PrintWriter saida = new PrintWriter(jogador.getOutputStream(), true);
                if (jogador == criador) saida.println("Jogo Criado com Sucesso - " + textoJogadores);
                else saida.println("Atualizar painel do jogo - " + textoJogadores);
            } catch (Exception e) {
                System.err.println("Erro ao enviar dados para " + jogador + ": " + e.getMessage());
            }
        }
    }
    
    private void gerarJogo() {
        int totalNumeros = 2 * numPares;
        int linhas = (int) Math.ceil(Math.sqrt(totalNumeros));
        int colunas = (int) Math.ceil((double) totalNumeros / linhas);

        jogo = new int[linhas][colunas];

        // Criar lista de números com os pares
        List<Integer> numeros = new ArrayList<>();
        for (int i = 1; i <= numPares; i++) {
            numeros.add(i);
            numeros.add(i);
        }

        Collections.shuffle(numeros);

        int index = 0;
        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < colunas; j++) {
                if (index < totalNumeros) {
                    jogo[i][j] = numeros.get(index++);
                } else {
                    jogo[i][j] = -1;
                }
            }
        }
    }
    
    public void imprimirJogo() {
        for (int[] linha : jogo) {
            for (int num : linha) {
                System.out.print(num + "\t");
            }
            System.out.println();
        }
    }
    
    public int checkTotalAchado(){
        int total = 0;
        for (int pontos : pontuacaoJogadores.values()) {
            total += pontos;
        }
        return total;
    }

    private boolean checkJogoAcabou(){
        int total = checkTotalAchado();
        return total == numPares;
    }

    public Socket getVencedor(){
        int maiorPontuacao = 0;
        Socket vencedor = null;
        for (Socket jogador : pontuacaoJogadores.keySet()) {
            if (pontuacaoJogadores.get(jogador) > maiorPontuacao) {
                maiorPontuacao = pontuacaoJogadores.get(jogador);
                vencedor = jogador;
            }
        }
        return vencedor;
    }
    
    private void acabarJogo(){
        Socket vencedor = getVencedor();
        for (Socket jogador : pontuacaoJogadores.keySet()) {
            try {
                String dados = "Acabou^^^";
                if (vencedor == jogador) dados = dados + "Parabéns";
                else dados = dados + "Perdeu";
                PrintWriter saida = new PrintWriter(jogador.getOutputStream(), true);
                saida.println(dados);
            } catch (Exception e) {
                System.err.println("Erro ao enviar dados para " + jogador + ": " + e.getMessage());
            }
        }
        listener.onJogoTerminado(this);
    }
   
    
    public void checkMovimento(int x, int y, Socket cliente){
        if (!pontuacaoJogadores.containsKey(cliente)) {
            System.out.println("Não pode jogar sendo que não é um jogador");
            return;
        }        
        if (cliente != ordemJogadores.get(turno)){
            System.out.println("Não pode jogar sendo que não é seu turno");
            return;
        }
        if (dentroDosLimites(x, y)) {
            if (movimentos.isEmpty()){
                movimentos.add(new int[]{x, y});
                atualizarTabuleiro(true);
                listener.onJogoAtualizado(this);
            }
            else{
                if (movimentos.get(0)[0] == x && movimentos.get(0)[1] == y){
                    System.out.println("Não pode fazer o mesmo movimento");
                }
                else{
                    movimentos.add(new int[]{x, y});
                    atualizarTabuleiro(false); // Deixar um tempo sem turno importar para os jogadores verem as cartas
                    listener.onJogoAtualizado(this);
                    if (jogo[movimentos.get(0)[0]][movimentos.get(0)[1]] == jogo[movimentos.get(1)[0]][movimentos.get(1)[1]]){
                        jogo[movimentos.get(0)[0]][movimentos.get(0)[1]] = -1;
                        jogo[movimentos.get(1)[0]][movimentos.get(1)[1]] = -1;
                        pontuacaoJogadores.put(cliente, 1 + pontuacaoJogadores.get(cliente));
                        movimentos.clear();
                        boolean acabou = checkJogoAcabou();
                        if (acabou){
                            acabarJogo();
                            return;
                        }
                    } else{
                        turno = (turno + 1) % ordemJogadores.size();
                        movimentos.clear();
                    }
                    try {
                        Thread.sleep(this.espera); 
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    atualizarTabuleiro(true);
                }
            }
        } else {
            System.out.println("Movimento inválido! Posições fora da matriz.");
        }
    }

    private void atualizarTabuleiro(boolean turnoImporta){
        String pontuacaoString = montarPontuacao();
        String matrizJogo = montarTabuleiro();
        
        for (Socket jogador : pontuacaoJogadores.keySet()) {
            try {
                String dados = "Tabuleiro^^^";
                if (turnoImporta) dados = dados + (jogador == ordemJogadores.get(turno)) + "^^^";
                else dados = dados + "false^^^";
                dados = dados + pontuacaoString + "^^^" + matrizJogo;
                
                PrintWriter saida = new PrintWriter(jogador.getOutputStream(), true);
                saida.println(dados);
            } catch (Exception e) {
                System.err.println("Erro ao enviar dados para " + jogador + ": " + e.getMessage());
            }
        }
    }
  
    public String montarPontuacao(){
        String pontuacaoString = "";
        for (Socket jogador : pontuacaoJogadores.keySet()) {
            pontuacaoString = pontuacaoString + "Jogador porta " + jogador.getPort() + ": " + pontuacaoJogadores.get(jogador) + "***";
        }
        return pontuacaoString;
    }
    
    public String montarTabuleiro() {
        StringBuilder sb = new StringBuilder(jogo.length + ";" + jogo[0].length + "++");
        for (int i = 0; i < jogo.length; i++) {
            for (int j = 0; j < jogo[i].length; j++) {
                // Verifica se a dupla (i, j) está na lista de movimentos
                if (jogo[i][j] == -1){
                    sb.append("-1 ");
                } else{
                    boolean encontrado = false;
                    for (int[] movimento : movimentos) {
                        if (movimento[0] == i && movimento[1] == j) {
                            encontrado = true;
                            break;
                        }
                    }

                    // Se a dupla (i, j) estiver na lista, adiciona o valor do jogo, senão adiciona 0
                    if (encontrado) {
                        sb.append(jogo[i][j]).append(" ");
                    } else {
                        sb.append("0 ");
                    }
                }
            }
            sb.append("++");
        }

        return sb.toString();
    }

    private boolean dentroDosLimites(int x, int y) {
        return x >= 0 && x < jogo.length && y >= 0 && y < jogo[0].length;
    }
    
}
