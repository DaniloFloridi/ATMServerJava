package ATMServerJava;
import java.io.*;
import java.net.*;
import java.util.*;

public class ATMServer {
    private static Map<String, Account> accounts = new HashMap<>();
    private static final int PORT = 12345;

    static class Account {
        String username;
        String password;
        int accountNumber;
        double balance;

        Account(String username, String password, int accountNumber, double balance) {
            this.username = username;
            this.password = password;
            this.accountNumber = accountNumber;
            this.balance = balance;
        }
    }

    public static void main(String[] args) {
        // Initialize accounts
        accounts.put("user1", new Account("user1", "pass1", 1001, 5000.00));
        accounts.put("user2", new Account("user2", "pass2", 1002, 3000.00));

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private Account currentAccount;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Authentication
                boolean authenticated = false;
                while (!authenticated) {
                    String username = in.readLine();
                    String password = in.readLine();
                    
                    Account account = accounts.get(username);
                    if (account != null && account.password.equals(password)) {
                        currentAccount = account;
                        authenticated = true;
                        out.println("SUCCESS");
                    } else {
                        out.println("FAIL");
                    }
                }

                // Handle operations
                String operation;
                while ((operation = in.readLine()) != null) {
                    String[] parts = operation.split(" ");
                    
                    switch (parts[0]) {
                        case "CONSULTAR_SALDO":
                            out.println(String.format("%.2f", currentAccount.balance));
                            break;
                            
                        case "SACAR":
                            double amount = Double.parseDouble(parts[1]);
                            if (amount <= currentAccount.balance && amount > 0) {
                                currentAccount.balance -= amount;
                                out.println("SUCCESS");
                            } else {
                                out.println("FAIL");
                            }
                            break;
                            
                        case "DEPOSITAR":
                            amount = Double.parseDouble(parts[1]);
                            if (amount > 0) {
                                currentAccount.balance += amount;
                                out.println("SUCCESS");
                            } else {
                                out.println("FAIL");
                            }
                            break;
                            
                        case "SAIR":
                            socket.close();
                            return;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
