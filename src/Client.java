import java.io.*;
import java.net.*;
import java.time.*;
import java.util.*;

/* CNT 4504 - Jinel Johnson, Lupe Ramos - 3/1/2020
Purpose of Program:
Retrieves information from command line and/or user input to send requests
from spawned clients and receive responses.*/

public class Client {
	private static long turnTime;

	public static void main(String[] args) throws InterruptedException {
		Scanner input = new Scanner(System.in);
		String address;
		int port;

		// Checks if execution has command line arguments, requests info if not
		if(args.length >= 2) {
			address = args[0];
			port = Integer.parseInt(args[1]);
		} else if(args.length == 1) {
			address = args[0];
			System.out.print("Enter port (1025 - 4998): ");
			port = input.nextInt();
		} else {
			System.out.print("Enter address: ");
			address = input.nextLine();
			System.out.print("Enter port (1025 - 4998): ");
			port = input.nextInt();
		}
		// Ensures port # is between valid comms ports
		while(port < 1025 || port > 4998) {
			System.out.print("Enter a valid port number: ");
			port = input.nextInt();
		}

		// Prints welcome screen and options
		System.out.print("\nWelcome!\n 1) Date & Time\n 2) Uptime\n 3) Memory Use"
				+ "\n 4) List Server Connections\n 5) List Current Users\n 6) Running Processes"
				+ "\n\nSelect the option you'd like to see: ");
		// Control loop for proper selection
		String choice = input.next();
		while(Integer.parseInt(choice) < 1 || Integer.parseInt(choice) > 6) {
			System.out.print("Invalid option. Please try again: ");
			choice = input.next();
		}

		System.out.println(" Option " + choice + " selected!");

		// Asks for the request count
		System.out.print("Number of client requests (1, 5, 15, 20, 25): ");
		int clReqs = input.nextInt();
		// Ensures count is 1, 5, 15, 20 or 25
		while(clReqs <= 1 || clReqs > 25 || clReqs % 5 != 0) {
			// Breaks before re-requesting input if only 1 client is desired(because 1 % 5 != 0)
			if(clReqs == 1) break;
			System.out.print("Invalid request number. Please try again: ");
			clReqs = input.nextInt();
		}
		input.close();

		List<Thread> threadHolder = new ArrayList<>();
		// Makes new thread to run server requests
		for(int i = 1; i <= clReqs; i++) {
			Thread reqThread = new RequestThread(port, address, choice, "Client #" + i);
			reqThread.start();
			threadHolder.add(reqThread);
		}

		// Loop for each thread in the array that stalls the program until they finish
		for(Thread thread : threadHolder) {
			thread.join();
		}

		// Prints turn-around total and avg after final thread completes
		System.out.printf("----------------------------------\nTotal Turn-around Time: %d ms"
				+ "\nAverage Turn-around Time: %d ms\n", turnTime, turnTime/clReqs);
	}

	public static void addToTurnSum(long time) {
		turnTime += time;
	}

	/* Sends specified message in parameters to server */
	public static void sendRequest(String message, Socket socket) throws IOException {
		OutputStream output = socket.getOutputStream();
		PrintWriter sender = new PrintWriter(output, true);

		sender.println(message);
	}

	/* Reads from the server's output stream and returns compiled response */
	public static String compileResponse(Socket socket) throws IOException {
		InputStream inpStream = socket.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inpStream));

		String msg;
		StringBuilder compiledMsg = new StringBuilder();

		while((msg = reader.readLine()) != null)
			compiledMsg.append(msg).append("\n");

		return compiledMsg.toString();
	}
}

/* Class for creating and handling the threads */
class RequestThread extends Thread {
	private int port;
	private String address, choice, threadName;


	public RequestThread(int port, String address, String choice, String threadName) {
		this.port = port;
		this.address = address;
		this.choice = choice;
		this.threadName = threadName;
	}

	@Override
	public void run() {
		try(Socket socket = new Socket(address, port)) {
			// Starts timer
			Instant start = Instant.now();
			// Sends client request
			Client.sendRequest(choice, socket);
			String response = Client.compileResponse(socket);
			// Ends timer
			Instant finish = Instant.now();
			long time = Duration.between(start, finish).toMillis();

			// Returns the server response
			System.out.print(threadName + " output:\n" + response
					+ "\n" + threadName + " finished in " + time + " ms\n\n");

			// Adds turn-around time to total sum.
			Client.addToTurnSum(time);
		} catch(IOException e) {
			System.out.print("I/O Exception: ");
			e.printStackTrace();
			System.exit(-1);
		}
	}
}