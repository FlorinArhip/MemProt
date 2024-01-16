package eu.atspace.hash.cc;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Log {
    public final String fn = "dbg.log";

    private Params par;
    private PrintWriter pw;

    public Log(Params par) throws FileNotFoundException {
        this.par = par;
        pw = new PrintWriter(new PrintWriter(fn), true);
    }

    public void log(Address addr, String str) {
        pw.println(" " + addr.toString() + " [" + par.getCurrTime() + "] " + str);
    }

    public void logNodeAdd(Address myAddr, Address addr) {
        String str = "Node " + addr.toString() + " joined at time " + par.getCurrTime();
        log(myAddr, str);
    }

    public void logNodeRemove(Address myAddr, Address addr) {
        String str = "Node " + addr.toString() + " removed at time " + par.getCurrTime();
        log(myAddr, str);
    }

}
