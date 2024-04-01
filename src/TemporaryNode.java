// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// Zakariyya Chawdhury
// 200024087
// zakariyya.chawdhury@city.ac.uk

import java.io.*;
import java.net.*;

// DO NOT EDIT starts
interface TemporaryNodeInterface {
    public boolean start(String startingNodeName, String startingNodeAddress) throws IOException;
    public boolean store(String key, String value) throws IOException;
    public String get(String key) throws IOException;
}
// DO NOT EDIT ends


public class TemporaryNode implements TemporaryNodeInterface {
    String port;
    InetAddress host;
    Socket clientSocket;

    public TemporaryNode() throws IOException {
    }

    public boolean start(String startingNodeName, String startingNodeAddress) throws IOException {

        // These are the details of the node we are connecting to
        String[] fullnodeName = startingNodeName.split(":");
        String[] fullnodeAddr = startingNodeAddress.split(":");

        String IPAddressString = fullnodeAddr[0];
        port = fullnodeAddr[1];
        InetAddress host = InetAddress.getByName(IPAddressString);

        try{
            System.out.println("TCPClient connecting to " + host.toString() + ":" + port);
            clientSocket = new Socket(host, Integer.parseInt(port));
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String response = reader.readLine();
            System.out.println(fullnodeName[0] + " says: " + response); // This will print out the fullnodes start message to us

            // Send the full node our start message
            Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());
            System.out.println("Sending a START message to the server");
            String myNode = "zakariyya.chawdhury@city.ac.uk:TempNodeZ123";
            writer.write("START 1 " + myNode + "\n");
            writer.flush();
            return true;
        } catch (SocketException e){
            System.out.println(e.toString());
            end("START failed");
            return false;
        }
    }

    public boolean store(String key, String value) throws IOException {
	// Implement this!
	// Return true if the store worked
	// Return false if the store failed
        try{
            Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());
            System.out.println("Sending a PUT message to the server");
            int keyLength = key.length();
            int valLength = value.length();
            writer.write("PUT?  " + keyLength + " " + valLength + "\n");
            writer.flush();
            return true;
        } catch(IOException e){
            System.out.println(e.toString());
            end("PUT failed");
            return false;
        }
    }

    public String get(String key) throws IOException {
	// Implement this!
	// Return the string if the get worked
	// Return null if it didn't
        try{
            Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());
            System.out.println("Sending a GET message to the server");
            int keyLength = key.length();
            String[] keyLine = key.split(" ");
            writer.write("GET? " + keyLength + " " + "\n");
            for(int i = 0; i < keyLength; i++){
                writer.write(keyLine + "\n");
            }
            String message = writer.toString();
            writer.flush();
            return message;
        } catch(IOException e){
            System.out.println(e.toString());
            end("GET failed");
            return null;
        }
    }

    public boolean echo() throws IOException {
        try{
            Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());
            System.out.println("Sending an ECHO message to the server");
            writer.write("ECHO?\n");
            writer.flush();
            return true;
        } catch(IOException e){
            System.out.println(e.toString());
            end("ECHO failed");
            return false;
        }
    }

    public void end(String reason) throws IOException {
        try{
            Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());
            writer.write("END " + reason + "\n");
            String message = writer.toString();
            writer.flush();
            clientSocket.close();
        } catch(IOException e){
            System.out.println(e.toString());
        }
    }
}
