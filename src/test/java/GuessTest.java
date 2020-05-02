import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class GuessTest {

	Wordguess w;
	
	@BeforeEach
	void init() {
		w = new Wordguess();
	}
	
	@Test
	void testInitWG() {
		assertEquals("Wordguess", w.getClass().getName(),"init failed on WG");
	}
	
	@Test
	void testConstructor() {
		assertEquals(0, w.position.size());
	}
	
	@Test
	void testPositionArrayListSize() {
		w.position.add(1);
		w.position.add(2);
		w.position.add(3);
		assertEquals(3, w.position.size());
	}
	
	@Test
	void testPositionArrayListRemoveSize() {
		w.position.add(1);
		w.position.add(2);
		w.position.add(3);
		w.position.remove(2);
		w.position.remove(1);
		assertEquals(1, w.position.size());
	}
	
	@Test
	void testPositionArrayListClearSize() {
		w.position.clear();
		assertEquals(0, w.position.size());
	}
	
	@Test
	void testInitCount() {
		assertEquals(0, w.count);
	}
	
	@Test
	void testInitNumPlayers() {
		assertEquals(0, w.numplayers);
	}
	
	@Test
	void testInitHaveOnePlayer() {
		assertEquals(false, w.haveoneplayer);
	}
	
	@Test
	void testInitCategory() {
		assertEquals("", w.category);
	}
	
	@Test
	void testInitNumOfLetters() {
		assertEquals(0, w.numberofletters);
	}
	
	@Test
	void testInitRemainingGuess() {
		assertEquals(6, w.remaininguess);
	}
	
	@Test
	void testInitGuessedFoods() {
		assertEquals(false, w.guessedfoods);
	}
	
	@Test
	void testInitGuessedGames() {
		assertEquals(false, w.guessedgames);
	}
	
	@Test
	void testInitGuessedCountries() {
		assertEquals(false, w.guessedcountries);
	}
	
	@Test
	void testInitGuessedGuess() {
		assertEquals("", w.guess);
	}
	
	@Test
	void testInitGuessedWordGuess() {
		assertEquals("", w.wordguess);
	}
	
	@Test
	void testInitWon() {
		assertEquals(false, w.won);
	}
	
	@Test
	void testInitMaxGuessFoods() {
		assertEquals(0, w.maxguessfoods);
	}
	
	@Test
	void testInitMaxGuessGames() {
		assertEquals(0, w.maxguessgames);
	}
	
	@Test
	void testInitMaxGuessCountries() {
		assertEquals(0, w.maxguesscountries);
	}
	
	@Test
	void testInitLost() {
		assertEquals(false, w.lost);
	}
	
	@Test
	void testInitCategoriesWon() {
		assertEquals(0, w.categorieswon);
	}
	
	@Test
	void testInitPlayAgain() {
		assertEquals(false, w.playagain);
	}
	    
    @Test
	void testSetCount() {
    	w.count = 10;
		assertEquals(10, w.count);
	}
	
	@Test
	void testSetNumPlayers() {
		w.numplayers = 10;
		assertEquals(10, w.numplayers);
	}
	
	@Test
	void testSetHaveOnePlayer() {
		w.haveoneplayer = true;
		assertEquals(true, w.haveoneplayer);
	}
	
	@Test
	void testSetCategory() {
		w.category = "hello";
		assertEquals("hello", w.category);
	}
	
	@Test
	void testSetNumOfLetters() {
		w.numberofletters = 10;
		assertEquals(10, w.numberofletters);
	}
	
	@Test
	void testSetRemainingGuess() {
		w.remaininguess--;
		assertEquals(5, w.remaininguess);
	}
	
	@Test
	void testSetGuessedFoods() {
		w.guessedfoods = true;
		assertEquals(true, w.guessedfoods);
	}
	
	@Test
	void testSetGuessedGames() {
		w.guessedgames = true;
		assertEquals(true, w.guessedgames);
	}
	
	@Test
	void testSetGuessedCountries() {
		w.guessedcountries = true;
		assertEquals(true, w.guessedcountries);
	}
	
	@Test
	void testSetGuessedGuess() {
		w.guess = "hello";
		assertEquals("hello", w.guess);
	}
	
	@Test
	void testSetGuessedWordGuess() {
		w.wordguess = "hello";
		assertEquals("hello", w.wordguess);
	}
	
	@Test
	void testSetWon() {
		w.won = true;
		assertEquals(true, w.won);
	}
	
	@Test
	void testSetMaxGuessFoods() {
		w.maxguessfoods = 10;
		assertEquals(10, w.maxguessfoods);
	}
	
	@Test
	void testSetMaxGuessGames() {
		w.maxguessgames = 10;
		assertEquals(10, w.maxguessgames);
	}
	
	@Test
	void testSetMaxGuessCountries() {
		w.maxguesscountries = 10;
		assertEquals(10, w.maxguesscountries);
	}
	
	@Test
	void testSetLost() {
		w.lost = true;
		assertEquals(true, w.lost);
	}
	
	@Test
	void testSetCategoriesWon() {
		w.categorieswon = 10;
		assertEquals(10, w.categorieswon);
	}
	
	@Test
	void testSetPlayAgain() {
		w.playagain = true;
		assertEquals(true, w.playagain);
	}

}