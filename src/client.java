// ================== File Abstract ====================
// # 
// # Name: Carmine Valentino
// # File: client.java
// # Program: TCP/UDP Client-Server Socket Program
// # Date: April 19, 2015
// # Course: CS 544 - Computer Networking
// # University: Drexel University
// #
// ================== End Abstract ======================

import java.io.*;
import java.net.*;

public class client {
	
	// Static Variables Defined for Max Packet Size
	// Note: Message is comprised of a 16Byte Payload
	// And, a 2Byte Header
	private static final int MAX_PAYLOAD_SIZE = 16;
	private static final int MAX_HEADER_SIZE = 2;
	private static final int MAX_BUFFER_SIZE = MAX_PAYLOAD_SIZE + MAX_HEADER_SIZE;

	public static void main(String[] args) {
		// -- Declare Objects/Variables --
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

		// -------------------------------

		// Check for Startup Parameters
		if (args.length > 2)
		{	
			// Set Flags
			hostname = args[0];
			n_port = Integer.parseInt(args[1]);
			filename = args[2];
			File file = new File(filename);
			try
			{
				System.out.println("Attempting to connect to "+ hostname + ":" + n_port + " ...");
				// Attempt To Establish Socket Connection at Hostname:Port Specified At Run-time
				clientSocket = new Socket(hostname, n_port);
				System.out.println("Connection Established ...");
			}
			catch (IOException error)
			{
				//error.printStackTrace();
				System.out.println("Unable to Connect to " + hostname + ":" + n_port + "...");
				System.exit(1);
			}
			
			// Try Sending 117 Ack To Server
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
			// Try Getting Random UDP Data Port From Server
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
	        // Attempt to Close TCP Sockets
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
	        // Begin Message Sending Attempt
	        try
	        {
	        	// Declare Local Variables/Objects
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
	        	
	        	// The Last Packet Contains less than Max Payload Size
	        	if (fileSize % MAX_PAYLOAD_SIZE > 0)
	        	{
	        		// We Need to Send an Additional Packet
	        		numberOfPackets = (fileSize/MAX_PAYLOAD_SIZE) + 1;
	        	}
	        	else
	        	{
	        		numberOfPackets = fileSize/MAX_PAYLOAD_SIZE;
	        	}
	        	System.out.println("Number of Packets to Be Sent " + numberOfPackets + "...");
	        	System.out.println("Total Byte Size " + fileSize + "...");
	        	
	        	// Consider Each Byte in Our File
	        	for(int currByte = 0; currByte < file.length(); ++currByte)
	        	{
	        		// Check our Byte Array Index Counter to Ensure
	        		/// That we are generating a buffer within the
	        		// Max Payload Size enforced (16 bytes)
	        		if(byteArrayIndex < MAX_PAYLOAD_SIZE)
	        		{
	        			payloadBuffer[byteArrayIndex] = byteArray[currByte]; // Add Current Byte to Current Payload Buffer
	        			++byteArrayIndex; // Increment our Byte Index
	        			
	        			// *We determine that the last byte
	        			// Is equivalent to the file size less
	        			// The current byte in the file
	        			// This will be our final byte and therefore
	        			// We should prepare to severe connections*
	        			if((fileSize - currByte) == 1)
	        			{
		        			headerBuffer = buildPacketHeader(packetIndex, -1); // -1 Indicates End of File
		        			System.arraycopy(payloadBuffer, 0, packetBuffer, 0, MAX_PAYLOAD_SIZE); // Add our payload buffer to our outgoing buffer
		        			System.arraycopy(headerBuffer, 0, packetBuffer, MAX_PAYLOAD_SIZE, MAX_HEADER_SIZE);// Add our header buffer to our outgoing buffer
		        			try
		        			{
		        				_hostname = InetAddress.getByName(hostname); // Get InetAddres Type from String
		        				packetOutput = new DatagramPacket(packetBuffer, packetBuffer.length, _hostname, r_port); // Create a new Datagram Object with our buffer
		        				dataSocket.send(packetOutput); // Send Packet
		        				dataSocket.close(); //Last Packet, so Lets Close Connection on Client end
		        			}
		        			catch (IOException error)
		        			{
		        				System.out.println("Unable to Transmit Packet ...");
		        			}
		        			String packetToString = new String(payloadBuffer); // Print Packet Output
		        			System.out.println(packetToString.toUpperCase());
		        			++packetIndex; // Increment our Packet Index For Good Measure
	        			}
	        		}
	        		else // This is the Final Byte in Our Packet (But NOT the final Packet)
	        		{
	        			headerBuffer = buildPacketHeader(packetIndex, 0); // 0 Indicates More Packets to Come! (This is our "last packet" indicator)
	        			System.arraycopy(payloadBuffer, 0, packetBuffer, 0, MAX_PAYLOAD_SIZE); // Add our payload buffer to our outgoing buffer
	        			System.arraycopy(headerBuffer, 0, packetBuffer, MAX_PAYLOAD_SIZE, MAX_HEADER_SIZE); // Add our header buffer to our outgoing buffer
	        			try
	        			{
	        				_hostname = InetAddress.getByName(hostname);// Get InetAddres Type from String
	        				packetOutput = new DatagramPacket(packetBuffer, packetBuffer.length, _hostname, r_port);// Create a new Datagram Object with our buffer
	        				dataSocket = new DatagramSocket(); // Create Our Socket
	        				dataSocket.send(packetOutput);// Send Packet
	        				try // Attempt to get ack from server (For packet receipt)
	        				{
		        				incomingPacket = new byte[MAX_BUFFER_SIZE]; // Create incoming packet buffer
		        				packetInput = new DatagramPacket(incomingPacket, incomingPacket.length, _hostname, r_port); // Create Datagram Object with our buffer
		        				dataSocket.receive(packetInput); // Get Ack Back From Server, Acknowledging receipt of our packet
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
	        			System.out.println(packetToString.toUpperCase()); // Print our Packet
	        			++packetIndex; // Increment Packet Index To determine which packet we are to transmit next
	        			byteArrayIndex = 0; // Clear Byte Index [0-15]
	        			payloadBuffer = null; // Set Overall buffer to Null
	        			payloadBuffer = new byte[MAX_PAYLOAD_SIZE]; // Reallocate our Buffer
	        			payloadBuffer[byteArrayIndex] = byteArray[currByte]; // * Set First Byte for our Next Packet
	        			++byteArrayIndex; // Increment Byte Index
	        		}
	        	}
	        }
	        catch (IOException error)
	        {
	        	System.out.print(error);
	        }
		}
		else // Catch Erroneous Input Parameters
		{
			System.out.println("You must specify the following: hostname, port, file...");
			System.exit(1);
		}
	}
	// Constructs our Header Buffer for Each Packet
	private static byte[] buildPacketHeader(int _sequence, int _eof)
	{
		byte[] packetHeader = new byte[MAX_HEADER_SIZE];
		packetHeader[0] = (byte) _sequence; // Packet #
		packetHeader[1] = (byte) _eof; // End of File or Packet Indicator
		return packetHeader; 
	}
}
