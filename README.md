# files


This project shows how to do file uploading and file downloading in a client-server environment


This project is using Maven and is a multi module project. 


The project is composed of several modules:

* The **API** module is used to share any POJO shared between modules, like DTO or protobuf classes.
* The **Storage** module is used to share logic used to handle StorageServices, in this case an AWS Storage class was implemented that uploads and download files from an S3 bucket.
* The **Crypto** module is used to put all the logic used to handle the Encryption/Decryption techniques used by the project. 
* The **Files** module is a backend server implementation that uses Spring Bootm that's provides a REST API, and GRPC server, communicates with a MongoDB database and so on.
* The **Client** module is a CLI application meant to be used so far only with the GRPC server, but this client module can encrypt, decrypt and generate a key to secure files.


## Compiling instructions

Note, this project assumes you have installed the AWS CLI and a valid set of credentials in your .aws folder in your homedirectory


This project uses Java 11, Maven, Docker

Java version

```
openjdk version "11.0.11" 2021-04-20
OpenJDK Runtime Environment AdoptOpenJDK-11.0.11+9 (build 11.0.11+9)
OpenJDK 64-Bit Server VM AdoptOpenJDK-11.0.11+9 (build 11.0.11+9, mixed mode)
```

Maven version

```
Maven home: /usr/local/Cellar/maven/3.8.2/libexec
Java version: 11.0.11, vendor: AdoptOpenJDK, runtime: /Users/magg/.sdkman/candidates/java/11.0.11.hs-adpt
Default locale: en_MX, platform encoding: UTF-8
OS name: "mac os x", version: "10.16", arch: "x86_64", family: "mac"
```

Docker version

```
Client:
 Cloud integration: v1.0.20
 Version:           20.10.10
 API version:       1.41
 Go version:        go1.16.9
 Git commit:        b485636
 Built:             Mon Oct 25 07:43:15 2021
 OS/Arch:           darwin/amd64
 Context:           default
 Experimental:      true

Server: Docker Engine - Community
 Engine:
  Version:          20.10.10
  API version:      1.41 (minimum version 1.12)
  Go version:       go1.16.9
  Git commit:       e2f740d
  Built:            Mon Oct 25 07:41:30 2021
  OS/Arch:          linux/amd64
  Experimental:     false
 containerd:
  Version:          1.4.11
  GitCommit:        5b46e404f6b9f661a205e28d59c982d3634148f8
 runc:
  Version:          1.0.2
  GitCommit:        v1.0.2-0-g52b36a2
 docker-init:
  Version:          0.19.0
  GitCommit:        de40ad0
```

## Usage guide

So the first step after cloning the repository using Git is the compile the project

`mvn clean install`

or if you want to skip the tests, you can run

`mvn clean install -DskipTests=true`


Next step is to start up the Docker container for our Mongo database, using the next command

`docker-compose up`

The next step is to run the backend server, please cd into the files directory

```
cd files
mvn spring-boot:run
```

Once these steps have been completed you can use the client module like so:


1. Generate a secret key:

`java -jar client/target/client-0.0.1-SNAPSHOT.jar -g test`


2. Using the key to encrypt a file and upload the encrypted file (this step will return an a File ID which can be used to download the file)

`java -jar client/target/client-0.0.1-SNAPSHOT.jar -k /Users/magg/.file-keys/test.key -f /Users/magg/Desktop/Snapsave_425834755704789_720p.mp4.zip`

3. Download a file and decrypt it:

`java -jar client/target/client-0.0.1-SNAPSHOT.jar -k /Users/magg/.file-keys/test.key -i 61c028c2f763eb10cf18a0a4`


4. (Optional step) you could potentially just decrypt a file:

`java -jar client/target/client-0.0.1-SNAPSHOT.jar -k /Users/magg/.file-keys/test.key -d /Users/magg/Downloads/Snapsave_425834755704789_720p.mp4_encrypted.zip`



The Backend server also offers a REST API, it has two endpoints

1. http://localhost:8080/upload (Needs a POST request with the "file" key sent as form-data)
2. http://localhost:8080/download/${fileID} (GET request that needs the file ID)




TODOs
* Document all the code.
* Add automated tests
* Maybe replace AWS S3 with a system like MinIO



