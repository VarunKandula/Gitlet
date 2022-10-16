package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Set;

public class Gitlet {

    /**I.V. for master.*/
    private String master;
    /**I.V. for _HEAD.*/
    private String _HEAD = "master";
    /**I.V. for _CWD.*/
    private File _CWD;
    /**I.V. for stagingArea.*/
    private StagingArea stagingArea;

    /**static final variable for specific folder in .gitlet directory.*/
    static final File GITLET_FOLDER = new File(".gitlet");
    /**static final variable for specific folder in .gitlet directory.*/
    static final File STAGING_FOLDER = new File(".gitlet/staging");
    /**static final variable for specific folder in .gitlet directory.*/
    static final File COMMIT_TREE_FOLDER = Utils.join(".gitlet",
            "commitTree");
    /**static final variable for specific folder in .gitlet directory.*/
    static final File COMMITS_FOLDER = new File(".gitlet/commits");
    /**static final variable for specific folder in .gitlet directory.*/
    static final File BLOB_FOLDER = new File(".gitlet/blobs");
    /**static final variable for specific folder in .gitlet directory.*/
    static final File STAGING_FILE = Utils.join(STAGING_FOLDER,
            "stagingObject.txt");
    /**static final variable for specific folder in .gitlet directory[EC].*/
    static final File REMOTE_FOLDER =
            Utils.join(GITLET_FOLDER, "global-repos");

    public Gitlet() {
        _CWD = new File(System.getProperty("user.dir"));
        String headPathName = ".gitlet/commitTree/HEAD.txt";
        File head = new File(headPathName);
        if (head.exists()) {
            _HEAD = Utils.readContentsAsString(new File(headPathName));
        }
        stagingArea = new StagingArea();
    }

    public void init() throws IOException {
        File start = new File(".gitlet");
        if (!start.exists()) {
            start.mkdir();
            Utils.join(start, "staging").mkdir();
            Utils.join(start, "commitTree").mkdir();
            Utils.join(start, "commits").mkdir();
            Utils.join(start, "blobs").mkdir();
            Utils.join(start, "global-repos").mkdir();

            Commit firstCommit = new Commit("initial commit",
                    null, null, new TreeMap<>());
            master = firstCommit.getsha1();
            String firstCommitHashCode = firstCommit.getsha1();
            _HEAD = master;

            File commitDir = Utils.join(start, "commits/"
                    + firstCommitHashCode + ".txt");


            commitDir.createNewFile();
            Utils.writeObject(commitDir, firstCommit);



            File mstr = Utils.join(start, "commitTree/master.txt");
            mstr.createNewFile();

            Utils.writeContents(mstr, firstCommitHashCode);


            File head = Utils.join(GITLET_FOLDER,
                    "commitTree/HEAD.txt");
            head.createNewFile();

            Utils.writeContents(head, "master");


            File stagArea = Utils.join(STAGING_FOLDER,
                    "stagingObject.txt");
            Utils.writeObject(stagArea, stagingArea);


        } else {
            System.out.println("A Gitlet version-control "
                    + "system already exists in the current directory.");
        }
    }

    public void add(String filename) {
        File currFile = new File(filename);
        if (currFile.exists()) {
            String blobfName = filename;
            String blobHash = getSha1String(currFile);
            byte [] blob = Utils.readContents(currFile);
            StagingArea s = getStagingArea();



            String latestCommitHash =
                    Utils.readContentsAsString(
                            Utils.join(COMMIT_TREE_FOLDER, _HEAD + ".txt"));

            Commit currCom = Utils.readObject(Utils.join(COMMITS_FOLDER,
                    latestCommitHash + ".txt"), Commit.class);

            if (currCom.getBlobs().get(filename) != null
                    &&
                    currCom.getBlobs().get(filename).equals(blobHash)) {
                if (s.getToBeRemoved().containsKey(filename)) {
                    s.getToBeRemoved().remove(filename);
                    Utils.writeObject(STAGING_FILE, s);
                }
                return;
            }

            if (s.getToBeRemoved().containsKey(filename)) {
                s.getToBeRemoved().remove(filename);
                Utils.writeObject(STAGING_FILE, s);
            }

            File addBlob = Utils.join(BLOB_FOLDER, blobHash + ".txt");
            Utils.writeContents(addBlob, blob);

            StagingArea old = Utils.readObject(STAGING_FILE, StagingArea.class);

            old.addition(filename, blobHash);

            Utils.writeObject(STAGING_FILE, old);

        } else {
            System.out.println("File does not exist.");
        }
    }

