package org.example;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class FIFAApp {
    private static String loadedFilePath; // Variável para armazenar o caminho do arquivo carregado
    private static boolean isConnected = false; // Variável para verificar se está conectado ao servidor
    private static String fileId;
    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static JPanel playerPanel;
    private static JFrame frame;

    public static void addPlayers(List<Player> players){
        playerPanel.removeAll();
        // Create and add buttons for each player
        for (Player player : players) {
            JButton button = new JButton(player.name());
            button.addActionListener(e -> showPlayerDialog(player));

           // Configurações do layout do GridBagConstraints
            var gbc = new GridBagConstraints();
            gbc.gridx = 0;  // Define a coluna
            gbc.gridy = GridBagConstraints.RELATIVE;  // Adiciona abaixo do último componente adicionado
            gbc.fill = GridBagConstraints.HORIZONTAL;  // Preenche horizontalmente
            gbc.weightx = 1.0;  // Distribui o espaço horizontalmente de forma igual

            playerPanel.add(button, gbc);
        }
    }

    private static void showPlayerDialog(Player player) {
        JDialog dialog = new JDialog(frame, "Player Details", true);
        dialog.setSize(300, 200);
        dialog.setLayout(new GridLayout(5, 2));

        dialog.add(new JLabel("ID:"));
        JTextField idField = new JTextField(player.id());
        dialog.add(idField);

        dialog.add(new JLabel("Name:"));
        JTextField nameField = new JTextField(player.name());
        dialog.add(nameField);

        dialog.add(new JLabel("Nationality:"));
        JTextField nationalityField = new JTextField(player.nationality());
        dialog.add(nationalityField);

        dialog.add(new JLabel("Club:"));
        JTextField clubField = new JTextField(player.club());
        dialog.add(clubField);

        JButton editButton = new JButton("Edit");
        editButton.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
                String id = idField.getText().trim();
                String name = nameField.getText().trim();
                String nationality = nationalityField.getText().trim();
                String club = clubField.getText().trim();
                try {

                    out.printf("atualiza %s %s;%s;%s;%s\n", fileId, id, name, nationality, club);
                    JOptionPane.showMessageDialog(frame, "Player updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch(Exception ex){
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Falha em remover jogador do arquivo");
                }
            dialog.dispose(); 
        }
        });
        dialog.add(editButton);

        JButton removeButton = new JButton("Remove");
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String id = idField.getText().trim();
                try {
                    out.printf("deleta %s %s \n", fileId, id);
                    JOptionPane.showMessageDialog(frame, "Player removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch(Exception ex){
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Falha em remover jogador do arquivo");
                }
            dialog.dispose(); 
        }});
        dialog.add(removeButton);

        dialog.setVisible(true);
    }


    private static void readPlayers(){
        String line;
        
        String name = null,nationality = null,club = null, id = null;
        var players = new ArrayList<Player>();
        // Lê as linhas da resposta do servidor
        try {
            while ((line = in.readLine()) != null && !line.equals("END")) {
                if (line.startsWith("ID: ")) {
                    id = line.substring(3);
                } else if (line.startsWith("Nome do Jogador: ")) {
                    // Atualiza o nome do jogador atual
                    name = line.substring(16);
                } else if (line.startsWith("Nacionalidade do Jogador: ")) {
                    // Atualiza a nacionalidade do jogador atual
                    nationality = line.substring(25);
                } else if (line.startsWith("Clube do Jogador: ")) {
                    // Atualiza o clube do jogador atual
                    club = line.substring(18);
                }

                if(club != null && name != null && nationality != null && id != null){
                    players.add(new Player(name, nationality, club, id));
                    club = null;
                    name = null;
                    nationality = null;
                }
            }

            addPlayers(players);
            playerPanel.revalidate();
            playerPanel.repaint();
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error connecting to server.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

  

    public static void main(String[] args) {
        JFrame frame = new JFrame("FIFA Player Management");
        frame.setSize(600, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem loadFileItem = new JMenuItem("Load FIFA File");
        JMenuItem connectServerItem = new JMenuItem("Connect to Server");
        JMenuItem generateListItem = new JMenuItem("Generate Player List");

        fileMenu.add(loadFileItem);
        fileMenu.add(connectServerItem);
        fileMenu.add(generateListItem);
        menuBar.add(fileMenu);
        frame.setJMenuBar(menuBar);

        // Panel for text fields
        JPanel inputPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        inputPanel.add(new JLabel("ID:"));
        JTextField idField = new JTextField();
        inputPanel.add(idField);

        inputPanel.add(new JLabel("Age:"));
        JTextField ageField = new JTextField();
        inputPanel.add(ageField);

        inputPanel.add(new JLabel("Player Name:"));
        JTextField nameField = new JTextField();
        inputPanel.add(nameField);

        inputPanel.add(new JLabel("Nationality:"));
        JTextField nationalityField = new JTextField();
        inputPanel.add(nationalityField);

        inputPanel.add(new JLabel("Club Name:"));
        JTextField clubField = new JTextField();
        inputPanel.add(clubField);

        JButton searchButton = new JButton("Search");
        inputPanel.add(searchButton);
        inputPanel.add(new JLabel()); // Empty label for alignment
        
        // Panel for displaying search results

        playerPanel = new JPanel(new GridBagLayout());
        JScrollPane scrollPane = new JScrollPane(playerPanel);


        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Placeholder for server address
        final String[] serverAddress = {null};

        // Action listeners for menu items and button
        loadFileItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    loadedFilePath = selectedFile.getAbsolutePath(); // Armazenar o caminho do arquivo carregado
                    System.out.println("Loaded file: " + loadedFilePath);
                    JOptionPane.showMessageDialog(frame, "File loaded successfully: " + loadedFilePath);

                    out.print("abrir ");
                    out.println(loadedFilePath);
                    try {
                        String identificador = in.readLine();
                        System.out.println("Identificador: " + identificador);
                        fileId = identificador;
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(frame, "Error connecting to server.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        connectServerItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Definir automaticamente o endereço do servidor como "localhost"
                serverAddress[0] = "localhost";
                try {
                    // Tentativa de conexão ao servidor
                    Socket serverSocket = new Socket(serverAddress[0], 1337);
                    socket = serverSocket;
                    out = new PrintWriter(socket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    JOptionPane.showMessageDialog(frame, "Connected to server at: " + serverAddress[0], "Success", JOptionPane.INFORMATION_MESSAGE);
                    System.out.println("Connected to server at: " + serverAddress[0]);
                    isConnected = true; // Marcar como conectado
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Failed to connect to server at: " + serverAddress[0], "Error", JOptionPane.ERROR_MESSAGE);
                    System.out.println("Failed to connect to server at: " + serverAddress[0]);
                }
            }
        });

        generateListItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!isConnected) {
                    JOptionPane.showMessageDialog(frame, "Please connect to the server first.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (loadedFilePath == null) {
                    JOptionPane.showMessageDialog(frame, "Please load a file first.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                out.print("buscatodos ");
                out.println(fileId);

                readPlayers();
            }
        });

        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!isConnected) {
                    JOptionPane.showMessageDialog(frame, "Please connect to the server first.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (fileId == null) {
                    JOptionPane.showMessageDialog(frame, "Please load a file first.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String id = idField.getText();
                String age = ageField.getText();
                String name = nameField.getText();
                String nationality = nationalityField.getText();
                String club = clubField.getText();

                System.out.printf("Campos %s %s %s %s", id, age, name, nationality, club);
                // Enviar comando e dados ao servidor
                out.printf("busca %s ", fileId);
                if(!id.isBlank()) out.printf("id %s ", id);
                if(!age.isBlank()) out.printf("idade %s ", age);
                if(!name.isBlank()) out.printf("nomeJogador %s ", name);
                if(!nationality.isBlank()) out.printf("nacionalidade %s ", nationality);
                if(!club.isBlank()) out.printf("nomeClube %s ", club);
                out.println();

                // Receber resposta do servidor
                readPlayers();
            }
        });

        frame.setVisible(true);
    }
}
