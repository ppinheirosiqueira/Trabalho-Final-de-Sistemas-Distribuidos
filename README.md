# Trabalho Final de Sistemas Distribuídos  

Este projeto foi desenvolvido como trabalho final da disciplina de Sistemas Distribuídos. O objetivo era criar um **servidor Java** para um jogo da memória, um **cliente Java** e um **cliente em outra linguagem** à escolha do aluno, o escolhido foi **Python**.  

O servidor deveria oferecer suporte a **jogadores** e **telespectadores**, permitindo ativar e desativar esses módulos conforme necessário.  

## 🛠️ Metodologia  

O projeto foi estruturado da seguinte forma:  
- **Jogadores** se conectam ao servidor via **TCP**.  
- **Telespectadores** se conectam ao servidor via **UDP**.  

### 📩 Comunicação entre Cliente e Servidor  

Embora existam formas mais eficientes de comunicação, todas as mensagens trocadas entre clientes e servidor foram enviadas como **Strings**, utilizando caracteres separadores para dividir suas partes e facilitar o processamento.  

### 🔗 Conexão TCP (Jogadores)  

- Cada novo jogador que se conecta ao servidor inicia uma **nova thread** dedicada a ele.  
- O jogador pode criar uma nova partida ou ingressar em uma já existente.  

### 📡 Conexão UDP (Telespectadores)  

- O servidor envia uma mensagem **a cada 5 segundos** informando quais jogos estão ativos.  
- Cada jogo também envia uma mensagem **sempre que um jogador realiza um movimento**.
  - Dessa forma, um telespectador pode se conectar a uma partida, mas só verá o estado atualizado quando um jogador fizer um movimento.  
  - Isso foi feito para reduzir o número de **threads simultâneas**, já que todos os processos estavam sendo executados na mesma máquina.  

### 🖼️ Imagens  

Cada cliente possui um **banco próprio de imagens**, numeradas de **1 a 30**, permitindo que cada usuário tenha suas próprias ilustrações do jogo. As imagens utilizadas fora pegas do [Pixel Art Icon Pack - RPG](https://cainos.itch.io/pixel-art-icon-pack-rpg), são imagens 32x32.

## 🎮 Fluxo de um Jogador  

1. O jogador se conecta ao servidor via TCP e é movido para outra tela.  
2. Ele pode **criar um novo jogo** (com 1 a 30 pares) ou **entrar em uma partida existente** que ainda não foi iniciada.  
  - As partidas suportam **1 a 4 jogadores**, conforme exigido pelo professor.  
3. Ao criar ou ingressar em um jogo, o jogador vai para uma **tela de espera**, onde pode ver os demais participantes.  
4. Apenas o **criador** da partida pode iniciar o jogo e definir o **tempo de espera entre jogadas**.  
5. Durante o jogo:  
   - Cada jogador é identificado por sua porta.  
   - Apenas um jogador pode jogar por vez.  
   - Quando um jogador erra uma jogada, há um tempo de espera antes de liberar o tabuleiro para que todos vejam as cartas reveladas.  
6. Quando o jogo termina, todos os jogadores retornam à tela de seleção de partida.  

## 👀 Fluxo de um Telespectador  

1. O telespectador recebe periodicamente uma lista de partidas ativas.  
2. Ele pode escolher qualquer partida iniciada para assistir.  
3. O estado do tabuleiro só é atualizado quando um jogador faz um movimento.  
4. Ao término da partida, o telespectador retorna à tela de seleção de jogos ativos.  

## 📌 Melhorias Futuras  

- Melhorar a **interface gráfica** dos clientes Java, ajustando automaticamente o tamanho da tela conforme o número de pares do jogo.  
- Melhorar a **gestão de threads**, permitindo que jogadores recebam atualizações periódicas sobre partidas disponíveis sem precisar reconectar.  
