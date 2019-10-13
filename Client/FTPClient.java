import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class FTPClient {
	Socket requestSocket;           //socket connect to the server
	DataOutputStream out;         //stream write to the socket
 	DataInputStream in;          //stream read from the socket
	String message;                //message send to the server
	
	public static void main(String args[])
	{
		System.out.println("Welcome!");
		FTPClient client = new FTPClient();
		client.run();
	}

	void run()
	{
		boolean notConnected = true;
		String host = "";
		int port = -1;
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		while(notConnected){
			try{
				System.out.println("To connect to the appropriate host enter the command -> ftpclient <hostName> <portNumber>");
				String hp = bufferedReader.readLine();
				StringTokenizer s = new StringTokenizer(hp);
				String command = s.nextToken();
				host = s.hasMoreTokens()?s.nextToken():"";
				port = s.hasMoreTokens()?Integer.parseInt(s.nextToken()):-1;
				requestSocket = new Socket(host, port);
				notConnected = false;
			}
			catch (ConnectException e) {
	    			System.err.println("Connection refused either due to a wrong port number or no server being present at the port you mentioned.\nMake sure the server has already been started at the port.");
			}
			catch(UnknownHostException unknownHost){
				System.err.println("The host you mentioned is unknown!\nMake sure you enter the correct hostname this time.");
			}
			catch(IOException e){
				System.err.println("Error occurred while connecting.");
				// e.printStackTrace();
			}
			catch(Exception e){
				System.err.println("Error occurred while connection. Try again below:");
			}
		}
		


		try{
			//create a socket to connect to the server
			//requestSocket = new Socket("localhost", 8000);
			System.out.println("Connected to "+host+" in port "+port);
			//initialize inputStream and outputStream
			out = new DataOutputStream(new BufferedOutputStream(requestSocket.getOutputStream()));
			out.flush();
			in = new DataInputStream(new BufferedInputStream(requestSocket.getInputStream()));
			
			//get Input from standard input
			// BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

			String welcomeMessage = in.readUTF();
			System.out.println(welcomeMessage);
			String unamePwd = bufferedReader.readLine();
			sendMessage(unamePwd);
			String loggedIn = in.readUTF();
			// System.out.println("Received boolean: "+loggedIn);
			while(!loggedIn.equals("true")){
				System.out.println("Login failed. Uname/pwd combination not matched on the server. Please enter the <uname> <pwd> again.");
				unamePwd = bufferedReader.readLine();
				sendMessage(unamePwd);
				loggedIn = in.readUTF();
			}

			while(true)
			{
				System.out.print("Hello, please input a command: ");
				//read a sentence from the standard input
				message = bufferedReader.readLine();
				if(message.length()==0) continue;
				StringTokenizer st = new StringTokenizer(message);
				//Send the sentence to the server
				
				//Receive the upperCase sentence from the server				
				String command = st.nextToken();
				if(command.equals("dir")){
					sendMessage(message);
					String received = in.readUTF();
					System.out.println("Files on the server:");
					System.out.println(received);
				}
				else if(command.equals("get")){
					sendMessage(message);
					receiveFile(st.nextToken());
				}
				else if(command.equals("upload")){
					// sendMessage(message);
					String filename = st.nextToken();
					uploadFile(filename, message);
				}
				else if(command.equals("local")||command.equals("l")){
					listFiles();
				} 
				else {
					System.out.println("The command you entered is wrong!");
				}
				// switch(command.charAt(0)){
				// 	case 'd': case 'D':
				// 		String received = in.readUTF();
				// 		System.out.println("Files on the server:");
				// 		System.out.println(received);
				// 		break;
				// 	case 'g': case 'G':
				// 		receiveFile(st.nextToken());
				// 		break;
				// 	case 'u': case 'U':
				// 		uploadFile(st.nextToken());
				// 		break;
				// 	case 'l': case 'L':
				// 		listFiles();
				// }
			}
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

	void uploadFile(String fileName, String message){
		System.out.println("Uploading file: "+fileName);
		InputStream filein = null;
		boolean uploaded = false;
		File file = new File(fileName);
		if (!file.exists()) {
			System.out.println("The file does not exist.");
			return;
		}
		sendMessage(message);
		long length = file.length();
		byte bytes[] = new byte[8192];
		try{
			System.out.println("size: "+length);
			out.flush();
			out.writeLong(length);
			filein = new FileInputStream(file);
			int count;
	        while ((count = filein.read(bytes)) > 0) {
	            out.write(bytes, 0, count);
	            // System.out.println("In the loop = "+count);
	        }
	        uploaded = true;
	        out.flush();
	        // filein.close();
		}
		catch(FileNotFoundException e){
			System.out.println("File not found on your side.\nNo such file!");
		}
		catch(IOException e){
			System.out.println("Exception while taking inputs/reading file");
		}
		finally{
			try{
				if(filein!=null) filein.close();
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
		if(uploaded) System.out.println("Uploaded file: "+fileName);
	}

	void receiveFile(String fileName){
		System.out.println("Receiving file: "+fileName);
		FileOutputStream fout=null;
		try{
			System.out.println("Trying to get filesize...");
			long filesize = in.readLong();
			if(filesize==-1){
				System.out.println(fileName+" does not exist on the FTP Server. Try the dir command to list the files on the server.");
				return;
			}
			fout = new FileOutputStream(fileName);
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
