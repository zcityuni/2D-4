// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// Zakariyya Chawdhury
// 200024087
// zakariyya.chawdhury@city.ac.uk

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.Arrays;

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
    String name;
    Socket clientSocket;

    public TemporaryNode() throws IOException {
    }

    public boolean start(String startingNodeName, String startingNodeAddress) throws IOException {

        // These are the details of the node we are connecting to
        name = startingNodeName;
        String[] fullnodeName = startingNodeName.split(":");
        String[] fullnodeAddr = startingNodeAddress.split(":");

        String IPAddressString = fullnodeAddr[0];
        port = fullnodeAddr[1];
        InetAddress host = InetAddress.getByName(IPAddressString);

        try{
            System.out.println("\nTCPClient connecting to " + host.toString() + ":" + port + "\n");
            clientSocket = new Socket(host, Integer.parseInt(port));
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String response = reader.readLine();
            System.out.println(fullnodeName[0] + " says: " + response); // This will print out the fullnodes start message to us
            System.out.println("\n Connection established!\n");
            // Send the full node our start message
            Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());
            System.out.println("\nSending a START message to the server...\n");
            String myNode = "addf081@city.ac.uk:TempNodeZ123";
            writer.write("START 1 " + myNode + "\n");
            writer.flush();
            System.out.println("\n START message sent!\n");
            System.out.println("START 1 " + myNode + "\n");
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
            System.out.println("\nSending a PUT message to the server...\n");
            int keyLength = key.length();
            int valLength = value.length();
            writer.write("PUT?  " + keyLength + " " + valLength + "\n");
            writer.flush();
            System.out.println("\n PUT message sent!\n");
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
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String[] keyLines = key.split("\\n");
            int numLines = keyLines.length;

            if (numLines >= 1) {
                String message = "GET? " + numLines + "\n";
                for (String line : keyLines) {
                    message += line + "\n";
                }
                System.out.println("Sending message:\n" + message);
                writer.write(message);
                System.out.println("\n GET message sent!\n");
                writer.flush();
                System.out.println("\n Waiting for server response...\n");

                // Handling the response if NOPE
                String serverResponse = reader.readLine();
                System.out.println("Server says: " + reader.readLine());
                if (serverResponse.equals("NOPE")) {
                    System.out.println("\nValue not found at this full node, asking for nearest nodes...\n");
                    String nodeHashID = hash(name);
                    writer.write("NEAREST? " + nodeHashID + "\n");
                    writer.flush();
                    System.out.println("\n NEAREST message sent!\n");
                    System.out.println("NEAREST?" + nodeHashID + "\n");

                    // Read and print out the response from nearest command which should have list of nodes
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line).append("\n");
                    }
                    System.out.println(response.toString());
                    // implement for loop to ask GET for each of those returned nodes

                    return "NOPE";
                } else{
                    // the response is valid
                    System.out.println("\n RESPONSE VALID!\n");
                    // print out the response (the value stored for that key)
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line).append("\n");
                    }
                    System.out.println(response.toString());
                    return response.toString();
                }

            } else{ // If numLines is less than 1
                end("GET numLines error");
                throw new IOException("Number of lines must be at least 1");
            }

        } catch(IOException e){
            System.out.println(e.toString());
            end("GET failed");
            return null;
        } catch (Exception e) {
            end("HASHING ID FAILED");
            throw new RuntimeException(e);
        }
    }

    public String hash(String nodeName) throws Exception {
        byte[] hashBytes = HashID.computeHashID(nodeName + "\n");
        BigInteger hashBigInt = new BigInteger(1, hashBytes);
        String hexString = hashBigInt.toString(16);
        while (hexString.length() < 64) {
            hexString = "0" + hexString;
        }
        return hexString;
    }

    public boolean echo() throws IOException {
        try{
            Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());
            System.out.println("\nSending an ECHO message to the server...\n");
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
