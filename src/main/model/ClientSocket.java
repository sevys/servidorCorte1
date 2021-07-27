package main.model;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Observable;
// es un hilo que espera los mensajes que vienen de un cliente
public class ClientSocket extends Observable implements Runnable {
    private Socket socket;
    private DataInputStream bufferEntrada = null;

    public ClientSocket(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            bufferEntrada = new DataInputStream(socket.getInputStream());
            String mensaje = "";
            do {
                //esperando mensaje
                mensaje = bufferEntrada.readUTF();
                System.out.println(mensaje);
                this.setChanged();
                //se envia los datos al observador(Clase controller) recividos del cliente a Update
                this.notifyObservers(mensaje);
            } while (mensaje != "exit");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
