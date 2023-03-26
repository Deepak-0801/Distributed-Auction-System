import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.KeyPair;

import javax.crypto.SealedObject;
import javax.crypto.KeyGenerator;
import javax.crypto.Cipher;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

import java.io.ObjectInputStream; // Import the File class
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.Object;
import java.io.File;

import java.util.Scanner;
import java.util.*;
import java.util.Formatter;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.io.*;
import java.net.*;

public class Client {
  private static final String SIGNING_ALGORITHM = "SHA256withRSA";
  private static final String RSA = "RSA";

  public static File getFileObj(String path) {
    File myObj = null;
    try {
      myObj = new File(path);
    } catch (Exception e) {}
    return myObj;
  }

  //Display Menu	
  public static String startMenu() {

    List < String > menuOptions = new ArrayList < > ();

    menuOptions.add("Welcome. Please select any option below ");
    menuOptions.add("----------------------------------------");
    menuOptions.add("Main Menu");
    menuOptions.add("---------");
    menuOptions.add("[1] -> Seller Menu");
    menuOptions.add("[2] -> Buyer Menu");
    menuOptions.add("[3] -> Exit");
    menuOptions.add("Enter your option: ");
	
    String menu = String.join("\n", menuOptions);

    return menu;
  }

  public static String sellerMenu() {

    List < String > menuOptions = new ArrayList < > ();

    menuOptions.add("Seller Menu");
    menuOptions.add("----------------------");
    menuOptions.add("[1] -> Get Item Specification");
    menuOptions.add("[2] -> Add New Auction Item");
    menuOptions.add("[3] -> List All Auction Items");
    menuOptions.add("[4] -> Close Auction");
	menuOptions.add("[5] -> Return to Main Menu");
    menuOptions.add("[6] -> Exit");
    menuOptions.add("Enter your option: ");
    String menu = String.join("\n", menuOptions);

    return menu;
  }

  public static String buyerMenu() {

    List < String > menuOptions = new ArrayList < > ();

    menuOptions.add("Buyer Menu");
    menuOptions.add("--------------");
    menuOptions.add("[1] -> Get Item Specification");
    menuOptions.add("[2] -> List All Auction Items");
    menuOptions.add("[3] -> Place Bid");
	menuOptions.add("[4] -> Return to Main Menu");
    menuOptions.add("[5] -> Exit");
    menuOptions.add("Enter your option: ");
	
    String menu = String.join("\n", menuOptions);

    return menu;
  }
  private static String encode(byte[] data) {
    return Base64.getEncoder().encodeToString(data);
  }
  private static byte[] decode(String data) {
    return Base64.getDecoder().decode(data);
  }

  //Create Client Signature using Client Private Key		
  public static byte[] Create_Digital_Signature(int userID, String eMail, byte[] pvtKey) {
    try {
      Signature signature = Signature.getInstance(SIGNING_ALGORITHM);
      KeyFactory kf = KeyFactory.getInstance("RSA");
      PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(pvtKey));

      signature.initSign(privateKey);
      signature.update(eMail.getBytes());
      return signature.sign();
    } catch (Exception e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
    return null;
  }