    public void commit(String message) {
        StagingArea beforeCom =
                Utils.readObject(STAGING_FILE, StagingArea.class);
        if (beforeCom.getToBeCommited().isEmpty() && beforeCom.getToBeRemoved()
                .isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        } else if (message.length() == 0) {
            System.out.println("Pleas enter a commit message.");
            return;
        }
        TreeMap<String, String> newBlobs =
                new TreeMap<>(beforeCom.getToBeCommited());
        Commit latest = getHeadCommit();
        TreeMap<String, String> currBlobs = latest.getBlobs();
        for (String fileName: newBlobs.keySet()) {
            currBlobs.put(fileName, newBlobs.get(fileName));
        }
        for (String fileToRemove: beforeCom.getToBeRemoved().keySet()) {
            currBlobs.remove(fileToRemove);
        }
        if (message.contains("Merged")) {
            String input = message;
            Pattern p = Pattern.compile("(?<=\\Merged\\b).*?(?=\\into\\b)");
            Matcher m = p.matcher(input);
            List<String> matches = new ArrayList<String>();
            while (m.find()) {
                matches.add(m.group());
            }
            String branchID = Utils.readObject(
                    Utils.join(COMMIT_TREE_FOLDER,
                            matches.get(0) + ".txt"),
                    String.class);
            Commit branch = Utils.readObject(
                    Utils.join(COMMITS_FOLDER, branchID + ".txt"),
                    Commit.class);
            Commit c =
                    new Commit(message, getHeadCommit(), branch, currBlobs);
            Utils.writeObject(Utils.join(COMMITS_FOLDER, c.getsha1() + ".txt"),
                    c);
            Utils.writeContents(Utils.join(COMMIT_TREE_FOLDER,
                            _HEAD + ".txt"),
                    c.getsha1());
            beforeCom.getToBeCommited().clear();
            beforeCom.getToBeRemoved().clear();
            Utils.writeObject(STAGING_FILE, beforeCom);
        } else {
            Commit c =
                    new Commit(message, getHeadCommit(), null, currBlobs);
            Utils.writeObject(Utils.join(COMMITS_FOLDER,
                            c.getsha1() + ".txt"),
                    c);
            Utils.writeContents(Utils.join(COMMIT_TREE_FOLDER, _HEAD + ".txt"),
                    c.getsha1());
            beforeCom.getToBeCommited().clear();
            beforeCom.getToBeRemoved().clear();
            Utils.writeObject(STAGING_FILE, beforeCom);
        }
    }

    /**
     * Case 1: 3 args.
     * Checks the head commit.
     * java gitlet.Main checkout -- [file name].
     * @param args
     * */
    public void checkout1(String[] args) {
        String fileName = args[2];
        Commit curr = getHeadCommit();
        TreeMap<String, String> currCommitBlobs = curr.getBlobs();
        if (currCommitBlobs.containsKey(fileName)) {
            String sha1OfBlob = currCommitBlobs.get(fileName);
            File currblobPointer = Utils.join(BLOB_FOLDER, sha1OfBlob + ".txt");
            byte [] currBlobContent = Utils.readContents(currblobPointer);
            File cwdFile = Utils.join(_CWD, fileName);
            Utils.writeContents(cwdFile, currBlobContent);
        } else {
            System.out.println("File does not exist in that commit.");
            return;
        }
    }

    /**
     * Case 2: 4 args.
     * checkout from a specific commit
     * java gitlet.Main checkout [commit id] -- [file name]
     * Edge case:
     * @param args
     * */
    public void checkout2(String[] args) {
        String commitID = args[1];
        String fileName = args[3];
        commitID = shortUID(commitID);

        if (!args[2].equals("--")) {
            System.out.println("Incorrect operands.");
            return;
        }
        List<String> allCommitIDs = Utils.plainFilenamesIn(COMMITS_FOLDER);
        if (!allCommitIDs.contains(commitID + ".txt")) {
            System.out.println("No commit with that id exists.");
            return;
        }

        File currComObj = Utils.join(COMMITS_FOLDER, commitID + ".txt");
        Commit currCom = Utils.readObject(currComObj, Commit.class);

        TreeMap<String, String> currCommitBlobs = currCom.getBlobs();


        if (allCommitIDs.contains(commitID + ".txt")) {
            if (currCommitBlobs.containsKey(fileName)) {
                String sha1OfBlob = currCommitBlobs.get(fileName);
                File currblobPointer =
                        Utils.join(BLOB_FOLDER, sha1OfBlob + ".txt");
                byte [] currBlobContent = Utils.readContents(currblobPointer);
                File cwdFile = Utils.join(_CWD, fileName);
                Utils.writeContents(cwdFile, currBlobContent);
            } else {
                System.out.println("File does not exist in that commit.");
                return;
            }
        } else {
            System.out.println("No commit with that id exists");
            return;
        }
    }

