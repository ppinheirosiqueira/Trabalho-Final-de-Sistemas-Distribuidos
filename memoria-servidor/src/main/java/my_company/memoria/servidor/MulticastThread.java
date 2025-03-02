/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package my_company.memoria.servidor;

/**
 *
 * @author pinhe
 */
public class MulticastThread implements Runnable {
    private static final int INTERVALO_ENVIO = 5000; // Intervalo de envio em milissegundos (5 segundos)
    private Multicast multicastSender;

    public MulticastThread(Multicast multicast){
        this.multicastSender = multicast;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                // Obtém a lista de jogos ativos
                String mensagem = MemoriaServidor.getJogosAtivos();

                // Envia a mensagem via multicast
                if (multicastSender != null) {
                    multicastSender.enviarMensagem(mensagem);
                }

                // Aguarda o intervalo de envio
                Thread.sleep(INTERVALO_ENVIO);
            }
        } catch (InterruptedException e) {
            System.out.println("Thread de informações de jogos interrompida.");
        }
    }
}
