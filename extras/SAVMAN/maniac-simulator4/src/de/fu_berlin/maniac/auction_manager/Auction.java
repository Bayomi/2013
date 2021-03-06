package de.fu_berlin.maniac.auction_manager;

import java.net.Inet4Address;
import java.net.InetAddress;

import de.fu_berlin.maniac.exception.NegativeBidException;
import de.fu_berlin.maniac.general.ManiacStrategyInterface;
import de.fu_berlin.maniac.network_manager.Sender;
import de.fu_berlin.maniac.packet_builder.*;
import java.util.List;

/**
 * This class actually handles a specific auction. It determines the winner,
 * using your selectWinner() function, updates the Data-Packet, creates a new
 * BidWin-Packet and sends them.
 * @author maniacchallenge
 *
 */
public final class Auction implements Runnable {
	final int BACKBONE_MAXBID = 100;

	Data pack;
	Sender sender;
	ManiacStrategyInterface strat;
	MyAuctionManager auctionmanager;
	PacketBuilder packetbuilder;
	InetAddress myBackbone;

	public Auction(Data pack, Sender sender, ManiacStrategyInterface strat,
			MyAuctionManager auctionmanager, InetAddress backbone, PacketBuilder packetbuilder) {
		this.pack = pack;
		this.sender = sender;
		this.strat = strat;
		this.auctionmanager = auctionmanager;
		//this.packetbuilder = PacketBuilder.getInstance();
		this.packetbuilder = packetbuilder;
		this.myBackbone = backbone;
	}

	@Override
	public void run() {
		System.err.println("[" + this.packetbuilder.getDeviceIP() +  "] Auction Manager: ending the auction at: " + System.currentTimeMillis());
		System.out.println("[" + this.packetbuilder.getDeviceIP() +  "] Auction Manager: trying to select winner for:" + pack.getTransactionID());
                                                         
		
		if(!strat.dropPacketAfter(pack)){
			
			/* winner = null makes the node send the packet to the backbone */
			//Bid winner = strat.selectWinner(auctionmanager.getBidsByID(pack.getTransactionID()));
			
			// SIM: TODO replace with stategy! Include Simple BidWinner Strategy
			List<Bid> bids = auctionmanager.getBidsByID(pack.getTransactionID());
			System.out.println("[" + this.packetbuilder.getDeviceIP() +  "] Auction Manager: Received Bids: " + bids.size());
			int lowest = Integer.MAX_VALUE;
			int bidId = -1;
			for(int i = 0; i < bids.size(); i++){
				if(bids.get(i).getBid() < lowest){
				lowest = bids.get(0).getBid();
				bidId = i; 
			}
                                                            }
                                                           System.out.println("[" + this.packetbuilder.getDeviceIP() +  "] Auction Manager: Winner is Bid: " + bidId + " with Value: " + lowest);
                                                              
                                                           Bid winner = bids.get(bidId);
			
			if (winner != null) {
				System.out.println("[" + this.packetbuilder.getDeviceIP() +  "] Auction Manager:  Sending BidWins to all");
				sender.send(packetbuilder.buildBidWin(winner, pack.getFine()));
				packetbuilder.updateData((Data) pack, winner.getSourceIP());
				System.out.println("[" + this.packetbuilder.getDeviceIP() +  "] Auction Manager:  Sending Data to: " + winner.getSourceIP());   
				sender.send(pack);
			} else {
				System.err.println("[" + this.packetbuilder.getDeviceIP() +  "] No Winner => Backbone: ");
				/*
				if(!myBackbone.equals(null)){
					packetbuilder.updateData((Data) pack,
							(Inet4Address) this.myBackbone);
					try {
						winner = (Bid) packetbuilder.buildDummyBid(
								pack.getTransactionID(), BACKBONE_MAXBID);
						if(!winner.equals(null)){
							sender.send(packetbuilder.buildBidWin(winner, pack.getFine()));
							sender.send(pack);
							
						}
					} catch (NegativeBidException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				*/
			}
		}
		auctionmanager.getAuctions().remove(pack.getTransactionID());

	}

}
