// IN2011 Computer Networks
// Coursework 2023/2024
//
// This is an example of how the TemporaryNode object can be used.
// It should work with your submission without any changes.
// This should make your testing easier.

import java.io.IOException;
import java.net.UnknownHostException;

// DO NOT EDIT starts
public class CmdLineEcho {

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage error!");
            System.err.println("DSTStoreCmdLine startingNodeName startingNodeAddress");
            return;
        } else {
            // A full node that is running on the network to be a first point of contact
            String startingNodeName = args[0];
            String startingNodeAddress = args[1];

            // Use a TemporaryNode to store the (key, value) pair on the network
            TemporaryNode tn = new TemporaryNode();

            // Make contact with the 2D#4 network
            if (tn.start(startingNodeName, startingNodeAddress)) {

                // Store the (key, value) pair
                if (tn.echo()) {
                    System.out.println("Echo worked! :-)");
                } else {
                    System.out.println("Echo failed! :-(");
                }

            } else {
                System.err.println("Could not contact network?");
            }

            return;
        }
    }
}
// DO NOT EDIT ends
