/**
 * Created by satejmhatre on 10/26/16.
 */

import CR.*;
import DDS.*;

import java.util.ArrayList;
import java.util.Scanner;


public class Player {
    private bjPlayer player = null;
    private ArrayList<CR.card> playerCards;

    private Player() {
        player = new bjPlayer();
        player.uuid = UUIDGen.generate_UUID();
        player.dealer_id = 0;
        player.wager = 0;
        player.credits = 100;
        player.action = bjp_action.none;
        player.seqno = 0;
        playerCards = new ArrayList<>();
        this.Publish(bjp_action.joining);
    }

    public static void main(String args[]) {
        Player p = new Player();

        System.out.println(MAX_PLAYERS.value);
        Scanner reader = new Scanner(System.in);
        System.out.println("Waiting for action:");
        p.SubscribeDealer();


//        System.out.println("Enter a number: ");
//        int n = reader.nextInt();
//        System.out.println("number: " + n);
        //Publish("Player", p);

    }

    public void PlayerPrint(String x) {
        System.out.println("[Player " + this.player.uuid + " !! SeqNo: " + this.player.seqno + "] " + x);
    }

    public void DealerCollecting(bjDealer d) {
        PlayerPrint("Message received from Dealer with id " + d.uuid + " :");
        Scanner a = new Scanner(System.in);
        PlayerPrint("Enter bets:");
        //int x = a.nextInt();
        int x = 10;
        if (x < this.player.credits) {
            this.player.credits = this.player.credits - x;
            this.player.wager = x;
        } else {
            PlayerPrint("You don't have enough credits!!!");
        }
        this.Publish(bjp_action.wagering);
    }

    public void PlayCards(bjDealer d) {
        int sum = 0;
        card c = new card();
        c.base_value = d.cards[0].base_value;
        c.suite = d.cards[0].suite;
        c.visible = d.cards[0].visible;
        playerCards.add(c);
        CardFunctions.PrintCards(playerCards);
        int aceExists = 0;
        for (int i = 0; i < playerCards.size(); ++i) {
            if (playerCards.get(i).base_value == 'A') {
                aceExists++;
            }
            sum += CardFunctions.GetValue(playerCards.get(i).base_value);
        }
        if (sum > 21) {
            while (aceExists != 0) {
                sum -= 10;
                aceExists--;
                if (sum == 21) {
                    PlayerPrint("BLACKJACK!");
                    break;
                }
            }
            if (sum > 21) {
                PlayerPrint("Sum = " + sum + "\nBUSTED!");
                this.Publish(bjp_action.exiting);
                return;
            }
        }
        if (sum == 21) {
            PlayerPrint("BLACKJACK!");
        }
        Scanner a = new Scanner(System.in);
        PlayerPrint("Sum = " + sum + "\nHit(1) or Stay(2)?:");
        int x = a.nextInt();
        if (x == 1) {
            this.Publish(bjp_action.requesting_a_card);
        } else {
            this.Publish(bjp_action.exiting);
        }
    }

    public void SubscribeDealer() {
        Runnable b = () -> {
            DDSEntityManager mgr = new DDSEntityManager();

            // create Domain Participant
            mgr.createParticipant("CR");

            // create Type
            bjDealerTypeSupport msgTS = new bjDealerTypeSupport();
            mgr.registerType(msgTS);

            // create Topic
            mgr.createTopic("Dealer");

            // create Subscriber
            mgr.createSubscriber();

            // create DataReader
            mgr.createReader();

            // Read Events

            DataReader dreader = mgr.getReader();
            bjDealerDataReader dealerReader = bjDealerDataReaderHelper.narrow(dreader);

            bjDealerSeqHolder msgSeq = new bjDealerSeqHolder();
            SampleInfoSeqHolder infoSeq = new SampleInfoSeqHolder();

            PlayerPrint("Dealer Subscriber Ready...");
            boolean terminate = false;
            int count = 0;

            while (!terminate) { // We dont want the example to run indefinitely
                dealerReader.take(msgSeq, infoSeq, LENGTH_UNLIMITED.value,
                        ANY_SAMPLE_STATE.value, ANY_VIEW_STATE.value,
                        ANY_INSTANCE_STATE.value);

                {
                    for (int i = 0; i < msgSeq.value.length; i++) {
                        bjDealer d = msgSeq.value[i];
                        if (d.getClass() == bjDealer.class) {
                            if (this.player.dealer_id == 0) {
                                if (d.action == bjd_action.waiting) {
                                    PlayerPrint("Dealer has Entered!");
                                    this.player.dealer_id = d.uuid;
                                    this.Publish(bjp_action.joining);
                                } else {
                                    continue;
                                }
                            }
                            if (d.target_uuid == this.player.uuid) {
                                if (d.action == bjd_action.waiting) {
                                    PlayerPrint("Dealer has already Entered!");
                                    //this.player.dealer_id = d.uuid;
                                    //this.Publish(bjp_action.joining);
                                }
                                if (d.action == bjd_action.collecting) {
                                    PlayerPrint("Dealer collecting!");
                                    this.DealerCollecting(d);
                                    //this.player.action = bjp_action.requesting_a_card;

                                    ++count;
                                    continue;
                                }
                                if (d.action == bjd_action.dealing) {
                                    PlayerPrint("Cards Recieved");
                                    this.PlayCards(d);
                                }
                            }
                            this.player.seqno++;
                        }

                    }

                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    // nothing to do
                }
            }
            dealerReader.return_loan(msgSeq, infoSeq);

            // clean up
            mgr.getSubscriber().delete_datareader(dealerReader);
            mgr.deleteSubscriber();
            mgr.deleteTopic();
            mgr.deleteParticipant();
        };
        Thread t = new Thread(b);
        t.start();
    }

    public void Publish(bjp_action action) {
        DDSEntityManager mgr = new DDSEntityManager();
        // create Domain Participant
        mgr.createParticipant("CR");
        // create Type
        bjPlayerTypeSupport msgTS = new bjPlayerTypeSupport();
        mgr.registerType(msgTS);
        // create Topic
        mgr.createTopic("Player");
        // create Publisher
        mgr.createPublisher();
        // create DataWriter
        mgr.createWriter();
        // Publish Events
        DataWriter dwriter = mgr.getWriter();
        bjPlayerDataWriter playerWriter = bjPlayerDataWriterHelper.narrow(dwriter);
        playerWriter.register_instance(this.player);

        this.player.action = action;
        if (action == bjp_action.joining) {
//            SubscribeDealer();
        } else if (action == bjp_action.wagering) {
            PlayerPrint("Player is wagering");
        }
        int status = playerWriter.write(this.player, HANDLE_NIL.value);
        ErrorHandler.checkStatus(status, "MsgDataWriter.write");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            // nothing to do
        }
        this.player.seqno++;
        // clean up
        mgr.getPublisher().delete_datawriter(playerWriter);
        mgr.deletePublisher();
        mgr.deleteTopic();
        mgr.deleteParticipant();

    }
}
