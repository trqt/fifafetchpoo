import socket
import threading
import signal
import sys
import subprocess
import random
import string

from typing import Tuple

def execute_command(input_data: str) -> Tuple[str, str]:
    process = subprocess.Popen(
        "./programaTrab",
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        shell=True,
        text=True
    )

    stdout, stderr = process.communicate(input=input_data)
    return stdout, stderr

def parser(line: str) -> str: 
    if len(line.split(' ')) < 2:
        return "ERROR Número errado de comandos\n"
    cmd = line.split(' ')[0]
    args = line.split(' ')[1:]

    print(f"Comando = {cmd}, args = {args}")

    match cmd:
        case "abrir":
            random_string = ''.join(random.choices(string.ascii_letters + string.digits, k=10))

            out, err = execute_command(f"1 {args[0]} {random_string}.bin")
            if(out.startswith("Falha")):
                return "ERROR Falha na abertura do arquivo"
            return random_string
        case "busca":
            query = ' '.join(args[1:])
            print(query)
            out, err = execute_command(f"3 {args[0]}.bin 1\n1 {query}")

            return out + "END"
            
        case "buscatodos":
            out, err = execute_command(f"2 {args[0]}.bin")
            print("Erro = " + err)
            print("Output = " + out)
            if(out.startswith("Falha")):
                return "ERROR Falha na abertura do arquivo"

            return out + "END"
        case "deleta":
            out, err = execute_command(f"4 {args[0]}.bin {args[0]}.index.bin")
            out, err = execute_command(f"5 {args[0]}.bin {args[0]}.index.bin 1\n1 id {args[1]}")

            return out + "END"
        case "atualiza":
            out, err = execute_command(f"4 {args[0]}.bin {args[0]}.index.bin")
            data = ' '.join(args[1:])
            player = data.split(';')
            print(f"Data = {player}") 
            out, err = execute_command(f"5 {args[0]}.bin {args[0]}.index.bin 1\n1 id {player[0]}")
            print(f"6 {args[0]}.bin {args[0]}.index.bin 1\n{player[0]} nulo \"{player[1]}\" \"{player[2]}\" \"{player[3]}\"")
            out, err = execute_command(f"6 {args[0]}.bin {args[0]}.index.bin 1\n{player[0]} NULO \"{player[1]}\" \"{player[2]}\" \"{player[3]}\"")
        
            return out + "END"

    print(f'ERRO {cmd}')
    return f"ERRO '{cmd}' não existe"

def handle_client(conn: socket.socket, addr: Tuple[str, int]):
    print('Connected by', addr)
    with conn:
        buffer = ""
        while True:
            data = conn.recv(1024).decode()
            if not data:
                break
            buffer += data
            while '\n' in buffer:
                line, buffer = buffer.split('\n', 1)
                conn.sendall((parser(line) + '\n').encode())
                

def main():
    port = 1337
    # Step 1: Create a socket object
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    
    # Step 2: Set socket options to allow reuse of the address
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    
    # Step 3: Bind the socket to an address and port
    s.bind(('localhost', port))
    
    # Step 4: Set the socket to listen for incoming connections
    s.listen(5)
    print(f'Server is listening on port {port}...')
    
    # Register a signal handler for graceful shutdown
    def signal_handler(sig, frame):
        print('Shutting down server...')
        s.close()
        sys.exit(0)
    
    signal.signal(signal.SIGINT, signal_handler)
    
    # Step 5: Accept connections in a loop
    while True:
        conn, addr = s.accept()
        threading.Thread(target=handle_client, args=(conn, addr)).start()

if __name__ == "__main__":
    main()
