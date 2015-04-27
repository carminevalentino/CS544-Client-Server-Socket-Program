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
	
	private static final int MAX_PAYLOAD_SIZE = 16;
	private static final int MAX_HEADER_SIZE = 2;
	private static final int FINAL_PACKET = -1;
	private static final int MAX_BUFFER_SIZE = MAX_PAYLOAD_SIZE + MAX_HEADER_SIZE;
	
	public static void main(String[] args) throws IOException {
		int n_port;
		if (args.length > 0)
		{
			n_port = Integer.parseInt(args[0]);
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
			try
			{
				clientSocket = serverSocket.accept();
			}
			catch (IOException error)
			{
				//error.printStackTrace();
				System.out.println("Unable to Accept Connection from Client ...");
				System.exit(1);
			}
			try
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
			if (117 == _ack)
			{
				System.out.println("Received an Ack of: " + _ack + " From the Client ...");
				dataOutput = new DataOutputStream(clientSocket.getOutputStream());
				_r_port = randomPort();
				System.out.println("Generated Random UDP Port " + _r_port + ". Sending to Client ...");
				dataOutput.writeInt(_r_port);
				dataSocket = new DatagramSocket(_r_port);
				File textFile = new File("receive.txt"); // Create New Text File
			}
			else
			{
				System.out.println("Invalid Ack Received ...");
				dataInput.close();
				closeTcpConnection(serverSocket);
			}
			System.out.println("Listening for Packets ...");
			try
			{
				while (true)
				{
					DatagramPacket packet = new DatagramPacket(incomingPacketBuffer, incomingPacketBuffer.length);
					dataSocket.receive(packet);
					byte[] _header = new byte[MAX_HEADER_SIZE];
					byte[] _payload = new byte[MAX_PAYLOAD_SIZE];
					_header = Arrays.copyOfRange(incomingPacketBuffer, 16, 18); // Header in Last 2 Bytes of Packet
					_payload = Arrays.copyOfRange(incomingPacketBuffer, 0, 16); // Payload in First 16 Bytes of Packet
					// -- HEADER -- //
					int _sequence = (int)_header[0]; // Packet Sequence (Added to Ensure No Packet Loss Occured)
					int _eof = (int)_header[1]; // Final Packet Transmission Byte (0 = no, -1 = yes)
					// -- PAYLOAD -- //
					String payloadString = new String(_payload);
					outgoingPacketBuffer = new String(incomingPacketBuffer).toUpperCase().getBytes();
					DatagramPacket outgoingPacket = new DatagramPacket(outgoingPacketBuffer, outgoingPacketBuffer.length, packet.getAddress(), packet.getPort());
					dataSocket.send(outgoingPacket);
					if (0 == _sequence) //First Packet, Clear Our Output File If Necessary
					{
						FileOutputStream initialFileOutput = new FileOutputStream("receive.txt");
						initialFileOutput.write((new String()).getBytes());
					}
					
					FileOutputStream fileOutput = new FileOutputStream("receive.txt",true);
					fileOutput.write(_payload); // Write Payload To File "receive.txt"
					if (FINAL_PACKET == _eof) // Final Packet Byte Received
					{
						System.out.println("Final Packet Received ...");
						System.out.println("Writing File ...");
						fileOutput.close();
						System.out.println("Closing Socket ...");
						dataSocket.close();
						System.exit(0);
					}
				}
			}
			catch (IOException error)
			{
				System.out.println("Unable To Receive Packet ...");
			}
		}
		else
		{
			System.out.println("You must specify a Port...");
			System.exit(1);
		}
	}
	private static void closeTcpConnection(ServerSocket _serverSocket) throws IOException
	{
		_serverSocket.close();
	}
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
	private static int randomPort()
	{
		// Create Random Object
		Random random = new Random();
		//Generate a Random Int between our Port Ranges (1024 -> 65535)
		int r_port = random.nextInt((65535 - 1024) + 1) + 1024;
		if(!isUdpAvailable(r_port))
		{
			randomPort();
		}
		return r_port;
	}
}