    /**
     * Case 3: 2 args.
     * * java gitlet.Main checkout [branch name]
     * @param args
     */
    public void checkout3(String[] args) {
        String branch = args[1];
        branch = shortUID(branch);
        File branchObj = Utils.join(COMMIT_TREE_FOLDER, branch + ".txt");
        if (!branchObj.exists()) {
            System.out.println("No such branch exists.");
            return;
        }
        String headPointer = Utils.readContentsAsString(
                Utils.join(COMMIT_TREE_FOLDER, "HEAD.txt"));
        if (headPointer.equals(branch)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        String branchCommitPointer = Utils.readContentsAsString(
                Utils.join(COMMIT_TREE_FOLDER, branch + ".txt"));
        Commit branchCommit = Utils.readObject(Utils.join(COMMITS_FOLDER,
                        branchCommitPointer + ".txt"),
                Commit.class);
        List<String> workingDirFiles = new ArrayList<>();
        Commit c = getHeadCommit();
        for (String s: Utils.plainFilenamesIn(_CWD)) {
            if (s.contains(".txt")) {
                workingDirFiles.add(s);
            }
        }
        for (String fName: workingDirFiles) {
            if (!c.getBlobs().keySet().contains(fName)
                    && branchCommit.getBlobs().keySet().contains(fName)) {
                System.out.println("There is an untracked file in the way; "
                        +
                        "delete it, or add and commit it first.");
                return;
            }
        }
        for (String fName: workingDirFiles) {
            if (c.getBlobs().keySet().contains(fName)
                    && !branchCommit.getBlobs().keySet().contains(fName)) {
                Utils.restrictedDelete(fName);
            }
        }
        for (String file: branchCommit.getBlobs().keySet()) {
            String fileHash = branchCommit.getBlobs().get(file);
            File blobFile = Utils.join(BLOB_FOLDER, fileHash + ".txt");
            byte[] blobContent = Utils.readContents(blobFile);
            Utils.writeContents(Utils.join(_CWD, file), blobContent);
        }
        Utils.writeContents(Utils.join(COMMIT_TREE_FOLDER, "HEAD.txt"),
                branch);
        StagingArea s = getStagingArea();
        s.getToBeCommited().clear();
        s.getToBeRemoved().clear();
        Utils.writeObject(STAGING_FILE, s);
    }


    public void log() {
        Commit c = getHeadCommit();
        while (c != null) {
            System.out.println("===");
            System.out.println("commit " + c.getsha1());
            System.out.println("Date: " + c.getTimestamp());
            System.out.println(c.getMessage());
            System.out.println();
            if (c.getParent1() != null) {
                c = Utils.readObject(Utils.join(COMMITS_FOLDER,
                                c.getParent1sha1() + ".txt"),
                        Commit.class);
            } else {
                return;
            }
        }
    }

    public void globalLog() {
        List<String> allCommitNames = Utils.plainFilenamesIn(COMMITS_FOLDER);
        int commitsLength = allCommitNames.size();
        for (int i = 0; i < commitsLength; i++) {
            File printCommitObject = Utils.join(COMMITS_FOLDER,
                    allCommitNames.get(i));
            Commit c = Utils.readObject(printCommitObject, Commit.class);
            System.out.println("===");
            System.out.println("commit " + c.getsha1());
            System.out.println("Date: " + c.getTimestamp());
            System.out.println(c.getMessage());
            System.out.println();
        }
    }

    /**
     * @param message */
    public void find(String message) {
        List<String> allCommitNames = Utils.plainFilenamesIn(COMMITS_FOLDER);
        int commitsLength = allCommitNames.size();
        List<String> sameMessageIDs = new ArrayList<>();
        for (int i = 0; i < commitsLength; i++) {
            File printCommitObject = Utils.join(COMMITS_FOLDER,
                    allCommitNames.get(i));
            Commit c = Utils.readObject(printCommitObject, Commit.class);
            if (c.getMessage().equals(message)) {
                sameMessageIDs.add(c.getsha1());
            }
        }
        if (sameMessageIDs.size() == 0) {
            System.out.println("Found no commit with that message.");
            return;
        }
        for (int i = 0; i < sameMessageIDs.size(); i++) {
            System.out.println(sameMessageIDs.get(i));
        }

    }

    /**
     * @param filename */
    public void rm(String filename) {
        Commit c = getHeadCommit();
        StagingArea s = Utils.readObject(Utils.join(STAGING_FOLDER,
                "stagingObject.txt"), StagingArea.class);

        if (s.getToBeCommited().keySet().contains(filename)) {

            s.getToBeCommited().remove(filename);

            Utils.writeObject(Utils.join(STAGING_FOLDER,
                    "stagingObject.txt"), s);
            return;
        } else if (c.getBlobs().keySet().contains(filename)) {
            s.getToBeRemoved().put(filename, null);
            Utils.restrictedDelete(filename);

            Utils.writeObject(Utils.join(STAGING_FOLDER,
                    "stagingObject.txt"), s);
            Utils.writeObject(Utils.join(COMMITS_FOLDER,
                    c.getsha1() + ".txt"), c);
            return;
        } else {
            System.out.println("No reason to remove the file.");
        }
    }

    public void status() {
        List<String> allBranches = Utils.plainFilenamesIn(COMMIT_TREE_FOLDER);
        String headPointer = Utils.readContentsAsString(
                Utils.join(COMMIT_TREE_FOLDER, "HEAD.txt"));

        List<String> realBranches = new ArrayList<>();
        for (int i = 0; i < allBranches.size(); i++) {
            realBranches.add(allBranches.get(i).
                    substring(0, allBranches.get(i).length() - 4));
        }

        realBranches.remove("HEAD");
        realBranches.remove(_HEAD);
        realBranches.add("*" + headPointer);

        Collections.sort(realBranches);


        StagingArea s = Utils.readObject(
                Utils.join(STAGING_FOLDER, "stagingObject.txt"),
                StagingArea.class);

        List<String> added = new ArrayList<>();
        for (String addingFiles: s.getToBeCommited().keySet()) {
            added.add(addingFiles);
        }
        Collections.sort(added);


        List<String> removed = new ArrayList<>();
        for (String removingFiles: s.getToBeRemoved().keySet()) {
            removed.add(removingFiles);
        }
        Collections.sort(removed);


        System.out.println("=== Branches ===");

        for (String branch: realBranches) {
            System.out.println(branch);
        }

        System.out.println();
        System.out.println("=== Staged Files ===");

        for (String fileName: added) {
            System.out.println(fileName);
        }

        System.out.println();
        System.out.println("=== Removed Files ===");

        for (String fileName: removed) {
            System.out.println(fileName);
        }
        System.out.println();


        statusSecondHalf();
    }

    public void statusSecondHalf() {
        StagingArea s = getStagingArea();
        List<String> allCWDFiles = Utils.plainFilenamesIn(_CWD);
        List<String> mNSFC = new ArrayList<>();
        TreeMap<String, String> headBlobs1 = getHeadCommit().getBlobs();

        for (String blobName: headBlobs1.keySet()) {
            File cwdFile = Utils.join(_CWD, blobName);
            if (cwdFile.exists()) {
                if (!headBlobs1.get(blobName).equals(getSha1String(cwdFile))
                        && !s.getToBeCommited().containsKey(blobName)
                        && !s.getToBeRemoved().containsKey(blobName)) {
                    mNSFC.add(blobName + " (modified)");

                }
            }
        }
        for (String f: s.getToBeCommited().keySet()) {
            if (!s.getToBeRemoved().containsKey(f)
                    && !allCWDFiles.contains(f)) {
                mNSFC.add(f + " (deleted)");
            }
        }
        for (String f: headBlobs1.keySet()) {
            if (!s.getToBeRemoved().containsKey(f)
                    && !allCWDFiles.contains(f)) {
                mNSFC.add(f + " (deleted)");
            }
        }
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String p: mNSFC) {
            System.out.println(p);
        }
        System.out.println();



        List<String> untrackedFiles = new ArrayList<>();
        Set<String> headBlobs = getHeadCommit().getBlobs().keySet();

        for (String f: allCWDFiles) {
            if (!headBlobs.contains(f) && !s.getToBeCommited().containsKey(f)) {
                untrackedFiles.add(f);
            }
        }

        System.out.println("=== Untracked Files ===");
        for (String f: untrackedFiles) {
            System.out.println(f);
        }

    }

