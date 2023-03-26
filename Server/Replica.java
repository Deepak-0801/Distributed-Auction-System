import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import javax.crypto.SealedObject;
import javax.crypto.KeyGenerator;
import javax.crypto.Cipher;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

import java.io.ObjectInputStream;  // Import the File class
import java.io.ObjectOutputStream; 
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import java.lang.Object;
import java.util.*;
import java.io.File;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature; 

import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import java.io.FileInputStream;


public class Replica extends UnicastRemoteObject implements Auction
{
    //private static final long serialVersionUID = 1L;
    private List<AuctionItem> AuctionList;
	private List<AuctionSaleItem> AuctionSaleItem;
	private List<SellerListing> SellerList;
	private List<BuyerListing> BuyerList;	
	private List<NewUserInfo> UserList;
    private static SecretKey aesKey = null;
   //private int newAuctionID = 0;
    //private int challengeValue = 0;
	private static PrivateKey privateKey;
	private static PublicKey publicKey;
	private List<String> emailList=new ArrayList<String>();  

    // Signing Algorithm
    private static final String SIGNING_ALGORITHM = "SHA256withRSA";
    private static final String RSA = "RSA";
	
	private String ReplicaName; 
	
	
	protected Replica(String RepName, List<AuctionItem> alist, List<AuctionSaleItem> saleList, List<SellerListing> sellList,List<NewUserInfo> usrList,List<BuyerListing> buyList)  throws RemoteException{ 
		super();
		this.ReplicaName = RepName;
		this.AuctionList = alist;
		this.AuctionSaleItem = saleList;
		this.SellerList = sellList;
		this.UserList = usrList;
		this.BuyerList = buyList;
	}
	
@Override
	public int getPrimaryReplicaID() throws RemoteException
	{
	   try{
		  // System.out.println("replica Name : " + ReplicaName);
		   String replicaID = ReplicaName.substring(8);

		   //System.out.println("get replica ID : " + replicaID);
		   SyncServerData();	
		   return Integer.parseInt(replicaID);
	   }
	   catch(Exception e){      
		    System.out.println("An error occurred.");
            e.printStackTrace();
        }
	   return 0;
	}

    //Create Server Signature using Server Private Key
	@Override
	public byte[] challenge(int userID) throws RemoteException
	{
	   try{
           Signature signature = Signature.getInstance(SIGNING_ALGORITHM);

            File myObj = new File("../keys/server_private.key");
            FileInputStream fos = new FileInputStream(myObj);
			byte[] buffer = new byte[(int) myObj.length()];
			fos.read(buffer);
			fos.close();
			byte[] key = decode(encode(buffer));
			String chStr = "auction";
			KeyFactory kf = KeyFactory.getInstance("RSA"); 
		    PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(buffer));
            signature.initSign(privateKey);
            signature.update(chStr.getBytes());
            return signature.sign();  
        }
        catch(Exception e){      
		    System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return null;  
	}
	 

    //Authenicate Client Request using CLient Public Key
	@Override
	public boolean authenticate(int userID, byte signature[]) throws RemoteException
	{
		byte[] pubKey = null;
		String uEmail = null;
		try{
            Signature signature1 = Signature.getInstance(SIGNING_ALGORITHM);
			uEmail = getUserEmail(userID);
			for (NewUserInfo user1 : UserList)
			{
            if (user1.userID == userID){
				pubKey = user1.publicKey;
				}
			}
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PublicKey publicKey = kf.generatePublic(new X509EncodedKeySpec(pubKey));

            signature1.initVerify(publicKey);
            signature1.update(uEmail.getBytes());
            return signature1.verify(signature); 

        }catch (Exception e){
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return false;
	}
	
   //Encode Byte Array	
	private static String encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }
	
	// Decode String
    private static byte[] decode(String data) {
        return Base64.getDecoder().decode(data);
    }
	
	//Generate Key Pair for Server
    public static void GenerateServerKeyPair() throws Exception
    {
        SecureRandom secureRandom = new SecureRandom();
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA);
        keyPairGenerator.initialize(1024);
        KeyPair keyP = keyPairGenerator.generateKeyPair();

        File myObj = new File("../keys/server_public.key");
		File myObj1 = new File("../keys/server_private.key");
        FileOutputStream fos = new FileOutputStream(myObj);
		FileOutputStream fos1 = new FileOutputStream(myObj1);
		
