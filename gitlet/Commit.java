package gitlet;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TreeMap;

public class Commit implements Serializable  {

    /**Instance Variable for message.*/
    private String message;
    /**Instance Variable for timestamp.*/
    private String timestamp;
    /**Instance Variable for blobs of commit.*/
    private TreeMap<String, String> blobs;

    /**Instance Variable for message.*/
    private Commit parent1;
    /**Instance Variable for message.*/
    private Commit parent2;

    public Commit(String mess, Commit par1, Commit par2,
                  TreeMap<String, String> commitBlobs) {
        this.message = mess;
        if (par1 == null) {
            this.timestamp = "Wed Dec 31 16:00:00 1969 -0800";
        } else {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter dtf =
                    DateTimeFormatter.ofPattern("E LLL d HH:mm:ss u");

            this.timestamp = dtf.format(now) + " -0800";
            this.parent1 = par1;
            this.parent2 = par2;
        }
        this.blobs = commitBlobs;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @SuppressWarnings("unchecked")
    public TreeMap<String, String> getBlobs() {
        return (TreeMap<String, String>) blobs.clone();
    }

    public String getsha1() {
        byte [] convertedCom = Utils.serialize(this);
        String hash = Utils.sha1(convertedCom);
        return hash;
    }

    public String getParent1sha1() {
        return parent1.getsha1();
    }


    public Commit getParent1() {
        return parent1;
    }

    public Commit getParent2() {
        return parent2;
    }

}
