package eu.atspace.hash.cc;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Queue;

/**
 * EmulNet emulates the network layer
 */
public class EmulNet {

    public final int MAX_NODES = 1000;
    public final int MAX_TIME = 3600;
    public final int MBUFFSIZE = 30000;

    static class Message {
        // source node
        public Address from;

        // destination node
        public Address to;

        // data bytes buffer
        public byte[] buffer;
    }


    // the next id, used to form the node's address
    private int nextId;

    // current buffer size
    private int currBuffSize;

    private Params par;

    // this actually stores the messages
    private Message buff[] = new Message[MBUFFSIZE];

    // used to count the messages
    private int sent_msgs[][] = new int[MAX_NODES][];
    private int recv_msgs[][] = new int[MAX_NODES][];


    /**
     * Constructor
     *
     * @param p parameters
     */
    public EmulNet(Params p) {
        par = p;
        nextId = 1;
        currBuffSize = 0;

        for (int i = 0; i < MAX_NODES; i++) {
            sent_msgs[i] = new int[MAX_TIME];
            recv_msgs[i] = new int[MAX_TIME];

            for (int j = 0; j < MAX_TIME; j++) {
                sent_msgs[i][j] = 0;
                recv_msgs[i][j] = 0;
            }
        }
    }


    /**
     * init EmulNet for this node
     *
     * @return the node address
     */
    public Address init() {
        return new Address(nextId++, par.getPORTNUM());
    }


    /**
     * cleanup EmulNet for this node
     *
     * @throws FileNotFoundException
     */
    public void cleanup() {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new PrintWriter("msgcount.log"), true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

        for (int i = 1; i <= par.getGROUP_SIZE(); i++) {
            pw.format("node %3d ", i);
            int sent_total = 0;
            int recv_total = 0;

            for (int j = 0; j < par.getCurrTime(); j++) {
                sent_total += sent_msgs[i][j];
                recv_total += recv_msgs[i][j];

                pw.format(" (%4d, %4d)", sent_msgs[i][j], recv_msgs[i][j]);
                if (j % 10 == 9) {
                    pw.println();
                    pw.print("         ");
                }
            }
            pw.println();
            pw.format("node %3d sent_total %6d  recv_total %6d\n\n", i, sent_total, recv_total);
        }

        pw.close();
    }


    /**
     * EmulNet send function
     *
     * @param myAddr from: address
     * @param toAddr to: address
     * @param data   data buffer
     * @return data buffer size
     */
    public int send(Address myAddr, Address toAddr, byte[] data) {
        int sendmsg = (int) (Math.random() * 100);
        int size = data.length;

        if ((currBuffSize >= MBUFFSIZE) || (size >= par.getMAX_MSG_SIZE()) || (par.isDropmsg() && sendmsg < (int) (par.getMSG_DROP_PROB() * 100)))
            return 0;

        Message msg = new Message();
        msg.from = myAddr;
        msg.to = toAddr;
        msg.buffer = data;

        buff[currBuffSize++] = msg;

        int src = myAddr.getId();
        int time = par.getCurrTime();

        assert (src < MAX_NODES);
        assert (time < MAX_TIME);

        sent_msgs[src][time]++;

        return size;
    }


    /**
     * EmulNet receive function
     *
     * @param myAddr my address
     * @param q      queue to put messages into
     * @return 0
     */
    public int recv(Address myAddr, Queue<Message> q) {
        for (int i = currBuffSize - 1; i >= 0; i--) {
            Message msg = buff[i];

            if (msg.to.equals(myAddr)) {
                buff[i] = buff[--currBuffSize];
                buff[currBuffSize] = null;

                q.add(msg);

                int dst = myAddr.getId();
                int time = par.getCurrTime();

                assert (dst < MAX_NODES);
                assert (time < MAX_TIME);

                recv_msgs[dst][time]++;
            }
        }

        return 0;
    }
}
