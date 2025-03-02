from tkinter import Tk, Label, Button, Frame, PhotoImage, messagebox, Scale, IntVar, LEFT, Spinbox
from tcp import ClienteTCP
from udp import ClienteUDP
import threading

class InterfaceCliente:
    def __init__(self):
        self.cliente_tcp = None
        self.cliente_udp = None
        self.thread_receber = None
        self.tela_atual = None
        self.telas = {}
        self.porta_assistir = -1
        self.imagens = []
        
        # Interface gráfica
        self.root = Tk()

        self.valor_pares = IntVar(value=20)

        self.root.title("Cliente de Jogo da Memória")
        self.root.geometry("600x500")

        # Cria a tela inicial
        self.criar_tela_inicial()

        # Mostra a tela inicial
        self.trocar_tela("inicial")
    
    def criar_tela_inicial(self):
        """Cria a tela inicial com os botões de conexão."""
        tela = Frame(self.root)
        self.telas["inicial"] = tela

        Label(tela, text="Tela Inicial", font=("Arial", 16)).pack(pady=10)

        btn_tcp = Button(tela, text="Conectar via TCP", command=self.conectar_tcp)
        btn_tcp.pack(pady=5)

        btn_udp = Button(tela, text="Conectar via UDP", command=self.conectar_udp)
        btn_udp.pack(pady=5)

    def criar_tela_tcp(self, mensagem):
        if "tcp" in self.telas:
            self.telas["tcp"].destroy()  # Destroi a tela antiga, se existir

        tela = Frame(self.root)
        self.telas["tcp"] = tela

        Label(tela, text="Criar jogo com X pares:", font=("Arial", 16)).pack(pady=10)
        slider = Scale(tela, from_=1, to=30, orient="horizontal", variable=self.valor_pares)
        slider.pack(pady=5)
        Button(tela, text="Criar", command=self.executar_criar_jogo).pack(pady=5)

        # Verifica se a mensagem está vazia ou se é uma mensagem inválida
        if not mensagem or mensagem == "Não há jogos":
            # Exibe a tela de "sem jogos" ou algo do tipo
            Label(tela, text="Não há jogos disponíveis no momento para entrar.", font=("Arial", 16)).pack(pady=10)
        else:
            
            linhas = mensagem.split("^^^")
            Label(tela, text=f"Número de Jogos Disponíveis para Entrar: {len(linhas)}", font=("Arial", 16)).pack(pady=10)
            for linha in linhas:
                if linha is None: continue

                partes = linha.split(": ")
                if len(partes) < 2: continue

                jogoInfo = partes[0].split(" ")
                jogoIndex = int(jogoInfo[1]) - 1

                textoBotao = "Entre " + partes[0] + " - " + partes[1]
                Button(tela, text=textoBotao, command=lambda jogoIndex=jogoIndex: self.enviar_mensagem(f"Entrando no jogo {jogoIndex}")).pack(pady=5)
            
        # Exibe a tela
        self.trocar_tela("tcp")
    
    def executar_criar_jogo(self):
        valor = self.valor_pares.get()
        self.criar_jogo(valor)

    def criar_jogo(self, pares):
        self.enviar_mensagem(f"Criar Jogo - {pares}")

    def criar_tela_espera(self, mensagem):
        if "espera_tcp" in self.telas:
            self.telas["espera_tcp"].destroy()  # Destroi a tela antiga, se existir

        tela = Frame(self.root)
        self.telas["espera_tcp"] = tela

        self.porta_socket = self.cliente_tcp.get_socket_port()
        self.tempo_espera = IntVar(value=3)
        jogador_1_porta = self.extrair_portas(mensagem)

        Label(tela, text="Setar Tempo de Espera (segundos):").pack(pady=5)
        self.entrada_tempo = Spinbox(tela, from_=0, to=60, textvariable=self.tempo_espera)
        self.entrada_tempo.pack(pady=5)

        # Desativa input se a porta não for do jogador 1
        if self.porta_socket == jogador_1_porta:
            self.entrada_tempo.config(state="normal")
        else:
            self.entrada_tempo.config(state="disabled")

        jogadores = mensagem.split(" - ")[1]
        for linha in jogadores.split("^^^"):
            Label(tela, text=linha).pack(pady=5)

        # Botão "Começar Jogo" (só para jogador 1)
        if self.porta_socket == jogador_1_porta:
            Button(tela, text="Começar Jogo", command=self.comecar_jogo).pack(pady=10)
        
        # Exibe a tela
        self.trocar_tela("espera_tcp")

    def extrair_portas(self, mensagem):
        try:
            partes = mensagem.split("^^^")
            porta_j1 = int(partes[0].split("porta: ")[1])
            return porta_j1
        except (IndexError, ValueError):
            return None

    def comecar_jogo(self):
        self.enviar_mensagem(f"Começar Jogo - {self.tempo_espera.get()}")

    def criar_tela_jogar(self, mensagem):
        """Cria a tela para assistir o jogo com base na mensagem recebida."""
        if "jogo" in self.telas:
            self.telas["jogo"].destroy()  # Destroi a tela antiga, se existir
        
        tela = Frame(self.root)
        self.telas["jogo"] = tela
        tela.pack(padx=20, pady=20, fill="both", expand=True)  # Usando pack para o Frame

        if mensagem:
            Label(tela, text=f"Você é o jogador da porta {self.porta_socket}", font=("Arial", 16)).pack(pady=10)

            # Estrutura de Labels à esquerda
            partes = mensagem.split("^^^")
            if len(partes) >= 2:
                if partes[0].startswith("Acabou"):
                    messagebox.showwarning(title="Acabou", message=f"{partes[1]}!")
                    self.criar_tela_tcp("")
                    return

                Label(tela, text="Pontuações:", font=("Arial", 16), anchor="w").pack(anchor="w", pady=5)
                for label_text in partes[2].split("***"):
                    Label(tela, text=label_text, font=("Arial", 12), anchor="w").pack(anchor="w", pady=5)

                partesTabuleiro = partes[3].split("++")
                tamanho = partesTabuleiro[0].split(";")
                linhas = int(tamanho[0])
                colunas = int(tamanho[1])

                # Exibindo a matriz de imagens
                for i in range(linhas):
                    linha_frame = Frame(tela)  # Criamos um frame para cada linha da matriz
                    valores = partesTabuleiro[i + 1].strip().split(" ")
                    
                    for j in range(colunas):
                        valor = int(valores[j])
                        if valor == -1:
                            img = PhotoImage(width=32, height=32)  # Imagem vazia
                        else:
                            try:
                                img = PhotoImage(file=f"images/{valor}.png")
                            except Exception as e:
                                print(f"Erro ao carregar a imagem images/{valor}.png: {e}")
                                img = PhotoImage(width=32, height=32)  # Imagem padrão em caso de erro

                        self.imagens.append(img)  # Armazena a imagem para evitar descarte
                        img_button = Button(linha_frame, image=img, command=lambda i=i, j=j: self.enviar_mensagem(f"Movimento - {i} - {j}"))
                        img_button.config(state="normal") if partes[1] == "true" else img_button.config(state="disabled")
                        img_button.pack(side="left", padx=5)

                    linha_frame.pack(anchor="c", pady=5)

        self.trocar_tela("jogo")

    def criar_tela_udp(self, mensagem):
        """Cria a tela UDP com base na mensagem passada."""
        if "udp" in self.telas:
            self.telas["udp"].destroy()  # Destroi a tela antiga, se existir

        # Verifica se a mensagem está vazia ou se é uma mensagem inválida
        if not mensagem or mensagem == "Não há jogos começados para ver":
            # Exibe a tela de "sem jogos" ou algo do tipo
            tela = Frame(self.root)
            self.telas["udp"] = tela
            Label(tela, text="Não há jogos disponíveis no momento.", font=("Arial", 16)).pack(pady=10)
        else:
            # Se a mensagem tiver jogos, processa normalmente
            tela = Frame(self.root)
            self.telas["udp"] = tela
            auxiliar = mensagem.split("***")
            linhas = auxiliar[1].split("^^^")
            Label(tela, text=f"Número de Jogos Disponíveis para Ver: {len(linhas) - 1}", font=("Arial", 16)).pack(pady=10)
            for linha in linhas:
                partes = linha.split(": ")
                if len(partes) > 1:  # Verifica se há pelo menos 2 partes
                    porta = int((partes[1].split(" ->"))[0])
                    Button(tela, text=f"Veja {linha}", command=lambda porta=porta: self.ver_jogo(porta, "n/a")).pack(pady=5)

        # Exibe a tela
        self.trocar_tela("udp")

    def ver_jogo(self, porta, mensagem):
        self.porta_assistir = porta
        self.criar_tela_assistir(mensagem)

    def criar_tela_assistir(self, mensagem):
        """Cria a tela para assistir o jogo com base na mensagem recebida."""
        if "assistir_jogo" in self.telas:
            self.telas["assistir_jogo"].destroy()  # Destroi a tela antiga, se existir
        
        tela = Frame(self.root)
        self.telas["assistir_jogo"] = tela
        tela.pack(padx=20, pady=20, fill="both", expand=True)  # Usando pack para o Frame

        if not mensagem:
            # Se a mensagem estiver vazia, mostramos a mensagem de espera
            Label(tela, text="Esperando movimento do jogo...", font=("Arial", 16)).pack(pady=10)
        else:
            Label(tela, text="Jogo em andamento...", font=("Arial", 16)).pack(pady=10)

            # Estrutura de Labels à esquerda
            partes = mensagem.split("^^^")
            if len(partes) >= 2:
                if partes[1].startswith("Jogo terminado"):
                    messagebox.showwarning(title="Acabou", message=f"{partes[1]}!")
                    self.criar_tela_udp("")
                    return

                Label(tela, text="Pontuações:", font=("Arial", 16), anchor="w").pack(anchor="w", pady=5)
                for label_text in partes[1].split("***"):
                    Label(tela, text=label_text, font=("Arial", 12), anchor="w").pack(anchor="w", pady=5)

                partesTabuleiro = partes[2].split("++")
                tamanho = partesTabuleiro[0].split(";")
                linhas = int(tamanho[0])
                colunas = int(tamanho[1])

                # Exibindo a matriz de imagens
                for i in range(linhas):
                    linha_frame = Frame(tela)  # Criamos um frame para cada linha da matriz
                    valores = partesTabuleiro[i + 1].strip().split(" ")
                    
                    for j in range(colunas):
                        valor = int(valores[j])
                        if valor == -1:
                            img = PhotoImage(width=32, height=32)  # Imagem vazia
                        else:
                            try:
                                img = PhotoImage(file=f"images/{valor}.png")
                            except Exception as e:
                                print(f"Erro ao carregar a imagem images/{valor}.png: {e}")
                                img = PhotoImage(width=32, height=32)  # Imagem padrão em caso de erro

                        self.imagens.append(img)  # Armazena a imagem para evitar descarte
                        img_label = Label(linha_frame, image=img)
                        img_label.pack(side="left", padx=5)

                    linha_frame.pack(anchor="c", pady=5)

        self.trocar_tela("assistir_jogo")

    def trocar_tela(self, nome_tela):
        """Troca a tela visível."""
        for tela in self.telas.values():
            tela.pack_forget()

        if nome_tela in self.telas:
            self.telas[nome_tela].pack(fill="both", expand=True)
            self.tela_atual = nome_tela

    def conectar_tcp(self):
        """Conecta ao servidor TCP."""
        ip_tcp = "127.0.0.1"  # Substitua pelo IP do servidor TCP
        porta_tcp = 6666      # Substitua pela porta do servidor TCP
        self.cliente_tcp = ClienteTCP(ip_tcp, porta_tcp, self.root, self)
        self.cliente_tcp.conectar()
        self.thread_receber = threading.Thread(target=self.cliente_tcp.receber_mensagem, daemon=True)
        self.thread_receber.start()
        self.criar_tela_tcp("")

    def conectar_udp(self):
        multicast_group = "230.0.0.0"
        multicast_port = 7777
        self.cliente_udp = ClienteUDP(multicast_group, multicast_port, self.root, self)
        self.cliente_udp.conectar()
        # Inicia a thread para receber mensagens UDP
        self.thread_receber = threading.Thread(target=self.cliente_udp.receber_mensagem, daemon=True)
        self.thread_receber.start()
        self.criar_tela_udp("")

    def receber_mensagens_udp(self, mensagem):
        if self.tela_atual == "udp" and mensagem.startswith("Jogos Disponiveis"):
            self.criar_tela_udp(mensagem)
        elif self.tela_atual == "assistir_jogo" and mensagem.startswith(str(self.porta_assistir)):
            self.criar_tela_assistir(mensagem)

    def receber_mensagens_tcp(self, mensagem):
        if self.tela_atual == "tcp" and mensagem.startswith("Jogo Criado com Sucesso"):
            self.criar_tela_espera(mensagem)
        elif self.tela_atual == "tcp" and mensagem.startswith("Jogo"):
            self.criar_tela_tcp(mensagem)
        elif self.tela_atual == "tcp" and mensagem.startswith("Entrando"):
            self.criar_tela_espera(mensagem)
        elif self.tela_atual == "espera_tcp" and mensagem.startswith("Tabuleiro"):
            self.criar_tela_jogar(mensagem)
        elif self.tela_atual == "espera_tcp":
            self.criar_tela_espera(mensagem)
        elif self.tela_atual == "jogo":
            self.criar_tela_jogar(mensagem)

    def enviar_mensagem(self, mensagem):
        if self.cliente_tcp and self.cliente_tcp.conectado:
            self.cliente_tcp.enviar_mensagem(mensagem)

    def iniciar(self):
        """Inicia a interface gráfica."""
        self.root.mainloop()

if __name__ == "__main__":
    interface = InterfaceCliente()
    interface.iniciar()
