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

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class LamportMutex {
    LamportClock c;
    int numAcks;
    Queue<QueueObject> q; // request queue
    Linker linker;
    AtomicBoolean needToPoll;
    
    HashSet<ClientRequest> sendedReleases = new HashSet<ClientRequest>();
    
    ReentrantLock pollingLock = new ReentrantLock();
    Condition pollingCondition = pollingLock.newCondition();

    public LamportMutex(Linker linker) {
        this.linker = linker;
        needToPoll = new AtomicBoolean(false);
        c = new LamportClock();
        q = new PriorityQueue<QueueObject>(1, new Comparator<QueueObject>() {
            public int compare(QueueObject a, QueueObject b) {
                return QueueObject.compare(a, b);
            }
        });
        numAcks = 0;
        // public HeartbeatChecker(Linker linker, LamportMutex lock)
    }

    public synchronized void requestCS(ClientRequest clientRequest) {
        
         c.tick(); 
         q.add(new QueueObject(c.getValue(), linker.connector.myID, clientRequest));
         this.numAcks = 0;
         needToPoll.set(q.peek().serverID != linker.connector.myID);
         pollingLock.lock();
         pollingCondition.signalAll();
         pollingLock.unlock();
         
         // TODO: Send Message to all. Update linker.N if necessary  
         Set<Integer> keysSet = linker.connector.link.keySet();
         Iterator<Integer> setIterator = keysSet.iterator();
         while(setIterator.hasNext()){
            Integer otherServerID = setIterator.next(); 
             
            if ((otherServerID != null) && (otherServerID != linker.connector.myID)) {
                try {
                    linker.sendMessage(otherServerID, new RequestMessage(c, clientRequest));
                    linker.ackLock.lock();
                    boolean serverDied = !linker.ackCondition.await(100, TimeUnit.MILLISECONDS);
                    linker.ackLock.unlock();
                    if (serverDied) {
                        
                        if (Server.DEBUG) {
                            System.out.println("server " + otherServerID + " seems to have died");
                        }
                        
                        setIterator.remove();
                        linker.N--;
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    if (Server.DEBUG) {
                        e.printStackTrace();
                    }
                }
            }
         }
         
         // TODO: Add heartbeat check 
         while((q.peek().serverID != linker.connector.myID) || (this.numAcks < linker.N-1)){
             try {
                 if(Server.DEBUG){
                     System.out.println("I am waiting to be first in queue!!");
                 }
                 
                wait();
            } catch (InterruptedException e) {
                if (Server.DEBUG) {
                    e.printStackTrace(); // TODO Auto-generated catch block
                }
            }
         }
         
    }

    public synchronized void releaseCS(ClientRequest clientRequest) {
        q.remove();
        if(q.peek() == null){
            needToPoll.set(false);
        }
        else {
            needToPoll.set(q.peek().serverID != linker.connector.myID);
        }
        pollingLock.lock();
        pollingCondition.signalAll();
        pollingLock.unlock();
        
        // TODO: SEND RELEASE TO ALL sendMsg(neighbors, "release",
        // c.getValue());
        Set<Integer> keysSet = linker.connector.link.keySet();
        Iterator<Integer> setIterator = keysSet.iterator();
        while (setIterator.hasNext()) {
            Integer otherServerID = setIterator.next();
            if ((otherServerID != null) && (otherServerID != linker.connector.myID)) {
                try {
                    linker.sendMessage(otherServerID, new ReleaseMessage(c, clientRequest));

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    if (Server.DEBUG) {
                        e.printStackTrace();
                    }
                }
            }
        }
    
    }

    public synchronized void handleMsg(Object message, int src) {
        
        if (message instanceof RequestMessage) {
            RequestMessage reqMsg = (RequestMessage) message;
            int timeStamp = reqMsg.clockValue;
            c.receiveAction(timeStamp);
            
            q.add(new QueueObject(timeStamp, src, reqMsg.clientRequest));
            try {
                linker.sendMessage(src, new AcknowledgeMessage(c));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                if (Server.DEBUG) {
                    e.printStackTrace();
                }
            }
        } 
        else if (message instanceof ReleaseMessage) {
            ReleaseMessage relMsg = (ReleaseMessage) message;
            int timeStamp = relMsg.clockValue;
            c.receiveAction(timeStamp);
            
            // REBROADCAST THIS RELEASE!!
            if (!sendedReleases.contains(relMsg.clientRequest)) {
                Set<Integer> keysSet = linker.connector.link.keySet();
                for (Integer otherServerID : keysSet) {
                    if ((otherServerID != null) && (otherServerID != linker.connector.myID) && (otherServerID != src)) {
                        try {
                            linker.sendMessage(otherServerID, new ReleaseMessage(c, relMsg.clientRequest));

                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            if (Server.DEBUG) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                sendedReleases.add(relMsg.clientRequest);
            }
            else{
                notifyAll();
                return;
            }
            
            QueueObject newReady = null;
            for(QueueObject inQ : q){
                if(inQ.clientRequest.equals(relMsg.clientRequest)){
                    newReady = inQ;
                }
            }
            if(newReady == null){
                notifyAll();
                return;     // this is when we get a duplicate release
            }
            newReady.readyToDo = true;
            
            ArrayList<ClientRequest> jobsToDo = new ArrayList<ClientRequest>();
            
            while(q.peek() != null){
                if(q.peek().readyToDo == true){
                    jobsToDo.add(q.remove().clientRequest);
                }
            }
            
            if(q.peek() == null){
                needToPoll.set(false);
            }
            else {
                needToPoll.set(q.peek().serverID != linker.connector.myID);
            }
            pollingLock.lock();
            pollingCondition.signalAll();
            pollingLock.unlock();
            
            // Do all jobs in the jobsToDo array
            for(ClientRequest job : jobsToDo){
                String response = HandleClientThread.handleRequest(job.request, Server.onlineStore);
                HandleClientThread.backlog.put(job, response);
            }
            
        } 
        else if (message instanceof AcknowledgeMessage) {
            AcknowledgeMessage ackMsg = (AcknowledgeMessage) message;
            int timeStamp = ackMsg.clockValue;
            c.receiveAction(timeStamp);
            
            numAcks++;
        }
        notifyAll();
    }
}
