package gitlet;

import java.io.File;
import java.io.IOException;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Varun Kandula
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        Gitlet obj1 = new Gitlet();
        if (args.length == 0) {
            System.out.println("Please enter a command.");
        } else if (!new File(".gitlet").exists() && !args[0].equals("init")) {
            System.out.println("Not in an initialized Gitlet directory.");
        } else {
            if (args[0].equals("init")) {
                obj1.init();
            } else if (args[0].equals("add")) {
                obj1.add(args[1]);
            } else if (args[0].equals("commit")) {
                obj1.commit(args[1]);
            } else if (args[0].equals("checkout")) {
                if (args.length == 3) {
                    obj1.checkout1(args);
                }
                if (args.length == 4) {
                    obj1.checkout2(args);
                }
                if (args.length == 2) {
                    obj1.checkout3(args);
                }
            } else if (args[0].equals("log")) {
                obj1.log();
            } else if (args[0].equals("global-log")) {
                obj1.globalLog();
            } else if (args[0].equals("find")) {
                obj1.find(args[1]);
            } else if (args[0].equals("rm")) {
                obj1.rm(args[1]);
            } else if (args[0].equals("status")) {
                obj1.status();
            } else if (args[0].equals("branch")) {
                obj1.branch(args[1]);
            } else if (args[0].equals("rm-branch")) {
                obj1.rmBranch(args[1]);
            } else if (args[0].equals("reset")) {
                obj1.reset(args[1]);
            } else if (args[0].equals("merge")) {
                obj1.merge(args[1]);
            } else {
                System.out.println("No command with that name exists.");
                return;
            }
        }
    }
}
