1. Overview
===========

The Membership protocol homework (MemProt) has as an objective the implementation
of a membership protocol. To be able to run it on an ordinary PC, an emulator of
the underlying network is provided (EmulNet). Your application will run in the
middle layer, in between Application and EmulNet:

---------------
| Application |  (provided)
---------------
| MPNode      | <-- your implementation
---------------
| EmulNet     |  (provided)
--------------- 

The simulated P2P network may have up to 1000 nodes. There are three tests that
use 10 nodes to test your implementation

The implemented protocol must satisfy the two properties:

1) Completeness all the time: every non-failed node must detect every node
join, leave and fail, and

2) Accuracy of failure detection when there are no message losses and message
delays are small. 

When there are message losses, completeness must be satisfied and accuracy
should be high.

You will have to implement two variants, all-to-all heartbeating and gossip-style
heartbeating. SWIM-style heartbeating is optional, but you most likely get a higher
grade if you do it.



2. The Layers
=============

The layers run multiple copies of P2P nodes, in a single-threaded simulation engine.
So you don't have to worry about race conditions.

The EmulNet layer ensures the network protocol to transfer messages between peers.
The interface represents a real network API. Here there are the following methods:

 * public Address init()
   used to initialize the address of the P2P node with the next available IPv4 address
   
 * public void cleanup()
   writes some statistics about the number and messages used to a msgcount.log file
   
 * public int send(Address myAddr, Address toAddr, byte[] data)
   sends a byte array to a given address; the byte array is the abstraction of a message;
   it is called by the MPNode layer to actually send a message
   
 * public int recv(Address myAddr, Queue<Message> q)
   receives a message from the network, specific for this given receiver address;
   there is a queue provided in MPNode layer that will contain all the messages
   addressed to this receiver; the sending order is retained
   
The Application layer runs the simulation. There is a global runtime, and at each
increment of global run time variable, the nodes are simulated. During each period,
some nodes are started, and/or some are crashed. The methods nodeStart() and 
nodeLoop() are called for each node. They offer the chance to the P2P nodes,
as implemented in MPNode.java, to check for message and perform their activities.

The MPNode layer has limited functionality for now. There is provided to you the
message structure in Message class, serialization methods, and a few beginnings 
to get you an idea. 
   
Please do not modify any files except the MPNode.java file. You will actually send your    
MPNode.java file(s) for grading, and only this. Once you have your file ready, you can
grade it yourself and see the score. Details will follow.


3. Current functionality
========================

For now, the code prints out debug messages in dbg.log file. The code starts by joining
the node into the group, sends a JOINREQ message, but it is not completed. In addition,
also the JOINACK, the corresponding response message, is also defined. Please take a look
and see how the send is implemented. You will likely to continue with implementing the
receiving the JOINREQ message and reply to the introducer with a JOINACK message.

The introducer is the first node, with address 1.0.0.0:8001. As you code, you will
have to call, in your code, the following methods:  

 * log.logNodeAdd(Address myAddr, Address addr)
 * log.logNodeRemove(Address myAddr, Address addr)
 
This way you will signal the dbg.log file that you have added or removed the specific P2P
node with addr address, to/from your membership list. The grader makes use of this information.


4. How to implement the MPNode layer
====================================

You will need to start with implementing the following methods:

 * private boolean recvCallBack(Member node, EmulNet.Message enMsg)
 * private void nodeLoopOps()
 
Both methods are called by nodeLoop() method, which is called by the Application layer
(that's why the latter is declared public). You will need to have the following functionality:

 * Introduction: once started, a peer contacts a well-known peer, to join the group. This
 is implemented with JOINREQ message; it is sent, but the receiving part is not here yet.
 Also you would need to handle the JOINACK message.
 
 * Membership: you need to implement the membership protocol that ensures accuracy all
 the time (on joins and failures), and accuracy when there are no losses or delays.
 Please implement first an all-to-all strategy, this is the easy part. Second, for the
 gossip-style strategy, consider sending only a limited number of messages to node(s)
 chosen at random. Take care that your code carefully detects nodes that failed and
 removes them from the peers list. Please check the course materials.

Submit them both all-to-all membership and gossip-style.
 
You may modify only the MPNode.java file. This is the only file that you will submit.


5. Other files
==============

The Params.java file contains the parameters read from files in the cases/ folder.
When you run the grader, it will take each of the three cases and run them.

In your code you will definitely need the time stamp. Please always use the
par.getCurrTime(), since this is the global clock maintained by the Application.

  
6. Grading
==========

The provided sources are laid down as an Apache Ant project. Running is pretty easy:

$ ant dist
$ java -jar dist/MemProt.jar cases/singlefailure.conf

Please see the dbg.log file afterwards.

To run the grader, issue:

$ sh Grader.sh

For both the all-to-all strategy and gossip-style strategy, you should get 100 points.
Your work is not complete if your code does not build or grades are different than these.

The configuration files specify:
 * GROUP_SIZE, the number of P2P nodes
 * SINGLE_FAILURE, true/false, indicates whether single or multiple failures are considered 
 * DROP_MSG, true/false, whether the messages are dropped
 * MSG_DROP_PROB, the probability of dropping messages
 
Your code is tested under there are three scenarios:
 * single node failure
 * multiple node failure
 * single node failure on a lossy network

The deadline is one week before the last week of the semester.

Submit the file (only MPNode.java, both versions), to: honorius_galmeanu@yahoo.com

Please do not borrow code from colleagues. You may discuss, but you should solve the problem
individually. There are means to check sources for similarities, and, if detected, your final
grade will be divided among the number of authors.

