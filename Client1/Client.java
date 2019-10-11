import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Client {
	Socket requestSocket;           //socket connect to the server
	DataOutputStream out;         //stream write to the socket
 	DataInputStream in;          //stream read from the socket
	String message;                //message send to the server
	
	public static void main(String args[])
	{
		Client client = new Client();
		client.run();
	}

	public void Client() {}

	void run()
	{
		try{
			//create a socket to connect to the server
			requestSocket = new Socket("localhost", 8000);
			System.out.println("Connected to localhost in port 8000");
			//initialize inputStream and outputStream
			out = new DataOutputStream(new BufferedOutputStream(requestSocket.getOutputStream()));
			out.flush();
			in = new DataInputStream(new BufferedInputStream(requestSocket.getInputStream()));
			
			//get Input from standard input
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

			String welcomeMessage = in.readUTF();
			System.out.println(welcomeMessage);
			String unamePwd = bufferedReader.readLine();
			sendMessage(unamePwd);
			String loggedIn = in.readUTF();
			System.out.println("Received boolean: "+loggedIn);
			while(!loggedIn.equals("true")){
				System.out.println("Login failed. Uname/pwd combination not matched on the server. Please enter the <uname> <pwd> again.");
				unamePwd = bufferedReader.readLine();
				sendMessage(unamePwd);
				loggedIn = in.readUTF();
			}

			while(true)
			{
				System.out.print("Hello, please input a sentence: ");
				//read a sentence from the standard input
				message = bufferedReader.readLine();
				StringTokenizer st = new StringTokenizer(message);
				//Send the sentence to the server
				sendMessage(message);
				//Receive the upperCase sentence from the server				
				String command = st.nextToken();
				switch(command.charAt(0)){
					case 'd': case 'D':
						String received = in.readUTF();
						System.out.println("Files on the server:");
						System.out.println(received);
						break;
					case 'g': case 'G':
						receiveFile(st.nextToken());
						break;
					case 'u': case 'U':
						uploadFile(st.nextToken());
						break;
					case 'l': case 'L':
						listFiles();
				}
			}
		}
		catch (ConnectException e) {
    			System.err.println("Connection refused. You need to initiate a server first.");
		}
		catch(UnknownHostException unknownHost){
			System.err.println("You are trying to connect to an unknown host!");
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
		finally{
			//Close connections
			try{
				in.close();
				out.close();
				requestSocket.close();
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
	}

	void listFiles(){
		File folder = new File(".");
		StringBuilder files = new StringBuilder("");
		int ctr = 1;
		for(File fe: folder.listFiles()){
			files.append(ctr++);
			files.append(".)\t");
			files.append(fe.getName());
			files.append("\n");
		}
		System.out.println(files);
	}

	void uploadFile(String fileName){
		System.out.println("Uploading file: "+fileName);
		File file = new File(fileName);
		long length = file.length();

		byte bytes[] = new byte[8192];
		try{
			System.out.println("size: "+length);
			out.flush();
			out.writeLong(length);
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
		System.out.println("Uploaded file: "+fileName);
	}

	void receiveFile(String fileName){
		System.out.println("Receiving file: "+fileName);
		try{
			FileOutputStream fout = new FileOutputStream(fileName);
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
	        fout.close();
		}
		catch(FileNotFoundException e){
			System.out.println("Error occurred while storing the file.");
		}
		catch(IOException e){
			System.out.println("Exception while taking inputs/writing file");
		}
		System.out.println("Received file: "+fileName);
	}

	//send a message to the output stream
	void sendMessage(String msg)
	{
		try{
			//stream write the message
			out.writeUTF(msg);
			out.flush();
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
}