    /**
     * @param branchName */
    public void branch(String branchName) {
        List<String> branches = Utils.plainFilenamesIn(COMMIT_TREE_FOLDER);
        if (branches.contains(branchName + ".txt")) {
            System.out.println("A branch with that name already exists.");
            return;
        }

        File newBranch = Utils.join(COMMIT_TREE_FOLDER,
                branchName + ".txt");


        String whatHeadIsPointingTo = Utils.readContentsAsString(
                Utils.join(COMMIT_TREE_FOLDER, "HEAD.txt"));
        String commitIDOfThat = Utils.readContentsAsString(
                Utils.join(COMMIT_TREE_FOLDER,
                whatHeadIsPointingTo + ".txt"));
        Utils.writeContents(newBranch, commitIDOfThat);
    }

    /**
     * @param branchName */
    public void rmBranch(String branchName) {
        List<String> branches = Utils.plainFilenamesIn(COMMIT_TREE_FOLDER);
        if (!branches.contains(branchName + ".txt")) {
            System.out.println("A branch with that name does not exist.");
            return;
        }

        String whatHeadIsPointingTo = Utils.readContentsAsString(
                Utils.join(COMMIT_TREE_FOLDER, "HEAD.txt"));

        if (whatHeadIsPointingTo.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }

        File branch = Utils.join(COMMIT_TREE_FOLDER, branchName + ".txt");

        branch.delete();
    }

