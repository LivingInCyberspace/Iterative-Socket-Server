import java.io.*;
import java.net.*;
import java.util.*;

/* CNT 4504 - Jinel Johnson, Lupe Ramos - 3/1/2020
Purpose of Program:
Listens to a port for client requests for a specific unix command and delivers
its output to the client */

public class Server {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        int clientCount = 0, port;

        // Checks if execution has command line arguments, requests info if not
        if(args.length >= 1) {
            port = Integer.parseInt(args[0]);
        } else {
            System.out.print("Enter port (1025 - 4998): ");
            port = input.nextInt();
            // ensures port # is between valid comm ports
            while(port < 1025 || port > 4998) {
                System.out.print("Enter a valid port number: ");
                port = input.nextInt();
            }
        }

        try(ServerSocket svSocket = new ServerSocket(port, 25)){
            System.out.println("Listening for clients at port " + port + "...");

            while(true) {
                Socket socket = svSocket.accept();
                System.out.println("Handling request #" + ++clientCount + "...");

                // Retrieves request value from client
                String choice = getClientChoice(socket);
                // Retrieves the command response
                String response = handleRequest(choice);
                // Prints to the client
                printToClient(response, socket);
                System.out.println(" Request completed!");
            }
        } catch(IOException e) {
            System.out.print("I/O Exception: ");
            e.printStackTrace();
            System.exit(-1);
        }

        input.close();
    }

    /* Reads input from the client's output stream and returns the response as an integer */
    public static String getClientChoice(Socket socket) throws IOException {
        InputStream inpStream = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inpStream));

        return reader.readLine();
    }

    /* Client response is passed down and handled depending on choice */
    public static String handleRequest(String choice) throws IOException {
        String fullMessage = null;

        switch(choice) {
            case "1":     // Date & Time
                fullMessage = getCmdOutput("date").toString();
                break;

            case "2":     // Uptime
                fullMessage = getCmdOutput("uptime -p").toString();
                break;

            case "3":     // Memory Use
                fullMessage = getCmdOutput("free -t -m").toString();
                break;

            case "4":     // Netstat (lists server connections)
                fullMessage = getCmdOutput("netstat -at").toString();
                break;

            case "5":     // List current users
                fullMessage = getCmdOutput("who").toString();
                break;

            case "6":     // Running processes
                fullMessage = getCmdOutput("ps").toString();
                break;
        }

        return fullMessage;
    }

    /* Runs specified unix command then passes its output to be printed */
    public static StringBuilder getCmdOutput(String command) throws IOException {
        // BufferedReader used to read the command response
        BufferedReader cmdResponse;
        Process cmd;

        // Executes whatever command is passed down from switch-case
        cmd = Runtime.getRuntime().exec(command);
        cmdResponse = new BufferedReader(new InputStreamReader(cmd.getInputStream()));

        // Reads results of command and calls client printing method
        StringBuilder fullMessage = new StringBuilder();
        for(String msg = ""; msg != null; msg = cmdResponse.readLine())
            fullMessage.append(msg).append("\n");

        return fullMessage;
    }

    /* Sends specified message in parameters to client */
    public static void printToClient(String message, Socket socket) throws IOException {
        OutputStream output = socket.getOutputStream();
        PrintWriter sender = new PrintWriter(output, true);

        sender.println(message);
        sender.close();
    }
}