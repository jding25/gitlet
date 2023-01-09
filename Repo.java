package gitlet;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.List;
import java.util.TreeMap;
import java.util.SortedMap;


public class
    Repo implements Serializable {

    /** The main folder.*/
    private File gitletFolder;
    /** Directory to all the Commits.*/
    private   File commitFolder = new File(".gitlet/committing");
    /** Directory to the Stage Folder.*/
    private File stageFolder = new File(".gitlet/staging");
    /** Directory to all the Blobs.*/
    private File blobFolder = new File(".gitlet/blobbing");
    /** Directory to all the branches. Need to update HEAD value every call.*/
    private File branchesFolder = new File(".gitlet/branching");
    /**Current working directory.*/
    private File cwd;
    /**SHA1 code of the Head commit.*/
    private String _head;
    /**Name of the current head branch.*/
    private String bHead = "master";
    /**Current stage.*/
    private Stage myStage;


    /**Key is the name of the remote and value is the directory.*/
    private HashMap<String, String> remoteMap;
    /**File that stores remoteMap.*/
    private File _remote = new File(".gitlet/remote");





    public Repo() {
        cwd = new File(System.getProperty("user.dir"));

        File oldStage = new File(".gitlet/staging/stage");
        if (oldStage.exists()) {
            myStage = Utils.readObject(oldStage, Stage.class);
        }
        File oldHead = new File(".gitlet/branching/HEAD");
        if (oldHead.exists()) {
            _head = Utils.readContentsAsString(oldHead);
        }
        File oldBranchHead = new File(".gitlet/branching/BHEAD");
        if (oldBranchHead.exists()) {
            bHead = Utils.readContentsAsString(oldBranchHead);
        }


        if (_remote.exists()) {
            remoteMap = Utils.readObject(_remote, HashMap.class);
        }


    }



   /**Creates a new Gitlet version-control system in the current directory.*/
    public void init() throws IOException {
        gitletFolder = new File(".gitlet");
        if (gitletFolder.exists()) {
            System.out.println("A Gitlet version-control "
                    + "system already exists in the current directory.");
        } else {

            gitletFolder.mkdirs();
            blobFolder.mkdirs();
            branchesFolder.mkdirs();
            commitFolder.mkdirs();
            stageFolder.mkdirs();

            myStage = new Stage();
            Utils.writeObject(Utils.join(stageFolder, "stage"), myStage);


            Commit initialCommit = new Commit("initial commit",
                    null, new HashMap<>());
            String initHash = initialCommit.getShaID();
            Utils.writeObject(Utils.join(commitFolder, initHash),
                    initialCommit);
            _head = initHash;
            Utils.writeContents(Utils.join(branchesFolder, "HEAD"), _head);
            Utils.writeContents(Utils.join(branchesFolder, "BHEAD"), "master");
            Utils.writeContents(Utils.join(branchesFolder, "master"), initHash);

            remoteMap = new HashMap<>();
            saveRemoteMap();

        }
    }

    /**
     * Adds a copy of the file as it currently exists to the staging area.
     * @param filename .
     */
    public void add(String filename) {
        File toStage = new File(filename);
        if (!toStage.exists()) {
            System.out.println("File does not exist.");
        } else {
            Blob thisBaby = new Blob(filename);
            String blobCode = thisBaby.getBlobCode();
            byte[] blobByte = thisBaby.getContentByte();
            if (myStage.getReList().contains(filename)) {
                myStage.deleteFromReList(filename);
                saveStage();
                return;
            }
            if (getCurrCommit().getBlob().containsKey(filename)
                    && getCurrCommit().getBlob().get(filename).
                    equals(blobCode)) {
                if (myStage.getAddList().containsKey(filename)) {
                    myStage.deleteFromAddList(filename);
                }
                return;
            }
            if (myStage.getAddList().containsKey(filename)
                    && !myStage.getAddList().get(filename).equals(blobCode)) {
                myStage.deleteFromAddList(filename);
            }
            myStage.addToAddList(filename, blobCode);
            Utils.writeContents(Utils.join(blobFolder, blobCode), blobByte);
            saveStage();

        }
    }
    /**Description: Saves a snapshot of tracked files in the current commit and
     * staging area so they can be restored at a later time,
     * creating a new commit.
     * @param message
     * @throws IOException
     */
    public void commit(String message) throws IOException {
        if (myStage.getAddList().isEmpty()
                && myStage.getReList().isEmpty()) {
            System.out.println("No changes added to the commit.");
        } else if (message.equals("")) {
            System.out.println("Please enter a commit message.");
        } else {
            Commit currCommit = getCurrCommit();
            String parentID = currCommit.getShaID();
            HashMap blobCopy = (HashMap) currCommit.getBlob().clone();
            Set<String> toAdd = myStage.getAddList().keySet();
            for (String name : toAdd) {
                blobCopy.put(name, myStage.getAddList().get(name));
            }
            for (String name : myStage.getReList()) {
                if (blobCopy.containsKey(name)) {
                    blobCopy.remove(name);
                }
            }
            Commit me = new Commit(message, parentID, blobCopy);
            saveCommit(me);
            saveHead(me.getShaID());
            updateCurrBranch(me.getShaID());
            myStage.clearAll();
            saveStage();
        }
    }
    /** Unstage the file if it is currently staged for addition.
     *  @param fname is file name*/
    public void rm(String fname) {
        if (!myStage.getAddList().containsKey(fname)
                && !getCurrCommit().getBlob().containsKey(fname)) {
            System.out.println("No reason to remove the file.");
        } else {
            if (myStage.getAddList().containsKey(fname)) {
                myStage.deleteFromAddList(fname);
                saveStage();
            }
            if (getCurrCommit().getBlob().containsKey(fname)) {
                myStage.addToReList(fname);
                saveStage();
                Utils.restrictedDelete(fname);
            }
        }
    }
    public void log() {
        Commit curr = getCurrCommit();
        while (curr != null) {
            curr.myLog();
            if (curr.getParentID() != null) {
                curr = Utils.readObject(Utils.join(commitFolder,
                        curr.getParentID()), Commit.class);
            } else {
                break;
            }
        }
    }
    /**Like log, except displays information about all commits ever made.*/
    public void globalLog() {
        List<String> names = Utils.plainFilenamesIn(commitFolder);
        for (String name: names) {
            Commit commit =
                    Utils.readObject(new File(commitFolder,
                            name), Commit.class);
            commit.myLog();
        }
    }
    public void find(String message) {
        List<String> names = Utils.plainFilenamesIn(commitFolder);
        boolean found = false;
        for (String name: names) {
            Commit commit = Utils.readObject(new File(commitFolder,
                    name), Commit.class);
            if (commit.getCommitMessage().equals(message)) {
                found = true;
                Utils.message(commit.getShaID());
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }
    /**Displays what branches currently exist,
     *  and marks the current branch with*.
     *  @param branchName
     *  @param untracked
     *  @param staged
     *  @param modified */
    public void statusHelper(List<String> branchName, List<String> staged,
                             List<String> modified, List<String> untracked) {
        Collections.sort(untracked);
        Utils.message("=== Branches ===");
        for (String f : branchName) {
            Utils.message(f);
        }
        Utils.message("");
        Utils.message("=== Staged Files ===");
        for (String f : staged) {
            Utils.message(f);
        }
        Utils.message("");
        Utils.message("=== Removed Files ===");
        for (String f : myStage.getReList()) {
            Utils.message(f);
        }
        Utils.message("");
        Utils.message("=== Modifications Not Staged For Commit ===");
        for (String f : modified) {
            Utils.message(f);
        }
        Utils.message("");
        Utils.message("=== Untracked Files ===");
        for (String f : untracked) {
            Utils.message(f);
        }
        Utils.message("");
    }
    /**Displays what branches currently exist,
     * and marks the current branch with*.*/
    public void status() {
        List<String> branchName = new ArrayList<>();
        for (File f: branchesFolder.listFiles()) {
            if (f.getName().equals("HEAD") || f.getName().equals("BHEAD")) {
                continue;
            }
            if (f.getName().equals(bHead)) {
                branchName.add("*" + f.getName());
            } else {
                branchName.add(f.getName());
            }
        }
        Collections.sort(branchName);
        List<String> staged = new ArrayList<>();
        for (String f: myStage.getAddList().keySet()) {
            staged.add(f);
        }
        Collections.sort(staged);
        Collections.sort(myStage.getReList());
        List<String> modified = new ArrayList<>();
        for (String f: getCurrCommit().getBlob().keySet()) {
            if (new File(f).exists()) {
                String bContent = Utils.readContentsAsString(
                        new File(blobFolder, getCurrCommit().getBlob().get(f)));
                if (!Utils.readContentsAsString(new File(f)).equals(bContent)
                        && !myStage.getAddList().containsKey(f)) {
                    modified.add(f + " (modified)");
                }
            } else {
                if (!myStage.getReList().contains(f)) {
                    modified.add(f + " (deleted)");
                }
            }
        }
        for (String f: myStage.getAddList().keySet()) {
            if (new File(f).exists()) {
                String bContent =
                        Utils.readContentsAsString(Utils.join(blobFolder,
                        myStage.getAddList().get(f)));
                if (!Utils.readContentsAsString(new File(f)).equals(bContent)) {
                    modified.add(f + " (modified)");
                }
            } else {
                modified.add(f + " (deleted)");
            }
        }
        Collections.sort(modified);
        List<String> untracked = new ArrayList<>();
        for (String f: Utils.plainFilenamesIn(cwd)) {
            if (!getCurrCommit().getBlob().containsKey(f)
                    && !myStage.getAddList().containsKey(f)) {
                untracked.add(f);
            }
        }
        Collections.sort(untracked);
        statusHelper(branchName, staged, modified, untracked);
    }
    /**Takes the version of the file as it exists in the head commit,
     * the front of the current branch, and puts it in the working directory.
     * @param fname */
    public void checkout1(String fname) {
        checkout2(_head, fname);
    }
    /**Takes the version of the file as it
     * exists in the commit with the given id.
     * and puts it in the working directory,
     * @param comID
     * @param fname */
    public void checkout2(String comID, String fname) {
        for (String realID: commitFolder.list()) {
            if (realID.contains(comID)) {
                comID = realID;
                break;
            }
        }
        File commit = new File(commitFolder, comID);
        if (!commit.exists()) {
            Utils.message("No commit with that id exists.");
            return;
        }
        Commit curr = Utils.readObject(commit, Commit.class);
        if (!curr.getBlob().containsKey(fname)) {
            Utils.message("File does not exist in that commit.");
            return;
        }
        if (new File(cwd, fname).exists()) {
            Utils.restrictedDelete(new File(cwd, fname));
        }
        if (myStage.getAddList().containsKey(fname)) {
            myStage.deleteFromAddList(fname);
            saveStage();
        }
        String blobCode = curr.getBlob().get(fname);
        byte[] blobContent = Utils.readContents(Utils.
                join(blobFolder, blobCode));
        Utils.writeContents(Utils.join(cwd, fname), blobContent);
    }
    /**Takes all files in the commit at the head of the given branch,
     * and puts them in the working directory.
     * @param branchName */
    public void checkout3(String branchName) {
        File branch = new File(branchesFolder, branchName);
        if (!branch.exists()) {
            Utils.message("No such branch exists.");
            return;
        }
        String commitID = Utils.readContentsAsString(branch);
        if (branchName.equals(bHead)) {
            Utils.message("No need to checkout the current branch.");
            return;
        }
        Commit currCom = Utils.readObject(new File(commitFolder,
                commitID), Commit.class);
        HashMap<String, String> babeblob = currCom.getBlob();
        for (String name: Utils.plainFilenamesIn(cwd)) {
            if (!getCurrCommit().getBlob().containsKey(name)
                    && babeblob.containsKey(name)) {
                Utils.message("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                return;
            }
        }
        for (String file: getCurrCommit().getBlob().keySet()) {
            if (!babeblob.containsKey(file)) {
                Utils.restrictedDelete(file);
            }
        }

        for (String fname: babeblob.keySet()) {
            byte[] content = Utils.readContents(new File(blobFolder,
                    babeblob.get(fname)));
            Utils.writeContents(new File(fname), content);
        }

        saveStage();
        saveBHead(branchName);
        saveCommit(currCom);
        saveHead(commitID);

    }
    /**Creates a new branch with the given name,
     * and points it at the current head node.
     * @param name */
    public void branch(String name) {
        File newBranch = new File(branchesFolder, name);
        if (newBranch.exists()) {
            Utils.message("A branch with that name already exists.");
            return;
        }
        Utils.writeContents(newBranch, _head);
    }
    /**Deletes the branch with the given name.
     * @param bname */
    public void rmBranch(String bname) {
        File branchPath = new File(branchesFolder, bname);
        if (!branchPath.exists()) {
            Utils.message("A branch with that name does not exist.");
            return;
        }
        if (bname.equals(bHead)) {
            Utils.message("Cannot remove the current branch.");
            return;
        }
        branchPath.delete();
    }
    /**Checks out all the files tracked by the given commit.
     * @param commitID */
    public void reset(String commitID) {
        for (String realID: commitFolder.list()) {
            if (realID.contains(commitID)) {
                commitID = realID;
                break;
            }
        }
        File commitPath = new File(commitFolder, commitID);
        if (!commitPath.exists()) {
            Utils.message("No commit with that id exists.");
            return;
        }
        Commit com = Utils.readObject(Utils.join(commitFolder,
                commitID), Commit.class);
        HashMap<String, String> blob = com.getBlob();
        for (String name: Utils.plainFilenamesIn(cwd)) {
            if (!getCurrCommit().getBlob().containsKey(name)
                    && blob.containsKey(name)) {
                Utils.message("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                return;
            }
        }
        for (String file: getCurrCommit().getBlob().keySet()) {
            if (!blob.containsKey(file)) {
                Utils.restrictedDelete(file);
            }
        }

        for (String fname: blob.keySet()) {
            byte[] content =
                    Utils.readContents(new File(blobFolder, blob.get(fname)));
            Utils.writeContents(new File(fname), content);
        }
        myStage.clearAll();
        saveStage();
        saveHead(com.getShaID());
        updateCurrBranch(com.getShaID());
    }
    public boolean mergeCheck(String givenBranch) {
        boolean value = true;
        if (myStage.getAddList().size() != 0
                || myStage.getReList().size() != 0) {
            Utils.message("You have uncommitted changes.");
            return false;
        }
        File branchPath = new File(branchesFolder, givenBranch);
        if (!branchPath.exists()) {
            Utils.message("A branch with that name does not exist.");
            return false;
        }
        if (givenBranch.equals(bHead)) {
            Utils.message("Cannot merge a branch with itself.");
            return false;
        }
        Commit givenCommit = Utils.readObject(Utils.join(commitFolder,
                Utils.readContentsAsString(branchPath)), Commit.class);
        Commit currCommit = getCurrCommit();
        for (String name: Utils.plainFilenamesIn(cwd)) {
            if (!currCommit.getBlob().containsKey(name)
                    && givenCommit.getBlob().containsKey(name)) {
                Utils.message("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                return false;
            }
        }
        return value;
    }
    public ArrayList<String> makeArray(HashMap<String, String> givenBlob,
                                       HashMap<String, String> currBlob,
                                       HashMap<String, String> splitBlob) {
        ArrayList<String> files = new ArrayList<>();
        for (String fname: givenBlob.keySet()) {
            files.add(fname);
        }
        for (String fname: currBlob.keySet()) {
            if (!files.contains(fname)) {
                files.add(fname);
            }
        }
        for (String fname: splitBlob.keySet()) {
            if (!files.contains(fname)) {
                files.add(fname);
            }
        }
        return files;


    }
    public void merge(String givenBranch) throws IOException {
        if (mergeCheck(givenBranch)) {
            File branchPath = new File(branchesFolder, givenBranch);
            Commit givenCommit = Utils.readObject(Utils.join(commitFolder,
                    Utils.readContentsAsString(branchPath)), Commit.class);
            Commit currCommit = getCurrCommit();
            TreeMap<Float, String> allGivenCommits = new TreeMap();
            findSplitHelper(givenCommit, allGivenCommits, (float) 0);
            TreeMap<Float, String> allCurrCommits = new TreeMap();
            findSplitHelper(currCommit, allCurrCommits, (float) 0);
            Commit splitCommit =
                    findSplitPoint(allCurrCommits, allGivenCommits);
            if (splitCommit.getShaID().equals(currCommit.getShaID())) {
                Utils.message("Current branch fast-forwarded.");
                checkout3(givenBranch);
                return;
            }

            if (allCurrCommits.containsValue(givenCommit.getShaID())) {
                Utils.message("Given branch is an ancestor of "
                        + "the current branch.");
                return;
            }
            HashMap<String, String> givenBlob = givenCommit.getBlob();
            HashMap<String, String> currBlob = currCommit.getBlob();
            HashMap<String, String> splitBlob = splitCommit.getBlob();
            ArrayList<String> files = makeArray(givenBlob, currBlob, splitBlob);

            HashMap<String, String> newBlob = superHelpful(givenBlob,
                    currBlob, splitBlob, files, currCommit, givenCommit);
            Commit newCommit = new Commit("Merged " + givenBranch
                    + " into " + bHead + ".", currCommit.getShaID(),
                    givenCommit.getShaID(), newBlob);
            saveCommit(newCommit);
            saveHead(newCommit.getShaID());
            updateCurrBranch(newCommit.getShaID());
            myStage.clearAll();
            saveStage();
        }
    }
    public HashMap<String, String> superHelpful
    (HashMap<String, String> givenBlob,
        HashMap<String, String> currBlob,
        HashMap<String, String> splitBlob,
        ArrayList<String> files, Commit currCommit,
        Commit givenCommit) {
        HashMap<String, String> newBlob = new HashMap<>();
        for (String file: files) {
            if (!(!currBlob.containsKey(file)
                    && !givenBlob.containsKey(file))) {
                if (!(currBlob.containsKey(file)
                        && givenBlob.containsKey(file)
                        && currBlob.get(file).
                        equals(givenBlob.get(file)))) {
                    if ((!splitBlob.containsKey(file)
                            && !currBlob.containsKey(file))
                            || (splitBlob.containsKey(file)
                            && currBlob.containsKey(file)
                            && splitBlob.get(file).
                            equals(currBlob.get(file)))) {
                        if (givenBlob.containsKey(file)) {
                            newBlob.put(file, givenBlob.get(file));
                            byte[] content =
                                    Utils.readContents(new File(blobFolder,
                                            givenBlob.get(file)));
                            Utils.writeContents(new File(file), content);
                        } else {
                            Utils.restrictedDelete(file);
                        }
                    } else if ((!splitBlob.containsKey(file)
                            && !givenBlob.containsKey(file))
                            || ((splitBlob.containsKey(file)
                            && givenBlob.containsKey(file)
                            && splitBlob.get(file).
                            equals(givenBlob.get(file))))) {
                        if (currBlob.containsKey(file)) {
                            newBlob.put(file, currBlob.get(file));
                        }
                    } else {
                        conflict(file, currCommit, givenCommit);
                        add(file);
                        Blob conflict = new Blob(file);
                        Utils.writeContents(Utils.join(blobFolder,
                                conflict.getBlobCode()),
                                conflict.getContentByte());
                        newBlob.put(file, conflict.getBlobCode());
                        Utils.message("Encountered a merge conflict.");
                    }
                }
            }
        }
        return newBlob;

    }
    public void conflict(String fname, Commit currCom, Commit givenCom) {
        String head = "<<<<<<< HEAD\n";
        String middle = "=======\n";
        String end = ">>>>>>>\n";
        String curr = "";
        String given = "";
        if (currCom.getBlob().containsKey(fname)) {
            curr = Utils.readContentsAsString(Utils.join(blobFolder,
                    currCom.getBlob().get(fname)));
        }
        if (givenCom.getBlob().containsKey(fname)) {
            given = Utils.readContentsAsString(Utils.join(blobFolder,
                    givenCom.getBlob().get(fname)));
        }
        String stringContent = head + curr + middle + given + end;
        Utils.writeContents(new File(fname), stringContent);
        return;
    }
    public Commit findSplitPoint(TreeMap<Float, String> allCurrCommits,
                                 TreeMap<Float, String> allGivenCommits)
            throws IOException {
        for (String comID: allCurrCommits.values()) {
            if (allGivenCommits.containsValue(comID)) {
                return Utils.readObject(Utils.join(commitFolder,
                        comID), Commit.class);
            }
        }
        return new Commit(null, null, null);
    }
    public void findSplitHelper(Commit commit, SortedMap<Float,
            String> currList, Float dis) {
        if (currList.containsKey(dis)) {
            currList.put((float) (dis + .5), commit.getShaID());
        } else {
            currList.put(dis, commit.getShaID());
        }
        if (commit.getParentID() != null) {
            if (commit.isHasParent2()) {
                findSplitHelper(Utils.readObject(Utils.join(commitFolder,
                                commit.getParentTwoId()), Commit.class),
                        currList, (float) Math.round(dis + 1));
                findSplitHelper(Utils.readObject(Utils.join(commitFolder,
                                commit.getParentID()), Commit.class),
                    currList, (float) Math.round(dis + 1));
            } else {
                findSplitHelper(Utils.readObject(Utils.join(commitFolder,
                                commit.getParentID()), Commit.class),
                        currList, (float) Math.round(dis + 1));
            }
        }
    }
    /**All the super helpful helper functions are below.
     * @return  :) */
    public Commit getCurrCommit() {
        return Utils.readObject(Utils.join(commitFolder, _head), Commit.class);
    }
    /**save head.
     * @param commitID*/
    public void saveHead(String commitID) {
        File head = new File(branchesFolder, "HEAD");
        head.delete();
        Utils.writeContents(head, commitID);
    }
    /**change the branch name of the head; branching/BHEAD.
     * @param branchName */
    public void saveBHead(String branchName) {
        Utils.writeContents(Utils.join(branchesFolder, "BHEAD"), branchName);
    }
    /**update branching/currentBranch contents to this new commit.
     * @param commitID*/
    public void updateCurrBranch(String commitID) {
        Utils.writeContents(Utils.join(branchesFolder, bHead), commitID);
    }
    /**put new commit into the commit folder.
     * @param me */
    public void saveCommit(Commit me) {
        String comCode = me.getShaID();
        Utils.writeObject(Utils.join(commitFolder, comCode), me);
    }
    public void saveStage() {
        deleteFiles(stageFolder);
        Utils.writeObject(Utils.join(stageFolder, "stage"), myStage);
    }
    public void deleteFiles(File folder) {
        for (File f: folder.listFiles()) {
            f.delete();
        }
    }

    /**Ok... here comes the extra credit.
     * @param reName
     * @param dirName .*/
    public void addRemote(String reName, String dirName) {
        if (remoteMap.containsKey(reName)) {
            Utils.message("A remote with that name already exists.");
            return;
        }
        remoteMap.put(reName, dirName.replace('/', File.separatorChar));
        saveRemoteMap();
        return;

    }

    /**Ok... here comes the extra credit.
     * @param reName .*/
    public void removeRemote(String reName) {
        if (remoteMap.containsKey(reName)) {
            remoteMap.remove(reName);
            saveRemoteMap();
            return;
        }
        Utils.message("A remote with that name does not exist.");
        return;
    }

    public void push(String reName, String theBranch) {
        return;
    }

    /**Brings down commits from the remote Gitlet repository
     * into the local Gitlet repository.
     * @param reName
     * @param theBranch .*/
    public void fetch(String reName, String theBranch) {
        return;
    }
    public void pull(String reName, String branch) throws IOException {
        fetch(reName, branch);
        merge(reName + File.separator + branch);
    }

    public void saveRemoteMap() {
        Utils.writeObject(_remote, remoteMap);
    }



}
