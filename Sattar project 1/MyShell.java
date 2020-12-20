import java.io.FileNotFoundException;
import java.util.*;

public class MyShell {
	

	public static void main(String[] args) {
		
		boolean flag = true; 
      String currentDir = System.getProperty("user.dir");
		CurrentDir dir = new CurrentDir(); 
		CommandHistory history = new CommandHistory();
		Scanner input = new Scanner(System.in); 
		
		String commandString; 		
      CommandManager manager; 
      
		
		//until user exits
      
		while (flag) {
			System.out.print(currentDir + "> ");
			commandString = input.nextLine(); 

			if (!commandString.isEmpty() && !commandString.equals("") && !commandString.equalsIgnoreCase("history")) {
				history.addHistory(commandString);
			}
			
			//if user specifies exit
			if (commandString.equalsIgnoreCase("exit")) {
				System.out.println("goodbye traveller, thank you for using joeys shell");
				System.exit(0);
			}
			else {
				manager = new CommandManager(commandString, history, dir); 
				
				try {
					manager.parse(); 
				} 
				catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}
}