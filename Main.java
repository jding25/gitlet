package gitlet;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Claire Ding
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        if (args.length == 0) {
            Utils.message("Please enter a command.");
            return;
        }
        if ((!new File(".gitlet").exists())
                && (!args[0].equals("init"))) {
            Utils.message("Not in an initialized Gitlet directory.");
            return;
        }
        Repo rp = new Repo();
        ArrayList<String> first =
                new ArrayList<>(Arrays.asList("init", "add",
                        "commit", "rm", "log"));
        ArrayList<String> second =
                new ArrayList<>(Arrays.asList("global-log", "find",
                        "status", "checkout"));
        ArrayList<String> third =
                new ArrayList<>(Arrays.asList("branch", "rm-branch",
                        "reset", "merge"));
        ArrayList<String> fourth =
                new ArrayList<>(Arrays.asList("add-remote", "rm-remote",
                        "push", "fetch", "pull"));
        if (first.contains(args[0])) {
            firstFunc(rp, args);
        } else if (second.contains(args[0])) {
            secondFunc(rp, args);
        } else if (third.contains(args[0])) {
            thirdFunc(rp, args);
        } else if (fourth.contains(args[0])) {
            fourthFunc(rp, args);
        } else {
            Utils.message("No command with that name exists.");
            return;
        }
        Utils.writeObject(new File(".gitlet", "gitlet"), rp);
    }
    /**first group.
     * @param rp is param
     * @param args is a parameter.*/
    static void firstFunc(Repo rp, String... args) throws IOException {
        switch (args[0]) {
        case "init": {
            if (checker(args, 1)) {
                rp.init();
            }
            break;
        }
        case "add": {
            if (checker(args, 2)) {
                rp.add(args[1]);
            }
            break;
        }
        case "commit": {
            if (checker(args, 2)) {
                rp.commit(args[1]);
            }
            break;
        }
        case "rm": {
            if (checker(args, 2)) {
                rp.rm(args[1]);
            }
            break;
        }
        case "log": {
            if (checker(args, 1)) {
                rp.log();
            }
            break;
        }
        default:
            break;
        }
    }
     /**Second group.
      * @param  rp is repo
      * @param args is arguments.*/
    static void secondFunc(Repo rp, String... args) throws IOException {
        switch (args[0]) {
        case "global-log": {
            if (checker(args, 1)) {
                rp.globalLog();
            }
            break;
        }
        case "find": {
            if (checker(args, 2)) {
                rp.find(args[1]);
            }
            break;
        }
        case "status": {
            if (checker(args, 1)) {
                rp.status();
            }
            break;
        }
        case "checkout": {
            if (args.length == 2) {
                rp.checkout3(args[1]);
                break;
            } else if (args.length == 3 && args[1].equals("--")) {
                rp.checkout1(args[2]);
                break;
            } else if (args.length == 4 && args[2].equals("--")) {
                rp.checkout2(args[1], args[3]);
                break;
            } else {
                Utils.message("Incorrect operands.");
                return;
            }
        }
        default:
            break;
        }
    }
     /**Third group.
      * @param rp is rpo.
      * @param args is argument*/
    static void thirdFunc(Repo rp, String... args) throws IOException {
        switch (args[0]) {
        case "branch": {
            if (checker(args, 2)) {
                rp.branch(args[1]);
            }
            break;
        }
        case "rm-branch": {
            if (checker(args, 2)) {
                rp.rmBranch(args[1]);
            }
            break;
        }
        case "reset": {
            if (checker(args, 2)) {
                rp.reset(args[1]);
            }
            break;
        }
        case "merge": {
            if (checker(args, 2)) {
                rp.merge(args[1]);
            }
            break;
        }
        default:
            break;
        }
    }
     /**Fourth group.
      * @param rp is repo
      * @param args is argument.*/
    static void fourthFunc(Repo rp, String... args) throws IOException {
        switch (args[0]) {
        case "add-remote": {
            if (checker(args, 3)) {
                rp.addRemote(args[1], args[2]);
            }
            break;
        }
        case "rm-remote": {
            if (checker(args, 2)) {
                rp.removeRemote(args[1]);
            }
            break;
        }
        case "push": {
            if (checker(args, 3)) {
                rp.push(args[1], args[2]);
            }
            break;
        }
        case "fetch": {
            if (checker(args, 3)) {
                rp.fetch(args[1], args[2]);
            }
            break;
        }
        case "pull": {
            if (checker(args, 3)) {
                rp.pull(args[1], args[2]);
            }
            break;
        }
        default:
            break;
        }
    }

     /**Check function.
      * @param args is args.
      * @param i is index.
      * @return boolean true or false.*/
    private static boolean checker(String[] args, int i) {
        if (args.length != i) {
            Utils.message("Incorrect operands.");
            return false;
        }
        return true;
    }
}


