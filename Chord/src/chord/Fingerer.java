package chord;

public class Fingerer extends Thread {

	private Node myNode;
	
	public Fingerer(Node node){
		myNode = node;
	}
	
	public void run(){
		while(true){
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			myNode.fix_fingers();
		}
	}
}
