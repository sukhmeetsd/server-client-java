# server-client-java
An FTP like server client duo in Java. It can connect multiple clients to the server.

To run the project follow the following steps:

1.) Place FTPServer.java and FTPClient.java in different folders.

2.) Open a command window (terminal in Mac) where FTPServer.java has been placed.

3.) Run the command - "javac FTPServer.java"

4.) Run the command - "java FTPServer"

5.) Open another command window where FTPClient.java has been placed.

6.) In the client's command window, run the command - "javac FTPCLient.java"

7.) In the client's command window, run the command - "java FTPClient"

8.) In the client's command window, run the command - "ftpclient <hostname> <port>" 

9.) In the client's command window, run the command - "<uname> <pwd>" (Uname is "client<clientNo>". Password is the square of client number)

	Eg: ftpclient localhost 8000
		client1 1
		

