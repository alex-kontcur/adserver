To build and start test application you need java 7 and maven-3.0.4 installed on your system, next:
    1. Run build.cmd
    2. Run target/adserver-1.0.0-SNAPSHOT/bin/adserver(.bat)

Usage:
1.  Server is ready on http://localhost:8080 All server properties can be found in the "conf/adserver.conf" configuration file

2.  Now supported only one runtime property: "ads.path" -> path for ads storing for external service

3.  To change runtime property use settings from "conf/adserver.conf" -> "runtime.properies.path" - it defines folder for runtime accepting of new runtime 
property value. Simply copy file with name "ads.path" to that folder. This file should contain new value of "ads.path" (here, full path for new ads). 
To check current values of runtime properties use the following URL: http://localhost:8080/config

4. Since you put ads to folder they are started to indexing for further searching: http://localhost:8080/search/{token}

5. To obtain ad's content use http://localhost:8080/banner/{id}

6. To get actual statistics use the following :

   - top statistics: http://localhost:8080/stat1/{24|4|1|1m}
   - bottom statistics: http://localhost:8080/stat2/{24|4|1|1m}

7. To avoid indexing duplication of ads on the next starting of server use some not memory based DB ("Database configuration" section in "conf/adserver.conf")
So memory structure will be filled with db values of previous ads processing while server startup. 
This will help to bypass excess indexing of already indexed entities.