    /**
     * @param commitID */
    public void reset(String commitID) {
        File com = Utils.join(COMMITS_FOLDER, commitID + ".txt");
        if (!com.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }

        Commit branchCommit = Utils.readObject(com, Commit.class);
        List<String> workingDirFiles = new ArrayList<>();

        Commit c = getHeadCommit();

        for (String s: Utils.plainFilenamesIn(_CWD)) {
            if (s.contains(".txt")) {
                workingDirFiles.add(s);
            }
        }

        for (String fName: workingDirFiles) {
            if (!c.getBlobs().keySet().contains(fName)
                    && branchCommit.getBlobs().keySet().contains(fName)) {
                System.out.println("There is an untracked file in the way; "
                        +
                        "delete it, or add and commit it first.");
                return;
            }
        }

        for (String fName: workingDirFiles) {
            if (c.getBlobs().keySet().contains(fName)
                    && !branchCommit.getBlobs().keySet().contains(fName)) {
                Utils.restrictedDelete(fName);
            }
        }

        for (String file: branchCommit.getBlobs().keySet()) {
            String fileHash = branchCommit.getBlobs().get(file);
            File blobFile = Utils.join(BLOB_FOLDER, fileHash + ".txt");
            byte[] blobContent = Utils.readContents(blobFile);
            Utils.writeContents(Utils.join(_CWD, file), blobContent);
        }

        StagingArea s = getStagingArea();
        s.getToBeCommited().clear();
        s.getToBeRemoved().clear();
        Utils.writeObject(STAGING_FILE, s);

        Utils.writeContents(
                Utils.join(COMMIT_TREE_FOLDER, _HEAD + ".txt"), commitID);

    }

    public void failureCases(String branchName, StagingArea s) {
        _fail = false;
        if (!s.getToBeCommited().isEmpty() || !s.getToBeRemoved().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            _fail = true;
            return;

        }
        List<String> branches = Utils.plainFilenamesIn(COMMIT_TREE_FOLDER);
        if (!branches.contains(branchName + ".txt")) {
            System.out.println("A branch with that name does not exist.");
            _fail = true;
            return;
        }
        String headBranchName = Utils.readContentsAsString(
                Utils.join(COMMIT_TREE_FOLDER, "HEAD.txt"));
        if (headBranchName.equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            _fail = true;
            return;
        }
    }

