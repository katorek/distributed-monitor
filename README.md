## Description


### Prerequires
* Installed [ZeroMQ](http://zeromq.org/area:download)  
* minimum Java 8

#### 

### Mutual exclusion Algorithm
I'm using [Ricart-Agrawala Algorithm](https://en.wikipedia.org/wiki/Ricart%E2%80%93Agrawala_algorithm)
for mutual exclusion 

#### Concept
I'm using ZMQ.Proxy to dynamically create group with processes that will be taken into consideration during requesting Critical Section.


#### New process in system
New process (NP) subscribe to Proxy Publisher port for messages.  
Then it publish message to Proxy Subscirber Port with itself infos (IP and PORT):  "NEW", "IP;PORT".  
Other processes then add the NP's info into List with IP and Port.

#### Requesting CS  
TODO

#### Releasing CS
TODO
