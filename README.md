# ftp_clientserver_udp
This code is the Client-Server model which follows UDP protocol. In this model, client can request following - 

1. GET a list of files and folders in client's current working working directory
    Command used - 'ls'

2. CHANGE the current working directory to both lower and upper levels
     Commands used - 'cd', 'cd ..', 'cd /' etc.
     
3. GET the desired file from Server and write it in its current working directory 
     Command used -
     '''get filename'''
> choose a protocol for transfering 
>> 1 for Stop & wait

> anything other than 1 for GoBackN
>> eg. 2,3 4
    

For file transfer part, all types of files - binary , text, can be transfered from server to client on request. This code implements STOP AND WAIT protocol with timeout inolved and progress of transfering of files can be seen in the console.

### there is a bash script that can be used for excuting java code directly

these are the commands for running the server and client
 
'''
./server.sh
'''
'''
./client.sh
'''
