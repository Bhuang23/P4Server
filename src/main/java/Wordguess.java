import java.io.Serializable;

//Serializable MorraInfo
public class Wordguess implements Serializable{
	private static final long serialVersionUID = 1L;
    int count;
    int numplayers;
    boolean haveoneplayer;
	Wordguess()
	{
		count = 0;
		numplayers = 0;
		haveoneplayer = false;
	}
}
