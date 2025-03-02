/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package my_company.memoria.jogador;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 *
 * @author pinhe
 */
public class Jogo extends javax.swing.JPanel {
    private final MenuCliente menu;
    private boolean pegouPorta = false;
    private boolean setouTamanho = false;
    private boolean turno = false;

    /**
     * Creates new form Jogo
     * @param menu
     */
    public Jogo(MenuCliente menu) {
        this.menu = menu;
        initComponents();
        
        pontuacoes.setLayout(new BoxLayout(pontuacoes, BoxLayout.Y_AXIS));
    }

    private void setarTabuleiro(int colunas, int linhas){
        tabuleiro.setSize(colunas * 32, linhas * 32);
        tabuleiro.setLayout(new GridLayout(colunas, linhas));
        this.revalidate();
        this.repaint();
        menu.pack();
    }
    
    private ImageIcon carregarImagem(int valor) {
        String caminho = "/" + valor + ".png"; // Caminho dentro do resources
        return new ImageIcon(Objects.requireNonNull(getClass().getResource(caminho)));
    }
    
    public void entrandoMenu(){
        if(!menu.getModo()) {
            identificador.setText("Esperando alguma jogada da partida para atualizar sua visualização");
            tabuleiro.removeAll();
            pontuacoes.removeAll();
        }
    }
    
    public void processarString(String texto){
        if(menu.getModo()){
            if (!this.pegouPorta){
                MemoriaJogador memoriaJogador = menu.getMemoriaJogador();
                identificador.setText("Você é o jogador da porta " + memoriaJogador.getPorta());
                this.pegouPorta = true;
            }
            if (texto.startsWith("Tabuleiro")){
                String[] partes = texto.split("\\^\\^\\^");
                turno = Boolean.parseBoolean(partes[1]); // Vendo se é o turno do jogador

                attPontuacoes(partes[2]);

                // Atualizando o Tabuleiro
                String[] partesTabuleiro = partes[3].split("\\+\\+");
                String[] tamanho = partesTabuleiro[0].split(";");
                int linhas = Integer.parseInt(tamanho[0]);
                int colunas = Integer.parseInt(tamanho[1]);
                    setarTabuleiro(linhas, colunas);
                if (!this.setouTamanho){
                    this.setouTamanho = true;
                }

                tabuleiro.removeAll();
                for (int i = 0; i < linhas; i++) {
                    String[] valores = partesTabuleiro[i + 1].trim().split(" ");
                    for (int j = 0; j < colunas; j++) {
                        int valor = Integer.parseInt(valores[j]);

                        // Se o valor for -1, não cria botão
                        if (valor == -1) {
                            tabuleiro.add(new JLabel()); // Adiciona um espaço vazio
                            continue;
                        }

                        // Criar botão com a imagem correspondente
                        JButton botao = new JButton();
                        botao.setIcon(carregarImagem(valor));
                        botao.setEnabled(turno); // Define se pode ser clicado

                        // Guarda a posição do botão ao clicar
                        final int linhaAtual = i;
                        final int colunaAtual = j;
                        botao.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                MemoriaJogador memoriaJogador = menu.getMemoriaJogador();
                                memoriaJogador.enviarMensagem("Movimento - " + linhaAtual + " - " + colunaAtual);
                            }
                        });

                        tabuleiro.add(botao);
                    }
                tabuleiro.revalidate();
                tabuleiro.repaint();
                }
            }else{
                String[] partes = texto.split("\\^\\^\\^");
                JOptionPane.showMessageDialog(null, partes[1] + "!");
            }   
        }else{
            if(texto.startsWith(menu.getPortaTelespectador())){
                String[] partes = texto.split("\\^\\^\\^");
                if(partes[1].startsWith("Jogo terminado")){
                    JOptionPane.showMessageDialog(null, partes[1] + "!");
                    menu.setPortaTelespectador(-1);
                    menu.trocarPainel("EntrarJogo");
                    return;
                }
                
                attPontuacoes(partes[1]);
                
                // Atualizando o Tabuleiro
                String[] partesTabuleiro = partes[2].split("\\+\\+");
                String[] tamanho = partesTabuleiro[0].split(";");
                int linhas = Integer.parseInt(tamanho[0]);
                int colunas = Integer.parseInt(tamanho[1]);
                    setarTabuleiro(linhas, colunas);
                if (!this.setouTamanho){
                    this.setouTamanho = true;
                }

                tabuleiro.removeAll();
                for (int i = 0; i < linhas; i++) {
                    String[] valores = partesTabuleiro[i + 1].trim().split(" ");
                    for (int j = 0; j < colunas; j++) {
                        int valor = Integer.parseInt(valores[j]);
                        if (valor == -1) {
                            tabuleiro.add(new JLabel()); // Adiciona um espaço vazio
                            continue;
                        }
                        JLabel label = new JLabel();
                        label.setIcon(carregarImagem(valor));
                        tabuleiro.add(label);
                    }
                }
                tabuleiro.revalidate();
                tabuleiro.repaint();
            }
        }
    }
    
    private void attPontuacoes(String texto){
        // Atualizando as pontuações
        pontuacoes.removeAll(); 
        JLabel labelP = new JLabel("Pontuações:");
        pontuacoes.add(labelP);
        for (String linha : texto.split("\\*\\*\\*")) {
            JLabel label = new JLabel(linha);
            pontuacoes.add(label);
        }
        pontuacoes.revalidate();
        pontuacoes.repaint();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        identificador = new javax.swing.JLabel();
        pontuacoes = new javax.swing.JPanel();
        tabuleiro = new javax.swing.JPanel();

        javax.swing.GroupLayout pontuacoesLayout = new javax.swing.GroupLayout(pontuacoes);
        pontuacoes.setLayout(pontuacoesLayout);
        pontuacoesLayout.setHorizontalGroup(
            pontuacoesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 112, Short.MAX_VALUE)
        );
        pontuacoesLayout.setVerticalGroup(
            pontuacoesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout tabuleiroLayout = new javax.swing.GroupLayout(tabuleiro);
        tabuleiro.setLayout(tabuleiroLayout);
        tabuleiroLayout.setHorizontalGroup(
            tabuleiroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 487, Short.MAX_VALUE)
        );
        tabuleiroLayout.setVerticalGroup(
            tabuleiroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 416, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pontuacoes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tabuleiro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(identificador, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(identificador, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tabuleiro, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pontuacoes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel identificador;
    private javax.swing.JPanel pontuacoes;
    private javax.swing.JPanel tabuleiro;
    // End of variables declaration//GEN-END:variables
}
