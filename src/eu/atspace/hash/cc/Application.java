package eu.atspace.hash.cc;

import java.io.FileNotFoundException;
import java.io.IOException;


public class Application {
    private Params par;
    private EmulNet en;
    private Log log;
    private MPNode[] mpn;

    public int TOTAL_RUNNING_TIME = 700;


    /**
     * FUNCTION NAME: constructor
     * <p>
     * DESCRIPTION: initializes nodes, setting their addresses
     */
    public Application(String fn) throws FileNotFoundException, IOException {
        par = new Params();
        par.setParams(fn);

        en = new EmulNet(par);
        log = new Log(par);

        // initialize member nodes
        mpn = new MPNode[par.getGROUP_SIZE()];
        for (int i = 0; i < par.getGROUP_SIZE(); i++) {
            Member memberNode = new Member();
            memberNode.inited = false;

            mpn[i] = new MPNode(memberNode, par, en, log, en.init());
            log.log(mpn[i].getMemberNode().addr, "Application");
        }

    }


    /**
     * FUNCTION NAME: run
     * <p>
     * DESCRIPTION: Main driver function of the Application layer
     */
    public int run() {
        // As time runs along
        for (int i = 0; i < TOTAL_RUNNING_TIME; i++) {
            par.setCurrTime(i);

            // Run the membership protocol
            mpnRun();

            // Fail some nodes
            fail();
        }

        // Clean up
        en.cleanup();

        for (int i = 0; i < par.getGROUP_SIZE(); i++)
            mpn[i].finishUpThisNode();

        return 0;
    }


    /**
     * FUNCTION NAME: mpnRun
     * <p>
     * DESCRIPTION:	This function performs all the membership protocol functionalities
     */
    public void mpnRun() {
        // For all the nodes in the system
        for (int i = 0; i < par.getGROUP_SIZE(); i++) {

            /*
             * Receive messages from the network and queue them in the membership protocol queue
             */
            if (par.getCurrTime() > (int) (par.getSTEP_RATE() * i) && !(mpn[i].getMemberNode().bFailed)) {
                // Receive messages from the network and queue them
                mpn[i].recvLoop();
            }
        }

        // For all the nodes in the system
        for (int i = par.getGROUP_SIZE() - 1; i >= 0; i--) {

            /*
             * Introduce nodes into the distributed system
             */
            if (par.getCurrTime() == (int) (par.getSTEP_RATE() * i)) {
                // introduce the ith node into the system at time STEPRATE * i
                mpn[i].nodeStart();
                System.out.println(i + "-th introduced node is assigned with the address: " + mpn[i].getMemberNode().addr);
            }

            /*
             * Handle all the messages in your queue and send heartbeats
             */
            else if (par.getCurrTime() > (int) (par.getSTEP_RATE() * i) && !(mpn[i].getMemberNode().bFailed)) {
                // handle messages and send heartbeats
                mpn[i].nodeLoop();

                if ((i == 0) && (par.getCurrTime() % 500 == 0))
                    log.log(mpn[i].getMemberNode().addr, "@@time = " + par.getCurrTime());
            }

        }
    }

    /**
     * FUNCTION NAME: fail
     * <p>
     * DESCRIPTION: This function controls the failure of nodes
     */
    public void fail() {
        // fail half the members at time t=400
        if (par.isDROP_MSG() && par.getCurrTime() == 50)
            par.setDropmsg(true);

        if (par.isSINGLE_FAILURE() && par.getCurrTime() == 100) {

            int removed = (int) (Math.random() * par.getGROUP_SIZE());

            log.log(mpn[removed].getMemberNode().addr, "Node failed at time = " + par.getCurrTime());

            mpn[removed].getMemberNode().bFailed = true;

        } else if (par.getCurrTime() == 100) {

            int removed = (int) (Math.random() * (par.getGROUP_SIZE() / 2));
            for (int i = removed; i < removed + par.getGROUP_SIZE() / 2; i++) {
                log.log(mpn[i].getMemberNode().addr, "Node failed at time = " + par.getCurrTime());
                mpn[i].getMemberNode().bFailed = true;
            }

        }

        if (par.isDROP_MSG() && par.getCurrTime() == 300)
            par.setDropmsg(false);

    }

}