		privateKey = keyP.getPrivate();
		publicKey = keyP.getPublic();

		byte[] keyPub = decode(encode(publicKey.getEncoded()));
		byte[] keyPvt = decode(encode(privateKey.getEncoded()));
		
		fos.write(keyPub);
		fos1.write(keyPvt);
		fos.close();
		fos1.close();
    }
	
	//Generate Key Pair for Client
    public static KeyPair Generate_RSA_KeyPair(int userID)throws Exception
    {
        SecureRandom secureRandom = new SecureRandom();
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA);
        keyPairGenerator.initialize(2048, secureRandom);
        KeyPair userKey = keyPairGenerator.generateKeyPair();
		return userKey;
    }
	
	//Get Item Specification
	@Override
    public AuctionItem getSpec(int itemId) throws RemoteException
    {
		SyncServerData();
         for (AuctionItem item : AuctionList)
        {
			  if (item.itemID == itemId){
                try{
                    return item;
                }catch(Exception e){
                    System.out.println("An error occurred.");
                     e.printStackTrace();
                }
            }
        }
        return null;
    }
	
	//Buyer : Bid Function
	@Override
	public boolean bid(int userID, int itemID, int price) throws RemoteException{
	try {
		int chkBuyerExists = -1;
		for(int i = 0; i < AuctionList.size(); i++)
        {
			   if (AuctionList.get(i).itemID == itemID){
				if (AuctionList.get(i).highestBid < price){
					AuctionItem aItem = new AuctionItem();
					aItem.itemID = AuctionList.get(i).itemID;
					aItem.name = AuctionList.get(i).name;
					aItem.description = AuctionList.get(i).description;
					aItem.highestBid = price;
					AuctionList.set(i,aItem);
					
					for(int j = 0; j < BuyerList.size(); j++)
					{		
						if (BuyerList.get(j).itemID == itemID && BuyerList.get(j).buyerID == userID)
						{
							chkBuyerExists = j;
							break;	
						}
					}
						BuyerListing bItem = new BuyerListing();
						bItem.itemID = itemID;
						bItem.reservePrice = price;
						bItem.sellerID = getSellerID(itemID);
						bItem.sellerEmail = getUserEmail(getSellerID(itemID));
						bItem.buyerID = userID;
						bItem.buyerEmail = getUserEmail(userID);
						
						//If Buyer exist update Buyer else add as new buyer		
						if (chkBuyerExists >= 0){		
							BuyerList.set(chkBuyerExists,bItem);	
						}
						else
						{	
							BuyerList.add(bItem);
						}
					SyncServerData();	
					return true;
				}
				else {
					return false;
				}		
			}	
		}
	  } 
	  catch(Exception e){
		System.out.println("An error occurred.");
		e.printStackTrace();
	  }	
      return false;
	}
	
	//Seller : Close Auction Fucntion
	@Override
	public AuctionCloseInfo closeAuction(int userID, int itemID) throws RemoteException
	{
	  try {
		int chkSellerExists = -1;
		int chkItemExists = -1;
		int chkBuyerExists = -1;
		String result1 = "";
		int result2 = 0;

		for(int j = 0; j < SellerList.size(); j++)
		{		
			if (SellerList.get(j).itemID == itemID) 
			{
				chkItemExists = j;
				if (SellerList.get(j).sellerID == userID){
					chkSellerExists = j;
					break;
				}	
			}
		}
		
		for(int i = 0; i < BuyerList.size(); i++)
		{		
			if (BuyerList.get(i).itemID == itemID) 
			{
				chkBuyerExists = i;
				break;	
			}
		}
		
		if (chkItemExists < 0){
			result1 = "Invalid Item ID, enter a valid Item ID";
        }
		else if (chkSellerExists < 0){
			result1 = "Cannot close Auction created by another seller";
		}	
		else if (chkBuyerExists < 0) {
			result1 = "No bids available. Auction Closed.";		
			removeSellerID(itemID);
			removeBuyerID(itemID);	
			removeAuctionItem(itemID);			
		}
		else{		
		int highestPrice = getHighestBid(itemID);
		int WinBuyer = getBuyerID(itemID,highestPrice);
		result1 = getUserEmail(WinBuyer);
		result2 = highestPrice;
 		removeSellerID(itemID);
		removeBuyerID(itemID);	
		removeAuctionItem(itemID);	 		
		}
		
		AuctionCloseInfo cInfo = new AuctionCloseInfo();
		cInfo.winningEmail = result1;
		cInfo.winningPrice = result2;
		SyncServerData();
		return cInfo;	
	  } 
	  catch(Exception e){
		System.out.println("An error occurred.");
		e.printStackTrace();
	  }	
	  return null;
	}
	
	//Seller and Buyer : List All Auction Items
	@Override
	public AuctionItem[] listItems() throws RemoteException
	{
		SyncServerData();
	    AuctionItem[] arr1 = AuctionList.toArray(new AuctionItem[AuctionList.size()]); 
		try{
			return arr1;
		} catch(Exception e){
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
		return null;
	}
	
	//Seller : Create New Auction Function
	@Override
	public int newAuction(int userID, AuctionSaleItem item) throws RemoteException
	{
		int tempID = getMaxItemID();
		int tempAID = SellerList.size()+1;
		
		//Add new auction Item
		AuctionItem aItem = new AuctionItem();
		aItem.itemID = tempID;
		aItem.name = item.name;
		aItem.description = item.description;
		aItem.highestBid = item.reservePrice;
		AuctionList.add(aItem);

		//Add new auction Sale Item
		AuctionSaleItem.add(item);

		//Add Seller Details	
		SellerListing sItem = new SellerListing();
		sItem.auctionID = tempAID;
		sItem.itemID = tempID;
		sItem.reservePrice = item.reservePrice;
		sItem.sellerID = userID;
		sItem.sellerEmail = getUserEmail(userID);
		SellerList.add(sItem);
		SyncServerData();
		return 0;
	}
	
	public String getUserEmail(int userID){
		for (NewUserInfo Users : UserList)
        {
			if (Users.userID == userID){
				int i = UserList.indexOf(Users);
				return emailList.get(i);
			}	
		}
		return null;
	}

	public int getSellerID(int itemID){
		for (SellerListing sList : SellerList)
        {
			if (sList.itemID == itemID){
				return sList.sellerID;
			}	
		}
		return 0;
	}

	public int getBuyerID(int itemID, int highestPrice){
		
		for (BuyerListing bList : BuyerList)
        {
			if (bList.itemID == itemID && bList.reservePrice == highestPrice){
				return bList.buyerID;
			}	
		}
		return 0;
	}
	
	public void removeSellerID(int itemID){
		for(int j = 0; j < SellerList.size(); j++)
		{		
			if (SellerList.get(j).itemID == itemID) {
			    SellerList.remove(j);	
			}	
		}
	}

	public void removeBuyerID(int itemID){
		for(int j = 0; j < BuyerList.size(); j++)
        {
			if (BuyerList.get(j).itemID == itemID){
				BuyerList.remove(j);
			}	
		}
	}
	
	public void removeAuctionItem(int itemID){
		for(int j = 0; j < AuctionList.size(); j++)
        {
			if (AuctionList.get(j).itemID == itemID){
				AuctionList.remove(j);
			}	
		}		
		
	}

	public int getHighestBid(int itemID){
         for (AuctionItem item : AuctionList)
        {
			  if (item.itemID == itemID){
				return item.highestBid;
			}	
		}
		return 0;
	}

	public int getMaxItemID(){
		if (AuctionList.size() > 0){
		 AuctionItem maxValue = AuctionList.stream().max(Comparator.comparing(v -> v.itemID)).get();	
		 return maxValue.itemID + 1;
		}
		else{
		 return 1;	
		}
	} 
	
	// New User creation
	@Override
	public NewUserInfo newUser(String email) throws RemoteException
	{
	
		String chkUser = "";
		for (String eList : emailList)
        {
            if (eList.equals(email)){
					chkUser = email;
					int i = emailList.indexOf(eList);
					return UserList.get(i);
            }
		}
			if (chkUser == ""){
				try{

				int tempUid = UserList.size()+1;

				KeyPair kp = Generate_RSA_KeyPair(tempUid);  
				PrivateKey uPvtKey = kp.getPrivate();
				PublicKey uPubKey = kp.getPublic();
				byte[] encPvtKey = decode(encode(uPvtKey.getEncoded()));
				byte[] encPubKey = decode(encode(uPubKey.getEncoded()));
		
				NewUserInfo User1 = new NewUserInfo();
				User1.userID = tempUid;
				User1.privateKey = encPvtKey;
				User1.publicKey = encPubKey;
				UserList.add(User1);
				
				emailList.add(email);
				
				}
				 catch(Exception e){
                    System.out.println("An error occurred.");
                    e.printStackTrace();
                } 
			}
		SyncServerData();	
        return UserList.get(UserList.size()-1);
		
	}

/* 	public List<AuctionItem> getAuctionItem()
	  {
		return AuctionList;
	  } */
	  
@Override
	public void setServerData(List<AuctionItem> aList, List<AuctionSaleItem> saleList, List<SellerListing> sellList,List<BuyerListing> buyList,List<NewUserInfo> usrList,List<String> emList )
	  {
		AuctionList = aList;
		AuctionSaleItem = saleList;
		SellerList = sellList;
		BuyerList = buyList;
		UserList = usrList;
		emailList = emList;		 
	  }
	  
@Override	    
	 public void SyncServerData()
	  {
		 List<AuctionItem> AucList = new ArrayList<>();
		 List<AuctionSaleItem> SaleItem = new ArrayList<>();
		 List<SellerListing> SellList = new ArrayList<>();
		 List<BuyerListing> BuyList = new ArrayList<>();	
		 List<NewUserInfo> UsersList = new ArrayList<>();
		 List<String> emList =new ArrayList<String>();  
		 
		 
		AucList = AuctionList;
		SaleItem = AuctionSaleItem;
		SellList = SellerList;
		BuyList = BuyerList;
		UsersList = UserList;
		emList = emailList;
		// Sync Data to other Servers
			
			try{
			Registry registry = LocateRegistry.getRegistry("localhost");
				for(String name : registry.list())
				{
				  if(!name.equals("FrontEnd") && !name.equals("Client")){
					try{
						
						Auction backupStub = (Auction) registry.lookup(name);
						//int I = backupStub.getPrimaryReplicaID();
						backupStub.setServerData(AucList,SaleItem,SellList,BuyList,UsersList,emList);		
					}
					catch (java.rmi.ConnectException CE) {
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
	 
	  }	  

    private static List<AuctionItem> initializeList() {
        List<AuctionItem> list = new ArrayList<>();
	return list;
    }
	
	private static List<AuctionSaleItem> initializeAuctionList() {
        List<AuctionSaleItem> list = new ArrayList<>();
	return list;
    }
	private static List<SellerListing> initializeSellerList() {
        List<SellerListing> list = new ArrayList<>();
	return list;
    }

	private static List<NewUserInfo> initializeUserList() {
		List<NewUserInfo> list = new ArrayList<>();
	return list;
    }
	private static List<BuyerListing> initializeBuyerList() {
        List<BuyerListing> list = new ArrayList<>();
	return list;
    }
	
	public static void main(String[] args) {
        try {
			if (args.length < 1) {
				System.out.println("Usage: java Replica <n>, where <n> like 1,2,3,...");
				return;
			}
		    GenerateServerKeyPair();
			String ReplicaID = args[0];
            String name = "FrontEnd";
			String ReplicaName = "Replica " + ReplicaID;
			Registry registry = LocateRegistry.getRegistry("localhost");
			// Check if Primary Replica already exists

		  for(String Servername : registry.list())
		  {
			try{
			if(Servername.equals("FrontEnd"))
				{
					name = ReplicaName;
					registry = null;
					break;
				}
			}
			catch (Exception exc) {
			  System.err.println("Client exception: " + exc.toString());
			  exc.printStackTrace();
			}
		  }			
			registry = LocateRegistry.getRegistry("localhost");
			
            registry.rebind(name, new Replica(ReplicaName,initializeList(),initializeAuctionList(),initializeSellerList(),initializeUserList(),initializeBuyerList()));
			
		if(!name.equals("FrontEnd"))
		{
			try{
				String name1 = "FrontEnd";
				Registry registry1 = LocateRegistry.getRegistry("localhost");
				Auction stub1 = (Auction) registry1.lookup(name1);		
				stub1.SyncServerData();	
			} 	
				catch (Exception e) {
				System.err.println("Exception:");
				e.printStackTrace();
			}
		}
			System.out.println("Server: " + ReplicaName + " ready ");
			
			} catch (Exception e) {
			System.err.println("Exception:");
			e.printStackTrace();
			}
    }
    
}