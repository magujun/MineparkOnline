package server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

/**
 * CLIENT <string> write -> read SERVER write -> read <string> CLIENT The
 * messages are sent in MpGP (Minepark Game Protocol) and contain an optional
 * header and the whole game grid/tileSet state in one long string. It allows
 * clients to make requests to the server and also share its current state and
 * tileSet. Headers: - LIST| - CONNECT|<opponent_nickname>| - WON| - LOST| The
 * server keeps a list of connected players and allows for an unlimited number
 * of pairs of players to play matches (each player is a thread). Players are
 * required to send their nicknames to be added to the list. A player that has a
 * closed socket or is unresponsive after (tileSet length)/5 rounds, loses any
 * ongoing matches and is removed from the list. A LIST request returns a pipe
 * separated list of connected players. A CONNECT request can be accepted and a
 * reply from the client with the same header starts a new match. A WON or LOST
 * header notifies the server (and the opponent) of the game state. If a client
 * has its opponent property set (not null), the server understands that a match
 * is being played between those two clients (player/opponent). Clients
 * requesting lists will display other players` nicknames grayed out when those
 * are playing a match.
 */
public class MineparkServer {

    /**
     * Runs the application.Accept connections from clients that want to play
     * online (multiplayer).Multiplayer mode can pair any two players connecting
     * to the server.
     *
     * @param args
     * @throws java.io.IOException
     */
    private static ArrayList<Player> players;
    private static ScheduledExecutorService executor;
    private static final int PORT = 7001;

    public static void main(String[] args) {

        try {
            // using the pre-defined passwords from "serverkeys" keystore, created by the keytool utility
            FileInputStream keystore = new FileInputStream("src/main/resources/serverkeys.jks");
            char keystorepass[] = "letmein".toCharArray();
            char keypassword[] = "letmein".toCharArray();

            //Set up SSL environment from the Keystore for creating a SSLServerSocket
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(keystore, keystorepass);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, keypassword);
            SSLContext sslcontext = SSLContext.getInstance("SSLv3");
            sslcontext.init(kmf.getKeyManagers(), null, null);
            ServerSocketFactory ssf = sslcontext.getServerSocketFactory();

            Runnable checkPlayers = () -> {
                checkConnectedPlayers();
            };

            executor = Executors.newScheduledThreadPool(1);
            executor.scheduleAtFixedRate(checkPlayers, 0, 5, TimeUnit.SECONDS);

            try (SSLServerSocket listener = (SSLServerSocket) ssf.createServerSocket(PORT)) {
                players = new ArrayList<>();
                System.out.println("Minepark Server is Running");
                while (true) {
                    SSLSocket socket = (SSLSocket) listener.accept();
                    Player player = new Player(socket);
                    if (!player.isConnected() || player.getNickname() == null) {
                        disconnectPlayer(player);
                        continue;
                    }
                    System.out.println(" - New player: " + player.getNickname());
                    player.send("CONNECTED");
                    player.setName(player.getNickname());
                    players.add(player);
                    player.start();
                }
            } catch (IOException ioe) {
                System.out.println("Socket error: " + ioe.getLocalizedMessage());
            }
        } catch (FileNotFoundException fnfe) {
            System.out.println("SSL key error: " + fnfe.getLocalizedMessage());
        } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException | UnrecoverableKeyException | KeyManagementException ex) {
            System.out.println("SSL socket error: " + ex.getLocalizedMessage());
        }
    }

    /**
     *
     * @param message
     */
    static void broadcast(String message) {
        for (Player p : players) {
            p.send(message);
        }
    }

    static void checkConnectedPlayers() {
        for (Player p : players) {
            if (p.getInput() == null) {
                disconnectPlayer(p);
            } else if (!p.isExit() && !p.isAlive()) {
                p.start();
            }
        }
    }

    static void disconnectPlayer(Player p) {
        try {
            System.out.println("Disconnecting " + p.getName());
            players.remove(p);
            p.setConnected(false);
            p.setOpponent(null);
            p.getInput().close();
            p.getOutput().close();
            p.getSocket().close();
            System.out.println(p.getName() + " disconnected");
        } catch (IOException ioe) {
            String message = "Error disconnecting player: " + ioe.getLocalizedMessage();
            System.out.println(message);
        }
    }

    /**
     *
     * @param player
     */
    static void listPlayers(Player player) {
        player.send("PLS");
        for (Player p : players) {
            if (p.getNickname().equals(player.getNickname())) {
                continue;
            }
            player.send(p.getOpponent() != null ? (p.getNickname() + " vs " + p.getOpponent().getNickname()) : p.getNickname());
        }
        player.send("PLE");
    }

    static Player getPlayer(String player) {
        for (Player p : players) {
            if (p.getNickname().equals(player)) {
                return p;
            }
        }
        return null;
    }
}
