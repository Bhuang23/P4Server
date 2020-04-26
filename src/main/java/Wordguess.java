import java.io.Serializable;
import java.util.ArrayList;

//Serializable MorraInfo
public class Wordguess implements Serializable{
	private static final long serialVersionUID = 1L;
    int count;
    int numplayers;
    boolean haveoneplayer;
    String category;
    int numberofletters;
    int remaininguess;
    boolean guessedfoods;
    boolean guessedgames;
    boolean guessedcountries;
    String guess;
    String wordguess;
    boolean won;
    ArrayList<Integer> position;
    int maxguessfoods;
    int maxguessgames;
    int maxguesscountries;
    boolean lost;
	Wordguess()
	{
		count = 0;
		numplayers = 0;
		won = false;
		lost = false;
		haveoneplayer = false;
		category = "";
		numberofletters = 0;
		guess = "";
		wordguess = "";
		guessedfoods = false;
		guessedgames = false;
		guessedcountries = false;
		maxguessfoods = 0;
	    maxguessgames = 0;
	    maxguesscountries = 0;
		position = new ArrayList<Integer>();
		remaininguess = 6;
	}
}
