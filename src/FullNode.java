// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// Zakariyya Chawdhury
// 200024087
// zakariyya.chawdhury@city.ac.uk

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

// DO NOT EDIT starts
interface FullNodeInterface {
    public boolean listen(String ipAddress, int portNumber) throws IOException;
    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) throws IOException;
}
// DO NOT EDIT ends


public class FullNode implements FullNodeInterface {
    private Map<String, String> valueMap = new HashMap<>();
    private Map<String, String> networkMap = new HashMap<>();
    private String selfName;
    private String selfAddress;
    private int selfPort;
    private ServerSocket serverSocket;
    private Socket selfClient;

    // Details of other servers we connect to
    private String port;
    private InetAddress host;
    private String IPAddr;
    private String name;

    // 127.0.0.1:2244
    public boolean listen(String ipAddress, int portNumber) throws IOException {
        // this is to open a server to listen for connections from other nodes
        final Socket[] clientSocket = new Socket[1];

        try {
            // open a new server socket to allow connections
            selfPort = portNumber;
            selfAddress = ipAddress + ":" + selfPort;
            serverSocket = new ServerSocket(selfPort);
            System.out.println("Opening server: " + ipAddress + " listening on port " + selfPort + "\n");

            // multithread to allow multiple connections and handle each one at same time
            while (true) {
                try {
                    clientSocket[0] = serverSocket.accept(); // Assign to array element
                    new Thread(() -> {
                        System.out.println("Connected to: " + clientSocket[0].getInetAddress().toString());
                        try {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket[0].getInputStream()));
                            Writer writer = new OutputStreamWriter(clientSocket[0].getOutputStream());
                            System.out.println("\nSending a START message to the server...\n");
                            selfName = "addf081@city.ac.uk:FullNodeZ123";
                            writer.write("START 1 " + selfName + "\n");
                            writer.flush();
                            System.out.println("START 1 " + selfName);
                            System.out.println("====START message sent!====\n");

                            String response = reader.readLine();
                            if(response.startsWith("START")){
                                System.out.println("Connection established!");
                            } else{
                                end("Invalid connection", clientSocket[0]);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        // repond to commmands from clients
                        try {
                            processClientRequests(clientSocket[0]);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).start();
                    return true;
                } catch (IOException e) {
                    System.out.println("Failed to accept client connection");
                    end("Connection failed", clientSocket[0]);
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to open server socket");
            if (clientSocket[0] != null) {
                clientSocket[0].close();
            }
            serverSocket.close();
            return false;
        }
    }

    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) throws IOException {
        // connect to the network and notify
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
            selfClient = new Socket(host, Integer.parseInt(port));
            BufferedReader reader = new BufferedReader(new InputStreamReader(selfClient.getInputStream()));
            Writer writer = new OutputStreamWriter(selfClient.getOutputStream());
            String response = reader.readLine();
            System.out.println(fullnodeName[0] + " replied: " + response); // This will print out the fullnodes start message to us

            if(response.startsWith("START")){
                System.out.println("Connection established!");
                // Send the full node our start message
                System.out.println("\nSending a START message to the server...\n");
                selfName = "addf081@city.ac.uk:FullNodeZ123";
                writer.write("START 1 " + selfName + "\n");
                writer.flush();
                System.out.println("START 1 " + selfName);
                System.out.println("====START message sent!====\n");
            } else{
                end("Invalid connection", selfClient);
            }

            writer.write("NOTIFY? \n");
            writer.write(selfName + " \n");
            writer.write(selfAddress + "\n");
            response = reader.readLine();
            System.out.println("Server replied: " + response);

        } catch (IOException e){
            System.out.println(e.toString());
            end("START failed", selfClient);
        }
    }

    public void processClientRequests(Socket connectedClient) throws IOException {
        // Handle the requests sent from other nodes here
        BufferedReader reader = new BufferedReader(new InputStreamReader(connectedClient.getInputStream()));
        Writer writer = new OutputStreamWriter(connectedClient.getOutputStream());
        String request = reader.readLine();
        String response;
        System.out.println("Server request: " + request);

        if(request.startsWith("ECHO?")){
            writer.write("OHCE\n");
            writer.flush();
            response = "OHCE\n";
        }
        else if(request.startsWith("GET?")){
            // handle a GET request by looking up the key in our table

        }
        else if(request.startsWith("PUT?")){
            // handle a PUT request by seeing if we are close enough to keys hashID store it
        }
        else if(request.startsWith("NEAREST?")){
            // respond with 3 closest nodes to tha requesters provided hashID

        }
        else if(request.startsWith("NOTIFY?")){
            // record the name given in our network map and respond with notified if appropriate (passive mapping)

        }
        else if(request.startsWith("END")){
            end("Client ended the communication", connectedClient);
            System.out.println("Client ended the communication");
        }
        else{
            end("Invalid command", connectedClient);
            System.out.println("Client sent an invalid command, communication has been closed.");
        }

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
            selfClient = new Socket(host, Integer.parseInt(port));
            BufferedReader reader = new BufferedReader(new InputStreamReader(selfClient.getInputStream()));
            String response = reader.readLine();
            System.out.println(fullnodeName[0] + " replied: " + response); // This will print out the fullnodes start message to us

            if(response.startsWith("START")){
                System.out.println("Connection established!");
                // Send the full node our start message
                Writer writer = new OutputStreamWriter(selfClient.getOutputStream());
                System.out.println("\nSending a START message to the server...\n");
                String myNode = "addf081@city.ac.uk:FullNodeZ123";
                writer.write("START 1 " + myNode + "\n");
                writer.flush();
                System.out.println("START 1 " + myNode);
                System.out.println("====START message sent!====\n");
                return true;
            } else{
                end("Invalid connection", selfClient);
            }
        } catch (SocketException e){
            System.out.println(e.toString());
            end("START failed", selfClient);
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
            Writer writer = new OutputStreamWriter(selfClient.getOutputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(selfClient.getInputStream()));

            String[] keyLines = key.split("\\n");
            String[] valueLines = value.split("\\n");
            int numLinesOfKey = keyLines.length;
            int numLinesOfValue = valueLines.length;

            if (numLinesOfKey < 1 || numLinesOfValue < 1) {
                end("PUT numLines error", selfClient);
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
                for (int i = 0; i < 7; i++){
                    responseLine = reader.readLine();
                    nearestResponse.append(responseLine).append("\n");
                    if(responseLine.isBlank()){
                        break;
                    }
                }
                String nearestResponseString = nearestResponse.toString();
                System.out.println(nearestResponse.toString());
                System.out.println("Reached");

                // Split the response string of nearest command
                String[] responseLines = nearestResponseString.split("\\n");
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
                        } else if (serverResponse.startsWith("FAILED")) {
                            System.out.println("Server replied: " + serverResponse);
                            System.out.println("Value could not be stored at this node.");
                        } else {
                            System.out.println("Server replied: " + serverResponse);
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
                end("PUT FAILED", selfClient);
            }
        } catch (IOException e) {
            System.out.println(e.toString());
            end("PUT FAILED", selfClient);
            return false;
        } catch (Exception e) {
            end("HASHING ID FAILED", selfClient);
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
            Writer writer = new OutputStreamWriter(selfClient.getOutputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(selfClient.getInputStream()));
            String[] keyLines = key.split("\\n");
            int numLines = keyLines.length;

            if(numLines < 1){
                end("GET numLines error", selfClient);
                throw new IOException("Number of lines must be at least 1");
            }

            String message = "GET? " + numLines + "\n";
            for (String line : keyLines) {
                message += line + "\n";
            }
            System.out.println("Sending a GET? message to the server...\n" + message);
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
                for (int i = 0; i < 7; i++){
                    responseLine = reader.readLine();
                    nearestResponse.append(responseLine).append("\n");
                    if(responseLine.isBlank()){
                        break;
                    }
                }
                System.out.println("Server replied:");
                String nearestResponseString = nearestResponse.toString();
                System.out.println(nearestResponse.toString());

                // Split the response string of nearest command
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

                        if (serverResponse.startsWith("VALUE")) { // This time we read from the main stream
                            found = true;
                            System.out.println("Server replied: " + serverResponse);
                            String[] extractLineAmount = serverResponse.split(" ");
                            int lineAmount = Integer.parseInt(extractLineAmount[1]);
                            StringBuilder valueResponse = new StringBuilder();
                            String valueResponseLine;
                            for (int i = 0; i < lineAmount; i++){
                                valueResponseLine = reader.readLine();
                                valueResponse.append(valueResponseLine).append("\n");
                                if(valueResponseLine.isBlank()){
                                    break;
                                }
                            }
                            //System.out.println(valueResponse.toString());
                            return valueResponse.toString();
                        }
                        else if (serverResponse.startsWith("NOPE")) {
                            System.out.println("Server replied: " + serverResponse);
                            System.out.println("Value was not found at this node.");
                        }
                        else{
                            System.out.println("Server replied: " + serverResponse);
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
                String[] extractLineAmount = serverResponse.split(" ");
                int lineAmount = Integer.parseInt(extractLineAmount[1]);
                StringBuilder valueResponse = new StringBuilder();
                String valueResponseLine;
                for (int i = 0; i < lineAmount; i++){
                    valueResponseLine = reader.readLine();
                    valueResponse.append(valueResponseLine).append("\n");
                    if(valueResponseLine.isBlank()){
                        break;
                    }
                }
                //System.out.println(valueResponse.toString());
                return valueResponse.toString();
            }
        } catch(IOException e){
            System.out.println(e.toString());
            end("GET FAILED", selfClient);
            return null;
        } catch (Exception e) {
            end("HASHING ID FAILED", selfClient);
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
            Writer writer = new OutputStreamWriter(selfClient.getOutputStream());
            System.out.println("\nSending an ECHO message to the server...\n");
            writer.write("ECHO?\n");
            writer.flush();
            return true;
        } catch(IOException e){
            System.out.println(e.toString());
            end("ECHO FAILED", selfClient);
            return false;
        }
    }
    public void end(String reason, Socket clientSocket) throws IOException {
        try (OutputStream outputStream = clientSocket.getOutputStream()) {
            outputStream.write(("END " + reason + "\n").getBytes());
        } catch (IOException e) {
            System.out.println(e.toString());
        } finally {
            clientSocket.close();
        }
    }
}
