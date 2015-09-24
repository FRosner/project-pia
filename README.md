### Setup

1. Launch R
    1. Install rJava `install.packages("rJava")`
    1. Get JRI library location `system.file("jri", package="rJava")`

1. Launch application
    1. Set environment variable `R_HOME`
    2. Set system property value `-Djava.library.path` to the JRI library location

### OS Specific Stuff

#### Mac OS

- `R_HOME` might be somewhere in `/Library/Frameworks/R.framework/Versions/<version>/Resources`
