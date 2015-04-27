// ================== File Abstract ====================
// # 
// # Name: Carmine Valentino
// # Program: TCP/UDP Client-Server Socket Program
// # Date: April 19, 2015
// # Course: CS 544 - Computer Networking
// # University: Drexel University
// #
// ================== End Abstract ======================

import java.io.*;
import java.net.*;

public class client {
	
	private static final int MAX_PAYLOAD_SIZE = 16;
	private static final int MAX_HEADER_SIZE = 2;
	private static final int MAX_BUFFER_SIZE = MAX_PAYLOAD_SIZE + MAX_HEADER_SIZE;

	public static void main(String[] args) {
		String hostname;
		int n_port;
		int r_port = 0;
		String filename;
		Socket clientSocket = null;
		DataInputStream dataInput = null;
		DataOutputStream dataOutput = null;
		DatagramSocket dataSocket = null;
		DatagramPacket packetOutput = null;
		DatagramPacket packetInput = null;
		InetAddress _hostname = null;
		
		byte[] incomingPacket = null;
		
		if (args.length > 2)
		{	
			hostname = args[0];
			n_port = Integer.parseInt(args[1]);
			filename = args[2];
			File file = new File(filename);
			try
			{
				System.out.println("Attempting to connect to "+ hostname + ":" + n_port + " ...");
				clientSocket = new Socket(hostname, n_port);
				System.out.println("Connection Established ...");
			}
			catch (IOException error)
			{
				//error.printStackTrace();
				System.out.println("Unable to Connect to " + hostname + ":" + n_port + "...");
				System.exit(1);
			}
			
			// Sending 117 Ack Out
			try
			{
				dataOutput = new DataOutputStream(clientSocket.getOutputStream());
				dataOutput.writeInt(117);
			}
			catch (IOException error)
			{
				System.out.println("Unable to Transmit Ack to Server ...");
				System.exit(1);
			}
	        try
	        {
	        	dataInput = new DataInputStream(clientSocket.getInputStream());
	        	r_port = dataInput.readInt();
	        	System.out.println("Received Random UDP Port (" + r_port + ") " + "from Server ...");
	        	
	        }
	        catch (IOException error)
	        {
	        	System.out.println("Unable to Get Input From the Server ...");
	        }
	        try
	        {
		        dataInput.close();
		        dataOutput.close();
		        clientSocket.close();
	        }
	        catch (IOException error)
	        {
	        	System.out.println("Unable to Close Sockets/Streams ...");
	        }
	        try
	        {
	        	byte[] byteArray = new byte[(int) file.length()];
	        	byte[] packetBuffer = new byte[MAX_BUFFER_SIZE];
	        	byte[] headerBuffer = new byte[MAX_HEADER_SIZE];
	        	byte[] payloadBuffer = new byte[MAX_PAYLOAD_SIZE];
	        	
	        	int byteArrayIndex = 0;
	        	int numberOfPackets = 0;
	        	int packetIndex = 0;
	        	FileInputStream fileInput = new FileInputStream(file);
	        	fileInput.read(byteArray);
	        	int fileSize = (int)file.length();
	        	// The Last Packet Contains less than Max Message Size
	        	if (fileSize % MAX_PAYLOAD_SIZE > 0)
	        	{
	        		numberOfPackets = (fileSize/MAX_PAYLOAD_SIZE) + 1;
	        	}
	        	else
	        	{
	        		numberOfPackets = fileSize/MAX_PAYLOAD_SIZE;
	        	}
	        	System.out.println("Number of Packets to Be Sent " + numberOfPackets + "...");
	        	System.out.println("Total Byte Size " + fileSize + "...");
	        	
	        	for(int currByte = 0; currByte < file.length(); ++currByte)
	        	{
	        		if(byteArrayIndex < MAX_PAYLOAD_SIZE)
	        		{
	        			payloadBuffer[byteArrayIndex] = byteArray[currByte];
	        			++byteArrayIndex;
	        			// Last Byte of File Reached
	        			if((fileSize - currByte) == 1)
	        			{
		        			headerBuffer = buildPacketHeader(packetIndex, -1); // -1 Indicates End of File
		        			System.arraycopy(payloadBuffer, 0, packetBuffer, 0, MAX_PAYLOAD_SIZE);
		        			System.arraycopy(headerBuffer, 0, packetBuffer, MAX_PAYLOAD_SIZE, MAX_HEADER_SIZE);
		        			try
		        			{
		        				_hostname = InetAddress.getByName(hostname);
		        				packetOutput = new DatagramPacket(packetBuffer, packetBuffer.length, _hostname, r_port);
		        				dataSocket.send(packetOutput);
		        				dataSocket.close(); //Last Packet, so Lets Close Connection on Client end
		        			}
		        			catch (IOException error)
		        			{
		        				System.out.println("Unable to Transmit Packet ...");
		        			}
		        			String packetToString = new String(payloadBuffer);
		        			System.out.println(packetToString.toUpperCase());
		        			++packetIndex;
	        			}
	        		}
	        		else
	        		{
	        			headerBuffer = buildPacketHeader(packetIndex, 0); // 0 Indicates More Packets to Come!
	        			System.arraycopy(payloadBuffer, 0, packetBuffer, 0, MAX_PAYLOAD_SIZE);
	        			System.arraycopy(headerBuffer, 0, packetBuffer, MAX_PAYLOAD_SIZE, MAX_HEADER_SIZE);
	        			try
	        			{
	        				_hostname = InetAddress.getByName(hostname);
	        				packetOutput = new DatagramPacket(packetBuffer, packetBuffer.length, _hostname, r_port);
	        				dataSocket = new DatagramSocket();
	        				dataSocket.send(packetOutput);
	        				try
	        				{
		        				incomingPacket = new byte[MAX_BUFFER_SIZE];
		        				packetInput = new DatagramPacket(incomingPacket, incomingPacket.length, _hostname, r_port);
		        				dataSocket.receive(packetInput);
		        			}
	        				catch(IOException error)
	        				{
	        					System.out.println("Did Not Receive Ack From Server on Data Sent ...");
	        					System.exit(1);
	        				}
	        			}
	        			catch (IOException error)
	        			{
	        				System.out.println("Unable to Transmit Packet ...");
	        			}
	        			String packetToString = new String(payloadBuffer);
	        			System.out.println(packetToString.toUpperCase()); //TODO: Send Packet
	        			++packetIndex;
	        			byteArrayIndex = 0;
	        			payloadBuffer = null;
	        			payloadBuffer = new byte[MAX_PAYLOAD_SIZE];
	        			payloadBuffer[byteArrayIndex] = byteArray[currByte];
	        			++byteArrayIndex;
	        		}
	        	}
	        }
	        catch (IOException error)
	        {
	        	System.out.print(error);
	        }
		}
		else
		{
			System.out.println("You must specify the following: hostname, port, file...");
			System.exit(1);
		}
	}
	private static byte[] buildPacketHeader(int _sequence, int _eof)
	{
		byte[] packetHeader = new byte[MAX_HEADER_SIZE];
		packetHeader[0] = (byte) _sequence;
		packetHeader[1] = (byte) _eof;
		return packetHeader;
	}
}
