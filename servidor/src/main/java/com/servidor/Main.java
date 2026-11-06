package com.servidor;



import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;



public class Main {

    // Atributs
    private static final Map<WebSocket, String> clients = new HashMap<>();



    // Metode main per iniciar el servidor 
    public static void main(String[] args) {

        // Crear el servidor WebSocket en el port 8080
        WebSocketServer servidor = new WebSocketServer(
            new InetSocketAddress("0.0.0.0", 8080)
        ) {

            // Metode que se executa cuan un client es connecta
            @Override
            public void onOpen(WebSocket connexio, ClientHandshake handshake) {

                // Missatges de debug per consola
                System.out.println("Nou client connectat");
                System.out.println("Direccio: " + connexio.getRemoteSocketAddress());

                // Generar codi d'usuari amb el metode declarat abaix 
                // i posarlo al hasmap per asociarlo amb la conexio del client
                String nom = "User-" + generarCodiAleatori();
                clients.put(connexio, nom);

                // Missatges de debug per consola
                System.out.println("Nom del client connectat: " + nom);

                // Enviarli un missatge al client dientli el seu 
                // nom per que el mostri en pantalla al client
                connexio.send("El teu nom es: " + nom);
            }   

            // Metode que se executa cuan rep un missatge d'un client
            @Override
            public void onMessage(WebSocket connexio, String missatge) {

                // Agafar el nom del client utlilitzant la conexio 
                // asosciada amb el nom en el hasmap de clients
                String nom = clients.get(connexio);

                // Afegirli el nom al missatge rebut per poder 
                // enviarlo amb el nom als clients
                String missatgeComplet = nom + ": " + missatge;

                // Missatges de debug per consola
                System.out.println("Missatge rebut: " + missatgeComplet);

                // Recorrer la el hasmap de clients i agafar el client de 
                // l'iteracio per enviar el missatge rebut a tots els clients
                for (WebSocket client : clients.keySet()) {

                    // Comprobar si el client iterat esta obert
                    if (client.isOpen()) {

                        // Enviar el misatge complet al client
                        client.send(missatgeComplet);
                    }
                }
            }

            // Metode que se executa cuan un client es desconnecta
            @Override
            public void onClose(WebSocket connexio, int codi, String motiu, boolean remot) {

                // Agafar el nom per posarlo per pantalla als missatges de debug, 
                // i borrar la conexio i el nom del client que s'ha desconectat
                String nom = clients.remove(connexio);

                // Missatges de debug per consola
                System.out.println("Client desconectat:" + nom);
                System.out.println("Motivo: " + motiu);
            }

            // Metode que se executa cuan ocurreix un error
            @Override
            public void onError(WebSocket connexio, Exception error) {

                // Missatges de debug per consola
                System.out.println("Error en el servidor: " + error.getMessage());
                error.printStackTrace();
            }

            // Metode que se executa cuan el servidor s'inicia
            @Override
            public void onStart() {

                // Missatges de debug per consola
                System.out.println("Servidor WebSocket iniciat");
                System.out.println("Escoltant en el port 8080...");
            }
        };



        // Iniciar el servidor
        servidor.start();
    }

    // Metode per donarli un codi aleatori de 9 caracters al usuari que mandi un missatge
    private static String generarCodiAleatori() {

        // Definr els caracters que s'utilitzaran per generar el codi
        String caracters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";

        // Crear un StringBuilder per poder crear el codi facilment
        StringBuilder codi = new StringBuilder();

        // Fer el bucle que afegeix un carater 9 vegades
        for (int i = 0; i < 9; i++) {

            // Elegeir la posicio d'un dels caractes al atzar i guardarlo en una variable
            int posicio = (int) (Math.random() * caracters.length());

            // Afegir el caracter al StringBuilder utilitzant la posicio que hem aconseguit abants
            codi.append(caracters.charAt(posicio));
        }

        // Retornar el codi complet pasat a String
        return codi.toString();
    }
}