    /**
     * @param branchName */
    public void merge(String branchName) {
        StagingArea s = getStagingArea();
        failureCases(branchName, s);
        if (_fail) {
            return;
        }
        Commit current = getHeadCommit();
        String commitIDofbranch = Utils.readContentsAsString(
                Utils.join(COMMIT_TREE_FOLDER, branchName + ".txt"));
        Commit given = Utils.readObject(
                Utils.join(COMMITS_FOLDER, commitIDofbranch + ".txt"),
                Commit.class);
        List<String> workingDirFiles = new ArrayList<>();
        for (String name: Utils.plainFilenamesIn(_CWD)) {
            if (name.contains(".txt")) {
                workingDirFiles.add(name);
            }
        }
        for (String fName: workingDirFiles) {
            if (!current.getBlobs().keySet().contains(fName)
                    && given.getBlobs().keySet().contains(fName)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                return;
            }
        }
        Object[] get = getTwoLists(current, given);
        @SuppressWarnings("unchecked")
        String splitID =
                getSplitPoint((List<String>) get[0], (List<String>) get[1]);
        String headID = Utils.readContentsAsString(
                Utils.join(COMMIT_TREE_FOLDER, _HEAD + ".txt"));
        if (splitID.equals(commitIDofbranch)) {
            System.out.println("Given branch is an ancestor of the current "
                    + "branch.");
            return;
        } else if (splitID.equals(headID)) {
            System.out.println("Current branch fast-forwarded.");
            Utils.writeContents(Utils.join(COMMIT_TREE_FOLDER,
                    _HEAD + ".txt"), commitIDofbranch);
            checkout3(new String[]{"checkout", branchName});
            Utils.restrictedDelete(new File("f.txt"));
            return;
        }
        Commit split = Utils.readObject(
                Utils.join(COMMITS_FOLDER,
                        splitID + ".txt"), Commit.class);
        TreeMap<String, String> currentBlobs = current.getBlobs();
        TreeMap<String, String> givenBlobs = given.getBlobs();
        TreeMap<String, String> splitBlobs = split.getBlobs();
        if (splitBlobs.size() == 0
                && new File("f.txt").exists()) {
            new File("f.txt").delete();
        }
        TreeMap<String, String> mergedBlobs = new TreeMap<>();
        mergeDriver(splitBlobs, currentBlobs, givenBlobs, mergedBlobs);
        finish(branchName, s, current, given, mergedBlobs);
    }

    public void finish(String branchName, StagingArea s,
                       Commit one, Commit two, TreeMap<String, String> a) {
        String message = "Merged " + branchName + " into " + _HEAD + ".";
        Commit newMergeCom = new Commit(message, one, two, a);
        Utils.writeObject(Utils.join(COMMITS_FOLDER,
                        newMergeCom.getsha1() + ".txt"),
                newMergeCom);
        Utils.writeContents(Utils.join(COMMIT_TREE_FOLDER, _HEAD + ".txt"),
                newMergeCom.getsha1());
        s.getToBeCommited().clear();
        s.getToBeRemoved().clear();
        Utils.writeObject(STAGING_FILE, s);
    }

    public Object[] getTwoLists(Commit current, Commit given) {
        _currentCommits.clear();
        Commit currentlog = current;
        _currentCommits.add(currentlog.getsha1());
        getCurrCommIDs(currentlog);
        List<String> commitIDsOfcurrent = _currentCommits;
        _givenCommits.clear();
        Commit givenlog = given;
        _givenCommits.add(givenlog.getsha1());
        getGivenCommIDs(givenlog);
        List<String> commitIDsOfGiven = _givenCommits;

        return new Object[]{commitIDsOfcurrent, commitIDsOfGiven};
    }

