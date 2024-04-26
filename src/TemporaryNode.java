// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// Zakariyya Chawdhury
// 200024087
// zakariyya.chawdhury@city.ac.uk

import java.io.*;
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

            // Send the full node our start message
            Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());
            System.out.println("\nSending a START message to the server...\n");
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
            System.out.println("\nSending a PUT message to the server...\n");
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
            int keyLength = key.length();

            if(keyLength >= 1){
                System.out.println("\nSending a GET message to the server...\n");
                String[] keyLine = key.split(" ");
                writer.write("GET? " + keyLength  + "\n"); // First part of GET
                for(int i = 0; i < keyLength; i++){ // Each line of the key
                    writer.write(keyLine + "\n");
                }
                String message = writer.toString();
                System.out.println(message);
                writer.flush();

                // Handling the response if NOPE
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                if(reader.readLine().equals("NOPE")){
                    System.out.println("\nValue not found at this full node, asking for nearest nodes...\n");
                    String nodeHashID = hash(name);
                    writer.write("NEAREST? " + nodeHashID);
                    writer.flush();

                    // Read the response from nearest command which should have list of nodes
                    StringBuilder response = new StringBuilder();
                    String line;
                    while((line = reader.readLine()) != null){
                        response.append(line);
                    }
                    System.out.println(response.toString());
                    // implement for loop to ask GET for each of those returned nodes
                    return "NOPE";
                }
                else{
                    // the response is valid
                    System.out.println("\n RESPONSE VALID!\n");
                    StringBuilder response = new StringBuilder();
                    String line;
                    while((line = reader.readLine()) != null){
                        response.append(line);
                    }
                    System.out.println(response.toString());
                    return response.toString();
                }

            } else{ // If key length is less than 1
                end("GET KeyLength error");
                throw new IOException("Key length must be at least 1");
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
        String convert = Arrays.toString(HashID.computeHashID(nodeName));
        System.out.println(convert);
        return convert;
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
