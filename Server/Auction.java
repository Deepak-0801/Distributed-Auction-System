import java.rmi.Remote;
import java.rmi.RemoteException;
import java.lang.Object;
import java.util.*;

public interface Auction extends Remote {
public NewUserInfo newUser(String email) throws RemoteException;

public byte[] challenge(int userID) throws RemoteException;

public boolean authenticate(int userID, byte signature[]) throws RemoteException;

public AuctionItem getSpec(int itemID) throws RemoteException;

public int newAuction(int userID, AuctionSaleItem item) throws RemoteException;

public AuctionItem[] listItems() throws RemoteException;

public AuctionCloseInfo closeAuction(int userID, int itemID) throws RemoteException;

public boolean bid(int userID, int itemID, int price) throws RemoteException;

public int getPrimaryReplicaID() throws RemoteException;

public void setServerData(List<AuctionItem> alist, List<AuctionSaleItem> saleList, List<SellerListing> sellList,List<BuyerListing> buyList,List<NewUserInfo> usrList,List<String> emList ) throws RemoteException;

public void SyncServerData() throws RemoteException;
}