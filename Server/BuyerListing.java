import java.io.Serializable;

public class BuyerListing implements Serializable 
{
	int itemID; 
    int reservePrice;
    int sellerID;   //client who listed auction
	String sellerEmail;
	int buyerID;   //client who bids
	String buyerEmail;
 }