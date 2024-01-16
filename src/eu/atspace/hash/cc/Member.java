package eu.atspace.hash.cc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class Member {

    @SuppressWarnings("serial")
    static class Entry implements Serializable {
        public int id;
        public short port;
        public long heartbeat;
        public long timestamp;

        public Entry(int id, short port, long heartbeat, long timestamp) {
            this.id = id;
            this.port = port;
            this.heartbeat = heartbeat;
            this.timestamp = timestamp;
        }

        public Entry() {
            this.id = 0;
            this.port = 0;
            this.heartbeat = 0;
            this.timestamp = 0;
        }
    }

    // this members's address
    public Address addr;

    // boolean indicating if this member is up
    public boolean inited;

    // boolean indicating if this member is in the group
    public boolean inGroup;

    // boolean indicating if this member has failed
    public boolean bFailed;

    // the node's own heartbeat
    public long heartbeat;

    // counter for next ping
    public int pingCounter;

    // counter for ping timeout
    public int timeOutCounter;

    // Membership table
    public List<Entry> memberList = new ArrayList<Entry>();

    // Queue for failure detection messages
    public Queue<EmulNet.Message> mpq = new LinkedList<EmulNet.Message>();


    public Member() {
        inited = false;
        inGroup = false;
        bFailed = false;
        heartbeat = 0;
        pingCounter = 0;
        timeOutCounter = 0;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((addr == null) ? 0 : addr.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Member other = (Member) obj;
        if (addr == null) {
            if (other.addr != null)
                return false;
        } else if (!addr.equals(other.addr))
            return false;
        return true;
    }

}
