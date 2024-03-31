// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// Zakariyya Chawdhury
// 200024087
// zakariyya.chawdhury@city.ac.uk

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

// DO NOT EDIT starts
interface FullNodeInterface {
    public boolean listen(String ipAddress, int portNumber) throws IOException;
    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) throws IOException;
}
// DO NOT EDIT ends


public class FullNode implements FullNodeInterface {

    // 127.0.0.1:2244
    public boolean listen(String ipAddress, int portNumber) throws IOException {


	return true;
    }
    
    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) throws IOException {
	// Implement this!

	return;
    }
}
