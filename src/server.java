// ================== File Abstract ====================
// # 
// # Name: Carmine Valentino
// # File: server.java
// # Program: TCP/UDP Client-Server Socket Program
// # Date: April 19, 2015
// # Course: CS 544 - Computer Networking
// # University: Drexel University
// #
// ================== End Abstract ======================

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Random;

public class server {
	
	// Static Variables Defined for Max Packet Size
	// Note: Message is comprised of a 16Byte Payload
	// And, a 2Byte Header
	private static final int MAX_PAYLOAD_SIZE = 16;
	private static final int MAX_HEADER_SIZE = 2;
	private static final int FINAL_PACKET = -1;
	private static final int MAX_BUFFER_SIZE = MAX_PAYLOAD_SIZE + MAX_HEADER_SIZE;
	
	public static void main(String[] args) throws IOException {
		// Check for Startup Parameters
		if (args.length > 0)
		{
			// -- Declare Objects/Variables --
			int n_port = Integer.parseInt(args[0]);
			int _ack = 0;
			DataInputStream dataInput = null;
			DataOutputStream dataOutput = null;
			ServerSocket serverSocket = null;
			Socket clientSocket = null;
			PrintWriter output = null;
			BufferedReader input = null;
			DatagramSocket dataSocket = null;
			int _r_port = 0;
			byte[] incomingPacketBuffer = new byte[MAX_BUFFER_SIZE];
			byte[] outgoingPacketBuffer  = new byte[MAX_BUFFER_SIZE];
			DatagramPacket ackPacket = null;
			// -------------------------------
			
			// Atempt to Open TCP socket at Given Port
			try
			{
				serverSocket = new ServerSocket(n_port);
			}
			catch (IOException error)
			{
				//error.printStackTrace();
				System.out.println("Cannot Listen on Port " + n_port);
				System.exit(1);
			}
			System.out.println("Server Listening for Connections on Port " + n_port + " ...");
			try //Listen for Comms on Socket
			{
				clientSocket = serverSocket.accept();
			}
			catch (IOException error)
			{
				//error.printStackTrace();
				System.out.println("Unable to Accept Connection from Client ...");
				System.exit(1);
			}
			try // Attempt to Receive (Int)Ack from Client
			{
				dataInput = new DataInputStream(clientSocket.getInputStream());
				_ack = dataInput.readInt();
			}
			catch (IOException error)
			{
				//error.printStackTrace();
				System.out.println("Unable to Accept Ack from Client ...");
				System.exit(1);
			}
			if (117 == _ack)// Authenticate Comms by analyzing Ack for 117 sent from Client
			{
				System.out.println("Received an Ack of: " + _ack + " From the Client ...");
				dataOutput = new DataOutputStream(clientSocket.getOutputStream()); //Open a Data Stream with Client
				_r_port = randomPort(); // Generate Random UDP Port
				System.out.println("Generated Random UDP Port " + _r_port + ". Sending to Client ...");
				dataOutput.writeInt(_r_port); // Send Random UDP port to Client
				closeTcpConnection(serverSocket); // Terminate TCP Socket
				dataSocket = new DatagramSocket(_r_port); // Create a Socket (UDP) at Random Port
				File textFile = new File("receive.txt"); // Create New Text File To Write our Input to
			}
			else
			{
				System.out.println("Invalid Ack Received ...");
				dataInput.close(); // Close Data Stream
				closeTcpConnection(serverSocket); // Terminate TCP Socket
			}
			System.out.println("Listening for Packets ...");
			try
			{
				while (true) // Listen to Packets indefinitely (Until Last Packet is Realized, then Exit)
				{
					DatagramPacket packet = new DatagramPacket(incomingPacketBuffer, incomingPacketBuffer.length); // New Datagram Packet Created For Input Buffer
					dataSocket.receive(packet); // Get Packet from Client
					byte[] _header = new byte[MAX_HEADER_SIZE]; // Allocate Header Buffer
					byte[] _payload = new byte[MAX_PAYLOAD_SIZE]; // Allocate Payload Buffer
					_header = Arrays.copyOfRange(incomingPacketBuffer, 16, 18); // Header in Last 2 Bytes of Packet
					_payload = Arrays.copyOfRange(incomingPacketBuffer, 0, 16); // Payload in First 16 Bytes of Packet
					// -- HEADER -- //
					int _sequence = (int)_header[0]; // Packet Sequence (Added to Ensure No Packet Loss Occured)
					int _eof = (int)_header[1]; // Final Packet Transmission Byte (0 = no, -1 = yes)
					// -- PAYLOAD -- //
					String payloadString = new String(_payload); 
					outgoingPacketBuffer = new String(incomingPacketBuffer).toUpperCase().getBytes(); // Convert Buffer to Upper Case For purpose of Ack'ing
					DatagramPacket outgoingPacket = new DatagramPacket(outgoingPacketBuffer, outgoingPacketBuffer.length, packet.getAddress(), packet.getPort()); //Create Outgoing Datagram to Send Ack
					dataSocket.send(outgoingPacket);// Send Packet Ack
					if (0 == _sequence) //First Packet, Clear Our Output File If Necessary
					{
						FileOutputStream initialFileOutput = new FileOutputStream("receive.txt");
						initialFileOutput.write((new String()).getBytes()); // Write Empty Buffer to Receive.txt to Clear File (Just in Case ..)
					}
					
					FileOutputStream fileOutput = new FileOutputStream("receive.txt",true); //Create New File Output Stream With Append Flag
					fileOutput.write(_payload); // Write Payload To File "receive.txt"
					if (FINAL_PACKET == _eof) // Final Packet Byte Received
					{
						System.out.println("Final Packet Received ...");
						System.out.println("Writing File ...");
						fileOutput.close(); // Close File
						System.out.println("Closing Socket ...");
						dataSocket.close(); // Terminate Socket Connection
						System.exit(0); // It is Time to Exit!
					}
				}
			}
			catch (IOException error)
			{
				System.out.println("Unable To Receive Packet ...");
			}
		}
		else // Catch Flag Errors
		{
			System.out.println("You must specify a Port...");
			System.exit(1);
		}
	}
	// TCP Socket Close
	private static void closeTcpConnection(ServerSocket _serverSocket) throws IOException
	{
		_serverSocket.close();
	}
	
	// Check Availability of Port
	private static boolean isUdpAvailable(int _r_port)
	{
		try
		{
			System.out.println("Port " + _r_port + " is available ...");
			DatagramSocket socket = new DatagramSocket(_r_port);
			socket.close();
		}
		catch (IOException error)
		{
			System.out.println("Port " + _r_port + " is busy ...");
			System.out.println("Attempting to connect on a different port ...");
			return false;
		}
		return true;
	}
	
	// Generate Random UDP Por
	private static int randomPort()
	{
		// Create Random Object
		Random random = new Random();
		//Generate a Random Int between our Port Ranges (1024 -> 65535)
		int r_port = random.nextInt((65535 - 1024) + 1) + 1024;
		if(!isUdpAvailable(r_port)) // Check if Port is Available Before Creating
		{
			randomPort(); // Call Random Port Again To Randomly Generate Port Again
		}
		return r_port;
	}
}
