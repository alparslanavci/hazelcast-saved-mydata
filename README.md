Hazelcast saved my data! 
=======================================

This application is for demonstrating how Hazelcast (out-of-box configuration) doesn't lose any data when a member crashes on peak load.


How to run
==========
To run web app; run `mvn jetty:run` command on the command line.

Notes:
------
Please note that you need to run a local two-member Hazelcast cluster setup (with 4GB heap per each at least) before you run this application. 


Usage
=====
1. Click on `Start inserting data` button
2. After ~20 secs, force shutdown master node of Redis
3. Click on `Show my data` button to see how much data you inserted. 
4. Click on `Validate` button to see which of the data exists on slave node. Green means data saved, red means data lost. 