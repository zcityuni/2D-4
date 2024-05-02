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
            System.out.println("\nTCPClient connecting to " + host.toString() + ":" + port + "\n" + name + "\n");
            clientSocket = new Socket(host, Integer.parseInt(port));
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String response = reader.readLine();
            System.out.println(fullnodeName[0] + " replied: " + response); // This will print out the fullnodes start message to us
            System.out.println("\nConnection established!\n");
            // Send the full node our start message
            Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());
            System.out.println("\nSending a START message to the server...\n");
            String myNode = "addf081@city.ac.uk:TempNodeZ123";
            writer.write("START 1 " + myNode + "\n");
            writer.flush();
            System.out.println("Sending Message:\nSTART 1 " + myNode + "\n");
            System.out.println("\n====START message sent!====\n");
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
            System.out.println("\n====PUT message sent!=====\n");
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
        boolean found = false;
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
                System.out.println("\n====GET message sent!====\n");
                writer.flush();
                System.out.println("\nWaiting for server response...\n");

                // Handling the response if NOPE
                String serverResponse = reader.readLine();
                System.out.println("Server says: " + serverResponse);
                if (serverResponse.equals("NOPE")) {
                    System.out.println("\nValue not found at this full node, asking for nearest nodes...");
                    String nodeHashID = hash(name);
                    writer.write("NEAREST? " + nodeHashID + "\n");
                    writer.flush();
                    System.out.println("NEAREST? " + nodeHashID + "\n");
                    System.out.println("====NEAREST message sent!====");

                    // Read and print out the response from nearest command which should have list of nodes
                    System.out.println("Server replied:");
                    String responseLine;
                    String currentName = null;
                    int nodesCount = 0;
                    while ((responseLine = reader.readLine()) != null || found) {
                        if (responseLine.startsWith("NODES")) {
                            nodesCount = Integer.parseInt(responseLine.split(" ")[1]);
                            System.out.println("\nSending a GET to each nearest full node\n");
                            System.out.println("Remaining Nodes to request: " + nodesCount);
                            continue;
                        }

                        if (nodesCount < 1) {
                            break;
                        } else {
                            currentName = responseLine;
                            String ipAddress = responseLine;
                            System.out.println("Name: " + currentName);
                            System.out.println("IP Address: " + ipAddress);
                            this.start(currentName, ipAddress);
                            System.out.println("Sending message:\n" + message);
                            writer.write(message);
                            System.out.println("====GET message sent!====");
                            writer.flush();
                            System.out.println("Waiting for server response...");
                            String eachResponse = reader.readLine();
                            if (eachResponse.startsWith("VALUE")) {
                                found = true;
                                System.out.println("\n Server Says:\n");
                                StringBuilder response = new StringBuilder();
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    response.append(line).append("\n");
                                }
                                System.out.println(response.toString());
                                return response.toString();
                            }
                            currentName = null;
                            nodesCount--;
                        }
                    }
                } else if(serverResponse.startsWith("VALUE")){
                    found = true;
                    System.out.println("\n Server Says:\n");
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
        return null;
    }

    public String hash(String nodeName) throws Exception {
        byte[] hashBytes = HashID.computeHashID(nodeName + "\n");
        StringBuilder convert = new StringBuilder();
        for(byte b: hashBytes){
            String hex = Integer.toHexString(0xff & b);
            if(hex.length() == 1){
                convert.append("0");
            }
            convert.append(hex);
        }
        return convert.toString();
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
