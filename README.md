# Gitlet: A Version Control System

Gitlet is a lightweight version control system that mimics the basic features of Git. This project implements a simplified but functional version control system that helps developers track changes, maintain different versions of their code, and collaborate effectively.

## Features

Gitlet supports the following core version control operations:

- **Saving Snapshots**: Commit your files to save the state of your entire project
- **Restoring Previous Versions**: Checkout previous commits or specific files
- **History Tracking**: View the history of all commits through logs
- **Branch Management**: Create and manage different branches of development
- **Merging**: Combine changes from different branches

## Commands

### Basic Commands
- `init`: Initialize a new Gitlet version-control system
- `add [file name]`: Add a file to the staging area
- `commit [message]`: Save a snapshot of tracked files in the current commit
- `rm [file name]`: Remove a file from tracking
- `log`: Display information about all commits in the current branch
- `global-log`: Display information about all commits ever made

### Status and History
- `status`: Display what branches currently exist, and what files are staged, modified, or untracked
- `find [commit message]`: Find all commits with a given commit message

### Branching and Checkout
- `branch [branch name]`: Create a new branch
- `rm-branch [branch name]`: Remove a branch
- `checkout [args]`: Switch branches or restore files
  - `checkout -- [file name]`: Restore a file to its state in the current commit
  - `checkout [commit id] -- [file name]`: Restore a file to its state in the specified commit
  - `checkout [branch name]`: Switch to a different branch

### Advanced Features
- `reset [commit id]`: Restore all files to their versions in the specified commit
- `merge [branch name]`: Merge the specified branch into the current branch

## Getting Started

1. Initialize a new Gitlet repository:
   ```
   java gitlet.Main init
   ```

2. Start tracking files:
   ```
   java gitlet.Main add [file name]
   ```

3. Make your first commit:
   ```
   java gitlet.Main commit "Initial commit"
   ```

## Project Structure

The project is implemented in Java and uses various data structures and algorithms including:
- SHA-1 cryptographic hashing for commit IDs
- File persistence for storing commits and blobs
- Graph traversal for commit history
- Java collections framework

## Technical Details

- Implements persistent storage of commits and file contents
- Uses SHA-1 hashing for unique commit identification
- Maintains a directed acyclic graph (DAG) of commits
- Handles branch management and merging
- Provides efficient file state tracking

## Requirements

- Java Runtime Environment (JRE)
- Java Development Kit (JDK)

## Contact
Email: varun.kandula@berkeley.edu
LinkedIn: linkedin.com/in/varunkandula/
