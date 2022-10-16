package gitlet;

import java.io.Serializable;

import java.util.TreeMap;

public class StagingArea implements Serializable {

    /**TreeMap containing fileNames and corresponding blobs for adding.*/
    private TreeMap<String, String> toBeCommited;
    /**TreeMap containing fileNames and corresponding blobs for removing.*/
    private TreeMap<String, String> toBeRemoved;

    public StagingArea() {
        toBeCommited = new TreeMap<>();
        toBeRemoved = new TreeMap<>();
    }

    /**
     * Method to add files to hashmap with those to be commited.
     * @param filename is name of the file.
     * @param sha1  is the sha1ID of the respective file.
     * */
    public void addition(String filename, String sha1) {
        toBeCommited.put(filename, sha1);
    }


    /**getter method for toBeCommited.
     * @return TreeMap of area staged for addition.
     * */
    public TreeMap<String, String> getToBeCommited() {
        return toBeCommited;
    }

    /**getter method for toBeRemoved.
     * @return TreeMap of area staged for removal.
     * */
    public TreeMap<String, String> getToBeRemoved() {
        return toBeRemoved;
    }

}
