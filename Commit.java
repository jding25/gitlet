package gitlet;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;


import static gitlet.Utils.message;
import static gitlet.Utils.serialize;


public class Commit implements Serializable {

    /**String formatter for timestamp.*/
    private String formatter = "EEE MMM d HH:mm:ss yyyy Z";

    /**Create a new commit. Taking in metadata/commit message, its parent's
     * sha1 code, the blob, generates the timestamp. Change the HEAD and
     * Master to the newly created commit.
     * @param curBlob Blob of the commit;
     * @param parent code of the parent;
     * @param message commit message.*/
    public Commit(String message, String parent,
                  HashMap<String, String> curBlob) throws IOException {
        if (message.equals("initial commit") && parent == null) {
            timeStamp = new SimpleDateFormat(formatter).
                            format(new Date(a, 1, 1, 0, 0, 0));
        } else {
            timeStamp = createTimeStamp();
        }
        commitMessage = message;
        parentID = parent;
        blob = curBlob;
        shaID = hashID(this);
    }

    public Commit(String message, String parent1, String parent2,
                  HashMap<String, String> curBlob) throws IOException {
        timeStamp = createTimeStamp();
        commitMessage = message;
        parentID = parent1;
        parentID2 = parent2;
        hasParent2 = true;
        blob = curBlob;
        shaID = hashID(this);
    }


    /**Generates a SHA1 code for the given object.
     * @return String returns a string
     * @param obj is the param*/
    protected String hashID(Commit obj) {
        return Utils.sha1(serialize(obj));
    }


    /**Generate the time stamp.
     * @return String is the return type*/
    public String createTimeStamp() {
        return new SimpleDateFormat(getFormatter()).format(new Date());
    }

    /**Return string formatter.*/
    public String getFormatter() {
        return formatter;
    }

    /** Return the commit Message given commit ID.*/
    public String getCommitMessage() {
        return commitMessage;
    }

    public String getShaID() {
        return shaID;
    }

    public String getParentID() {
        return parentID;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public HashMap<String, String> getBlob() {
        return blob;
    }

    public void myLog() {
        message("===");
        String commMess = "commit " + getShaID();
        message(commMess);
        String dateMess = "Date: " + getTimeStamp();
        message(dateMess);
        message(getCommitMessage());
        message("");
    }
    public boolean isHasParent2() {
        return hasParent2;
    }

    public String getParentTwoId() {
        return parentID2;
    }


    /**Timestamp of the commit when it's created.*/
    private String timeStamp;

    /**Metadata or commit message.*/
    private String commitMessage;

    /**the SHA1 code of commit itself.*/
    private String shaID;

    /** the SHA1 code of parent ID.*/
    private String parentID;

    /**the hashmap of blob inherited directly from stage.
     * Key is the file name and value is the BlobSHA1code.*/
    private HashMap blob;

    /**Whether it has two parents.*/
    private boolean hasParent2 = false;

    /**SHA 1 code for its second parent.*/
    private String parentID2;

    /**magic number.*/
    protected Integer a = 1970;
}
