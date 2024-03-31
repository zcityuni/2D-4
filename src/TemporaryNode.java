// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// Zakariyya Chawdhury
// 200024087
// zakariyya.chawdhury@city.ac.uk

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.UnknownHostException;

// DO NOT EDIT starts
interface TemporaryNodeInterface {
    public boolean start(String startingNodeName, String startingNodeAddress) throws IOException;
    public boolean store(String key, String value);
    public String get(String key);
}
// DO NOT EDIT ends


public class TemporaryNode implements TemporaryNodeInterface {
    // martin.brain@city.ac.uk:MyCoolImplementation,1.41,test-node-2
    public boolean start(String startingNodeName, String startingNodeAddress) throws IOException {
        String[] name_parts = startingNodeName.split(":");
        String[] address_parts = startingNodeAddress.split(":");

        String IPAddressString = address_parts[0];
        String port = address_parts[1];
        InetAddress host = InetAddress.getByName(IPAddressString);

        System.out.println("TCPClient connecting to " + host.toString() + ":" + port);
        Socket clientSocket = new Socket(host, Integer.parseInt(port));

        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());

        System.out.println("Sending a message to the server");
        String version = "TempNodeZ123";
        String sender = "zakariyya.chawdhury@city.ac.uk";
        writer.write("START 1" + sender + ":" + version);
        writer.flush();

        // We can read what the server has said
        String response = reader.readLine();
        System.out.println("The server said : " + response);

        // Close down the connection
        clientSocket.close();
	return true;
    }

    public boolean store(String key, String value) {
	// Implement this!
	// Return true if the store worked
	// Return false if the store failed
	return true;
    }

    public String get(String key) {
	// Implement this!
	// Return the string if the get worked
	// Return null if it didn't
	return "Not implemented";
    }
}
