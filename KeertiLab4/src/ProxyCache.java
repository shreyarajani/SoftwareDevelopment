
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class ProxyCache 
{
	private static String CRLF = "\r\n";;
	/** Port for the proxy */
	private static int port;
	private static int port1;
	static int myPort = 0;
	/** Socket for client connections */
	private static ServerSocket socket;
	
	/** Create the ProxyCache object and the socket */
	private static Map<String, String> cache = new HashMap<String, String>();
	static HashMap<InetAddress,Integer> hashMap = new HashMap<InetAddress,Integer>();
	//HashMap<InetAddress,Integer> map = new HashMap<InetAddress,Integer>();
	public static void init(int p) 
	{
		port1 = p;
		try {
			socket = new ServerSocket(port1);
		} catch (IOException e) {
			System.out.println("Error creating socket: " + e);
			System.exit(-1);
		}
	}
	

	
	public void handle(Socket client) 
	{
		System.out.println("inside handle method");
		Socket server = null;
		HttpRequest request = null;
		HttpResponse response = null;

		/*
		 * Process request. If there are any exceptions, then simply return and
		 * end this request. This unfortunately means the client will hang for a
		 * while, until it timeouts.
		 */
		request = readRequest(client);
		if(!cache.containsKey(request.getURI()))
		{
			SocketReceive sr = new SocketReceive();
			hashMap = sr.getHashMap();
			System.out.println("hashMap size is "+hashMap.size());
			//System.exit(0);
			for(int i=0;i<hashMap.size();i++)
			{
				System.out.println(hashMap.entrySet());
				System.out.println(myPort);
				System.out.println("Am about to connect other systems. Fingers crossed");
			}
			//System.exit(0);
				System.out.println("From server:");
				/* Send request to server */
				server = sendRequestToServer(request); //////commented for testing the peer
				response = getResponseFromServer(server);///////commented for testing the peer
				addResponseToCache(request,response);/////////commented for testing the peer
//				String address ="172.16.220.248";
//				Integer port =9090;
//				server = sendRequestToPeer(request,address,port);
//				response = getResponseFromPeer(server);
//				addResponseToCache(request,response);
				/*Get response from server and send it to client*/
				try{
				//response = getResponseFromServer(server);
				sendResponseToClient(response, client,server);///////commented for testing the peer
				//addResponseToCache(request,response);
				client.close();
				server.close();	
				}catch(IOException e)
				{
					return;
				}
		}
	
		else
		{
			System.out.println("From cache:");
			//Get from cache and send it to client
			String fCache = cache.get(request.getURI());
			File cacheFile = new File(fCache);
			int totalBytes = (int) cacheFile.length();
			byte[] cacheData = new byte[totalBytes];
			try {
				FileInputStream inFile = new FileInputStream(cacheFile);
				inFile.read(cacheData);
				
				DataOutputStream toClient = new DataOutputStream(client.getOutputStream());
				toClient.write(cacheData);
				
				client.close();
				inFile.close();//added
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	
	private static Socket sendRequestToServer(HttpRequest request)
	{
		try {
				/* Open socket and write request to socket */
				Socket server = new Socket(request.getHost(), request.getPort());
				DataOutputStream toServer = new DataOutputStream(
						server.getOutputStream());
				toServer.writeBytes(request.toString());//
				return server;
			} catch (UnknownHostException e) {
				System.out.println("Unknown host: " + request.getHost());
				System.out.println(e);
				return null;
			} catch (IOException e) {
				System.out.println("Error writing to server: " + e);
				return null;
			}
	}
		
		
	/* Read request */
	private static HttpRequest readRequest(Socket client)
	{
		try {
			BufferedReader fromClient = new BufferedReader(
					new InputStreamReader(client.getInputStream()));
			HttpRequest request = new HttpRequest(fromClient);// initialize the request obj		
			return request;
			
		} catch (IOException e) {
			System.out.println("Error reading from client: " + e);
			return null;
			}
	}
	
	//get response from server.
	private static HttpResponse getResponseFromServer(Socket server)
	{
		try {
			//System.out.println("Get response from serveer");
			DataInputStream fromServer = new DataInputStream(
					server.getInputStream());
			HttpResponse response = new HttpResponse(fromServer);
			return response;
		} catch (IOException e) {
			System.out.println("Error writing response to client: " + e);
			return null;
			
		}
		}
	
	//send response from to client
	private static void sendResponseToClient(HttpResponse response, Socket client, Socket server)
	{
		try{
			
			
		DataOutputStream toClient = new DataOutputStream(
				client.getOutputStream());
		toClient.writeBytes(response.toString());//
		/* Write response to client. First headers, then body */
		//toClient.writeBytes(response.headers);
		toClient.write(response.body);
		toClient.close();
		client.close();
		server.close();
		
		/* Insert object into the cache */
		/* Fill in (optional exercise only) */
		}
	catch (IOException e) {
		System.out.println("Error writing response to client: " + e);
	}
	}
	
	//caching start point
	private static HttpResponse checkCache(HttpRequest request) 
	{
		HttpResponse response = null;
		if(cache.get(request.getURI())!=null)
			{
	        //response = cache.get(request.getURI());
			}
		return response;
	}
	
	private static void addResponseToCache(HttpRequest request, HttpResponse response)
	{
		System.out.println("Add response to cache");
		String fileName = "Cache/"+"file_" + System.currentTimeMillis();
		File fCache = new File(fileName);
		DataOutputStream fStream;
		try {
			fStream = new DataOutputStream(new FileOutputStream(fCache));
			fStream.writeBytes(response.toString());
			fStream.write(response.body);
			cache.put(request.getURI(), fileName);
			System.out.println("Add response to cache done!!");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	 public class SendingThreadHandler implements Runnable
	 {
	
		@Override
		public void run() 
		{
		SocketSend ss = new SocketSend();
		try {
			ss.sendResponse();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
			
		}
		 
	 }
 
 
	public class ReceivingThreadHandler implements Runnable
	{
		SocketReceive sr = new SocketReceive();
		//HashMap<InetAddress,Integer> hashMap = new HashMap<InetAddress,Integer>();
		@Override
		public void run() 
		{
			sr.receiveResponse();
			//System.out.println("map size is ===================================================== "+hashMap.size());
		}
	}
	
	public class ProxyCacheThreadHandler implements Runnable
	{

		@Override
		public void run() 
		{
			ProxyCache pc = new ProxyCache();
			pc.proxyCacheMethod();
			
		}
		
	}
	
	public void proxyCacheMethod()
	{
		System.out.println("inside proxy cache method");
		System.out.println("thread is "+Thread.currentThread());
		/**
		 * Main loop. Listen for incoming connections and spawn a new thread for
		 * handling them
		 */
		//init(myPort);
		//System.out.println("socket is "+socket);
		Socket client = null;

		while (true) 
		{
			try {
					System.out.println("I am here");
					System.out.println("hey ");
					//System.out.println("socket"+socket);
					client = socket.accept();
					System.out.println("client socket is "+client);
					//ProxyCache pc = new ProxyCache();
					handle(client);
				}
			catch (IOException e)
			{
				System.out.println("Error reading request from client(main): " + e);
				/*
				 * Definitely cannot continue processing this request, so skip
				 * to next iteration of while loop.
				 */
				continue;
			}
		}
		
	}
 
	 public void threadHandler()
	 {
		 SendingThreadHandler sendThreadObj = new SendingThreadHandler();
		 ReceivingThreadHandler receiveThreadObj = new ReceivingThreadHandler();
		 ProxyCacheThreadHandler proxyCacheThreadObj = new ProxyCacheThreadHandler();
		 Thread t1 = new Thread(sendThreadObj);
		 Thread t2 = new Thread(receiveThreadObj);
		 Thread t3 = new Thread(proxyCacheThreadObj);
		 t1.start();
		 t2.start();
		 t3.start();
//		 try 
//		 {
//			t1.join();
//			t2.join();
//			t3.join();
//		 } 
//		 catch (InterruptedException e) 
//		 {
//			e.printStackTrace();
//		 }
	}
	 
	/** test code
	 * 
	 */
	
	 public static Socket sendRequestToPeer(HttpRequest request,String address, Integer ip)
	 {
		 try {
				/* Open socket and write request to socket */
				Socket server = new Socket(address, ip);
				DataOutputStream toServer = new DataOutputStream(
						server.getOutputStream());
				toServer.writeBytes(request.toString());//
				return server;
			} catch (UnknownHostException e) {
				System.out.println("Unknown host: " + request.getHost());
				System.out.println(e);
				return null;
			} catch (IOException e) {
				System.out.println("Error writing to peer: " + e);
				return null;
			}

	 }
	 private static HttpResponse getResponseFromPeer(Socket server)
		{
			try {
				//System.out.println("Get response from serveer");
				DataInputStream fromServer = new DataInputStream(
						server.getInputStream());
				HttpResponse response = new HttpResponse(fromServer);
				return response;
			} catch (IOException e) {
				System.out.println("Error writing response to client: " + e);
				return null;
				
			}
			}
	 
	 
	 /**test code ends here
 
 

	/** Read command line arguments and start proxy */
	public static void main(String args[]) 
	{
		File folder = new File("Cache/");
		if (!folder.exists()) 
		{
			folder.mkdir();
		}
		try 
		{	
			myPort = Integer.parseInt(args[0]);
		}
		catch (ArrayIndexOutOfBoundsException e) 
		{
			System.out.println("Need port number");
			System.exit(-1);
		}
		catch (NumberFormatException e) 
		{
			System.out.println("Please give port number as integer.");
			System.exit(-1);
		}
		System.out.println("myport"+myPort);
		init(myPort);
		ProxyCache proxObj = new ProxyCache();
		proxObj.threadHandler();
	}
}	 
