package eu.atspace.hash.cc;

import java.io.*;
import java.util.List;

/*
 * Note: You can change/add any functions in MPNode.java
 * if you think it is necessary for your logic to work
 */

public class MPNode {
    public final int TREMOVE = 20;
    public final int TFAIL = 5;

    /*
     * fill in with your message types
     */
    private enum MsgTypes {
        JOINREQ,
        JOINACK,
        DUMMYLASTMSGTYPE,
        PING
    }

    @SuppressWarnings("serial")
    private static class Message implements Serializable {
        // message type
        private MsgTypes msgType;

        // originator
        private Address addr;

        // its details
        private long heartbeat;
        private long timestamp;

        // entries (may be null)
        public List<Member.Entry> entries;


        /**
         * Serialize a messsage to an array of bytes
         *
         * @param m the message
         * @return array of bytes
         */
        private static byte[] serialize(Message m) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = null;

            try {
                out = new ObjectOutputStream(bos);
                out.writeObject(m);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
            byte[] bytes = bos.toByteArray();

            try {
                out.close();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }

            return bytes;
        }


        /**
         * Serialize the message from an array of bytes
         *
         * @param bytes array
         * @return the message
         */
        private static Message serialize(byte[] bytes) {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInput in = null;
            Object o = null;

            try {
                in = new ObjectInputStream(bis);
                o = in.readObject();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }

            try {
                bis.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }

            return (Message) o;
        }
    }


    private EmulNet emulNet;
    private Log log;
    private Params par;
    private Member memberNode;


    public Member getMemberNode() {
        return memberNode;
    }


    /**
     * FUNCTION NAME: constructor
     * <p>
     * DESCRIPTION: This initializes member variables
     */
    public MPNode(Member m, Params p, EmulNet en, Log l, Address addr) {
        memberNode = m;
        emulNet = en;
        log = l;
        par = p;
        memberNode.addr = addr;
    }


    /**
     * FUNCTION NAME: recvLoop
     * <p>
     * DESCRIPTION: This function receives message from the network and pushes into the queue
     * This function is called by a node to receive messages currently waiting for it
     */
    public void recvLoop() {
        if (memberNode.bFailed)
            return;

        emulNet.recv(memberNode.addr, memberNode.mpq);
    }


    /**
     * FUNCTION NAME: nodeStart
     * <p>
     * DESCRIPTION: This function bootstraps the node
     * All initializations routines for a member.
     * Called by the application layer.
     */
    public void nodeStart() {
        // coordinator address is 1.0.0.0:0
        Address joinaddr = new Address(1, par.getPORTNUM());

        // Self booting routines
        initThisNode();

        if (!introduceSelfToGroup(joinaddr)) {
            finishUpThisNode();
            log.log(memberNode.addr, "Unable to join self to group. Exiting.");
            System.exit(1);
        }
    }


    /**
     * FUNCTION NAME: initThisNode
     * <p>
     * DESCRIPTION: Find out who I am and start up
     */
    private void initThisNode() {
        /*
         * This function is partially implemented and may require changes
         */
        memberNode.bFailed = false;
        memberNode.inited = true;
        memberNode.inGroup = false;

        // node is up!
        memberNode.heartbeat = 0;
        memberNode.pingCounter = TFAIL;
        memberNode.timeOutCounter = TREMOVE;
        initMemberListTable(memberNode);
    }


    /**
     * FUNCTION NAME: finishUpThisNode
     * <p>
     * DESCRIPTION: Wind up this node and clean up state
     */
    public void finishUpThisNode() {
        /*
         * Your code goes here
         */
    }


    /**
     * FUNCTION NAME: initMemberListTable
     * <p>
     * DESCRIPTION: Initialize the membership list
     */
    private void initMemberListTable(Member memberNode) {
        memberNode.memberList.clear();
    }


    /**
     * FUNCTION NAME: introduceSelfToGroup
     * <p>
     * DESCRIPTION: Join the distributed system
     */
    private boolean introduceSelfToGroup(Address joinaddr) {

        if (joinaddr.equals(memberNode.addr)) {
            // I am the group booter (first process to join the group). Boot up the group
            log.log(memberNode.addr, "Starting up group...");

            memberNode.inGroup = true;

        } else {
            Message msg = createMessage(MsgTypes.JOINREQ, memberNode.addr, memberNode.heartbeat, par.getCurrTime());

            log.log(memberNode.addr, "Trying to join...");

            // send JOINREQ message to introducer member
            emulNet.send(memberNode.addr, joinaddr, Message.serialize(msg));
        }

        return true;
    }


    /**
     * FUNCTION NAME: nodeLoop
     * <p>
     * DESCRIPTION: Executed periodically at each member
     * Check your messages in queue and perform membership protocol duties
     */
    public void nodeLoop() {
        if (memberNode.bFailed) {
            return;
        }

        // Check my messages
        checkMessages();

        // Wait until you're in the group...
        if (!memberNode.inGroup) {
            return;
        }

        // ...then jump in and share your responsibilites!
        nodeLoopOps();
    }


    /**
     * FUNCTION NAME: checkMessages
     * <p>
     * DESCRIPTION: Check messages in the queue and call the respective message handler
     */
    private void checkMessages() {
        // Pop waiting messages from memberNode's mp1q
        while (!memberNode.mpq.isEmpty()) {
            EmulNet.Message msg = memberNode.mpq.remove();
            recvCallBack(memberNode, msg);
        }
    }


