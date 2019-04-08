package p3_server;
import java.io.Serializable;
import java.util.function.Consumer;

public class server extends networkConnection {

	private int port;//empty port
	
	public server(int port, Consumer<Serializable> callback) {
		super(callback); 
		this.port = port; //declare port
	}
	
	public boolean isServer() { //checks if its a server
		return true;
	}
	
	protected int getPort() {//return port number
		return port;
	}

	protected String getIP() {
		return null;//not needed in server
	}
	
}