    public void mergeDriver(TreeMap<String, String> splitBlobs,
                            TreeMap<String, String> currentBlobs,
                            TreeMap<String, String> givenBlobs,
                            TreeMap<String, String> mergedBlobs) {
        while (splitBlobs.size() != 0 || currentBlobs.size() != 0
                || givenBlobs.size() != 0) {
            String splitfileName =
                    getCurrFile(splitBlobs, currentBlobs, givenBlobs);
            fileinAllBlobs(splitBlobs, currentBlobs,
                    givenBlobs, mergedBlobs, splitfileName);
            if (currentBlobs.containsKey(splitfileName)
                    && !givenBlobs.containsKey(splitfileName)
                    && !splitBlobs.containsKey(splitfileName)) {
                mergedBlobs.put(splitfileName,
                        currentBlobs.get(splitfileName));
                currentBlobs.remove(splitfileName);
            } else if (givenBlobs.containsKey(splitfileName)
                    && !currentBlobs.containsKey(splitfileName)
                    && !splitBlobs.containsKey(splitfileName)) {
                Utils.writeContents(new File(splitfileName),
                        Utils.readContentsAsString(
                                Utils.join(BLOB_FOLDER,
                                        givenBlobs.get(splitfileName)
                                                + ".txt")));
                mergedBlobs.put(splitfileName, givenBlobs.get(splitfileName));
                givenBlobs.remove(splitfileName);
            } else if (!givenBlobs.containsKey(splitfileName)
                    && currentBlobs.containsKey(splitfileName)
                    && splitBlobs.containsKey(splitfileName)
                    && currentBlobs.get(splitfileName).
                    equals(splitBlobs.get(splitfileName))) {
                splitBlobs.remove(splitfileName);
                currentBlobs.remove(splitfileName);
                new File(splitfileName).delete();
            } else if (givenBlobs.containsKey(splitfileName)
                    && !currentBlobs.containsKey(splitfileName)
                    && splitBlobs.containsKey(splitfileName)
                    && givenBlobs.get(splitfileName).
                    equals(splitBlobs.get(splitfileName))) {
                splitBlobs.remove(splitfileName);
                givenBlobs.remove(splitfileName);
                new File(splitfileName).delete();
            } else if ((splitBlobs.containsKey(splitfileName)
                    && currentBlobs.containsKey(splitfileName)
                    && !givenBlobs.containsKey(splitfileName))
                    && !splitBlobs.get(splitfileName).
                    equals(currentBlobs.get(splitfileName))) {
                System.out.println("Encountered a merge conflict.");
                File currentf = Utils.join(BLOB_FOLDER,
                        currentBlobs.get(splitfileName) + ".txt");
                String conflict = "<<<<<<< HEAD\n"
                        + Utils.readContentsAsString(currentf)
                        + "=======\n"
                        + ">>>>>>>\n";
                Utils.writeContents(new File(splitfileName), conflict);
                mergedBlobs.put(splitfileName,
                        getSha1String(new File(splitfileName)));
                splitBlobs.remove(splitfileName);
                currentBlobs.remove(splitfileName);
            }
        }
    }


    public void fileinAllBlobs(TreeMap<String, String> splitBlobs,
                               TreeMap<String, String> currentBlobs,
                               TreeMap<String, String> givenBlobs,
                               TreeMap<String, String> mergedBlobs,
                               String splitfileName) {
        if (givenBlobs.keySet().contains(splitfileName)
                && currentBlobs.keySet().contains(splitfileName)) {
            if (splitBlobs.get(splitfileName).
                    equals(currentBlobs.get(splitfileName))
                    && !splitBlobs.get(splitfileName).
                    equals(givenBlobs.get(splitfileName))) {
                mergedBlobs.put(splitfileName,
                        givenBlobs.get(splitfileName));
                Utils.writeContents(new File(splitfileName),
                        Utils.readContentsAsString(Utils.join(BLOB_FOLDER,
                                givenBlobs.get(splitfileName)
                                        + ".txt")));
                rmFromAllMaps(splitBlobs, currentBlobs,
                        givenBlobs, splitfileName);
            }
            if (!splitBlobs.get(splitfileName).
                    equals(currentBlobs.get(splitfileName))
                    && splitBlobs.get(splitfileName).
                    equals(givenBlobs.get(splitfileName))) {
                mergedBlobs.put(splitfileName,
                        currentBlobs.get(splitfileName));
                rmFromAllMaps(splitBlobs, currentBlobs,
                        givenBlobs, splitfileName);
            }
            if (!splitBlobs.get(splitfileName).
                    equals(currentBlobs.get(splitfileName))
                    && !splitBlobs.get(splitfileName).
                    equals(givenBlobs.get(splitfileName))
                    && givenBlobs.get(splitfileName).
                    equals(currentBlobs.get(splitfileName))) {
                mergedBlobs.put(splitfileName,
                        currentBlobs.get(splitfileName));
                rmFromAllMaps(splitBlobs, currentBlobs,
                        givenBlobs, splitfileName);
            }
            if (!splitBlobs.get(splitfileName).
                    equals(currentBlobs.get(splitfileName))
                    && !splitBlobs.get(splitfileName).
                    equals(givenBlobs.get(splitfileName))
                    && !givenBlobs.get(splitfileName).
                    equals(currentBlobs.get(splitfileName))) {
                System.out.println("Encountered a merge conflict.");
                File currentf = Utils.join(BLOB_FOLDER,
                        currentBlobs.get(splitfileName) + ".txt");
                File givenf = Utils.join(BLOB_FOLDER,
                        givenBlobs.get(splitfileName) + ".txt");
                String conflict = "<<<<<<< HEAD\n"
                        + Utils.readContentsAsString(currentf)
                        + "=======\n" + Utils.readContentsAsString(givenf)
                        + ">>>>>>>\n";
                Utils.writeContents(new File(splitfileName), conflict);
                mergedBlobs.put(splitfileName,
                        getSha1String(new File(splitfileName)));
                rmFromAllMaps(splitBlobs, currentBlobs,
                        givenBlobs, splitfileName);
            }
        }
    }

