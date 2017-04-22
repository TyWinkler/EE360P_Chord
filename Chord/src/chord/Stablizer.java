package chord;

public class Stablizer extends Thread{

	private Node myNode;
	
	public Stablizer(Node node){
		myNode = node;
	}
	
	public void run(){
		while(true){
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			myNode.stabilize();
		}
	}
	
}
