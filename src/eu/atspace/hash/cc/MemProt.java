package eu.atspace.hash.cc;

import java.io.FileNotFoundException;
import java.io.IOException;

public class MemProt {
	public static void main(String[] args) throws FileNotFoundException, IOException {
		if (args.length < 1) {
			System.err.println(".conf file required");
			System.exit(1);
		}
		
		Application app = new Application(args[0]);		
		app.run();
	}
}
