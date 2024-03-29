package chord_section4;


import java.io.Serializable;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Set;


public class Node implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int nodeID;
	private Finger finger[];
	private IP ipAddress;
	private Node successor = null;
	private Node predecessor = null;
	public static final int m = 10;
	private HashMap<Integer, String> map = new HashMap<Integer, String>();
	private boolean allowSelfFinger = false;
	
	
	public Node(String ipAddress, int port){
		this.ipAddress = new IP(ipAddress, port);
		this.nodeID = this.ipAddress.hash(m);
		
		Chord.debugPrint("node with port " + port + " has ID " + this.nodeID);
		
		// initialize finger table's start 
		// NOTE: the actual node the finger points to, 
		//       is NOT initialized. This should be done later
		this.finger = new Finger[m + 1];
		for(int i = 1; i < m + 1; i++) {
			finger[i] = new Finger(m, nodeID, i);
		}
	}

	@Override
	public boolean equals(Object node){
		if(!(node instanceof Node)){
			return false;
		} 
		if(((Node) node).nodeID == this.nodeID){
			return true;
		}
		return false;
	}
	
	@Override
	public String toString(){
		return Integer.toString(nodeID);
	}
	
	/**
	 * This method sets the successor of THIS node
	 * @param i
	 */
	public void setSuccessor(Node i){
		this.successor = i;
	}

	/**
	 * returns the key value bucket of this node
	 */
	public HashMap<Integer, String> getMap() {
		return map;
	}
	
	/**
	 * returns the IP address of this node
	 * @return
	 */
	public IP getIP(){
		return this.ipAddress;
	}
	
	/**
	 * returns the id of this node
	 */
	public int getID(){
		return nodeID;
	}
	
	/**
	 * returns the predecessor of this node
	 */
	public Node getSuccessor() throws RemoteException{
		return finger[1].node;
	}
	
	/**
	 * returns the predecessor of this node
	 */
	public Node getPredecessor() throws RemoteException{
		return this.predecessor;
	}
	
	/**
	 * returns the finger table of this node
	 */
	public Finger[] getFingerTable() throws RemoteException {
		return finger;
	}
	
	/**
	 * sets the predecessor for this node 
	 * (can be done remotely)
	 */
	public void setPredecessor(Node newPredecessor) throws RemoteException{
		this.predecessor = newPredecessor;
	}
	
	/**
	 * This function adds one node to the finger table of THIS node
	 */
	public void addNewNode(Node newNodeID){
		for(int i = 1; i <= m; i++){
			if(nodeIdIsOnOrAfterLeftAndBeforeRight(newNodeID.nodeID, this.nodeID, finger[i].node.nodeID)){
				finger[i].node = newNodeID;
			}
		}
	}
	
	/**
	 * makes this node join the network
	 * @param node
	 * @throws RemoteException 
	 */
	public void join(int guiderPort) throws RemoteException{
		Node originalNode = new Node("localhost", guiderPort);
		initFingerTable(getNode(originalNode));
		update_others();
		initFingerTable(getNode(originalNode));
		update_others();
		NodeRMIInterface successorRMI = getNodeRMIStub(successor);
		this.map = successorRMI.getKeysAfterLeftAndUpToRight(predecessor.nodeID, this.nodeID);
	}
	
	/**
	 * this method returns and retrieves all the keys and values after the leftBound
	 * and upTo the right bound
	 */
	public HashMap<Integer,String> getKeysAfterLeftAndUpToRight(int left, int right) throws RemoteException{
		HashMap<Integer,String> exportMap = new HashMap<>();
		
		// extract all keys in the specified range, and if they have
		// a value, add them to the map to be exported
		for(int i = left + 1; i <= right; i++){
			String exportVal = map.remove(i);
			if(exportVal != null){
				exportMap.put(i, exportVal);
			}
		}
		
		return exportMap;
	}
	
	/**
	 * initializes THIS node finger table using the 
	 * @param nPrime
	 * @throws RemoteException 
	 */
	private void initFingerTable(Node nPrime) throws RemoteException{
		finger[1].node = nPrime.find_successor(finger[1].start);
		successor = finger[1].node;
		predecessor = getNode(successor).predecessor;
		NodeRMIInterface nodeRMIStub = getNodeRMIStub(successor);
		
		Chord.debugPrint("nodeRMI Stub is null: " + (nodeRMIStub == null));
		
		nodeRMIStub.setPredecessor(this);
		// ------------------------------------------------------
		
		for(int i = 1; i < m; i++){
			if(nodeIdIsOnOrAfterLeftAndBeforeRight(finger[i + 1].start, this.nodeID, finger[i].node.nodeID)){
				finger[i + 1].node = finger[i].node;
			}
			else{
				finger[i + 1].node = nPrime.find_successor(finger[i + 1].start);
			}
		}
	}
	
	/**
	 * if candidateNode is the finger on the candidateRow, then update 
	 * the candidate row with the candidate node
	 * @param candidateNode
	 * @param candidateRow
	 */
	public void update_finger_table(Node candidateNode, int candidateRow) throws RemoteException{
		if(nodeIdIsOnOrAfterLeftAndBeforeRight(candidateNode.nodeID, this.nodeID, finger[candidateRow].node.nodeID)){
			finger[candidateRow].node = candidateNode;
			
			NodeRMIInterface predecessorRMI = getNodeRMIStub(predecessor);
			predecessorRMI.update_finger_table(candidateNode, candidateRow);	
		}
	}
	
	/**
	 * This method update the finger table of all other nodes once this
	 * node has been added
	 * @throws RemoteException
	 */
	public void update_others() throws RemoteException{
		for(int i = 1; i <= m; i++){
			int logPredecessorIndex = this.nodeID - ((int) Math.pow(2, i - 1));
			if(logPredecessorIndex < 0){
				logPredecessorIndex = logPredecessorIndex + ((int) Math.pow(2, m));
			}
			
			Node logPredecessor = null;
			int logPredecessorPort = isNode(logPredecessorIndex);
			if(logPredecessorPort == -1){
				logPredecessor = find_predecessor(logPredecessorIndex);
			}
			else{
				logPredecessor = new Node("localhost", logPredecessorPort);
			}
			
			NodeRMIInterface predecessorRMI = getNodeRMIStub(logPredecessor);
			predecessorRMI.update_finger_table(this, i);
		}
	}
	
	/**
	 * Checks if the specified node id belongs to the database
	 */
	private int isNode(int nodeId){
		String[] nodes = null;
		try {
			nodes = Chord.registry.list();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String node : nodes) {
			int thisNode = Integer.parseInt(node);
			int thisNodeId = new Node("localhost", thisNode).getID();
			if (nodeId == thisNodeId){
				return thisNode;
			}
		}
		return -1;
	}
	
	/**
	 * This method checks if the given id is within on or after left bound
	 * but before rightbount
	 * @param id
	 * @param leftBound
	 * @param rightBound
	 * @return true if the finger is within [leftBound, rightBound)
	 */
	private boolean nodeIdIsOnOrAfterLeftAndBeforeRight(int id, int leftBound, int rightBound){
		boolean onOrAfterLeftBound = false;
		boolean beforeRightBound = false;
		
		if(this.nodeID == id && !allowSelfFinger){
			return false;
		}
		
		// deal with wrap-around case
		if(leftBound == rightBound){
			return true;
		}
		
		if(leftBound <= rightBound){
			onOrAfterLeftBound = leftBound <= id;
			beforeRightBound = id < rightBound;
		}
		else{		// leftBound > rightBound
			if(leftBound < id){
				rightBound = rightBound + ((int) Math.pow(2, this.m));
				onOrAfterLeftBound = leftBound <= id;
				beforeRightBound = id < rightBound;
			}
			else{	// leftBound >= id
				leftBound = leftBound - ((int) Math.pow(2, this.m));
				onOrAfterLeftBound = leftBound <= id;
				beforeRightBound = id < rightBound;
			}
		}
		return onOrAfterLeftBound && beforeRightBound;
	}
	
	
	
	
	/**
	 * finds the successor node of the associated id
	 * @throws RemoteException 
	 */
	public synchronized Node find_successor(int id) throws RemoteException {
		Node nPrime = find_predecessor(id);
		return getNode(nPrime.getSuccessor());
	}
	
	/**
	 * This method finds the predecessor node of the given id
	 * @throws RemoteException 
	 */
	public Node find_predecessor(int id) throws RemoteException {
		if(this.nodeID == this.getSuccessor().nodeID ){
			return this;
		}
		
		Node nPrime = this;
		while(nodeIdIsNotAfterLeftBoundAndWithinUpToRightBound(id, nPrime.nodeID, nPrime.getSuccessor().getID())) {
			nPrime = nPrime.closest_preceding_finger(id);
		}
		return nPrime;
	}
	
	/**
	 * This method checks if the given id does not lie after leftBound
	 * and up to rightBound
	 * @param id
	 * @param leftBound
	 * @param rightBound
	 * @return true if the finger is not within (leftBound, rightBound]
	 */
	private boolean nodeIdIsNotAfterLeftBoundAndWithinUpToRightBound(int id, int leftBound, int rightBound){
		if(leftBound == rightBound){
			return false;
		}
		
		boolean afterLeftBound = false;
		boolean upToRightBound = false;
		
		if(leftBound <= rightBound){
			afterLeftBound = leftBound < id;
			upToRightBound = id <= rightBound;
		}
		else{		// leftBound > rightBound
			if(leftBound < id){
				rightBound = rightBound + ((int) Math.pow(2, this.m));
				afterLeftBound = leftBound < id;
				upToRightBound = id <= rightBound;
			}
			else{	// leftBound >= id
				leftBound = leftBound - ((int) Math.pow(2, this.m));
				afterLeftBound = leftBound < id;
				upToRightBound = id <= rightBound;
			}
		}
		return !(afterLeftBound && upToRightBound);
	}
	
	/**
	 * This method finds the finger of THIS node that is preceding
	 * of the argument ID
	 */
	public Node closest_preceding_finger(int id) {
		for(int i = m; i >= 1; i--){
			int thisFingerID = finger[i].node.getID();
			
			if(fingerIsWithinNonInclusiveBoundsOf(thisFingerID, this.nodeID, id)) {
				return getNode(finger[i].node);
			}
			
		}
		return this;
	}
	
	/**
	 * This method checks if the given finger lies within the given
	 * non-inclusive bounds
	 * @param finger
	 * @param leftBound
	 * @param rightBound
	 * @return true if the finger lies within (leftBound, rightBound)
	 */
	private boolean fingerIsWithinNonInclusiveBoundsOf(int finger, int leftBound, int rightBound){
		boolean afterLeftBound = false;
		boolean beforeRightBound = false;
		
		// deal with the wrapAround case
		if(leftBound == rightBound){
			if(finger == leftBound){
				return false;
			}
			else{
				return true;
			}
		}
		
		
		if(leftBound <= rightBound){
			afterLeftBound = leftBound < finger;
			beforeRightBound = finger < rightBound;
		}
		else{		// leftBound > rightBound
			if(leftBound < finger){
				rightBound = rightBound + ((int) Math.pow(2, this.m));
				afterLeftBound = leftBound < finger;
				beforeRightBound = finger < rightBound;
			}
			else{	// leftBound >= finger
				leftBound = leftBound - ((int) Math.pow(2, this.m));
				afterLeftBound = leftBound < finger;
				beforeRightBound = finger < rightBound;
			}
		}
		return afterLeftBound && beforeRightBound;
	}
	
	/**
	 * gets the value associated with this key in this node's
	 * bucket
	 */
	public String getValue(int keyID){
		return map.get(keyID) + " keyID: " + keyID + " (stored at node with ID  " + this.nodeID + " and port " + this.ipAddress.getPort() + ")";
	}
	
	/**
	 * puts the key value pair in this node's bucket
	 */
	public void putValue(int keyID, String value){
		map.put(keyID, value);
	}
	
	/**
	 * This method gets the value associated with the following key in this bucket,
	 * or null otherwise
	 * @param key
	 * @return
	 * @throws RemoteException 
	 */
	public String get(String key) throws RemoteException {
		int keyID = (int) (Long.parseLong((Hasher.hash(key)).substring(0, 8),16) % Math.pow(2, m));
		Node nodeForGet = find_successor(keyID);
		
		if(nodeForGet.nodeID == this.nodeID){
			return this.getValue(keyID);
		}
		
		NodeRMIInterface nodeRMI = getNodeRMIStub(nodeForGet);
		return nodeRMI.getValue(keyID);
	}
	
	/**
	 * This method puts the specified key value pair within
	 * this node's bucket
	 * @param key
	 * @param value
	 * @throws RemoteException 
	 */
	public void put(String key, String value) throws RemoteException {
		int keyID = (int) (Long.parseLong((Hasher.hash(key)).substring(0, 8),16) % Math.pow(2, m));
		Node nodeForPut = find_successor(keyID);
		
		if(nodeForPut.nodeID == this.nodeID){
			this.putValue(keyID, value);
		}
		else{
			NodeRMIInterface nodeRMI = getNodeRMIStub(nodeForPut);
			nodeRMI.putValue(keyID, value);
		}
	}
	
	/**
	 * This method adds all the keys of leaving node to this
	 */
	public void obtainKeys(HashMap<Integer, String> newKeys) throws RemoteException{
		Set<Integer> keySet = newKeys.keySet();
		for(Integer key : keySet){
			String newEntry = newKeys.get(key);
			
			if(newEntry != null){
				this.map.put(key, newEntry);
			}
			
		}
		
	}
	
	/**
	 * deletes the given node from THIS node
	 */
	public void deleteNode(Node goneNode) throws RemoteException{
		if(getSuccessor().getID() == goneNode.getID()){
			successor = goneNode.getSuccessor();
		}
		
		if(find_predecessor(nodeID).getID() == goneNode.getID()){
			predecessor = goneNode.predecessor;
		}
		
		for(int i = 1; i <= m; i++){
			if(finger[i].node.getID() == goneNode.getID()){
				finger[i].node = goneNode.getSuccessor();
			}
		}
	}
	
	
	
	/**
	 * This method gets a node object from the network
	 * out of the specified nodeID
	 * @param id
	 * @return a node object of the id of the given ID
	 */
	public Node getNode(Node node) {
		if(node.nodeID == nodeID || node == this) {
			return this;
		}
		try {
            Registry registry = LocateRegistry.getRegistry("localhost", Registry.REGISTRY_PORT);
            NodeRMIInterface stub = (NodeRMIInterface) registry.lookup(Integer.toString(node.ipAddress.getPort()));
            Node n = new Node("localhost", node.ipAddress.getPort());
            n.finger = stub.getFingerTable();
            n.predecessor = stub.getPredecessor();
            n.successor = stub.getSuccessor();
            n.nodeID = stub.getID();
            n.map = stub.getMap();
            return  n;
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
		return null;
	}
	
	/**
	 * this method gets an RMI stub for the specified node ID
	 */
	public static NodeRMIInterface getNodeRMIStub(Node node){
		try {
            Registry registry = LocateRegistry.getRegistry("localhost", Registry.REGISTRY_PORT);
            NodeRMIInterface stub = (NodeRMIInterface) registry.lookup(Integer.toString(node.ipAddress.getPort()));
            return  stub;
        } catch (Exception e) {
            System.out.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
		return null;
	}
	
	
}
