import java.io.Serializable;

public class AuctionItem implements Serializable {
    int itemID;
    String name;
    String description;
    int highestBid;
}