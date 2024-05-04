// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// Zakariyya Chawdhury
// 200024087
// zakariyya.chawdhury@city.ac.uk

import java.io.*;
import java.net.*;
import java.util.*;

// DO NOT EDIT starts
interface FullNodeInterface {
    public boolean listen(String ipAddress, int portNumber) throws IOException;
    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) throws IOException;
}
// DO NOT EDIT ends


public class FullNode implements FullNodeInterface {
    private Map<String, String> valueMap;
    private Map<Integer, List<String[]>> networkMap; // Distance -> List of nodes at that distance
    private String selfName;
    private String selfAddress;
    private int selfPort;
    private ServerSocket serverSocket;
    private Socket connectedClient;
    private Socket selfClient;

    // Details of other servers we connect to
    private String port;
    private InetAddress host;
    private String IPAddr;
    private String name;
    private List<String[]> nearestNodes;
    public FullNode(){
        networkMap = new HashMap<>();
        valueMap = new HashMap<>();
        selfName = "addf081@city.ac.uk:FullNodeZ123";
    }
    public boolean listen(String ipAddress, int portNumber) throws IOException {
        // this is to open a server to listen for connections from other nodes
        try {
            // open a new server socket to allow connections
            selfPort = portNumber;
            selfAddress = ipAddress + ":" + selfPort;
            serverSocket = new ServerSocket(selfPort);
            System.out.println("Opening server: " + ipAddress + " listening on port " + selfPort + "\n");
            // add self to the network map with a distance of 0, double arr used because List.of treats as one
            networkMap.put(0, List.of(new String[][] {new String[] {selfName, selfAddress}}));

            // multithread to allow multiple connections and handle each one at same time
            new Thread(() -> {
                while (true) {
                    try {
                        connectedClient = serverSocket.accept();
                        System.out.println("\nClient has connected: " + connectedClient.getInetAddress());
                        new Thread(() -> {
                            try {
                                BufferedReader reader = new BufferedReader(new InputStreamReader(connectedClient.getInputStream()));
                                Writer writer = new OutputStreamWriter(connectedClient.getOutputStream());
                                System.out.println("\nSending a START message to the server...\n");
                                writer.write("START 1 " + selfName + "\n");
                                writer.flush();
                                System.out.println("START 1 " + selfName);
                                System.out.println("====START message sent!====\n");

                                String response = reader.readLine();
                                if(response.startsWith("START")){
                                    System.out.println("Connection established!");
                                    processClientRequests(connectedClient);
                                } else{
                                    end("Invalid connection", connectedClient);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }).start();
                    } catch (IOException e) {
                        System.out.println("Failed to accept client connection");
                        try {
                            end("Connection failed", connectedClient);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            }).start();
            return true;
        } catch (IOException e) {
            System.out.println("Failed to open server socket");
            if (connectedClient != null) {
                connectedClient.close();
            }
            serverSocket.close();
            return false;
        }
    }

    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) throws IOException {
        // connect to the network and notify (Active mapping)
        // these are the details of the node we are connecting to
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

            System.out.println("Sending a NOTIFY? message to add myself to the network map...");
            String notifyMessage = "NOTIFY? \n" + selfName + "\n" + selfAddress + "\n";
            writer.write(notifyMessage);
            writer.flush();
            response = reader.readLine();
            System.out.println("Server replied: " + response);

            System.out.println("Sending a nearest request to actively map nearby nodes...\n");
            String nodeHashID = hash(selfName);
            writer.write("NEAREST? " + nodeHashID + "\n");
            writer.flush();
            System.out.println("NEAREST? " + nodeHashID + "\n");
            System.out.println("====NEAREST message sent!====\n");

            // read the response and store it in a string
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

            // split the response string of nearest command
            String[] responseLines = nearestResponseString.split("\\n");
            int nodesCount = 0; // so we know when to stop
            String currentName = null;
            String currentAddress = null;
            // details of nodes to add to my own network map
            String nameToSave = null;
            String IPAddrToSave = null;

            // for each line in the NEAREST? response extract the names and addresses
            for (String nearestResponseLine : responseLines) {
                if (nearestResponseLine.startsWith("NODES")) {
                    nodesCount = Integer.parseInt(nearestResponseLine.split(" ")[1]);
                    System.out.println(nodesCount + " Full nodes found, sending each one a NOTIFY?");
                    continue;
                }
                if (nearestResponseLine.startsWith(selfName)) {
                    System.out.println("Skipping the same node we are connected to...");
                    continue;
                }
                if (nearestResponseLine.startsWith(selfAddress)) {
                    nodesCount--;
                    System.out.println("Decreasing remaining node count to: " + nodesCount + "\n");
                    continue;
                }

                if (nodesCount < 1) {
                    break; // break out of the loop if we have parsed and acted on all nodes
                }
                // parse each of the names and addresses to connect to and send a NOTIFY?
                if (currentName == null) {
                    currentName = nearestResponseLine;
                    System.out.println("Name: " + currentName);
                    nameToSave = currentName;
                } else {
                    currentAddress = nearestResponseLine;
                    IPAddrToSave = currentAddress;
                    System.out.println("IP Address: " + currentAddress);
                    this.start(currentName, currentAddress); // connect to the node and send NOTIFY?
                    System.out.println("Sending a NOTIFY? message to add myself to the network map...");
                    writer.write(notifyMessage);
                    writer.flush();
                    System.out.println("Server replied: " + response);

                    // get distance by comparing hashID of our name to hashID of their name
                    String hashID = hash(nameToSave);
                    int distance = calculateHashIDDistance(hash(selfName), hashID);
                    // store in our map at that distance
                    networkMap.put(distance, List.of(new String[][] {new String[] {nameToSave, IPAddrToSave}}));
                    // reset name and IP for the next node to parse and connect to and decrement count
                    currentName = null;
                    currentAddress = null;
                    nameToSave = null;
                    IPAddrToSave = null;
                    nodesCount--;
                }
            }
        } catch (IOException e){
            System.out.println(e.toString());
            end("START failed", selfClient);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void processClientRequests(Socket connectedClient) throws Exception {
        // handle the requests sent from other nodes here
        BufferedReader reader = new BufferedReader(new InputStreamReader(connectedClient.getInputStream()));
        Writer writer = new OutputStreamWriter(connectedClient.getOutputStream());
        String request = reader.readLine();
        String response;
        System.out.println("Server request: " + request);

        if(request.startsWith("ECHO?")){
            writer.write("OHCE\n");
            writer.flush();
            response = "OHCE\n";
            System.out.println("Echoed back!");
        }
        else if(request.startsWith("GET?")){
            // handle a GET? request by looking up the key in our table
            if(valueMap.size() < 1){
                writer.write("NOPE\n");
                writer.flush();
            }

            String[] getResponse = request.split("\\n");
            String firstLine = getResponse[0];
            String[] splitFirstLine = firstLine.split(" ");
            int keyLength = Integer.parseInt(splitFirstLine[1]);

            StringBuilder key = new StringBuilder();
            String keyResponseLine;
            for (int i = 0; i < keyLength; i++){
                keyResponseLine = reader.readLine();
                key.append(keyResponseLine).append("\n");
            }

            String value = valueMap.get(key.toString());
            if(value == null){
                writer.write("NOPE\n");
                writer.flush();
            }
            else{
                String[] splitValue = value.split("\\n");
                int valueLength = splitValue.length;

                StringBuilder message = new StringBuilder();
                message.append("VALUE ").append(keyLength).append(" ").append(valueLength).append("\n");
                writer.write(message.toString());
                writer.flush();
                System.out.println("Sent back found value");
            }
        }
        else if(request.startsWith("PUT?")){
            // handle a PUT? request by seeing if we are close enough to keys hashID store it
            String[] putResponse = request.split("\\n");
            String firstLine = putResponse[0];
            String[] splitFirstLine = firstLine.split(" ");
            int keyLength = Integer.parseInt(splitFirstLine[1]);
            int valueLength = Integer.parseInt(splitFirstLine[2]);

            StringBuilder key = new StringBuilder();
            String keyResponseLine;
            for (int i = 0; i < keyLength; i++){
                keyResponseLine = reader.readLine();
                key.append(keyResponseLine).append("\n");
            }

            StringBuilder value = new StringBuilder();
            String valueResponseLine;
            for (int i = 0; i < valueLength; i++){
                valueResponseLine = reader.readLine();
                value.append(valueResponseLine).append("\n");
            }

            String hashID = hash(key.toString());
            List<String[]> nearestNodes = getClosestNodes(hashID);
            int count = 0; // if all nodes are checked and the for exits without breaking then none of the hashes match
            for (String[] node : nearestNodes) {
                String nodeHashID = hash(node[0]);
                if(hashID.equals(nodeHashID)){
                    writer.write("SUCCESS\n");
                    writer.flush();
                    valueMap.put(key.toString(), value.toString());
                    System.out.println("Stored item in value hashmap\n");
                    break;
                }
                count++;
            }
            if(count >= nearestNodes.size()){
                writer.write("FAILED\n");
                writer.flush();
            }
        }
        else if(request.startsWith("NEAREST?")){
            // respond with 3 closest nodes to tha requesters provided hashID
            String[] nearestResponse = request.split(" ");
            String hashID = nearestResponse[1];
            List<String[]> nearestNodes = getClosestNodes(hashID);
            nearestNodes.removeIf(node -> Arrays.asList(node).contains(null)); // remove null elements
            // print out the nearest nodes names and addresses for the hash
            for (String[] node : nearestNodes) {
                String nodeName = node[0];
                String nodeAddress = node[1];
                System.out.println("Node Name: " + nodeName);
                System.out.println("Node IP: " + nodeAddress);
                System.out.println();
            }
            // write it to the writer
        }
        else if(request.startsWith("NOTIFY?")){
            // record the name given in our network map and respond with notified if appropriate (passive mapping)
            String[] notifyResponse = request.split("\\n");
            String name = notifyResponse[1];
            String IPAddr = notifyResponse[2];
            String hashID = hash(notifyResponse[1]);
            // get distance by comparing hashID of our name to hashID of their name
            int distance = calculateHashIDDistance(hash(selfName), hashID);
            // store in our map at that distance
            networkMap.put(distance, List.of(new String[][] {new String[] {name, IPAddr}}));
            writer.write("NOTIFIED\n");
            writer.flush();
            System.out.println("Notified node and stored details");
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

    public int calculateHashIDDistance(String hashID1, String hashID2) {
        int distance = 0;
        for (int i = 0; i < 64; i++) {
            // compare the hash similarity one char at a time
            if (hashID1.charAt(i) != hashID2.charAt(i)) {
                distance = 256 - i;
                break;
            }
        }
        return distance;
    }

    public List<String[]> getClosestNodes(String givenHashID) throws Exception {
        String closestNodeName = null;
        String closestNodeIP = null;
        String secondClosestNodeName = null;
        String secondClosestNodeIP = null;
        String thirdClosestNodeName = null;
        String thirdClosestNodeIP = null;
        // init max distances to 256 as described in RFC
        int closestDistance = 256;
        int secondClosestDistance = 256;
        int thirdClosestDistance = 256;

        for (Map.Entry<Integer, List<String[]>> entry : networkMap.entrySet()) {
            List<String[]> nodes = entry.getValue();
            for (String[] node : nodes) {
                String nodeName = node[0];
                String nodeAddress = node[1];
                String nodeHashID = hash(nodeName);
                int distance = calculateHashIDDistance(nodeHashID, givenHashID);

                if (distance < closestDistance) {
                    thirdClosestDistance = secondClosestDistance;
                    thirdClosestNodeName = secondClosestNodeName;
                    thirdClosestNodeIP = secondClosestNodeIP;
                    secondClosestDistance = closestDistance;
                    secondClosestNodeName = closestNodeName;
                    secondClosestNodeIP = closestNodeIP;
                    closestDistance = distance;
                    closestNodeName = nodeName;
                    closestNodeIP = nodeAddress;
                } else if (distance < secondClosestDistance) {
                    thirdClosestDistance = secondClosestDistance;
                    thirdClosestNodeName = secondClosestNodeName;
                    thirdClosestNodeIP = secondClosestNodeIP;
                    secondClosestDistance = distance;
                    secondClosestNodeName = nodeName;
                    secondClosestNodeIP = nodeAddress;
                } else if (distance < thirdClosestDistance) {
                    thirdClosestDistance = distance;
                    thirdClosestNodeName = nodeName;
                    thirdClosestNodeIP = nodeAddress;
                }
            }
        }
        List<String[]> closestNodes = new ArrayList<>();
        closestNodes.add(new String[]{closestNodeName, closestNodeIP});
        closestNodes.add(new String[]{secondClosestNodeName, secondClosestNodeIP});
        closestNodes.add(new String[]{thirdClosestNodeName, thirdClosestNodeIP});
        return closestNodes;
    }

    public boolean start(String startingNodeName, String startingNodeAddress) throws IOException {
        // these are the details of the node we are connecting to
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
            System.out.println(fullnodeName[0] + " replied: " + response); // this will print out the fullnodes start message to us

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

            // handling the response
            String serverResponse = reader.readLine();
            System.out.println("Server replied: " + serverResponse);
            if(serverResponse.startsWith("SUCCESS")){
                return true;
            }
            else if(serverResponse.startsWith("FAILED")){ // try to store in node closest to hash
                System.out.println("\nValue could not be stored here, checking closest nodes to hashed key...");
                String nodeHashID = hash(key);
                writer.write("NEAREST? " + nodeHashID + "\n");
                writer.flush();
                System.out.println("NEAREST? " + nodeHashID + "\n");
                System.out.println("====NEAREST message sent!====\n");

                // read the response and store it in a string
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

                // split the response string of nearest command
                String[] responseLines = nearestResponseString.split("\\n");
                int nodesCount = 0; // so we know when to stop
                String currentName = null;
                String currentAddress = null;

                // for each line in the NEAREST? response extract the names and addresses
                for (String nearestResponseLine : responseLines) {
                    if (nearestResponseLine.startsWith("NODES")) {
                        nodesCount = Integer.parseInt(nearestResponseLine.split(" ")[1]);
                        System.out.println(nodesCount + " Full nodes found, sending each one a PUT?");
                        continue;
                    }
                    if (nodesCount < 1) {
                        break; // break out of the loop if we have parsed and acted on all nodes
                    }
                    // parse each of the names and addresses to connect to and send a PUT?
                    if (currentName == null) {
                        currentName = nearestResponseLine;
                        System.out.println("Name: " + currentName);
                    } else {
                        currentAddress = nearestResponseLine;
                        System.out.println("IP Address: " + currentAddress);
                        // send the start command to connect then send a GET?
                        this.start(currentName, currentAddress); // connect to the node
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
                        // reset name and IP for the next node to parse and connect to and decrement count
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

            // handling the response if NOPE
            String serverResponse = reader.readLine();
            System.out.println("Server replied: " + serverResponse);
            if (serverResponse.equals("NOPE")) {
                System.out.println("\nValue not found at this full node, asking for nearest nodes...");
                String nodeHashID = hash(name);
                writer.write("NEAREST? " + nodeHashID + "\n");
                writer.flush();
                System.out.println("NEAREST? " + nodeHashID + "\n");
                System.out.println("====NEAREST message sent!====\n");

                // read the response and store it in a string
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

                // split the response string of nearest command
                String[] responseLines = nearestResponseString.split("\\n");
                int nodesCount = 0; // so we know when to stop
                String currentName = null;
                String currentAddress = null;

                // for each line in the NEAREST? response extract the names and addresses
                for (String nearestResponseLine : responseLines) {
                    // skip lines that are not necessary
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
                        break; // break out of the loop if we have parsed and acted on all nodes
                    }
                    // parse each of the names and addresses to connect to and send a GET?
                    if (currentName == null) {
                        currentName = nearestResponseLine;
                        System.out.println("Name: " + currentName);
                    }
                    else {
                        currentAddress = nearestResponseLine;
                        System.out.println("IP Address: " + currentAddress);
                        // send the start command to connect then send a GET?
                        this.start(currentName, currentAddress); // Connect to the node
                        System.out.println("Sending a GET? message to the server...\n" + message + "test");
                        writer.write(message);
                        System.out.println("====GET message sent!====\n");
                        writer.flush();
                        System.out.println("\nWaiting for server response...\n");

                        if (serverResponse.startsWith("VALUE")) { // this time we read from the main stream
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
                        // reset name and IP for the next node to parse and connect to and decrement count
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
            BufferedReader reader = new BufferedReader(new InputStreamReader(selfClient.getInputStream()));
            Writer writer = new OutputStreamWriter(selfClient.getOutputStream());
            System.out.println("\nSending an ECHO message to the server...\n");
            writer.write("ECHO?\n");
            writer.flush();
            String response = reader.readLine();
            System.out.println("Server replied: " + response);
            return true;
        } catch(IOException e){
            System.out.println(e.toString());
            end("ECHO FAILED", selfClient);
            return false;
        }
    }
    public void sendNearest(String name) throws Exception {
        String hashID = hash(name);
        Writer writer = new OutputStreamWriter(selfClient.getOutputStream());
        writer.write("NEAREST? " + hashID);
        writer.flush();
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
