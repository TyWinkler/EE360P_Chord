/*
 * LamportMutex.java
 * (adapted from Dr. Garg's example code)
 * EE 360P HW_4_Q1
 *
 * Aaron Babber (aab3456)
 * Santiago Echeverri (se7365)
 *
 */

package chord_section4;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class LamportMutex {
    LamportClock c;
    int numAcks;
    Queue<Timestamp> q; // request queue
    static int numProc = 1;
    
    ReentrantLock mutLock = new ReentrantLock();
	Condition mutCond = mutLock.newCondition();
    
    public LamportMutex() {
        c = new LamportClock();
        q = new PriorityQueue<Timestamp>(1, new Comparator<Timestamp>() {
            public int compare(Timestamp a, Timestamp b) {
                return Timestamp.compare(a, b);
            }
        });
        numAcks = 0;
    }

    /**
     * This functions asks the network for permission to access the CS
     * @param myId
     */
	public void requestCS(int myId) {
		int curNumProc = numProc;
		c.tick();
		q.add(new Timestamp(c.getValue(), myId));
		
		// send message to all other processes
		numAcks = 0;
		sendMsg("request", c.getValue());
		
		while ((q.peek().getPid() != myId) || (numAcks < curNumProc - 1))
			try {
				mutLock.lock();
				mutCond.await();
				mutLock.unlock();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

    public void releaseCS(String relMsg) {
    	q.remove();
    	
    	// tell all processes that it is over
		sendMsg(relMsg, c.getValue());
    }

    public void handleMsg(int srcId, String tag, int srcClk) {
    	//int timeStamp = m.getMessageInt();
		c.receiveAction(srcClk);
		if (tag.equals("request")) {
			q.add(new Timestamp(srcClk, srcId));
			
			// send message back to who sent you message
			sendMsg("ack",c.getValue(), srcId);
		} else if (tag.equals("releaseJoin")) {

			Iterator<Timestamp> it =  q.iterator();			    
			while (it.hasNext()){
				if (it.next().getPid() == srcId) it.remove();
			}
			numProc++;
		} else if (tag.equals("releaseLeave")) {

			Iterator<Timestamp> it =  q.iterator();			    
			while (it.hasNext()){
				if (it.next().getPid() == srcId) it.remove();
			}
			numProc--;
		} else if (tag.equals("ack"))
			numAcks++;
		
		mutLock.lock();
		mutCond.signalAll();
		mutLock.unlock();
    }
    
    private void sendMsg(String tag, int myClk){
		try {
			Integer myId = Chord.node.getIP().getPort();
			String myIdStr = myId.toString();
			String[] dests = Chord.registry.list();

			for (String destStr : dests) {
				if (destStr.equals(myIdStr))
					continue;

				int destId = Integer.parseInt(destStr);
				NodeRMIInterface destStub = Node.getNodeRMIStub(new Node("localhost", destId));
				destStub.newLamportMsg(myId, tag, c.getValue());
				
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
private void sendMsg(String tag, int myClk, int destId){
    	NodeRMIInterface destStub = Node.getNodeRMIStub(new Node("localhost", destId));
    	try {
			destStub.newLamportMsg(Chord.node.getIP().getPort(), tag, c.getValue());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
