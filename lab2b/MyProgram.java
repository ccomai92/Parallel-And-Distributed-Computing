import mpi.*;   // for mpiJava

public class MyProgram {
    private final static int aSize = 100; // the size of dArray
    private final static int master = 0;  // the master rank
    private final static int tag = 0;     // Send/Recv's tag is always 0.

    public static void main( String[] args ) throws MPIException {
	// Start the MPI library.
	MPI.Init( args );
	
	// compute my own stripe
	int nodeSize = MPI.COMM_WORLD.Size(); 
	int tag = MPI.COMM_WORLD.Rank();
	int stripe = aSize / nodeSize; // each portion of array
	double[] dArray = null;

	if ( MPI.COMM_WORLD.Rank( ) == 0 ) { // master

	    // initialize dArray[100].
	    dArray = new double[aSize];
	    for ( int i = 0; i < aSize; i++ )
		dArray[i] = i;

	    // send a portion of dArray[100] to each slave
		// (1) implement by yourself
		for (int i = 1; i < nodeSize; i++) { // excluding myself 
			int offset = stripe * i; 
			int endIndex = stripe * (i + 1);  // endIndex exclusive 
			int count = endIndex - offset;   // should be 25 in this case 
			
			MPI.COMM_WORLD.Send(dArray, offset, count, MPI.DOUBLE, i, i);  // object, offset, count, datatype, dest, tag
			System.out.println("sent to " + i + " node");
		}
		

	}
	else { // slaves: rank 1 to rank n - 1

	    // allocates dArray[25].
	    dArray = new double[stripe];	    
	    // receive a portion of dArray[100] from the master
		// (2) implement by yourself
	    System.out.println("Hi, I am slave" + tag);
	    MPI.COMM_WORLD.Recv(dArray, 0, stripe, MPI.DOUBLE, 0, tag);
	    System.out.println("received, I am " + tag);

	}

	// compute the square root of each array element
	for ( int i = 0; i < stripe; i++ )
	    dArray[i] = Math.sqrt( dArray[i] );

	
	if ( MPI.COMM_WORLD.Rank( ) == 0 ) { // master

	    // receive answers from each slave
		// (3) implement by yourself
		for (int i = 1; i < nodeSize; i++) { // receving in order of tag number 
			int offset = stripe * i; 
			int count = 25;
			MPI.COMM_WORLD.Recv(dArray, offset, count, MPI.DOUBLE, i, i);
			System.out.println(i + "th result recevied by 0");
		}

	    // print out the results
	    for ( int i = 0; i < aSize; i++ )
		System.out.println( "dArray[ " + i + " ] = " + dArray[i] );
	}
	else { // slaves: rank 0 to rank n - 1
	    
	    // send the results back to the master
		// (4) implement by yourself
		MPI.COMM_WORLD.Send(dArray, 0, stripe, MPI.DOUBLE, 0, MPI.COMM_WORLD.Rank());
		System.out.println("sending " + tag + "result");

	}

	// Terminate the MPI library.
	MPI.Finalize( );
    }
}
