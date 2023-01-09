# Gitlet Design Document
author: Claire Ding



## 1. Classes and Data Structures

### Main.java
This class is the entry point of the program. It implements methods to set up persistance and support each command of the program.
It takes in the input like "init/add/commit/checkout" etc and calls its corresponding method in repo respectively.

####Fields:
1. static File CWD: a pointer to the current working directory of the program

### Commits.java
This class represents a commit instance. If the file to be committed has different current content than the content of being staged, then throw error.
####Fields:
2. ?static variable HEAD pointer (created at first commit)
3. ?static variable Master pointer (created at first commit)
4. private variable String meCode: SHA1 code of the given commit
5. private variable String time: Timestamp of the given commit
6. private variable String message: Commit message when created
7. private variable HashMap myBlob: HashMap that store all the blob this commit has.
8. private variable String mommy: SHA1 code of the parent of commit.

### StagingArea.java
stage a file: at the time that add is called, the file[file name] has this content[blob].
####Fields:
1.Hashmap addList: contains all the files staged for addition.
2.Hashmap reList: contains all the files staged for removal.
### repo.java

####Fields:
1. init method as a constructor
   1. new staging field create
   2. new commit field create
   3. static variable: HEAD (poniter) point to the current version
   4. static variable: Master(pointer) 
   5. starts a new staging area(mkdir)
   6. starts a new commit area(mkdir)
   7. starts a new blob area(mkdir)
2. checkout method(three different inputs)
   1. changing HEAD pointer
   2. throw error
3. status method
4. reset method
   1. changing pointers
   2. changing staging field, blob field, and commits field
   3. 

### Blob.java
A folder that stores all files（each is the snapshot of file content at the time of staging).
####Fields:
1. String name: name of the file
2. String contentWords: contents of the blob
3. String blobCode: SHA1 code of the blob
4. byte[] contentByte: contents of file as byte array

### Commits.java
#### Fields:
1.public get commit id method 
2.public get timestamp method 
3. public get commit message method
4. public get parent commit id method
5. public get pointer blob method
6. public get split point

## 2. Algorithms
### Repo
####
1. init method as a constructor
   1. new staging field create
   2. new commit field create
   3. static variable: HEAD (poniter) point to the current version
   4. static variable: Master(pointer)
   5. starts a new staging area(mkdir)
   6. starts a new commit area(mkdir)
   7. starts a new blob area(mkdir)
2. checkout method(three different inputs)
   1. changing HEAD pointer
   2. throw error
3. status method
4. reset method
   1. changing pointers
   2. changing staging field, blob field, and commits field
   
### Stage
####
1. add (takes in file name)
2. remove (take in file name and the current blob pointer)
3. clear: clear both the add and remove stage




## 3. Persistence



## 4. Design Diagram


