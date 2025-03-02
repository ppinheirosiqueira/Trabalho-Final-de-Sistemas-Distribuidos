import socket
import threading

class ClienteTCP:
    def __init__(self, servidor_ip, servidor_porta, root, interface):
        self.servidor_ip = servidor_ip
        self.servidor_porta = servidor_porta
        self.socket = None
        self.conectado = False
        self.root = root
        self.interface = interface

    def conectar(self):
        """Conecta ao servidor TCP."""
        try:
            self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.socket.connect((self.servidor_ip, self.servidor_porta))
            self.conectado = True
            print("Conectado ao servidor TCP.")

            # Inicia a thread para receber mensagens
            thread = threading.Thread(target=self.receber_mensagem, daemon=True)
            thread.start()

        except Exception as e:
            print(f"Erro ao conectar via TCP: {e}")

    def receber_mensagem(self):
        """Recebe mensagens continuamente do servidor TCP."""
        while self.conectado:
            try:
                data = self.socket.recv(4096)
                if not data:
                    print("Conexão encerrada pelo servidor.")
                    break

                mensagem = data.decode()
                print(f"Recebido: {mensagem}")

                # Atualiza a interface (chama no contexto principal do Tkinter)
                self.root.after(0, self.interface.receber_mensagens_tcp, mensagem)
            except Exception as e:
                print(f"Erro na recepção: {e}")
                break

        self.desconectar()

    def enviar_mensagem(self, mensagem):
        """Envia uma mensagem ao servidor TCP."""
        try:
            if self.conectado:
                self.socket.sendall((mensagem + "\n").encode())
                print(f"Enviado: {mensagem}")
        except Exception as e:
            print(f"Erro ao enviar mensagem: {e}")

    def desconectar(self):
        """Encerra a conexão com o servidor."""
        if self.conectado:
            try:
                self.socket.close()
                self.conectado = False
                print("Desconectado do servidor TCP.")
            except Exception as e:
                print(f"Erro ao desconectar: {e}")

    def get_socket_port(self):
        """Retorna a porta local do socket TCP, se estiver conectado."""
        if self.conectado and self.socket:
            try:
                return self.socket.getsockname()[1]  # Pega a porta local do socket
            except Exception as e:
                print(f"Erro ao obter a porta do socket: {e}")
                return None
        return None
