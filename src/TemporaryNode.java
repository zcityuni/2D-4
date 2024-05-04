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
    String IPAddr;
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
        IPAddr = startingNodeAddress;
        port = fullnodeAddr[1];
        host = InetAddress.getByName(IPAddressString);

        try{
            System.out.println("\nTCPClient connecting to " + host.toString() + ":" + port + "\n" + name + "\n");
            clientSocket = new Socket(host, Integer.parseInt(port));
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String response = reader.readLine();
            System.out.println(fullnodeName[0] + " replied: " + response); // This will print out the fullnodes start message to us

            if(response.startsWith("START")){
                System.out.println("Connection established!");
                // Send the full node our start message
                Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());
                System.out.println("\nSending a START message to the server...\n");
                String myNode = "addf081@city.ac.uk:TempNodeZ123";
                writer.write("START 1 " + myNode + "\n");
                writer.flush();
                System.out.println("START 1 " + myNode);
                System.out.println("====START message sent!====\n");
                return true;
            } else{
                end("Invalid connection");
            }
        } catch (SocketException e){
            System.out.println(e.toString());
            end("START failed");
            return false;
        }
        return false;
    }

    public boolean store(String key, String value) throws IOException {
	// Implement this!
	// Return true if the store worked
	// Return false if the store failed
        boolean stored = false;
        try{
            Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String[] keyLines = key.split("\\n");
            String[] valueLines = value.split("\\n");
            int numLinesOfKey = keyLines.length;
            int numLinesOfValue = valueLines.length;

            if (numLinesOfKey < 1 || numLinesOfValue < 1) {
                end("PUT numLines error");
                throw new IOException("Number of lines in key or value must be at least one");
            }

            String message = "PUT? " + numLinesOfKey + " " + numLinesOfValue + "\n";
            for (String line : keyLines) {
                message += line + "\n";
            }
            for (String line : valueLines) {
                message += line + "\n";
            }

            System.out.println("\nSending a PUT? message to the server...\n" + message);
            writer.write(message);
            writer.flush();
            System.out.println("====PUT message sent!=====\n");

            // Handling the response
            String serverResponse = reader.readLine();
            System.out.println("Server replied: " + serverResponse);
            if(serverResponse.startsWith("SUCCESS")){
                return true;
            }
            else if(serverResponse.startsWith("FAILED")){ // Try to store in node closest to hash
                System.out.println("\nValue could not be stored here, checking closest nodes to hashed key...");
                String nodeHashID = hash(key);
                writer.write("NEAREST? " + nodeHashID + "\n");
                writer.flush();
                System.out.println("NEAREST? " + nodeHashID + "\n");
                System.out.println("====NEAREST message sent!====\n");

                // Read the response and store it in a string
                StringBuilder nearestResponse = new StringBuilder();
                String responseLine;
                while ((responseLine = reader.readLine()) != null) {
                    nearestResponse.append(responseLine).append("\n");
                }

                // Split the response string of nearest command
                String nearestResponseString = nearestResponse.toString();
                String[] responseLines = nearestResponseString.split("\\r?\\n");

                int nodesCount = 0; // so we know when to stop
                String currentName = null;
                String currentAddress = null;

                // For each line in the NEAREST? response extract the names and addresses
                for (String nearestResponseLine : responseLines) {
                    // Skip lines that are not necessary
                    if (nearestResponseLine.startsWith("NODES")) {
                        nodesCount = Integer.parseInt(nearestResponseLine.split(" ")[1]);
                        System.out.println(nodesCount + " Full nodes found, sending each one a PUT?");
                        continue;
                    }
                    if (nodesCount < 1) {
                        break; // Break out of the loop if we have parsed and acted on all nodes
                    }
                    //Parse each of the names and addresses to connect to and send a PUT?
                    if (currentName == null) {
                        currentName = nearestResponseLine;
                        System.out.println("Name: " + currentName);
                    } else {
                        currentAddress = nearestResponseLine;
                        System.out.println("IP Address: " + currentAddress);
                        // Send the start command to connect then send a GET?
                        this.start(currentName, currentAddress); // Connect to the node
                        System.out.println("\nSending a PUT? message to the server...\n" + message);
                        writer.write(message);
                        writer.flush();
                        System.out.println("====PUT message sent!=====\n");

                        System.out.println("Server replied: " + serverResponse);
                        if(serverResponse.startsWith("SUCCESS")){
                            stored = true;
                            return true;
                        } else if (responseLine.startsWith("FAILED")) {
                            System.out.println("Server replied: " + responseLine);
                            System.out.println("Value could not be stored at this node.");
                        } else {
                            System.out.println("Server replied: " + responseLine);
                            System.out.println("Wrong PUT? format?");
                        }
                        // Reset name and IP for the next node to parse and connect to and decrement count
                        currentName = null;
                        currentAddress = null;
                        nodesCount--;
                    }
                }
            }
            else{
                System.out.println("Invalid response, check request format?");
                end("PUT FAILED");
            }
        } catch (IOException e) {
            System.out.println(e.toString());
            end("PUT FAILED");
            return false;
        } catch (Exception e) {
            end("HASHING ID FAILED");
            throw new RuntimeException(e);
        }
        return false;
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

            if(numLines < 1){
                end("GET numLines error");
                throw new IOException("Number of lines must be at least 1");
            }

            String message = "GET? " + numLines + "\n";
            for (String line : keyLines) {
                message += line + "\n";
            }
            System.out.println("Sending a GET? message to the server...\n" + message + "test");
            writer.write(message);
            System.out.println("====GET message sent!====\n");
            writer.flush();
            System.out.println("\nWaiting for server response...\n");

            // Handling the response if NOPE
            String serverResponse = reader.readLine();
            System.out.println("Server replied: " + serverResponse);
            if (serverResponse.equals("NOPE")) {
                System.out.println("\nValue not found at this full node, asking for nearest nodes...");
                String nodeHashID = hash(name);
                writer.write("NEAREST? " + nodeHashID + "\n");
                writer.flush();
                System.out.println("NEAREST? " + nodeHashID + "\n");
                System.out.println("====NEAREST message sent!====\n");

                // Read the response and store it in a string
                StringBuilder nearestResponse = new StringBuilder();
                String responseLine;
                while ((responseLine = reader.readLine()) != null) {
                    nearestResponse.append(responseLine).append("\n");
                }

                // Split the response string of nearest command
                String nearestResponseString = nearestResponse.toString();
                System.out.println(nearestResponseString);
                String[] responseLines = nearestResponseString.split("\\n");
                int nodesCount = 0; // so we know when to stop
                String currentName = null;
                String currentAddress = null;

                // For each line in the NEAREST? response extract the names and addresses
                for (String nearestResponseLine : responseLines) {
                    // Skip lines that are not necessary
                    if (nearestResponseLine.startsWith("NODES")) {
                        nodesCount = Integer.parseInt(nearestResponseLine.split(" ")[1]);
                        System.out.println(nodesCount + " Full nodes found, sending each one a GET?");
                        continue;
                    }
                    if (nearestResponseLine.startsWith(name)) {
                        System.out.println("Skipping the same node we are connected to...");
                        continue;
                    }
                    if (nearestResponseLine.startsWith(IPAddr)) {
                        nodesCount--;
                        System.out.println("Decreasing remaining node count to: " + nodesCount + "\n");
                        continue;
                    }

                    if (nodesCount < 1 || found) {
                        break; // Break out of the loop if we have parsed and acted on all nodes
                    }
                    //Parse each of the names and addresses to connect to and send a GET?
                    if (currentName == null) {
                        currentName = nearestResponseLine;
                        System.out.println("Name: " + currentName);
                    }
                    else {
                        currentAddress = nearestResponseLine;
                        System.out.println("IP Address: " + currentAddress);
                        // Send the start command to connect then send a GET?
                        this.start(currentName, currentAddress); // Connect to the node
                        System.out.println("Sending a GET? message to the server...\n" + message + "test");
                        writer.write(message);
                        System.out.println("====GET message sent!====\n");
                        writer.flush();
                        System.out.println("\nWaiting for server response...\n");

                        if (responseLine.startsWith("VALUE")) { // This time we read from the main stream
                            found = true;
                            System.out.println("Server replied: " + responseLine);
                            StringBuilder response = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line).append("\n");
                            }
                            System.out.println(response.toString());
                            return response.toString();
                        }
                        else if (responseLine.startsWith("NOPE")) {
                            System.out.println("Server replied: " + responseLine);
                            System.out.println("Value was not found at this node.");
                        }
                        else{
                            System.out.println("Server replied: " + responseLine);
                            System.out.println("Wrong GET? format?");
                        }
                        // Reset name and IP for the next node to parse and connect to and decrement count
                        currentName = null;
                        currentAddress = null;
                        nodesCount--;
                    }
                }
            }
            else if(serverResponse.startsWith("VALUE")){
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
        } catch(IOException e){
            System.out.println(e.toString());
            end("GET FAILED");
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
            end("ECHO FAILED");
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
