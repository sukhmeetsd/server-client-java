import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class FTPServer {
	static int sPort = 8000;    //The server will be listening on this port number
	static ServerSocket sSocket;   //serversocket used to listen on port number 8000
	Socket connection = null; //socket for the connection with the client
	String message;    //message received from the client
	String MESSAGE;    //uppercase message send to the client
	DataOutputStream out;  //stream write to the socket
	DataInputStream in;    //stream read from the socket
	static int clientNum;

	public static void main(String args[]) {
        System.out.println("The server is running."); 
        // ServerSocket listener = null;
			clientNum = 1;
        	try {
        			sSocket = new ServerSocket(sPort);
            		while(true) {
                		new Handler(sSocket.accept(),clientNum).start();
						System.out.println("Client "  + clientNum + " is connected!");
						clientNum++;
            		}
        	} 
        	catch(IOException e){
        		e.printStackTrace();
        	}
        	finally {
        		try{
        			sSocket.close();
        		}
        		catch(IOException e){
        			e.printStackTrace();
        		}
            	
        	}  
    }

	


	/**
    * A handler thread class.  Handlers are spawned from the listening
    * loop and are responsible for dealing with a single client's requests.
    */
    private static class Handler extends Thread {
        
	    private String message;    //message received from the client
		private String MESSAGE;    //uppercase message send to the client
		private Socket connection;
	    private DataInputStream in;	//stream read from the socket
	    private DataOutputStream out;    //stream write to the socket
		private int no;		//The index number of the client

	    public Handler(Socket connection, int no) {
	       		this.connection = connection;
		   		this.no = no;
	    }

	    public void run() {
		    	try
				{
					// //create a serversocket
					// sSocket = new ServerSocket(sPort, 10);
					// //Wait for connection
					// System.out.println("Waiting for connection");
					// //accept a connection from the client
					// connection = sSocket.accept();
					// System.out.println("Connection received from " + 
					// 	connection.getInetAddress().getHostName());
					// //initialize Input and Output streams
					out = new DataOutputStream(
						new BufferedOutputStream(connection.getOutputStream())
						);
					out.flush();
					in = new DataInputStream(
						new BufferedInputStream(connection.getInputStream())
						);
					sendMessage("Welcome! You are the client number "+no+". Can you spare a square? ;)\nPlease enter your username and password in the format -> '<uname> <pwd>'");
					message = in.readUTF();
					StringTokenizer unamePwd = new StringTokenizer(message);
					String uname="", pwd="";
					if(unamePwd.hasMoreTokens()) uname = unamePwd.nextToken();
					if (unamePwd.hasMoreTokens()) pwd = unamePwd.nextToken();
					// System.out.println("u:p :: "+uname+":"+pwd);
					int p = 0;
					boolean correct = uname.equals("client"+no);
					try{
						p = Integer.parseInt(pwd);
						if(p==no*no) correct = correct && true;
					}
					catch(Exception e){
						correct = false;
					}
					// System.out.println("Sending correct: "+correct);

					while(!correct){
						// System.out.println("inside incorrect while");
						sendMessage(correct+"");
						message = in.readUTF();
						unamePwd = new StringTokenizer(message);
						uname=""; pwd="";
						if(unamePwd.hasMoreTokens()) uname = unamePwd.nextToken();
						if (unamePwd.hasMoreTokens()) pwd = unamePwd.nextToken();
						p = 0;
						correct = uname.equals("client"+no);
						try{
							p = Integer.parseInt(pwd);
							if(p==no*no) correct = correct && true;
						}
						catch(Exception e){
							correct = false;
						}
					}
					sendMessage(correct+"");


					while(true)
					{
						System.out.println("Listening");
						//receive the message sent from the client
						message = (String)in.readUTF();
						//show the message to the user
						System.out.println("Received message: " + message);
						// Process the message
						StringTokenizer st = new StringTokenizer(message);
						String command = st.nextToken();
						switch(command.charAt(0)){
							case 'g': case 'G':
								String fileName = st.nextToken();
								sendFile(fileName);
								break;
							case 'u': case 'U':
								receiveFile(st.nextToken());
								break;
							case 'd': case 'D':
								File folder = new File(".");
								String files = getFiles(folder);
								sendMessage(files);
						}
						// MESSAGE = message.toUpperCase();
						// //send MESSAGE back to the client
						// sendMessage(MESSAGE, false);
					}
				}
				catch(IOException ioException){
					System.err.println("Connection to client no. "+no+" might have been lost.");
					// ioException.printStackTrace();
				}
				finally{
					//Close connections
					try{
						in.close();
						out.close();
						sSocket.close();
					}
					catch(IOException ioException){
						ioException.printStackTrace();
					}
				}
	    	}


		    void receiveFile(String fileName){
			System.out.println("Receiving File: "+fileName);
			FileOutputStream fout = null;
			try{
				fout = new FileOutputStream(fileName);
				// DataInputStream dis = new DataInputStream(new BufferedInputStream(in));
				long filesize = in.readLong();
				System.out.println("Filesize: "+filesize);
				fout.flush();
				byte[] bytes = new byte[8192];
				// if(filesize>bytes.length) filesize-=bytes.length;
		        int count;
		        while (filesize>0 && (count = in.read(bytes,0,(int)Math.min(bytes.length,filesize))) != -1) {
		        	// System.out.println("read "+count+" bytes : filesize="+filesize);
		            fout.write(bytes, 0, count);
		            filesize -= count;
		            // System.out.println("In the loop, reduced filesize to "+filesize);
		        }
		        // System.out.println("Out of the loop");
		        // fout.close();
			}
			catch(FileNotFoundException e){
				System.out.println("Error occurred while storing the file.");
			}
			catch(IOException e){
				System.out.println("Exception while taking inputs/writing file");
			}
			finally{
				try{
					if(fout!=null) fout.close();	
				}
				catch(IOException e){
					e.printStackTrace();
				}
				
			}
			// System.out.println("Out of the loop");
		}

		void sendFile(String fileName){
			System.out.println("Sending file:"+fileName);
			File file = new File(fileName);
			long length = file.length();

			byte bytes[] = new byte[8192];
			try{
				if(!file.exists()){
					System.out.println("The File being asked for doesn't exist");
					out.flush();
					out.writeLong(-1);
					out.flush();
					return;
				}
				out.flush();
				out.writeLong(length);
				System.out.println("size: "+length);
				InputStream filein = new FileInputStream(file);
				int count;
		        while ((count = filein.read(bytes)) > 0) {
		            out.write(bytes, 0, count);
		            // System.out.println("In the loop = "+count);
		        }
		        out.flush();
		        filein.close();
			}
			catch(FileNotFoundException e){
				System.out.println("File not found on the server.");
			}
			catch(IOException e){
				System.out.println("Exception while taking inputs/reading file");
			}
			System.out.println("Sent file:"+fileName);
			
		}

		String getFiles(File folder){
			StringBuilder files = new StringBuilder("");
			int ctr = 1;
			for(File fe: folder.listFiles()){
				files.append(ctr++);
				files.append(".)\t");
				files.append(fe.getName());
				files.append("\n");
			}
			return files.toString();
		}

		//send a message to the output stream
		void sendMessage(String msg)
		{
			try{
				// System.out.println("Sending message:\n"+msg);
				out.writeUTF(msg);
				out.flush();
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
    }


}