  //Authenticate Server using server public Key	
  public static boolean verifySigniture(byte[] signatureToVerify) {
    try {
      Signature signature = Signature.getInstance(SIGNING_ALGORITHM);

      File myObj = new File("../keys/server_public.key");
      FileInputStream fos = new FileInputStream(myObj);
      byte[] buffer = new byte[(int) myObj.length()];
      fos.read(buffer);
      fos.close();
      byte[] key = decode(encode(buffer));
      String chStr = "auction";

      KeyFactory kf = KeyFactory.getInstance("RSA");
      PublicKey publicKey = kf.generatePublic(new X509EncodedKeySpec(buffer));

      signature.initVerify(publicKey);
      signature.update(chStr.getBytes());
      return signature.verify(signatureToVerify);

    } catch (Exception e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
    return false;
  }

  //Authenticate Server
  public static boolean AuthenticateServer(Auction server, int userID) {
    try {
      byte[] x = server.challenge(userID);
      boolean request = verifySigniture(x);
      return request;

    } catch (Exception e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
    return false;
  }

  //Authenticate Client
  public static boolean AuthenticateClient(int userID, Auction server, String email, byte[] pvtKey) {
    try {
      //int x = server.authenticate();
      byte[] m = Create_Digital_Signature(userID, email, pvtKey);
      boolean request = server.authenticate(userID, m);
      return request;
    } catch (Exception e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
    return false;

  }
  
/* public static void clearConsole() {
    try {
        if (System.getProperty("os.name").contains("Windows")) {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        }
        else {
            System.out.print("\033\143");
        }
    } catch (IOException | InterruptedException ex) {}
}  */

  public static void main(String[] args) {
    if (args.length < 1) {
      System.out.println("Usage: java Client email");
      return;
    }

    int userID = 0;
    SecretKey aesKey = null;
    Auction server = null;
    byte[] buffer = null;
    byte[] pvtKey = null;
 try {

      String name = "Client";
      Registry registry = LocateRegistry.getRegistry("localhost");
      server = (Auction) registry.lookup(name);
    } catch (Exception e) {
      System.err.println("Exception:");
      e.printStackTrace();
    }
    String UserEmail = args[0];
    try {
		NewUserInfo user = server.newUser(UserEmail);
		userID = user.userID;
		pvtKey = user.privateKey;

//Authenticate Server and Client for every new client request

// Skipping Server Authentication
/*       if (AuthenticateServer(server, userID)) {
        System.out.println("Server Authenticated");
        if (AuthenticateClient(userID, server, UserEmail, pvtKey)) {
          System.out.println("User Authenticated");
          System.out.println("Authentication Complete");
        } else {
          System.out.println("User Authentication failed");
          return;
        }
      } else {
        System.out.println("Server Authentication failed");
        return;
      } */
	  
    } catch (Exception e) {
      System.err.println("Exception:");
      e.printStackTrace();
    }
    while (true) {

      System.out.println(startMenu());
      Scanner scanner = new Scanner(System.in); // Create a Scanner object
      try {
        int selectedMenu = scanner.nextInt();
        //Seller Menu Selected
        if (selectedMenu == 1) {
		  while (true) {
            System.out.println(sellerMenu());
			try {
            int sellerMenu = scanner.nextInt();
            //Get Item Spec
            if (sellerMenu == 1) {
              try {
                System.out.println("Enter Item Id: ");
                int selectedItemID = scanner.nextInt();
                AuctionItem result = server.getSpec(selectedItemID);
				if(result != null){
                System.out.println("Item Specification:");
                System.out.println("-------------------");				
                Formatter fmt = new Formatter();
                fmt.format("%-10s %-15s %-25s %10s \n", "Item ID ", "Name", "Description", "HighestBid");
                fmt.format("%60s \n", "---------------------------------------------------------------");
				fmt.format("%-9d %-15s %-25s %10d \n", selectedItemID, result.name, result.description, result.highestBid);	
				System.out.println(fmt);
				}
				else{
				 System.out.println("Invalid entry, enter a valid Item ID.");	
				}
						
              } catch (Exception e) {
                System.err.println("Exception:");
                e.printStackTrace();
              }
            }
            // Add Auction Items
            else if (sellerMenu == 2) {
              try {
                System.out.println("Enter Item Name:");
                scanner.nextLine();
                String auctionItemName = scanner.nextLine();
                System.out.println("Enter Item Description:");
                String auctionItemDescription = scanner.nextLine();
                System.out.println("Enter Item Reserve Price");
                int auctionReservePrice = scanner.nextInt();	
				
                AuctionSaleItem asItem = new AuctionSaleItem();
                asItem.name = auctionItemName;
                asItem.description = auctionItemDescription;
                asItem.reservePrice = auctionReservePrice;

                int x = server.newAuction(userID, asItem); 
                System.out.println("Item has been added");

              } catch (Exception e) {
                System.err.println("Exception:");
                e.printStackTrace();
              }
            }
            // List Auction Items
            else if (sellerMenu == 3) {
              try {
                System.out.println("Auction Item List:");
                System.out.println("------------------");
                Formatter fmt = new Formatter();
                fmt.format("%-10s %-15s %-25s %10s \n", "Item ID ", "Name", "Description", "HighestBid");
                fmt.format("%60s \n", "---------------------------------------------------------------");
                AuctionItem[] itemArray = server.listItems();
                for (int i = 0; i < itemArray.length; i++) {
                  fmt.format("%-9d %-15s %-25s %10d \n", itemArray[i].itemID, itemArray[i].name, itemArray[i].description, itemArray[i].highestBid);
                }
                System.out.println(fmt);
              } catch (Exception e) {
                System.err.println("Exception:");
                e.printStackTrace();
              }
            }
            // Seller Close Auction
            else if (sellerMenu == 4) {
              try {
                System.out.println("Enter Item Id to close Auction: ");
                int selectedItemID = scanner.nextInt();
                AuctionCloseInfo result = server.closeAuction(userID, selectedItemID);
				

				if(result.winningPrice > 0 ){
					System.out.println("Auction closed successfully");	
					Formatter fmt = new Formatter();					
					fmt.format("%-22s %10s \n", "Winning Email", "Winning Price");
					fmt.format("%35s \n", "------------------------------------");
					fmt.format("%-25s %10d \n", result.winningEmail, result.winningPrice);	
					System.out.println(fmt);
				}
				else{
					System.out.println(result.winningEmail);	
				}
              } catch (Exception e) {
                System.err.println("Exception:");
                e.printStackTrace();
              }
            }
            // Return to Main Menu			
            else if (sellerMenu == 5) {
			  break;
            } 
			// Exit Menu
			else if (sellerMenu == 6) {
              scanner.close();
              System.exit(0); 
            }
			else {
              System.out.println("Invalid option detected. Please select from the options.");
            }
          } catch (Exception e) {
            /* System.err.println("Exception:");
            e.printStackTrace(); */
            System.out.println("Invalid option detected. Please select from the options.");
          }
          } 
		}
		//Buyer Menu Selected 
		else if (selectedMenu == 2) {
          while (true) {
            System.out.println(buyerMenu());
		  try {	
            int buyerMenu = scanner.nextInt();
            //Get Item Spec
            if (buyerMenu == 1) {
              try {
                System.out.println("Enter Item Id: ");
                int selectedItemID = scanner.nextInt();
                AuctionItem result = server.getSpec(selectedItemID);
				if(result != null){
                System.out.println("Item Specification:");
                System.out.println("-------------------");				
                Formatter fmt = new Formatter();
                fmt.format("%10s %15s %25s %10s \n", "Item ID ", "Name", "Description", "HighestBid");
                fmt.format("%60s \n", "---------------------------------------------------------------");
				fmt.format("%9d %15s %25s %10d \n", selectedItemID, result.name, result.description, result.highestBid);	
				System.out.println(fmt);
				}
				else{
				 System.out.println("Invalid entry, enter a valid Item ID.");	
				}
						
              } catch (Exception e) {
                System.err.println("Exception:");
                e.printStackTrace();
              }
            }
             // List Auction Items
            else if (buyerMenu == 2) {
              try {
                System.out.println("Auction Item List:");
                System.out.println("------------------");
                Formatter fmt = new Formatter();
                fmt.format("%10s %15s %25s %10s \n", "Item ID ", "Name", "Description", "HighestBid");
                fmt.format("%60s \n", "---------------------------------------------------------------");
                AuctionItem[] itemArray = server.listItems();
                for (int i = 0; i < itemArray.length; i++) {
                  fmt.format("%9d %15s %25s %10d \n", itemArray[i].itemID, itemArray[i].name, itemArray[i].description, itemArray[i].highestBid);
                 }
                System.out.println(fmt);
              } catch (Exception e) {
                System.err.println("Exception:");
                e.printStackTrace();
              }
            }
            // Seller Bid Action
            else if (buyerMenu == 3) {
              try {
                System.out.println("Enter Item Id: ");
                int selectedItemID = scanner.nextInt();
                System.out.println("Enter Bidding Price: ");
                int bPrice = scanner.nextInt();
                boolean result = server.bid(userID, selectedItemID, bPrice);
                if (result == true) {
                  System.out.println("Bidding Successful");
                } else {
                  System.out.println("please bid with a Higher price or correct Item ID");
                }
              } catch (Exception e) {
                System.err.println("Exception:");
                e.printStackTrace(); 
              }
            }
			else if (buyerMenu == 4) {
				break;
            }
            //Exit Menu			
            else if (buyerMenu == 5) {
              scanner.close();
              System.exit(0);
            } else {
              System.out.println("Invalid option detected. Please select from the options.");
            }
          } catch (Exception e) {
			/* 	System.err.println("Exception:");
			e.printStackTrace(); */
            System.out.println("Invalid option detected. Please select from the options.");
          }
		}  

        }
		// Exit from Main Menu		
		else if (selectedMenu == 3) {
          scanner.close();
          System.exit(0);
        } else {
          System.out.println("Invalid option detected. Please select from the options.");
        }
      } catch (Exception e) {
        System.out.println("Invalid option detected. Please select from the options.");
      }
    }
  }
}