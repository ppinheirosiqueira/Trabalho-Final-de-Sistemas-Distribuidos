import socket
import struct

'''
    UDP QUE RECEBE INFINITAMENTE

    multicast_group = '230.0.0.0'
    server_port = 7777

    # Criação do socket UDP
    client_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
    client_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

    # Vincula o socket à porta (0.0.0.0 para aceitar de qualquer interface)
    client_socket.bind(('0.0.0.0', server_port))

    # Junta-se ao grupo multicast
    mreq = struct.pack('4s4s', socket.inet_aton(multicast_group), socket.inet_aton('0.0.0.0'))
    client_socket.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP, mreq)

    print(f"Esperando mensagens do grupo multicast {multicast_group}:{server_port}...")

    try:
        while True:
            data, addr = client_socket.recvfrom(4096)
            print(f"Recebido de {addr}: {data.decode()}")
    finally:
        client_socket.close()
'''

class ClienteUDP:
    def __init__(self, multicast_group, multicast_port, root, interface):
        self.multicast_group = multicast_group
        self.multicast_port = multicast_port
        self.socket = None
        self.conectado = False
        self.root = root
        self.interface = interface

    def conectar(self):
        try:
            self.socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
            self.socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            self.socket.bind(('0.0.0.0', self.multicast_port))
            mreq = struct.pack('4s4s', socket.inet_aton(self.multicast_group), socket.inet_aton('0.0.0.0'))
            self.socket.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP, mreq)
            self.conectado = True
            print("Conectado ao grupo multicast.")
        except Exception as e:
            print(f"Erro ao conectar via UDP: {e}")

    def receber_mensagem(self):
        while True:
            try:
                data, addr = self.socket.recvfrom(4096)
                mensagem = data.decode()
                print(f"Recebido de {addr}: {mensagem}")

                # Atualiza a interface (chama no contexto principal do Tkinter)
                self.root.after(0, self.interface.receber_mensagens_udp, mensagem)
            except Exception as e:
                print(f"Erro na recepção: {e}")
                break

    def desconectar(self):
        """Sai do grupo multicast."""
        if self.conectado:
            try:
                self.socket.close()
                self.conectado = False
                print("Desconectado do grupo multicast.")
            except Exception as e:
                print(f"Erro ao desconectar: {e}")