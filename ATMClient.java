package ATMServerJava;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class ATMClient extends JFrame {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private static PrintWriter out;
    private static BufferedReader in;
    
    private JTextField userField;
    private JPasswordField passField;
    private JTextField amountField;
    private JTextArea outputArea;
    private JPanel operationsPanel;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public ATMClient() {
        setTitle("Caixa Eletrônico");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Login Panel
        JPanel loginPanel = createLoginPanel();
        
        // Operations Panel
        operationsPanel = createOperationsPanel();
        
        mainPanel.add(loginPanel, "login");
        mainPanel.add(operationsPanel, "operations");
        
        add(mainPanel);
        cardLayout.show(mainPanel, "login");
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        userField = new JTextField(15);
        passField = new JPasswordField(15);
        JButton loginButton = new JButton("Login");
        
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Usuário:"), gbc);
        gbc.gridx = 1;
        panel.add(userField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Senha:"), gbc);
        gbc.gridx = 1;
        panel.add(passField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(loginButton, gbc);
        
        loginButton.addActionListener(e -> authenticate());
        
        return panel;
    }

    private JPanel createOperationsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel buttonsPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        amountField = new JTextField(10);
        outputArea = new JTextArea(10, 30);
        outputArea.setEditable(false);
        
        JButton depositButton = new JButton("Depositar");
        JButton withdrawButton = new JButton("Sacar");
        JButton balanceButton = new JButton("Consultar Saldo");
        JButton exitButton = new JButton("Sair");
        
        buttonsPanel.add(depositButton);
        buttonsPanel.add(withdrawButton);
        buttonsPanel.add(balanceButton);
        buttonsPanel.add(exitButton);
        
        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Valor: R$"));
        inputPanel.add(amountField);
        
        panel.add(buttonsPanel, BorderLayout.NORTH);
        panel.add(inputPanel, BorderLayout.CENTER);
        panel.add(new JScrollPane(outputArea), BorderLayout.SOUTH);
        
        depositButton.addActionListener(e -> deposit());
        withdrawButton.addActionListener(e -> withdraw());
        balanceButton.addActionListener(e -> checkBalance());
        exitButton.addActionListener(e -> exit());
        
        return panel;
    }

    private void authenticate() {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            out.println(userField.getText());
            out.println(new String(passField.getPassword()));
            
            String response = in.readLine();
            if (response.equals("SUCCESS")) {
                cardLayout.show(mainPanel, "operations");
                outputArea.append("Login realizado com sucesso!\n");
            } else {
                JOptionPane.showMessageDialog(this, "Usuário ou senha incorretos!");
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro de conexão: " + ex.getMessage());
        }
    }

    private void deposit() {
        try {
            double amount = Double.parseDouble(amountField.getText());
            out.println("DEPOSITAR " + amount);
            String response = in.readLine();
            if (response.equals("SUCCESS")) {
                outputArea.append("Depósito de R$ " + amount + " realizado com sucesso!\n");
            } else {
                outputArea.append("Erro ao realizar depósito!\n");
            }
        } catch (Exception ex) {
            outputArea.append("Erro: Valor inválido!\n");
        }
        amountField.setText("");
    }

    private void withdraw() {
        try {
            double amount = Double.parseDouble(amountField.getText());
            out.println("SACAR " + amount);
            String response = in.readLine();
            if (response.equals("SUCCESS")) {
                outputArea.append("Saque de R$ " + amount + " realizado com sucesso!\n");
            } else {
                outputArea.append("Erro: Saldo insuficiente ou valor inválido!\n");
            }
        } catch (Exception ex) {
            outputArea.append("Erro: Valor inválido!\n");
        }
        amountField.setText("");
    }

    private void checkBalance() {
        out.println("CONSULTAR_SALDO");
        try {
            String balance = in.readLine();
            outputArea.append("Saldo atual: R$ " + balance + "\n");
        } catch (IOException ex) {
            outputArea.append("Erro ao consultar saldo!\n");
        }
    }

    private void exit() {
        out.println("SAIR");
        System.exit(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ATMClient().setVisible(true);
        });
    }
}