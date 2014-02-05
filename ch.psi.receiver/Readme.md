#Overview
The receiver package provides an easy to use way to receive streamed files via ZMQ.


# Usage



## Receiver Server
The receiver server can be started via the 'receiver' script located in the `bin` folder.

The usage of the `receiver` is as follows:

```
Usage: receiver
 -h         Help
 -p <arg>   Webserver port (default: 8080)
 -d <path>	Base path for storing relative files [required]
```

## Reciever Server - UI
Receiver comes with a web UI. One can use this UI to list and manage current receivers.
The web UI is accessible on `http://<host>:8080/static/` . Note: It is important to have the last / inside the url!

# Development

## Build
The project is build via Maven. The installation zip package can be build by executing `mvn clean compile assembly:assembly` . 
After the build the zip file will be inside the `target` directory.

## REST API

Get version of server

```
GET version
200 - 0.0.0
```

Get list of active receivers

```
GET receiver
Accept: application/json

200 - [ ]
```

Register for receiver changes 

```
GET events
 
200 - Server Send Event (SSE) stream.
```

****

Messages currently supported:

* **receiver** - list of current streams

    ```
    [ ]
```

****

Start new receiver

```
PUT receiver/{id}

{
	"hostname":"",
    "port":8888,
    "numberOfImages":0
    
}

204 Stream created
```
* The `numberOfImages` property is optional


Terminate receiver

```
DELETE receiver/{id}

200 - 
```

Wait for receiver to terminate after configured number of images are received

```
GET receiver/{id}/done

200 -
``` 

Get receiver status

```
GET receiver/{id}

200 -
{
    "messagesReceived": 0,
    "configuration": {
        "hostname": "x10sa-pd-1-v132.psi.ch",
        "port": 9988,
        "numberOfImages": null
    }
}

```

Reset status/counters of all receivers

```
PUT receiver/reset

204 - Success
```

### Command Line

Create stream

```
curl -XPUT --data '{""hostname":"somehost", "port":8888, "numberOfImages":10}' --header "Content-Type: application/json" http://<hostname>:<port>/receivers/<id>
```

Stop receiver

```
curl -XDELETE http://<hostname>:<port>/receivers/<id>
```

Get active receivers

```
curl -H "Accept: application/json" http://<hostname>:<port>/receivers
```

Get active receivers (monitor)

```
curl http://<hostname>:<port>/receivers
```


# Installation
The **Receiver** package required Java 7 or greater.

## Simple Installation
Extract zip file

## Daemon Installation

```
mkdir /opt/receiver
cd /opt/receiver
unzip ch.psi.receiver-<version>-bin.zip
ln -s ch.psi.receiver-<version> latest
```

Register Init Script

```
cp latest/var/receiver /etc/init.d/
chmod 755 /etc/init.d/receiver
```

On RHEL Linux

```
chkconfig --add receiver
chkconfig receiver on
```

On Ubuntu Linux

```
update-rc.d receiver defaults
```

Start/Stop Receiver Service

```
service receiver start
service receiver stop
```