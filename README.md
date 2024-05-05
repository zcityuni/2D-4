## How to build this project
1) Unzip the project
2) Navigate to the src folder in the terminal
3) Input `javac [CmdLine Class to build].java` to build the class. For example, for `CmdLineGet` type `javac CmdLineGet.java` in the terminal.
4) Run the project from the terminal using the `java` command, be sure to use the correct amount of arguments. I have added two extra testing CmdLine classes for testing `ECHO?` and `NEAREST?`, these both take 2 arguments, being the starting node name and IP address respectively.
5) This flow of this project can be easily followed via the terminal which will have many print statements output to it along the way, alternatively, use wireshark for packet capture - There are wireshark packet recording files for various tested sitatuons included in this repo.
6) The only changes made to the DO NOT EDIT were exception signatures required by some methods, nothing material.

## Things that work:
### TempNode
1) `START`, the TempNode is able to connect to a starting node on the network and exchange the `START` method with the starting node and it is with validation such that it will disconnect if the exchange is not completed.
2) `ECHO?`, the TempNode can send `ECHO?` requests to servers to check for connectivity.
3) `NEAREST?` the TempNode can retrieve the three nearest nodes for a given hashID.
2) `PUT?` the TempNode can store key-value pairs of plaintext at full nodes that it connects to, if a `PUT?` request fails, it will automatically ask for `NEAREST?` nodes to connect to and attempt to `PUT?` the information in the three nearest nodes.
3) `GET?` the TempNode can retrieve values stored in full nodes when it provides a key, if a `GET?` request fails, it will automatically ask for `NEAREST?` nodes to connect to and attempt to `GET?` the information from the three nearest nodes.

### FullNode
The FullNode can do everything a TempNode (client) can do but also
1) Listen for connections, the FullNode opens a `ServerSocket` on a given port and waits for connections, upon accepting a client over TCP, the FullNode will initiate the 2D#4 protocol with the `START` request to the connected node, it will then await a `START` response, upon which it will create a new Thread to handle the conversation with the node. Thus, it is able to handle multiple clients as a server.
2) Passive and Active network mapping: the FullNode can passively map by listening for `NOTIFY?` requests and handling it by adding the node details to its network map with its calculated hashID distance. The FullNode upon connecting to a starting node as a client, after engaging in the `START` procedure, will automatically begin to query the node's `NEAREST?` nodes and connect to and then `NOTIFY?` each node to be added to their network maps. 
3) Handle `ECHO?` requests, the FullNode wil respond with `OHCE` as described by the RFC.
4) Handle `NEAREST?` requests, the FullNode will return three closest nodes to a given hashID as described by RFC.
5) Handle `GET?` requests, the FullNode will return values for any given keys stored in its hashmap.
6) Handle `PUT?` requests (partial), the FullNode can handle `PUT?` requests by storing key-value pairs in its hashmap provided the FullNode is one of the closest nodes to the given hashID of the key. (There is a nullptr error here for some reason when attempting to check if its closest).
