// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// YOUR_NAME_GOES_HERE
// YOUR_STUDENT_ID_NUMBER_GOES_HERE
// YOUR_EMAIL_GOES_HERE


// DO NOT EDIT starts
interface FullNodeInterface {
    public boolean listen(String ipAddress, int portNumber);
    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress);
}
// DO NOT EDIT ends


public class FullNode implements FullNodeInterface {

    public boolean listen(String ipAddress, int portNumber) {
	// Implement this!
	// Return true if the node can accept incoming connections
	// Return false otherwise
	return true;
    }
    
    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) {
	// Implement this!
	return;
    }
}
