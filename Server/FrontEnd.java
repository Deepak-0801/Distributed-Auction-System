import java.io.*;
import java.net.*;
import java.lang.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.rmi.*;
import java.util.*;

public class FrontEnd extends UnicastRemoteObject implements Auction
{
  private Auction stub;
  private ArrayList<Auction> servers;
  private int userID = 0;
  
  
public FrontEnd() throws RemoteException
  {
    servers = getServerList();
	//stub = CheckPrimaryStatus(this);
	
		 String name = "FrontEnd";
		 Registry registry = LocateRegistry.getRegistry("localhost");
		 System.out.println("Check Server Status ");
		 try {   
			 
			stub = (Auction) registry.lookup(name);	
			 System.out.println("Connected to server : Replica " + stub.getPrimaryReplicaID());
			}
			catch (java.rmi.ConnectException CE) {
				System.out.println("FrontEnd Server Not found");
				stub = connectNewServer();
			}
			catch (NotBoundException NB) {
				System.out.println("FrontEnd Server Not Bound");
			}
			catch (Exception e) {
				System.err.println("Client exception: " + e.toString());
				e.printStackTrace();
			}	
 }
 
 
@Override
	public int getPrimaryReplicaID() 
	{
	try {
	stub = CheckPrimaryStatus(this);	
	int PrimaryReplicaID = stub.getPrimaryReplicaID();
	return PrimaryReplicaID;
	}
	catch (Exception e) {
		System.err.println("Client exception: " + e.toString());
		e.printStackTrace();
	}
		return 0;
	}
	
@Override
	public NewUserInfo newUser(String email)
	{
		try {
		stub = CheckPrimaryStatus(this);	
		NewUserInfo user = stub.newUser(email);
		userID = user.userID;
		return user;
		}
		catch (Exception e) {
		System.err.println("Client exception: " + e.toString());
		e.printStackTrace();
		}
		return null;
	}
	
@Override
	public boolean bid(int userID, int itemID, int price) 
	{
	try {
		stub = CheckPrimaryStatus(this);
	    boolean result = stub.bid(userID, itemID, price);
		return result;
	}
		catch (Exception e) {
		System.err.println("Client exception: " + e.toString());
		e.printStackTrace();
		}
		return false;	
	}

@Override
	public AuctionCloseInfo closeAuction(int userID, int itemID) 
	{
	try {
		stub = CheckPrimaryStatus(this);
		AuctionCloseInfo result = stub.closeAuction(userID, itemID);	
		return result;
	}
	catch (Exception e) {
		System.err.println("Client exception: " + e.toString());
		e.printStackTrace();
		}
		return null;
	}	

@Override	
    public AuctionItem getSpec(int itemId) 
    {
	try {
	    stub = CheckPrimaryStatus(this);	
		AuctionItem result = stub.getSpec(itemId);
	    return result;	
		}
		catch (Exception e) {
		System.err.println("Client exception: " + e.toString());
		e.printStackTrace();
		}
		return null;
	}
	
@Override
public boolean authenticate(int userID, byte signature[]) 
	{
	 return true;	
	}

@Override
	public byte[] challenge(int userID) 
	{
	return null;	
	}

@Override	
public int newAuction(int userID, AuctionSaleItem item) 
	{
		try {
	    stub = CheckPrimaryStatus(this);		
		int x = stub.newAuction(userID, item);
		return x;
		}
		catch (Exception e) {
		System.err.println("Client exception: " + e.toString());
		e.printStackTrace();
		}
		return 0;
	}

@Override
public AuctionItem[] listItems() 
	{
		try {
		stub = CheckPrimaryStatus(this);	
		AuctionItem[] itemArray = stub.listItems();
		return itemArray;
		}
		catch (Exception e) {
		System.err.println("Client exception: " + e.toString());
		e.printStackTrace();
		}
		return null;	
	}
 
@Override
	public void setServerData(List<AuctionItem> alist, List<AuctionSaleItem> saleList, List<SellerListing> sellList,List<BuyerListing> buyList,List<NewUserInfo> usrList,List<String> emList )
	  {
	  }		  
	  
@Override
	public void SyncServerData()
	{
	}