    public void rmFromAllMaps(TreeMap<String, String> splitBlobs,
                              TreeMap<String, String> currentBlobs,
                              TreeMap<String, String> givenBlobs,
                              String splitfileName) {
        splitBlobs.remove(splitfileName);
        givenBlobs.remove(splitfileName);
        currentBlobs.remove(splitfileName);
    }


    public void getCurrCommIDs(Commit c) {
        Commit start = c;
        if (c != null && c.getParent1() == null && c.getParent2() == null) {
            return;
        } else if (c.getParent1() != null) {
            _currentCommits.add(c.getParent1().getsha1());
            getCurrCommIDs(c.getParent1());
        } else if (c.getParent2() != null) {
            _currentCommits.add(c.getParent2().getsha1());
            getCurrCommIDs(c.getParent2());
        }
    }

    public void getGivenCommIDs(Commit c) {
        if (c != null && c.getParent1() == null && c.getParent2() == null) {
            return;
        } else if (c.getParent1() != null) {
            _givenCommits.add(c.getParent1().getsha1());
            getGivenCommIDs(c.getParent1());
        } else if (c.getParent2() != null) {
            _givenCommits.add(c.getParent2().getsha1());
            getGivenCommIDs(c.getParent2());
        }
    }

    public String getCurrFile(TreeMap<String, String> a,
                              TreeMap<String, String> b,
                              TreeMap<String, String> c) {
        if (a.size() != 0) {
            return a.firstKey();
        }
        if (b.size() != 0) {
            return b.firstKey();
        }
        if (c.size() != 0) {
            return c.firstKey();
        }
        return "nothing";
    }

    public String getSha1String(File f) {
        return Utils.sha1(Utils.readContents(f));
    }

    /**Given 2 lists of logs for 2 commits,
     * returns the first split point between them.
     * @param c1Log
     * @param c2Log
     * */
    public String getSplitPoint(List<String> c1Log, List<String> c2Log) {
        for (String givenID: c2Log) {
            for (String currID: c1Log) {
                if (currID.equals(givenID)) {
                    return currID;
                }
            }
        }
        return "Error finding split point";
    }

    public Commit getHeadCommit() {
        String latestCommitHash =
                Utils.readContentsAsString(
                        Utils.join(COMMIT_TREE_FOLDER, _HEAD + ".txt"));

        return Utils.readObject(Utils.join(COMMITS_FOLDER,
                latestCommitHash + ".txt"), Commit.class);
    }

    public StagingArea getStagingArea() {
        return Utils.readObject(
                Utils.join(STAGING_FOLDER,
                        "stagingObject.txt"), StagingArea.class);
    }

    public String shortUID(String branch) {
        List<String> allCommits = Utils.plainFilenamesIn(COMMITS_FOLDER);
        List<String> allCommitsWOext = new ArrayList<>();
        for (String com: allCommits) {
            allCommitsWOext.add(com.substring(0, com.length() - 4));
        }
        for (String comName: allCommitsWOext) {
            if (comName.contains(branch)) {
                return comName;
            }
        }
        return branch;
    }

    /**Special boolean for merge exceptions.*/
    private boolean _fail = false;

    /**TREEMAP FOR FINDING SPLIT POINT RECURSEVLY OF CURRENT COMMITS.*/
    private ArrayList<String> _currentCommits = new ArrayList<>();

    /**TREEMAP FOR FINDING SPLIT POINT RECURSEVLY OF CURRENT COMMITS.*/
    private ArrayList<String> _givenCommits = new ArrayList<>();
}
