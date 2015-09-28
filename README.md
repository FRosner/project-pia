### Requirements

- ProtocolBuffers 2.5.0

### Setup

1. Launch R
    1. Install Rserve if not installed `install.packages("Rserve")`
    1. Start Rserve `library(Rserve); Rserve()`

1. Launch application

### Send messages via curl

`curl --data-binary "@<file-name>" <interface>:<port>`
