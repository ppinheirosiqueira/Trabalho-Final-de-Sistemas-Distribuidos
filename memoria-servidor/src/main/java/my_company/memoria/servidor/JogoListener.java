/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package my_company.memoria.servidor;

/**
 *
 * @author pinhe
 */
public interface JogoListener {
    void onJogoAtualizado(Jogo jogo); // Notifica atualizações no jogo
    void onJogoTerminado(Jogo jogo);  // Notifica que o jogo terminou e deve ser removido
}