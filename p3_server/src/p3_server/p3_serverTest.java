package p3_server;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class p3_serverTest {

	p3_server p3;
	
	@BeforeEach
	public void init() {//initialize	
		p3 = new p3_server();
	}
	void test1() {
		//test number of players
		p3.numPlayers = 2;
		assert(p3.numPlayers == 2);
	}
	
	@Test
	void test2() {
		//test scores of each player
		p3.p1ScoreVal = 3;
		p3.p2ScoreVal = 1;
		
		assert(p3.p1ScoreVal == 3);
		assert(p3.p2ScoreVal == 1);
	}

	@Test
	void test3() {
		//test number of plays 
		p3.plays = 2;
		assert(p3.plays == 2);
		p3.plays = 1;
		assert(p3.plays == 1);
		p3.plays = 3;
		assert(p3.plays == 3);
	}
	@Test
	void test4() {
		//test round winner with tie
		Integer test4 = p3.roundWinner("P1: ROCK", "P1: ROCK");
		assert(test4 == 0);
	}
	@Test
	void test5() {
		//test round winner with rock vs scissors
		Integer test1 = p3.roundWinner("P1: ROCK", "P2: SCISSOR");
		assert(test1 == 1);//player 1 should win
		
		Integer test2 = p3.roundWinner("P1: PAPER", "P2: LIZARD");
		assert(test2 == 2);//player 2 should win
	}
	

}