package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Stage implements Serializable {

    /** Hashmap with file name as key and blob hash code as value.*/
    private HashMap<String, String> addList;
    /**Array list for removal.*/
    private ArrayList<String> reList;


    public Stage() {
        addList = new HashMap<String, String>();
        reList = new ArrayList();
    }

    public void addToAddList(String fname, String bcode) {
        addList.put(fname, bcode);
    }

    public void addToReList(String fname) {
        reList.add(fname);
    }

    public void deleteFromAddList(String fname) {
        addList.remove(fname);
    }

    public void deleteFromReList(String fname) {
        reList.remove(fname);
    }


    public HashMap<String, String> getAddList() {
        return addList;
    }
    public ArrayList<String> getReList() {
        return reList;
    }
    public void clearAll() {
        addList = new HashMap<>();
        reList = new ArrayList();
    }

}
