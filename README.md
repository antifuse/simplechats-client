# simplechats-client
![](https://img.shields.io/github/v/release/antifuse/simplechats-client)
## Overview
SimpleChats is a rudimentary chat system that uses Java Sockets. It uses the eponymous protocol, specified below. Don't expect too much from it.
## The Protocol
Messages are sent in the format `TYPE:Field1:Field2:etc`. The type specifies the required number of fields as well as how to interpret the latter. 

The current types (as of Protocol version 1.2) are:
### Server to Client
* **JOIN**: User join message. Format: `JOIN:User`
* **LEAVE**: User leave message (duh). Format: `LEAVE:User`
* **USERLIST**: List of online users. Format: `USERLIST:User1:User2:...`
* **NAMECHANGE**: Nickname change announcement. Format: `NAMECHANGE:OldName:NewName`
* **MESSAGE**: A message to all online users. Format: `MESSAGE:Author:The message's content`
* **DIRECT**: A direct message to the client user. Format: `DIRECT:Author:The direct message`
* **SYSTEM**: A system message, often an error message. Format: `SYSTEM:message.localisation.code`
### Client to server
* **RQ_NICK**: Request for name change. Format: `RQ_NICK:NewUsername`
* **RQ_DISCONNECT**: A disconnect announcement. Format: `RQ_DISCONNECT`
* **RQ_LIST**: A request to list all online users. Format: `RQ_LIST`
* **RQ_SEND**: A request to send a message to all. Format: `RQ_SEND:The message's content`
* **RQ_DIRECT**: A request to send a direct message. Format: `RQ_DIRECT:RecipientName:The direct message`

### Escape character and other rules
As one can see, **quotes and spaces are allowed**. The escape character is `&` (Ampersand), followed by exactly two letters. Escape strings are case-sensitive. Character codes are:
* `&nd` for &
* `&cl` for :
* `&au` for ä, `&ou` for ö, `&uu` for ü and capital letters for their capital counterparts
* `&su` for ß

## The client
The given client is written in JavaFX. Logging is not supported, although some things are still written to stdout (relics of unprofessional debugging I am too lazy to remove).
You can build it yourself with `./gradlew shadowJar` or download the latest Jarfile from the releases section. The client can be started with command line arguments *--defaultIP=1.2.3.4* and/or *--defaultName=Username* to pre-fill the given text fields.
