package bgu.spl.mics.application;

/** This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {
    public static void main(String[] args) {
        if (args.length >= 2) {
			parseConfigFile(args[1]); // TODO: can assume correctness of input?
		}
    }

	private static void parseConfigFile(String configFilePath) {
		// TODO
	}
}
