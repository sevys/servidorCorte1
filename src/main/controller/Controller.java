package main.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import main.model.ClientSocket;
import main.model.Nodo;
import main.model.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class Controller implements Observer {
    ServerSocket serverSocket = null;
    private final int PORT = 3001;
    private ArrayList<Nodo> poolSocket = new ArrayList<>();

    @FXML
    private Button btnOpenServer;

    @FXML
    private Button btnSalir;

    @FXML
    private ListView<String> listClient;

    @FXML
    private Circle circleLed;
    private DataInputStream bufferEntrada = null;

    @FXML
    void OpenServerOnMouseClicked(MouseEvent event) {
        byte[] ipBytes = {(byte)127,(byte)0,(byte)0, (byte)1 };
        InetAddress ip = null;

        try {
            ip = InetAddress.getByAddress(ipBytes);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        try {
            serverSocket = new ServerSocket(PORT,100,ip);
            listClient.getItems().add("Server abierto: " + serverSocket.getInetAddress().getHostName());
            circleLed.setFill(Color.GREEN);

           Server server = new Server(serverSocket, listClient);
           server.addObserver(this);
           new Thread(server).start();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @FXML
    void SalirOnMouseClicked(MouseEvent event) {
        System.exit(1);
    }

    @Override
    public void update(Observable o, Object arg) {

        if (o instanceof Server) {
            Socket socket = (Socket)arg;
            try {
                bufferEntrada = new DataInputStream(socket.getInputStream());
                String mensaje = "";
                mensaje = bufferEntrada.readUTF();
                poolSocket.add(new Nodo(socket.hashCode(),mensaje,socket));
                System.out.println(mensaje);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Broadcast a todos los sockets conectados para actualizar la lista de conexiones
            broadCast();
            // Crear un hilo que reciba mensajes entrantes de ese nuevo socket creado
            ClientSocket clientSocket = new ClientSocket(socket);
            clientSocket.addObserver(this);
            new Thread(clientSocket).start();

        }
        if (o instanceof ClientSocket){
            String mensaje = (String)arg;
            //se crea un array para guardar los datos recibidos desde "arg"
            String[] datagrama;
            datagrama = mensaje.split(":");
            if (datagrama[0] != null) {
              //  bufferDeSalida.writeUTF(datos.getNombre()+":"+usuarios.getValue()+":"+txtEnviar.getText());
                System.out.println("Recibiendo para enviar --> "+datagrama[0]+" : "+datagrama[1]+" : "+datagrama[2]);
                //se le envia los datos por cada posicion donde se guardaron los datos recibidos
                sendMessage(datagrama[0],datagrama[1],datagrama[2]);
                //0:enviador, 1:receptor:2Mensaje
            }
        }



    }

    private void broadCast(){
        DataOutputStream bufferDeSalida = null;
        String conectados = poolSocket.get(0).getName()+":";

        for (int i = 0; i< poolSocket.size(); i++){

            String aux = conectados;
            conectados = null;
            conectados = aux+poolSocket.get(i).getName()+":";
        }
        Nodo ultimaConexion = poolSocket.get(poolSocket.size()-1);
        for (Nodo nodo: poolSocket) {
            try {
                bufferDeSalida = new DataOutputStream(nodo.getSocket().getOutputStream());
                bufferDeSalida.flush();
                bufferDeSalida.writeUTF("Conectado:"+"Te has Conectado:"+ultimaConexion.getName()+":"+conectados);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void sendMessage(String source, String destino, String mensaje){
        DataOutputStream bufferDeSalida = null;
        for (Nodo nodo : poolSocket) {
            //se compara el destino recibido con los destinos guardados en el Array
            if (destino.equals(nodo.getName())) {
                try {
                    bufferDeSalida = new DataOutputStream(nodo.getSocket().getOutputStream());
                    bufferDeSalida.flush();
                    //se esta enviando el mensaje
                    bufferDeSalida.writeUTF(source + ":" + destino+ ":" + mensaje);
                    System.out.println("llego a la parte de Enviado --> "+source+destino+mensaje);
                } catch (IOException e) {

                    e.printStackTrace();

                }
            }


        }

    }


}



