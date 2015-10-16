### Architecture

![image](https://cloud.githubusercontent.com/assets/3427394/10540156/b32fbeda-7405-11e5-9738-fd9c037deb1c.png)

### Requirements

- ProtocolBuffers 2.6.0

### Setup

1. Launch R
    1. Install Rserve if not installed `install.packages("Rserve")`
    1. Start Rserve `library(Rserve); Rserve()`

2. Build application `sbt assembly`
2. `cp -R src/main/protobuf client/resources/public/`
3. Provide `init.R` and `predict.R`
3. Launch application `java -jar target/scala-2.10/project-pia-assembly-0.1.0-SNAPSHOT.jar`
4. Compile javascript in `client` folder: `lein cljsbuild once dev`
5. Launch simple static server in `client/resources/public` folder: `python -m SimpleHTTPServer 9292`

### System Properties

| Property                           | Function                                         |
|------------------------------------|--------------------------------------------------|
| `pia.concurrentRConnections`       | Number of concurrent connections to the R server |
| `pia.rServerInterface`             | Interface of the R server |
| `pia.rServerPort`                  | Port of the R server |
| `pia.script.init`                  | Location of the init R script |
| `pia.script.predict`               | Location of the predict R script |

### REST API

| URI | Method | Action (Response Code) |
|---|---|---|
| `/predictions`      | GET | return array of all available prediction result ids (200) |
| `/predictions`      | POST | create prediction job using the given feature vector and return location header containing the endpoint `/predictions/<id>` (201) |
| `/predictions`      | PUT | operation not supported (405) |
| `/predictions`      | DELETE | operation not supported (405) |
| `/predictions/<id>` | GET | prediction result with given id (200), prediction not finished (204), prediction errored (500), id is invalid (404) |
| `/predictions/<id>` | POST | operation not supported (405) |
| `/predictions/<id>` | PUT | operation not supported (405) |
| `/predictions/<id>` | DELETE | operation not supported (405) |

### Send messages via curl

`curl --data-binary "@<file-name>" <interface>:<port>`
