# NeoGit 

|Student|Project|
|:---:|:---:|
|**Gerardo Gullo 0522501033** | Git Protocol|

## Index
<!--ts-->
* [Introduction](#Introduction)
* [Problem Statement](#Problem-Statement)
* [Solution Approach](#Solution-Approach)
* [Additional Features](#Additional-Features)
* [Deployment](#Deployment)
  * [Used technologies](#Used-technologies)
  * [Implementation details](#Implementation-details)
  * [Project structure](#Project-structure)
* [Testing](#Testing)
* [How to run](#How-to-run)

<!--te-->

Introduction
============
A peer-to-peer network that implements the basic functions of the Git protocol. Users can create, clone, add files, edit a repository.
Thanks to the peer-to-peer network the repositories are shared and each user can collaborate on each repository.

Problem Statement
================
Design and develop the Git protocol, distributed versioning control on a P2P network.
Each peer can manage its projects (a set of files) using the Git protocol (a minimal version). 
The system allows the users to create a new repository in a specific folder, add new files to be tracked by the system, 
apply the changing on the local repository (commit function), push the network’s changes, and pull the changing from the network.
The git protocol has lot-specific behavior to manage the conflicts; in this version, it is only required that if there are some conflicts,
the systems can download the remote copy, and the merge is manually done. 
As described in the [GitProtocol Java API](https://github.com/spagnuolocarmine/distributedsystems-unisa/blob/master/homework/GitProtocol.java).

Solution Approach
=================
A file system has been developed for the correct management of the repositories.

Each repository has its own file, `data.ng`, which will be generated at the time of creation or at the moment of its cloning from the network and will be stored inside the
repository folder.

All changes made by the network user are stored in this file.

On the other hand, when a peer on the network starts up, another file, `repos.ng`, is created or loaded into memory, which contains the paths relative to the "data.ng" files of each repository.

This system allows you not to lose the changes made in previous peer runs.

Additional Features
=============
In addition to the basic operations defined by the [GitProtocol Java API](https://github.com/spagnuolocarmine/distributedsystems-unisa/blob/master/homework/GitProtocol.java), I have been developed:

1. Clone Repository.
2. Show Local History, to view the commits made in a repository
3. Show file in Repository, to view all files present in a Repository

So you can view the new interface [here](./src/main/java/it/unisa/neogit/GitProtocol.java).

Deployment
========

Used technologies
-----------

- IntelliJ IDEA
- Apache Maven
- Tom P2P
- Java
- JUnit
- Docker

Implementation details
--------------------
As described in the upper sections, files are used for managing repositories. To avoid that the system for each operation to a repository has to load the information contained in its data.ng file, an instance variable is used which keeps the repository object currently used. Obviously, before carrying out any operation, it is first verified that the repository matches the one with which we really want to carry out an operation.

Project structure
---------------

- src/main/java/it/unisa/neogit/entity
  
  *package that contains all entities needing to create a peer-to-peer repository*

  - _**Commit.java**_: Class that models a commit of a repository
  - _**Repository.java**_: Class that models a local repository
  - _**RepositoryP2P.java**_: Class that extends a repository to model a remote repository.
- src/main/java/it/unisa/neogit/
  - _**GitProtocol.java**_: Interface for the methods needed for some features of the git protocol.
  - _**NeoGit.java**_: Concrete class that implements the interfaces defined in GitProtocol.
  - _**NeoGitRunner.java**_: Startup class.
- _**src/test/java/it/unisa/neogit/NeoGitTest.java**_: Testing Class.

- **Dockerfile**: A text document that contains all the commands a user could call on the command line to assemble an image.
- _**pom.xml**_: File needed for a maven project. It contains the dependencies and instructions for creating the `.jar`.

Testing
======
The functionality test was done through the jUnit library. For the execution of the tests, a sandbox was created that separated the execution environment of each test case.
In fact, each test case uses a unique test repository for that test, so as not to be affected by other executions. A directory for each peer is created before the tests are run. This allows you to prevent files written by one peer from conflicting with those of some other peer. At the end of the test run these directories are removed.
Finally, to simulate the creation of files and their contents, an ad-hoc method has been developed which creates a file and, if required, populates it with a unique string.

How to run
======

This application is provided using Docker container, running on a local machine. See the Dockerfile, for the building details.

First you have to build your docker container:

```docker build --no-cache -t neogit  .```

#### Start the master peer

**⚠️  Before carrying out the deployment of the various peers it would be advisable to create a different folder for each of them. So that you can properly manage each peer and its files.**

After that you can start the master peer using the below command:

```docker run -i --rm -v $pwd/master:/home --name MASTER -e MASTERIP="127.0.0.1" -e ID=0 -e WB=/home neogit```

|options|Description|
|:---:|:---:|
|--rm|Automatically remove the container when it exits
|--name|Assign a name to the container
|-i|Keep STDIN open even if not attached
|-v|Bind mount a volume
|-e|Set environment variables

**🛠️ --rm and -v they can be optional**

Through this command I start a peer that will act as master. Note that a volume has been attached in order to map the `/master` path to the `/home` path of the container.
_**This operation allows you to easily add a file into the repository.**_

|variable|Description|
|:---:|:---:|
|MASTERIP|the master peer ip address
|ID|the **unique** id of your peer
|-i|Keep STDIN open even if not attached
|-v|Bind mount a volume
|-e|Set environment variables

**⚠️ Remember you have to run the master peer using the ID=0.**

**Note that**: after the first launch **if you haven't used the --rm flag**, you can launch the master node using the following command:
```docker start -i MASTER```.

#### Start a generic peer

When master is started you have to check the ip address of your container:

- Check the docker <container ID>: ```docker ps```
- Check the IP address: ```docker inspect <container ID>```

Now you can start your peers varying the unique peer id:

```docker run -i --rm -v $pwd/peer-1:/home --name PEER-1 -e MASTERIP="172.17.0.2" -e ID=1 -e WB=/home neogit```

**Note that**: after the first launch **if you haven't used the --rm flag**, you can launch the master node using the following command:
```docker start -i PEER-1```.