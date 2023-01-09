package gitlet;

import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {
    /**The name of the file.*/
    private String name;
    /**The pointer to the file.*/
    private File bloby;
    /**The SHA1 code of the byte of the file.*/
    private String blobCode;
    /**The content of the file in bytes.*/
    private byte[] contentByte;

    public Blob(String fname) {
        name = fname;
        bloby = new File(fname);
        contentByte = Utils.readContents(bloby);
        blobCode = Utils.sha1(name, contentByte);
    }

    /** Return the SHA1 code of the Blob object.*/
    public String getBlobCode() {
        return blobCode;
    }
    /** Return the byte of the content of Blob object.*/
    public byte[] getContentByte() {
        return contentByte;
    }
}
