# Gitlet Design Document
author: Varun Kandula


## 1. Classes and Data Structures


##Commit Class
This class makes the Commit objects. It represents a commit in
a branch of commits

####Instance Variables
1. HashMap<String SHA-1 code, Blob> -> contains all the blobs for a specific commit
2. Message
3. String Parent


##Staging Class

Contains (added) Blobs to be commited.


- Contains List of Blobs for commiting
- Cotains List of messages corresponding to Blobs for commiting
- Messages for Commiting
- Added Blobs are stored in a directory before running the commit command
- Creates blobs which are file objects in the blob directory 



###Methods
- Method that creates blobs
  - Compare added file contents to most recent commit
  - if not the same:
    - Generate UID from file contents of added file
    - Make a file object named after UID with its path in the Blob directory and its contents as the current file that is added to the staging area.  




## CommitTree Class

This class contains all the commits that are made in the repo. 

###Instance/Static Variables
- HEAD pointer (Type Commit)
- Master pointer (Type Commit)
- List of (String SHA-1 codes) UID's for commits
- Master Pointer
- HEAD Pointer ()


###Constructor
- Create Commit 0
- Initialize HEAD and Master pointers and assign to Commit 0
- Create a List of UID'S

###Methods
- Method that takes a commit object and:
  - Gets object UID, put inside the commit directory
  - fill it with the sereialized commit object
  - Add UID

- Method that adds files to list
  - Take UID and adds to 



##Gitlet Class

Constructor (Only method in class)
- Creates .gitlet repositiry 
- Single object initalized in main
- Creates a commit directory
- Creates a BLOB directory


Future Classes:
- Class for every command which accesses 



## 2. Algorithms


###Main 

- Have a series of switch cases to see which command is being run and enters the object that is assoicated with the specific command. 


## 3. Persistence


###java gitlet.Main init
- Initializes the git repository before allowing the user to run any other commands. 
- If other commands are ran before running init, system will return error. 

###java gitlet.Main add [filename]
- Puts modified blobs in staging directory
- Calls staging class


###java gitlet.Main commit [filename] -m [message]
- works with the Commit class and blob folder to store the commits in a structured manner

###java gitlet.Main checkout -- [filename]
- Takes the version of the file as it exists in the head commit, the front of the current branch, and puts it in the working directory, overwriting the version of the file that's already there if there is one. The new version of the file is not staged.




## 4. Design Diagram


![](../../../../../var/folders/0j/1rbzs0w969b35bpx9b0z7j6m0000gn/T/TemporaryItems/NSIRD_screencaptureui_a04VrS/Screen Shot 2022-04-14 at 6.04.35 PM.png)