    /**
     * FUNCTION NAME: recvCallBack
     * <p>
     * DESCRIPTION: Message handler for different message types
     */
    private boolean recvCallBack(Member node, EmulNet.Message enMsg) {
        // get message and process according to type
        Message msg = Message.serialize(enMsg.buffer);

        switch (msg.msgType) {
            case JOINREQ:
                log.log(node.addr, "JOINREQ received");

                // add message to memberlist
                pushToMemberList(msg);

                // send message
                sendMessage(msg.addr, MsgTypes.JOINACK);
                break;

            case JOINACK:
                log.log(node.addr, "JOINACK received");
                // add message to memberlist
                pushToMemberList(msg);

                // set inGroup to true
                memberNode.inGroup = true;
                break;

            case PING:
                pingHandler(msg);
                break;

            default:
                log.log(node.addr, "unknown type, ignored");
                break;
        }

        return false;
    }

    private void pingHandler(Message message) {
        updateSourceMember(message);

        for (Member.Entry entry : message.entries) {
            if (entry.id > 10 || entry.id < 0) {
                throw new RuntimeException("invalid entry id");
            }

            Member.Entry node = getMemberList(entry.id, entry.port);

            // Pushes the member list to the node.
            if (node != null) {
                if (entry.heartbeat > node.heartbeat) {
                    node.heartbeat = entry.heartbeat;
                    node.timestamp = par.getCurrTime();
                }
            } else {
                pushToMemberList(entry);
            }
        }
    }

    private void updateSourceMember(Message message) {
        Member.Entry sourceMember = getMemberList(message.addr);

        if (sourceMember != null) {
            sourceMember.heartbeat++;
            sourceMember.timestamp = par.getCurrTime();
        } else {
            pushToMemberList(message);
        }
    }

    private void sendMessage(Address toAddress, MsgTypes type) {
        Message message = new Message();
        message.msgType = type;
        message.addr = memberNode.addr;
        message.entries = memberNode.memberList;

        emulNet.send(memberNode.addr, toAddress, Message.serialize(message));
    }

    private void pushToMemberList(Message message) {
        // Check if the member list is valid.
        if (getMemberList(message.addr.getId(), message.addr.getPort()) != null) return;

        Member.Entry entry = new Member.Entry(
                message.addr.getId(),
                message.addr.getPort(),
                1L,
                par.getCurrTime());

        memberNode.memberList.add(entry);

        log.logNodeAdd(memberNode.addr, new Address(entry.id, entry.port));
    }

    private void pushToMemberList(Member.Entry entry) {
        Address address = new Address(entry.id, entry.port);

        // Check if the member node is a member node
        if (memberNode.addr.equals(address)) return;

        // Add a member to the member list.
        if (par.getCurrTime() - entry.timestamp < TREMOVE) {
            log.logNodeAdd(memberNode.addr, address);

            Member.Entry e = new Member.Entry(entry.id, entry.port, entry.heartbeat, entry.timestamp);
            memberNode.memberList.add(e);
        }
    }

    private Member.Entry getMemberList(int id, short port) {
        for (Member.Entry entry : memberNode.memberList) {
            // Returns the entry if it is a valid entry.
            if (entry.id == id && entry.port == port) {
                return entry;
            }
        }
        return null;
    }

    private Member.Entry getMemberList(Address address) {
        for (Member.Entry entry : memberNode.memberList) {
            // Returns the entry that is the same as the address.
            if (entry.id == address.getId() && entry.port == address.getPort()) {
                return entry;
            }
        }
        return null;
    }


    /**
     * FUNCTION NAME: nodeLoopOps
     * <p>
     * DESCRIPTION: Check if any node hasn't responded within a timeout period and then delete
     * the nodes
     * Propagate your membership list
     */
    private void nodeLoopOps() {
        // Update heartbeat
        memberNode.heartbeat++;

        // Check if any node hasn't responded within a timeout period and then delete
        for (int i = memberNode.memberList.size() - 1; i >= 0; i--) {
            if (par.getCurrTime() - memberNode.memberList.get(i).timestamp >= TREMOVE) {
                log.logNodeRemove(
                        memberNode.addr,
                        new Address(
                                memberNode.memberList.get(i).id,
                                memberNode.memberList.get(i).port));

                memberNode.memberList.remove(i);
            }
        }

        // Send PING to the members of memberList
        memberNode.memberList
                .forEach(e -> sendMessage(new Address(e.id, e.port), MsgTypes.PING));
    }


    /**
     * FUNCTION NAME: createMessage
     * <p>
     * DESCRIPTION: packs a message
     */
    private Message createMessage(MsgTypes msgType, Address address, long heartbeat, long timestamp) {
        Message msg = new Message();

        msg.msgType = msgType;
        msg.addr = address;
        msg.heartbeat = heartbeat;
        msg.timestamp = timestamp;
        msg.entries = null;

        return msg;
    }


    /**
     * FUNCTION NAME: printList
     * <p>
     * DESCRIPTION: puts the list of member entries to the log
     */
    private void printList(List<Member.Entry> list) {
        for (Member.Entry e : list) {
            String s = String.format("id: %d hb: %d ts: %d", e.id, e.heartbeat, e.timestamp);
            log.log(memberNode.addr, s);
        }
    }
}
