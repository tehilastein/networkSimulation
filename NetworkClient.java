package networks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class NetworkClient {
/**
 * Client representation of a network connection
 * @param args
 * @throws IOException
 */
	public static void main(String[] args) throws IOException {

		args = new String[] { "127.0.0.1", "49732" };

		if (args.length != 2) {
			System.err.println("Usage: java NetworkClient <host name> <port number> ");
			System.exit(1);
		}

		String hostName = args[0];
		int portNumber = Integer.parseInt(args[1]);

		try (Socket clientSocket = new Socket(hostName, portNumber);
				PrintWriter requestWriter = new PrintWriter(clientSocket.getOutputStream(), true);				//writes request to server
				BufferedReader responseReader = new BufferedReader(												//reads response from server
						new InputStreamReader(clientSocket.getInputStream()));
				BufferedReader requestReader = new BufferedReader(new InputStreamReader(System.in))) {			//reads input from user
			String userInput;
			String[] array = null;																				//initialize an array to hold incoming packets
			StringBuilder sb = null;																			//initialize StringBuilder to append missingPackets to String
			while ((userInput = requestReader.readLine()) != null) {											//While user is interacting with client
				requestWriter.println(userInput);																//send user input to server	
				int totalPackets = 0;				
				String serverResponse;
				int lastIndex;																					//variable to hold lastIndex of substring
	
				while (!(serverResponse = responseReader.readLine()).equals("Done.")) {							//initial read from server 
					int index = serverResponse.indexOf("/");													//separation of metadata and data
					lastIndex = serverResponse.indexOf(":");
					int packetNum = Integer.parseInt(serverResponse.substring(0, index));
					if (totalPackets == 0) {																	//if packet is the first received
						totalPackets = Integer.parseInt(serverResponse.substring((index + 1), lastIndex));		//set total packets with metadata information
						array = new String[totalPackets - 1];													//instantiate array
					}

					String data = serverResponse.substring(lastIndex + 1, serverResponse.length());				//accept the data segment of the packet
					array[packetNum - 1] = data;																//put data into its respective position in the array
				}

				boolean hasNull = checkForNulls(array);															//check to see if any packets were dropped
				while (hasNull) {																				//while packets are missing  
					sb = new StringBuilder();																	//reset StringBuilder
					for (int i = 0; i < array.length; i++) {													//add indexes of missing packets to a String
						if (array[i] == null) {
							sb.append(i + " ");
						}
					}

					requestWriter.println(sb.toString());														//send missingPacket String to Server
					while (!(serverResponse = responseReader.readLine()).equals("Done.")) {						//while server is not done sending packets
						int index = serverResponse.indexOf("/");												//separate metadata and data
						lastIndex = serverResponse.indexOf(":");
						int packetNum = Integer.parseInt(serverResponse.substring(0, index));
						array[packetNum - 1] = serverResponse.substring(lastIndex + 1, serverResponse.length());
					}
					hasNull = checkForNulls(array);																//check again for missingPackets
				}
				for (int i = 0; i < array.length; i++) {														//display the final message
					System.out.print(array[i]);
				}
				clientSocket.close();																			//terminate the connection
			}

		} catch (UnknownHostException e) {
			System.err.println("Host " + hostName + "is unknown.");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " + hostName);
			System.exit(1);
		}
	}

	/**
	 * checks to see if there are any missing packets
	 * indicated by null value
	 * @param array
	 * @return
	 */
	public static boolean checkForNulls(String[] array) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == null) {
				return true;
			}
		}
		return false;
	}

}