  public static void main(String args[]) throws Exception
  {
	try {
	Registry registry = LocateRegistry.getRegistry("localhost");
			
	registry.rebind("Client", new FrontEnd());
	System.out.println("FrontEnd Ready ");
	
	} catch (Exception e) {
	System.err.println("Exception:");
	e.printStackTrace();
	}
  }
  
 public static Auction CheckPrimaryStatus(FrontEnd myFrontEnd)
 {  
	try{
		 String name = "FrontEnd";
		 Registry registry = LocateRegistry.getRegistry("localhost");
		 System.out.println("Check Server Status");
		 try {   
			 
			 Auction stub1 = (Auction) registry.lookup(name);	
			 System.out.println("Connected to server : Replica " + stub1.getPrimaryReplicaID());
			 return stub1;
			}
			catch (java.rmi.ConnectException CE) {
				System.out.println("FrontEnd Server Not found");
				Auction stub1 = (Auction) registry.lookup(name);	
				stub1 = myFrontEnd.connectNewServer(); 
				if(stub1 == null){
				    System.out.println("No Active Server Replicas found");
				}
				return stub1;
			}
			catch (NotBoundException NB) {
				System.out.println("FrontEnd Server Not Bound");
			}
			catch (Exception e) {
				System.err.println("Client exception: " + e.toString());
				e.printStackTrace();
			}
		}
			catch (Exception e) {
				System.err.println("Client exception: " + e.toString());
				e.printStackTrace();
			}	
        return null; 		
} 
  
  
  
  public static ArrayList<Auction> getServerList()
  {
    ArrayList<Auction> serverList = new ArrayList<Auction>();
    try{
      Registry registry = LocateRegistry.getRegistry("localhost");
      for(String name : registry.list())
      {
	  if(!name.equals("Client")){  
        try{
         
          Auction serverStub = (Auction) registry.lookup(name);
           int I = serverStub.getPrimaryReplicaID();
		   
		//   System.out.println("Server Name : " + name + " -- Replica ID : " + I);	
		serverList.add(serverStub);	
        }
		catch (java.rmi.ConnectException CE) {
			 //System.out.println("Server connection error");
		}
        catch (Exception exc) {
          System.err.println("Client exception: " + exc.toString());
          exc.printStackTrace();
        }
      }
	 } 
    }
    catch (Exception exc) {
      System.err.println("Client exception: " + exc.toString());
      exc.printStackTrace();
    }

      return serverList;
    }


  // Method to promote backup server
  // unbind primary server
  // loop through list of servers 
  // try and set one of backup servers to primary server, i.e. rename to FrontEnd
  // rebind this new server.

  // When any service called by client, i.e add an getSpec, New Auction , Bid
  // or Close Bid. Front end attempts to connect primary server,
  // if unavailable, promotes backup server to primary server and continues.

 public Auction connectNewServer()
  {
    try{
	  System.out.println("Connecting to new backup server");
      Registry registry = LocateRegistry.getRegistry("localhost");
      ArrayList<Auction> serverList = FrontEnd.getServerList();
	  if (serverList.size() > 0)
	  {  
		  registry.unbind("FrontEnd"); 
		  for(Auction server: serverList)
			  {
				try{
			  
				  // set stub of backup server to server 1, i.e. make primary
				  // bind new primary
				  registry.rebind("FrontEnd", server);
				  int i = server.getPrimaryReplicaID();
				  String ReplicaName = "Replica " + i;
//		          registry.unbind("ReplicaName"); 				  
				  System.out.println("Connected to new Primary Server : " + ReplicaName );
				  return server;
				}
				catch (Exception e) {
				  System.err.println("Server exception: " + e.toString());
				  e.printStackTrace();
				}		  
				  
			 }
	  }
	  else
		  {
			 System.out.println("No Servers Found");
			 return null;
		  }
		
     return null;
    }
    catch (Exception e) {
      System.err.println("Server exception: " + e.toString());
      e.printStackTrace();
      return null;
    }
  }

}

