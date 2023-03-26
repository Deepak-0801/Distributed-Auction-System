import java.io.Serializable;

public class SellerListing implements Serializable 
{
    int auctionID;
	int itemID; 
    int reservePrice;
    int sellerID;   //client who listed auction
	String sellerEmail;
 }