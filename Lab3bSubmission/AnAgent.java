import java.io.*;
import UWAgent.*;

public class AnAgent extends UWAgent implements Serializable {
    private String[] destination;
    public AnAgent( String[] args ) {
	System.out.println( "Injected" );
	this.destination = new String[args.length];
	for (int i = 0; i < args.length; i++) {
	    this.destination[i] = args[i];
	}
	
    }
    public AnAgent( ) { 
	System.out.println( "Injected" );
	this.destination = new String[1];
	this.destination[0] = "localhost";
    }

    public void init( ) {
	System.out.println( "I'll hop to " + this.destination[0] );
	printHop();
	
    }

    public void printHop( ) {
	System.out.println("Hop");
	hop(this.destination[0], "printStep");
    }

    public void printStep() {
	System.out.println("Step");
	hop(this.destination[1], "printJump");
    
    }

    public void printJump() {
	System.out.println("Jump");
    }


    
}
