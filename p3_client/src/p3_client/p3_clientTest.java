package p3_client;

import static org.junit.jupiter.api.Assertions.*;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;




class p3_clientTest{
	
	p3_client p3; //p3
	
	
	@BeforeEach
	public void init() {//initialize client 	
		
		p3 = new p3_client();
		
	}
	@Test
	void test1() throws Exception{
		
		//test player1 and player2 score 
		p3.p1Score = 3;
		assert(3 == p3.p1Score);
		
		p3.p2Score = 1;
		assert(1 == p3.p2Score);
		
	}
	
	@Test
	void test2() {
		
		//test player id
		p3.playerID = null;
		assert(null == p3.playerID);
		
		p3.playerID = 1;
		assert(p3.playerID == 1);
		
		p3.playerID = 2;
		assert(p3.playerID == 2);
	}

}
