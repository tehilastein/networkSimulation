package networks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class NetworkServer {
/**
 * Server representation of network connection
 * @param args
 * @throws IOException
 */
	public static void main(String[] args) throws IOException {

		args = new String[] { "49732" };

		if (args.length != 1) {
			System.err.println("Usage: java NetworkServer <port number>");
			System.exit(1);
		}

		 
		int portNumber = Integer.parseInt(args[0]); 												// establishes connection with I/O port

		try (ServerSocket serverSocket = new ServerSocket(portNumber);
				Socket clientSocket = serverSocket.accept(); 										// connects client to the server
				PrintWriter responseWriter = new PrintWriter(clientSocket.getOutputStream(), true); // will write response to client
				BufferedReader requestReader = new BufferedReader( 									// reads from client
						new InputStreamReader(clientSocket.getInputStream()));) {

			String usersRequest; 																// initialize necessary variables
			ArrayList<String> completePackets = null;											// initialize an ArrayList that will hold a deep copy of created packets 	
			boolean requestDatatype;															
			Random random = new Random();

			while ((usersRequest = requestReader.readLine()) != null ) {						// while connection is maintained with the client
				requestDatatype = false; 														// data type be default represents a digit
				String copy = usersRequest;														// create a copy of user's request
				copy=copy.replaceAll(" ", "");													// replace all spaces in the copy to enable data type checking
				char[] chars = copy.toCharArray();												// create an array of copy's characters
				for (Character x : chars) { 													// if the input includes a non-digit
					if (!Character.isDigit(x)) {
						requestDatatype = true; 												// the data type becomes true and represents a String
					}
				}
				if (requestDatatype) { 															// if the data type is not numeric
					System.out.println("\"" + usersRequest + "\"received "); 					// server displays user's request
					ArrayList<String> packets = new ArrayList<>(); 								// 2 arrayLists are instantiated to hold packet data
					completePackets = new ArrayList<>();
					createPackets(random, packets);												// fill the packets array with message packets
					copyPackets(packets, completePackets);										// copy packets to new arrayList
					packets.remove(packets.size() - 1); 										// remove the last packet from the packets arrayList (so it will
																								// not be sent until later)
					Collections.shuffle(packets); 												// shuffle packets to randomize them
					for (int i = 0; i < packets.size(); i++) {									// attempt to send each packet
						double send = random.nextDouble();
						if (send > .20) { 														// send with an 80% probability
							responseWriter.println(packets.get(i));
						}
					}
					responseWriter.println(completePackets.remove(completePackets.size() - 1)); // send the last packet to indicate that sending has completed
				}

				else {																			// if data is entirely numeric
					String[] missingPacketsArray = usersRequest.split(" ");						// separate usersRequest by space
					for (int i = 0; i < missingPacketsArray.length; i++) {
						int index = Integer.parseInt(missingPacketsArray[i]);				
						double send = random.nextDouble(); 
						if (send > .20) {														//send with an 80% probability
							responseWriter.println(completePackets.get(index));					//send appropriate packet based on userRequest
						}
					}
					responseWriter.println("Done.");											//send final message when attempt is complete
				}

			}

		} catch (IOException e) {
			System.out.println(
					"Exception caught when trying to listen on port " + portNumber + "or listening for a connection");
			System.out.println(e.getMessage());

		}

	}

	/**
	 * Creates packets to be sent to the client with a message provided
	 * 
	 * @param random
	 * @param packets
	 * @return
	 */
	private static void createPackets(Random random, ArrayList<String> packets) {

		int numPackets = random.nextInt((10) + 1) + 20; 									// create a Random number of packets ranging from 20-30
		String message = "Hello there. This message should be received in full. ";
		int remainder = 0; 																	// initialize numPackets to be divisible by message length

		if (message.length() % (numPackets - 1) != 0) { 									// without including last packet, if the number of packets is
																							// not divisible
			remainder = message.length() % (numPackets - 1); 								// update the remainder
		}

		int subLength = message.length() / (numPackets - 1); 								// the length of each packet will be determined by
																							// sublength
		int beginIndex = 0; 																// start splitting the message at the strings first character
		for (int i = 1; i < numPackets; i++) { 												// iterate according to the number of packets (less one)
			StringBuilder sb = new StringBuilder(); 										// create a String builder to combine data and metadata
			sb.append(i + "/" + numPackets + ":"); 											// APPEND METADATA TO THE STRINGBUILDER
			if (i != numPackets - 1) { 														// if i is not the second to last packet
				sb.append(message.substring(beginIndex, beginIndex + subLength)); 			// add data to the stringBuilder
			} else {
				switch (remainder) { 														// if the remainder is zero
				case 0:
					sb.append(message.substring(beginIndex, beginIndex + subLength)); 		// add the regular quantity of
																							// data to the stringBuilder
					break;
				default: 																	// otherwise, 
					sb.append(message.substring(beginIndex, beginIndex + remainder + subLength)); 	// add the remainder of the message to the stringBuilder
				}
			}
			packets.add(sb.toString()); 													// add the contents of the StringBulder to the list of packets
			beginIndex += subLength; 														// increment beginIndex
		}
		packets.add("Done."); 																// add the last packet to the list
	}

	/**
	 * copy the packets to a new arrayList before shuffling
	 * @param packets
	 * @param copyPackets
	 */
	private static void copyPackets(ArrayList<String> packets, ArrayList<String> copyPackets) {

		for (int i = 0; i < packets.size(); i++) {
			copyPackets.add(packets.get(i));
		}
	}

}
