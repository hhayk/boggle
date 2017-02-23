/**
 * @(#)Boggle.java
 *
 * Boggle application
 *
 * This application is a fairly efficient Boggle solver for a 4x4 grid.
 * The database of words is read into a HashSet which provides
 * a contains method which operates at O(c). This beats my original
 * idea which was to implement a binary search on an ArrayList which
 * would have been O(log(N))
 *
 * At first I was going to use an iterative solution to the problem of finding
 * all words in the board but since the HashSet implements my dictionary data
 * base so efficiently, I went with a recursive search of neighboring letters
 * and am seeing solutions
 *
 *
 * A HashSet, by its nature, can not be sorted.  To sort the words found,
 * the HashSet is used to create an ArrayList which is sortable.
 *
 * @author Kenneth L Moore
 * @version 1.00 2011/12/6
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.text.*;

// create a window that responds to events
public class Boggle extends JFrame implements ActionListener{

	// some global variables
	//
	// ends in:
	//
	//  Ar = array
	//  TF = TextField
	//  TA = TextArea
	//  HS = HashSet
	//  L  = Label
	private static int MIN_WORDS_COUNT = 100;
	public int WORD_LENGTH_MAX = 8;
    public int countSolve=0;
	private HashSet<String> dictionaryHS = new HashSet<String>();
	private HashSet<String> boardsHS = new HashSet<String>();
	private HashSet<String> outputHS = new HashSet<String>();
	private JLabel boggleInputL = new JLabel("Enter boggle board here as 16 characters");
	private JLabel boggleInputL2 = new JLabel("Enter key starts solve");
	private JLabel boggleInputL3 = new JLabel("Set minimum words count in board");
//	private JTextField boggleInputTF = new JTextField("ABCDEFGHIJKLMNOP");
//	private JTextField boggleInputTF = new JTextField("QITSERTSHAETBOOT");
	private JTextField boggleInputTF = new JTextField("qitsertshaetboot");
	private	JTextField upperLimit = new JTextField(MIN_WORDS_COUNT + "");
	private JTextArea solutionTA = new JTextArea("",80,50);
	private JPanel boardP = new JPanel();
	private JTextField [] boardTFA = new JTextField[16];


	// unit array is squares on board + neighbors + visited
	unit [] unitAr = new unit[16];

	// comparator to sort strings by length.
	private class StringSort implements Comparator<String>{
		public int compare(String a, String b){
			if(a.length()<b.length()){
				return 1;
			}
			// if size the same sort alphabetically
			if(a.length()==b.length()){
				  return a.compareTo(b);
			}
			if(a.length()>b.length()){
				return -1;
			}
		   return 1;
		}
	}

	// read from dict.txt
	private void getData(){
		long start = System.currentTimeMillis();
		File data = new File("dict.txt");
		Scanner scanner=null;
		int count = 0;
		try {
			scanner = new Scanner(data);
			while (scanner.hasNextLine()) {
				dictionaryHS.add(scanner.nextLine().toUpperCase());
				count++;
			}
		} catch (FileNotFoundException fnfe) {}
		long end = System.currentTimeMillis();
		double t = ((end-start)/1000.0);
		System.out.println("Time to read database from file "+ count + " items " + t + " seconds");
		if(scanner!=null)scanner.close(); // need if if file missing
		solutionTA.setText("Time to read database from file "+ count + " items " + t + " seconds");
	}

	private void loadBoards(String boardPath) {
		long start = System.currentTimeMillis();
		File data = new File(boardPath);
		Scanner scanner=null;
		int count = 0;
		try {
			scanner = new Scanner(data);
			while (scanner.hasNextLine()) {
				boardsHS.add(scanner.nextLine());
				count++;
			}
		} catch (FileNotFoundException fnfe) {}
		long end = System.currentTimeMillis();
		if(scanner!=null)scanner.close(); // need if if file missing
	}

	// constructor
	public Boggle(String boardPath){
		super();
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Boggle Solver Kenneth Moore Professor CCAC Dec 2011");
		solutionTA.setFont(new Font("Courier New",Font.PLAIN,12));
		boardP.setLayout(new GridLayout(4,4));
		setLayout(new GridBagLayout());
		for(int i = 0; i < 16; i++){
			boardTFA[i] = new JTextField(" ");
			boardP.add(boardTFA[i]);
		}

		// read data from dict.txt
		getData();

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0, 0, 0, 0);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = 0;// first row

		c.gridy = 1;// third row
		add(boggleInputL3,c);

		c.gridy = 2;
		add(upperLimit, c);
		JButton startButton = new JButton("Export");
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportBoards();
				System.exit(0);
			}
		});
		add(startButton, c);

		loadBoards(boardPath);
		add(getBoards());
		c.gridy = 3;

		add(boardP,c);
		c.gridy = 4;
		add(boggleInputL,c);
		c.gridy = 5;// first row
		boggleInputTF.addActionListener(this);
		add(boggleInputTF,c);
		c.gridy = 6;// third row
		add(boggleInputL2,c);
		c.gridy = 7;// first row
		add(solutionTA,c);



		//pack();
	    setVisible(true);
	    setSize(600,800);
	    validate();
	}

	private HashMap<Integer, HashSet<String>> boardsWithWords = new HashMap<Integer, HashSet<String>>();

	private JScrollPane getBoards() {
		StringBuilder content = new StringBuilder();
		int k = 0;
		for (String s : boardsHS) {
			int count = getFoundWordsCount(s);
			String str = k + " : " + s + "  |  " + count;

			content.append(str);
			content.append("\n");

			System.out.println(str);
			k++;

			HashSet<String> boardsSet;
			if(boardsWithWords.containsKey(count)) {
				boardsSet = boardsWithWords.get(count);
			} else {
				boardsSet = new HashSet<String>();
			}
			boardsSet.add(s);
			boardsWithWords.put(count, boardsSet);
		}

		JTextArea textArea = new JTextArea (20, 30);
		textArea.append(content.toString());

		JScrollPane scroll = new JScrollPane (textArea, 
   			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		JScrollBar bar = scroll.getVerticalScrollBar();
		scroll.setPreferredSize(new Dimension(500, 300));
		scroll.setMinimumSize(new Dimension(500, 300));
		return scroll;
	}

	private void exportBoards() {
		System.out.println("//////////////////// EXPORTING ///////////////////////");
		System.out.println("Exporting Boards With At Least " + MIN_WORDS_COUNT + " words");

		int limiter = Integer.valueOf(upperLimit.getText());
		HashSet<String> exportResult = new HashSet<String>();

		for (Map.Entry<Integer, HashSet<String>> entry : boardsWithWords.entrySet()) {
    		Integer key = entry.getKey();
    		HashSet<String> value = entry.getValue();
	        // System.out.println(key + " = " + value);

	        if(key >= limiter) {
	        	exportResult.addAll(value);
	        }
		}
	    saveToFile(new ArrayList<String>(exportResult));
	}

	private void saveToFile(ArrayList<String> exportResult) {
		System.out.println("////////////////////// SAVING /////////////////////////");
	    System.out.println(exportResult);

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Specify a file to save");   
		 
		int userSelection = fileChooser.showSaveDialog(this);
		 
		if (userSelection == JFileChooser.APPROVE_OPTION) {
		    File fileToSave = fileChooser.getSelectedFile();
		    System.out.println("Save as file: " + fileToSave.getAbsolutePath());

		    BufferedWriter bw = null;
			FileWriter fw = null;
			String fileName = fileToSave.getAbsolutePath();

			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < exportResult.size(); i++) {
				String entry = exportResult.get(i);
				sb.append(entry);
				if(i < exportResult.size() - 1) 
					sb.append("\n");
			}

			try {
				fw = new FileWriter(fileName);
				bw = new BufferedWriter(fw);
				bw.write(sb.toString());

				System.out.println("Done");

			} catch (IOException e) {

				e.printStackTrace();

			} finally {

				try {

					if (bw != null)
						bw.close();

					if (fw != null)
						fw.close();

				} catch (IOException ex) {

					ex.printStackTrace();

				}
			}
		}
	}

    StringSort sst = new StringSort();
	private int getFoundWordsCount(String board) {
		outputHS.clear();

		for(int i = 0; i < 16; i++){
			setupUnits(board);
    		String s = "";
    		solver(i, s);
    	}
    	ArrayList<String> aL = new ArrayList<String>(outputHS);
		Collections.sort(aL, sst);
		return aL.size();
	}

	private void setupUnits(String board) {
		for(int ii = 0; ii < board.length(); ii++){
			unitAr[ii] = new unit(board.charAt(ii));
			unitAr[ii].visited = false;
		}

		unitAr[15].ABUT_SIZE = 3;
		unitAr[15].abut[ 0] = 10;
		unitAr[15].abut[ 1] = 11;
		unitAr[15].abut[ 2] = 14;
	    // neighbors of 14
		unitAr[14].ABUT_SIZE = 5;
		unitAr[14].abut[ 0] =  9;
		unitAr[14].abut[ 1] = 10;
		unitAr[14].abut[ 2] = 11;
		unitAr[14].abut[ 3] = 13;
		unitAr[14].abut[ 4] = 15;
	    // neighbors of 13
		unitAr[13].ABUT_SIZE = 5;
		unitAr[13].abut[ 0] =  8;
		unitAr[13].abut[ 1] =  9;
		unitAr[13].abut[ 2] = 10;
		unitAr[13].abut[ 3] = 12;
		unitAr[13].abut[ 4] = 14;
	    // neighbors of 12
		unitAr[12].ABUT_SIZE = 3;
		unitAr[12].abut[ 0] =  8;
		unitAr[12].abut[ 1] =  9;
		unitAr[12].abut[ 2] = 13;
	    // neighbors of 11
		unitAr[11].ABUT_SIZE = 5;
		unitAr[11].abut[ 0] =  6;
		unitAr[11].abut[ 1] =  7;
		unitAr[11].abut[ 2] = 10;
		unitAr[11].abut[ 3] = 14;
		unitAr[11].abut[ 4] = 15;
	    // neighbors of 10
		unitAr[10].ABUT_SIZE = 8;
		unitAr[10].abut[ 0] =  5;
		unitAr[10].abut[ 1] =  6;
		unitAr[10].abut[ 2] =  7;
		unitAr[10].abut[ 3] =  9;
		unitAr[10].abut[ 4] = 11;
		unitAr[10].abut[ 5] = 13;
		unitAr[10].abut[ 6] = 14;
		unitAr[10].abut[ 7] = 15;
	    // neighbors of 9
		unitAr[ 9].ABUT_SIZE = 8;
		unitAr[ 9].abut[ 0] =  4;
		unitAr[ 9].abut[ 1] =  5;
		unitAr[ 9].abut[ 2] =  6;
		unitAr[ 9].abut[ 3] =  8;
		unitAr[ 9].abut[ 4] = 10;
		unitAr[ 9].abut[ 5] = 12;
		unitAr[ 9].abut[ 6] = 13;
		unitAr[ 9].abut[ 7] = 14;
	    // neighbors of 8
		unitAr[8].ABUT_SIZE = 5;
		unitAr[8].abut[0] = 4;
		unitAr[8].abut[1] = 5;
		unitAr[8].abut[2] = 9;
		unitAr[8].abut[3] =12;
		unitAr[8].abut[4] =13;
	    // neighbors of 7
		unitAr[7].ABUT_SIZE = 5;
		unitAr[7].abut[0] = 2;
		unitAr[7].abut[1] = 3;
		unitAr[7].abut[2] = 6;
		unitAr[7].abut[3] =10;
		unitAr[7].abut[4] =11;
	    // neighbors of 6
		unitAr[ 6].ABUT_SIZE = 8;
		unitAr[ 6].abut[ 0] =  1;
		unitAr[ 6].abut[ 1] =  2;
		unitAr[ 6].abut[ 2] =  3;
		unitAr[ 6].abut[ 3] =  5;
		unitAr[ 6].abut[ 4] =  7;
		unitAr[ 6].abut[ 5] =  9;
		unitAr[ 6].abut[ 6] = 10;
		unitAr[ 6].abut[ 7] = 11;
	    // neighbors of 5
		unitAr[ 5].ABUT_SIZE = 8;
		unitAr[ 5].abut[ 0] =  0;
		unitAr[ 5].abut[ 1] =  1;
		unitAr[ 5].abut[ 2] =  2;
		unitAr[ 5].abut[ 3] =  4;
		unitAr[ 5].abut[ 4] =  6;
		unitAr[ 5].abut[ 5] =  8;
		unitAr[ 5].abut[ 6] =  9;
		unitAr[ 5].abut[ 7] = 10;
	    // neighbors of 4
		unitAr[4].ABUT_SIZE = 5;
		unitAr[4].abut[0] = 0;
		unitAr[4].abut[1] = 1;
		unitAr[4].abut[2] = 5;
		unitAr[4].abut[3] = 8;
		unitAr[4].abut[4] = 9;
	    // neighbors of 3
		unitAr[3].ABUT_SIZE = 3;
		unitAr[3].abut[0] = 2;
		unitAr[3].abut[1] = 6;
		unitAr[3].abut[2] = 7;
	    // neighbors of 2
		unitAr[2].ABUT_SIZE = 5;
		unitAr[2].abut[0] = 1;
		unitAr[2].abut[1] = 3;
		unitAr[2].abut[2] = 5;
		unitAr[2].abut[3] = 6;
		unitAr[2].abut[4] = 7;
	    // neighbors of 1
		unitAr[1].ABUT_SIZE = 5;
		unitAr[1].abut[0] = 0;
		unitAr[1].abut[1] = 2;
		unitAr[1].abut[2] = 4;
		unitAr[1].abut[3] = 5;
		unitAr[1].abut[4] = 6;
	    // neighbors of 0 t
		unitAr[0].ABUT_SIZE = 3;
		unitAr[0].abut[0] = 1;
		unitAr[0].abut[1] = 4;
		unitAr[0].abut[2] = 5;
	}

	/*
	 * A small data structure represent each square on the board.
	 * char in square + neighbors + visited
	 */
	class unit{
		public char c;
		boolean visited;
		// abut holds neighbors
		// abut - definition: To border upon or end at; be next to.
		public int [] abut  = new int[10];
		public int ABUT_SIZE;
		public unit(char ch){c = ch; visited = false; ABUT_SIZE=0;}
		public String toString(){
			String s = "";
			s += "unit "+c;
			for(int i = 0; i < ABUT_SIZE; i++)
				s += " abuts " + unitAr[abut[i]].c+" ";
			return s;
		}
	}

	// initialize
	private boolean setUpRecursion(){
		String board = boggleInputTF.getText();
		board = board.toUpperCase();
		if(board.length() != 16){
			System.out.println("not 16");
			solutionTA.setText("You must enter 16 characters.  \nYou entered "+board.length());
			return false;
		}
		for(int i = 0; i < board.length(); i++){
			unitAr[i] = new unit(board.charAt(i));
			unitAr[i].visited = false;
			boardTFA[i].setText(" "+board.charAt(i));
		}
	/*
	 *  grid by unit indicies
	 *
	 *  0  1  2  3
	 *  4  5  6  7
	 *  8  9 10 11
	 * 12 13 14 15
	 *
	 * Used to type in all the neighboring letter locations
	 * This probably could have been more efficient with
	 * a matrix but this did not take long to type
	 * using cut and paste
	 *
	 */

    // neighbors of 15
	unitAr[15].ABUT_SIZE = 3;
	unitAr[15].abut[ 0] = 10;
	unitAr[15].abut[ 1] = 11;
	unitAr[15].abut[ 2] = 14;
    // neighbors of 14
	unitAr[14].ABUT_SIZE = 5;
	unitAr[14].abut[ 0] =  9;
	unitAr[14].abut[ 1] = 10;
	unitAr[14].abut[ 2] = 11;
	unitAr[14].abut[ 3] = 13;
	unitAr[14].abut[ 4] = 15;
    // neighbors of 13
	unitAr[13].ABUT_SIZE = 5;
	unitAr[13].abut[ 0] =  8;
	unitAr[13].abut[ 1] =  9;
	unitAr[13].abut[ 2] = 10;
	unitAr[13].abut[ 3] = 12;
	unitAr[13].abut[ 4] = 14;
    // neighbors of 12
	unitAr[12].ABUT_SIZE = 3;
	unitAr[12].abut[ 0] =  8;
	unitAr[12].abut[ 1] =  9;
	unitAr[12].abut[ 2] = 13;
    // neighbors of 11
	unitAr[11].ABUT_SIZE = 5;
	unitAr[11].abut[ 0] =  6;
	unitAr[11].abut[ 1] =  7;
	unitAr[11].abut[ 2] = 10;
	unitAr[11].abut[ 3] = 14;
	unitAr[11].abut[ 4] = 15;
    // neighbors of 10
	unitAr[10].ABUT_SIZE = 8;
	unitAr[10].abut[ 0] =  5;
	unitAr[10].abut[ 1] =  6;
	unitAr[10].abut[ 2] =  7;
	unitAr[10].abut[ 3] =  9;
	unitAr[10].abut[ 4] = 11;
	unitAr[10].abut[ 5] = 13;
	unitAr[10].abut[ 6] = 14;
	unitAr[10].abut[ 7] = 15;
    // neighbors of 9
	unitAr[ 9].ABUT_SIZE = 8;
	unitAr[ 9].abut[ 0] =  4;
	unitAr[ 9].abut[ 1] =  5;
	unitAr[ 9].abut[ 2] =  6;
	unitAr[ 9].abut[ 3] =  8;
	unitAr[ 9].abut[ 4] = 10;
	unitAr[ 9].abut[ 5] = 12;
	unitAr[ 9].abut[ 6] = 13;
	unitAr[ 9].abut[ 7] = 14;
    // neighbors of 8
	unitAr[8].ABUT_SIZE = 5;
	unitAr[8].abut[0] = 4;
	unitAr[8].abut[1] = 5;
	unitAr[8].abut[2] = 9;
	unitAr[8].abut[3] =12;
	unitAr[8].abut[4] =13;
    // neighbors of 7
	unitAr[7].ABUT_SIZE = 5;
	unitAr[7].abut[0] = 2;
	unitAr[7].abut[1] = 3;
	unitAr[7].abut[2] = 6;
	unitAr[7].abut[3] =10;
	unitAr[7].abut[4] =11;
    // neighbors of 6
	unitAr[ 6].ABUT_SIZE = 8;
	unitAr[ 6].abut[ 0] =  1;
	unitAr[ 6].abut[ 1] =  2;
	unitAr[ 6].abut[ 2] =  3;
	unitAr[ 6].abut[ 3] =  5;
	unitAr[ 6].abut[ 4] =  7;
	unitAr[ 6].abut[ 5] =  9;
	unitAr[ 6].abut[ 6] = 10;
	unitAr[ 6].abut[ 7] = 11;
    // neighbors of 5
	unitAr[ 5].ABUT_SIZE = 8;
	unitAr[ 5].abut[ 0] =  0;
	unitAr[ 5].abut[ 1] =  1;
	unitAr[ 5].abut[ 2] =  2;
	unitAr[ 5].abut[ 3] =  4;
	unitAr[ 5].abut[ 4] =  6;
	unitAr[ 5].abut[ 5] =  8;
	unitAr[ 5].abut[ 6] =  9;
	unitAr[ 5].abut[ 7] = 10;
    // neighbors of 4
	unitAr[4].ABUT_SIZE = 5;
	unitAr[4].abut[0] = 0;
	unitAr[4].abut[1] = 1;
	unitAr[4].abut[2] = 5;
	unitAr[4].abut[3] = 8;
	unitAr[4].abut[4] = 9;
    // neighbors of 3
	unitAr[3].ABUT_SIZE = 3;
	unitAr[3].abut[0] = 2;
	unitAr[3].abut[1] = 6;
	unitAr[3].abut[2] = 7;
    // neighbors of 2
	unitAr[2].ABUT_SIZE = 5;
	unitAr[2].abut[0] = 1;
	unitAr[2].abut[1] = 3;
	unitAr[2].abut[2] = 5;
	unitAr[2].abut[3] = 6;
	unitAr[2].abut[4] = 7;
    // neighbors of 1
	unitAr[1].ABUT_SIZE = 5;
	unitAr[1].abut[0] = 0;
	unitAr[1].abut[1] = 2;
	unitAr[1].abut[2] = 4;
	unitAr[1].abut[3] = 5;
	unitAr[1].abut[4] = 6;
    // neighbors of 0 t
	unitAr[0].ABUT_SIZE = 3;
	unitAr[0].abut[0] = 1;
	unitAr[0].abut[1] = 4;
	unitAr[0].abut[2] = 5;
	return true;
	}

	// resursively visit all neighbors passing the
	// current string to the next neighbor
	private void solver(int unitIndex, String s){
        unit current = unitAr[unitIndex];
        countSolve++;
        if(s.length()>2)
			if(dictionaryHS.contains(s)) {
				outputHS.add(s);
			}
        if(s.length() > (WORD_LENGTH_MAX - 1))return;
		s += current.c;
		if(current.c == 'Q')
			s += 'U';

		// can't come back to this letter
		current.visited = true;
		for(int i = 0; i < current.ABUT_SIZE; i++){
			int neighborIndex = current.abut[i];
			unit n = unitAr[neighborIndex];
			if(!n.visited){
				solver(neighborIndex,s);
			}
		}
		// this letter removed from string, can visit
		current.visited = false;
	}

	/*
	 * Call the recursive solve for each staring letter
	 * Incidently display the solution
	 *
	 */
    private void recursiveSolveCaller(){
		long start = System.currentTimeMillis();
		outputHS.clear();

		// solve for all words from each letter in grid
    	for(int i = 0; i < 16; i++){
			if(!setUpRecursion())return;// user did not enter 16 letters
    		String s = "";
    		solver(i, s);
			System.out.println("recursiveSolveCaller done with "+i);
    	}
		solutionTA.setText("");
		ArrayList<String> aL = new ArrayList<String>(outputHS);
		Collections.sort(aL, new StringSort());
		int count = 0;
		char charCount = 0;
		solutionTA.append("Found "+aL.size()+ " words\n");
		for(int i = 0; i < aL.size(); i++){
			count++;
			String so = aL.get(i);
			System.out.println(count+" " + aL.get(i));
			for(int k = so.length(); k < WORD_LENGTH_MAX+1; k++){
				so += " ";
			}
			solutionTA.append(so);
			charCount += 8;
			if(charCount > 70){
				charCount = 0;
				solutionTA.append("\n");
			}
		}
		long end = System.currentTimeMillis();
		double t = ((end-start)/1000.0);
		System.out.println("Time to solve " + t + " seconds");
		solutionTA.append("\nTime to solve " + t + " seconds");
    }

	// respond to user input.
	public void actionPerformed(ActionEvent e){

		// respond to Enter key event in text field
		if(e.getSource().equals(boggleInputTF)){
			recursiveSolveCaller();
		}
	}

    public static void main(String[] args) {
		Boggle b = new Boggle(args[0]);
    }


}
