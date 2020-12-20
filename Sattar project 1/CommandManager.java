import java.io.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;


public class CommandManager {
	private String rawCommands; 
	private List<cmdThread> commandObjects; 
	private List<String[]> commandStrings; 
	private boolean validCommands = true; 
	private CurrentDir dir; 
	private CommandHistory history; 
	public CommandManager(String rawCommands, CommandHistory history, CurrentDir dir) {
		this.rawCommands = rawCommands;
		commandObjects = new LinkedList<cmdThread>(); 
		commandStrings = new LinkedList<String[]>(); 
		this.dir = dir;
		this.history = history;
	}
	public void parse() throws FileNotFoundException {
	
		rawCommands = rawCommands.replace(">", "|>"); 
		
		String[] pipeArray = rawCommands.split("\\|"); 
		for (int j = 0; j < pipeArray.length; j++) {
			pipeArray[j] = pipeArray[j].replaceAll("\\s+", " ");
			pipeArray[j] = pipeArray[j].trim();
		}
		
		for (int i = 0; i < pipeArray.length; i++) {

			String[] spaceArray = pipeArray[i].split(" "); 
			commandStrings.add(spaceArray); 
			
			validCommands = validate(spaceArray); 
			
			if (!validCommands) {
				break;
			}
		}
		
	
		if (validCommands) {
		
			if (validatePipeOrder()) {
				
				Iterator<String[]> commandIter = commandStrings.iterator();
				
			
				while (commandIter.hasNext()) {
					createCommands((String[])commandIter.next());
				}
			}
		}
			
		manageCommands(); 
	}
	public boolean validatePipeOrder() {
		String temp = null;
		boolean validOrder = true;
		
		for (int counter = 0; counter < commandStrings.size(); counter++) {
			
			temp = commandStrings.get(counter)[0]; 
			

			if (temp.equalsIgnoreCase("exit")) {
				System.out.println("invalid pipe order");
				validOrder = false;
				break;
			}
			else if (temp.equalsIgnoreCase("cd")) {
				if (counter != 0 || commandStrings.size() > 1) {
					System.out.println("invalid pipe order");
					validOrder = false;
					break;
				}
			}
			else if (temp.equalsIgnoreCase("pwd") || temp.equalsIgnoreCase("ls") || temp.equalsIgnoreCase("cat") || temp.equalsIgnoreCase("history")) {
				if (counter != 0) {
					System.out.println("invalid pipe order");
					validOrder = false;
					break;
				}
			}
		
			else if (temp.equalsIgnoreCase("grep")) {
				if (counter == 0 || commandStrings.size() < 2) {
					System.out.println("invalid pipe order");
					validOrder = false;
					break;
				}
			}	
		}	
		return validOrder; 
	}
	


	public void manageCommands() {
	for (cmdThread f : commandObjects) {
			f.start();
		}
		for (cmdThread f : commandObjects) {
			try {
				f.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public boolean validate(String[] temp) throws FileNotFoundException {
		boolean isValid = true;
		
		int i = 0;
		
		if (temp[i].equalsIgnoreCase("pwd") || temp[i].equalsIgnoreCase("ls") || temp[i].equalsIgnoreCase("history")) {
			isValid = validateGroup(temp);
		}

		else if (temp[i].equalsIgnoreCase("cat")) {
			isValid = validateCat(temp);
		}
		else if (temp[i].equalsIgnoreCase("cd")) {
			isValid = validateCd(temp);
		}
		else if (temp[i].equalsIgnoreCase("grep")) {
			isValid = validateGrep(temp);
		}
		else if (!temp[i].equalsIgnoreCase("exit")){
			System.out.println(temp[i] + ": invalid command");
			isValid = false;
		}
			
		return isValid;
	}
	
		

	public boolean validateCat(String[] commandAndArgs) {

		if (commandAndArgs.length == 1) {
			System.out.println("cat: missing argument");
			return false;
		}
		else {
		
			for (int i = 1; i < commandAndArgs.length; i++) {
				String path = commandAndArgs[i];
				File file = new File(dir.getDir() + System.getProperty("file.separator") + path);
				
	
				if (!file.isFile()) {
					System.out.println("cat: file not found");
					return false;
				}
			}
			return true;
		}
	}
	
	public boolean validateGroup(String[] commandAndArgs) {

		if (commandAndArgs.length > 1) {
			System.out.println(commandAndArgs[0] + ": invalid argument");
			return false;
		}
		
		return true;
	}
	

	public boolean validateCd(String[] commandAndArgs) throws FileNotFoundException {
		
		if (commandAndArgs.length == 1) {
			System.out.println("cd: missing argument");
			return false;
		}
	
		else if (commandAndArgs.length > 2) {
			System.out.println("cd: invalid argument");
			return false;
		}
		
		else if (commandAndArgs[1].equals("..") || commandAndArgs[1].equals(".")) {
			return true;
		}
	
		else {
			String path = commandAndArgs[1]; 
			File file = new File(dir.getDir() + System.getProperty("file.separator") + path);
	
			if (file.isDirectory()) {
				return true;
			}
		
			else {
				System.out.println("cd: directory not found");
				return false;
			}
		}
	}
	

	public boolean validateGrep(String[] commandAndArgs) {

		if (commandAndArgs.length == 1) {
			System.out.println("grep: missing argument");
			return false;
		}

		else if (commandAndArgs.length > 2) {
			System.out.println("grep: invalid argument");
			return false;
		}
		else {
			return true;
		}
	}
	
	public void createCommands(String[] spaceArray) {
		String j = spaceArray[0];

		if (j.equalsIgnoreCase("pwd")) {
			commandObjects.add(new Pwd(new LinkedBlockingQueue<Object>(), dir));
		}

		else if (j.equalsIgnoreCase("ls")) {
			commandObjects.add(new Ls(new LinkedBlockingQueue<Object>(), dir));
		}
		
		else if (j.equalsIgnoreCase("history")) {
			commandObjects.add(new History(new LinkedBlockingQueue<Object>(), history));
		}

		else if (j.equalsIgnoreCase("cat")) {
	
			List<String> files = Arrays.asList(Arrays.copyOfRange(spaceArray, 1, spaceArray.length));
			
			commandObjects.add(new Cat(new LinkedBlockingQueue<Object>(), files, dir));
		}

		else if (j.equalsIgnoreCase("cd")) {
			commandObjects.add(new Cd(spaceArray[1], dir));
		}

		else if (j.equalsIgnoreCase("grep")) {
		
			LinkedBlockingQueue<Object> temp = (LinkedBlockingQueue<Object>) commandObjects.get(commandObjects.size()-1).out; 
			
			commandObjects.add(new Grep(temp, new LinkedBlockingQueue<Object>(), spaceArray[1]));
		}


	}
	public void kill() {	
	}
}