package p3_client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public abstract class networkConnection {

	abstract protected boolean isServer();
	abstract protected String getIP();
	abstract protected int getPort();
	
	private Consumer<Serializable> callback;
	private connectionThread connection = new connectionThread();
	
	public networkConnection(Consumer<Serializable> callback) {
		this.callback = callback; //initialize callback
		this.connection.setDaemon(true); //Daemon Thread: runs in the background to perform tasks
	}
	
	public void startConnection() throws Exception{
		this.connection.start(); //starts connection
		
	}
	
	public void stopConnection() throws Exception{
		this.connection.socket.close(); //stops connection by closing socket
		
	}
	
	public void send(Serializable data) throws Exception{
		this.connection.out.writeObject(data); //sends data
	}
	
	class connectionThread extends Thread{
		
		private Socket socket;
		private ObjectOutputStream out;
		
		public void run() {
			try(ServerSocket serverObj = isServer() ? new ServerSocket(getPort()) : null; 
					Socket socket = isServer() ? serverObj.accept() : new Socket(getIP(), getPort() );
					ObjectOutputStream out = new ObjectOutputStream( socket.getOutputStream() );
					ObjectInputStream in = new ObjectInputStream(socket.getInputStream() )
					){
				
				this.socket = socket;
				this.out = out;
				socket.setTcpNoDelay(true); 
				//System.out.println("Connection Started!" );
				callback.accept("CLIENT");
				while(true) {
					Serializable data = (Serializable) in.readObject();
					callback.accept(data);
					
				}
			}
			catch(Exception e) {
				callback.accept("CLOSE_CLIENT");
			}	
		}//end of run	
	}//end of connectionThread
	
	
}
