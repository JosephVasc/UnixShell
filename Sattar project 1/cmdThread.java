import java.util.concurrent.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class cmdThread extends Thread {
	protected BlockingQueue<Object> in; //input queue
	protected BlockingQueue<Object> out; //output queue
	protected volatile boolean done; 

	protected volatile boolean killed;

	public cmdThread (BlockingQueue<Object> in, BlockingQueue<Object> out) {
		this.in = in;
		this.out = out;
		this.done = false;
		this.killed = false;
	}


	public void cmdKill() {
		this.killed = true;
	}

	 
	public void run() {
        Object o = null;
     
         while(! this.done) {
        	if (in != null) {
	            try {
					o = in.take();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        	}
            o = transform(o);
            if (out != null && o != null) {
	            try {
					out.put(o);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}    
            }
        }
	}
	public abstract Object transform(Object o);
}// end of cmd thread class

class CurrentDir{
	private String currentDir;	

   public CurrentDir() {
		currentDir = System.getProperty("user.dir"); 
	}
	
	public String getDir() {
		return currentDir;
	}

	public void setDir(String destination, Boolean append) {
		if (append) {
			currentDir = currentDir + destination;
		}
		else {
			currentDir = destination;
		}
	}
}// end of class
class Cat extends cmdThread{
	private List<String> files; 
	int fileCount; 
	boolean fileTraversing; 
	File currentFile; 
	Scanner currentFileScan; 
	private CurrentDir dir; 
	
	public Cat(BlockingQueue<Object> out, List<String> files, CurrentDir dir) {
		super(null, out); 
		this.files = files;
		fileCount = 0;
		fileTraversing = false;
		this.dir = dir; 
	}
	
	public Object transform(Object o) {
		if (fileCount < files.size()) {		
			
			String line = null;
			
			if (fileTraversing == false) {
				currentFile = new File(dir.getDir() + System.getProperty("file.separator") + files.get(fileCount));
				
				try {
					currentFileScan = new Scanner(currentFile); 
				} 
				catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				fileTraversing = true; 
			}	
         else {
			
				if (currentFileScan.hasNextLine()) {
					line = currentFileScan.nextLine(); 
				}
				else {
					fileTraversing = false; 
					fileCount++; 
				}
			}
			return line; 
		}
		else {
			this.done = true; 
			currentFileScan.close(); 
			return new EndOfFileMarker(); 
		}
	}
}// end of class
class EndOfFileMarker {
}// end of class
class Cd extends cmdThread {
	private CurrentDir dir; 
	private String destination;
	
	public Cd(String destination, CurrentDir dir) {
		super(null, null); 
		this.dir = dir;
		this.destination = destination;
	}
	

	public Object transform(Object o) {
	
		if (destination.equals("..")) {		
			File file = new File(dir.getDir());
			String fileString;
			fileString = file.getParent(); 
			dir.setDir(fileString, false); 
		}
	
		else if (destination.equals(".")) {
			
		}
      else {

			dir.setDir(System.getProperty("file.separator") + destination, true);
		}
	   this.done = true; 
		return null; 
	}
}// end of class
class Ls extends cmdThread {
	private int counter; 
	File location; 
	String[] filesAndDirs; 
	
	public Ls(BlockingQueue<Object> out, CurrentDir dir) {
		super(null, out); 
		counter = 0;
		location = new File(dir.getDir()); 
		filesAndDirs = location.list(); 
	}
	
   public Object transform(Object o) {	
		if (counter < filesAndDirs.length) {
			o = filesAndDirs[counter];
			counter++;
			return o;
		}
		else {
			this.done = true; 
			return new EndOfFileMarker(); 
		}
	}
} // end of class
class Pwd extends cmdThread {
	private CurrentDir dir; 
	private boolean pwdReturned = false; 
	

	public Pwd(BlockingQueue<Object> out, CurrentDir dir) {
		super(null, out); 
		this.dir = dir;
	}
	public Object transform(Object o) {
		if (!pwdReturned) {
			pwdReturned = true;
			return dir.getDir(); 
		}
		else {
			this.done = true; 
			return new EndOfFileMarker(); 
		}
	}
}
class Grep extends cmdThread {
	private String pattern; 
	public Grep(BlockingQueue<Object> in, BlockingQueue<Object> out, String pattern) {
		super(in, out); 
		this.pattern = pattern;
	}
	

	public Object transform(Object o) {
		Object input = o;
		
		if (!(input instanceof EndOfFileMarker)) {
			if (((String) input).contains(pattern)) {
				return input; 
			}
	
			else {
				return null;
			}
		}
		else {
			this.done = true; 
			return o; 
		}
	}
}
class History extends cmdThread {
	private int counter;
	private CommandHistory history;
   
	public History(BlockingQueue<Object> out, CommandHistory history) {
		super(null, out); 
		counter = 0;
		this.history = history;
	}

	public Object transform(Object o) {
	
		if (counter < history.getHistory().size()) {
			String command = history.getHistory().get(counter);
			counter++;
			return command;
		}
		else {
			this.done = true; 
			return new EndOfFileMarker(); 
		}	
	}
}
class CommandHistory {
	private List<String> commandHistory; 
	
	public CommandHistory() {
		commandHistory = new LinkedList<String>();
	}

	public List<String> getHistory() {
		return commandHistory;
	}
	
	public void addHistory(String command) {
		commandHistory.add(command);
	}
}


