package com.example.projectpoker.model.game;

import java.util.ArrayList;
import java.util.Comparator;

public final class PotUtil {

    public static ArrayList<Pot> addNewSidePot(ArrayList<Pot> pots, Player p, int newPotSize)   {
        int potPriority = -1;
        boolean adjustPot = true;
        Pot bestPot = findBestAvailablePot(pots,p);

        if ((bestPot.getInvestmentPP()+newPotSize)==p.getActiveBet()) {
            potPriority = bestPot.getPotPriority() + 1;
        } else {
            potPriority = bestPot.getPotPriority();
        }

        if (bestPot.equals(findHighestPriorityPot(pots)) && bestPot.getInvestmentPP() <=
                (p.getTotalPotInvestment(bestPot) + p.getBalance())) {
            adjustPot = false;
        }

        Pot newSidePot = new Pot(p,potPriority);
        newSidePot.addBet(p,newPotSize);

        if (adjustPot) {
            newSidePot.setIsOpen(false);
            return adjustMultiplePots(pots, newSidePot);
        }
        else {
            newSidePot.setIsOpen(true);
            pots.add(newSidePot);
            return pots;
        }
    }

    private static ArrayList<Pot> adjustMultiplePots(ArrayList<Pot> pots, Pot newSidePot) {
        for (Pot pot : pots) {
            if (pot.getPotPriority() > newSidePot.getPotPriority()) {
                pot.adjustPot(newSidePot);
            }
            if (pot.getPotPriority() == newSidePot.getPotPriority()+1) {
                for (Player player : pot.getPlayers()) {
                    newSidePot.addPlayer(player);
                    newSidePot.addPlayer2Table(player,newSidePot.getToPlay());
                }
            }
        }
        pots.add(newSidePot);
        return pots;
    }

    // TODO: potentially remove or implement
    public static void setPotsToPlay(ArrayList<Pot> pots, int toPlay) {
        Integer openPotIndex = getOpenPotIndex(pots);
        if (openPotIndex != null) {
            pots.get(openPotIndex).setToPlay(toPlay);
        } else {
            for (int i = pots.size()-1; i >=0 ; i--) {
                if (pots.get(i).getIsOpen()) {
                    if (pots.get(i).getInvestmentPP()<toPlay) {
                        pots.get(i).setToPlay(pots.get(i).getInvestmentPP());
                        toPlay -= pots.get(i).getInvestmentPP();
                    } else {
                        pots.get(i).setToPlay(toPlay);
                        break;
                    }
                }
            }
        }
    }

    // -- test --
    public static int getToCall(ArrayList<Pot> pots, Player p) {
        Integer openPotIndex = getOpenPotIndex(pots);
        if (openPotIndex != null) {

            Pot openPot = pots.get(openPotIndex);
            int investedInOpenPot = p.getTotalPotInvestment(openPot);
            if (openPot.getToPlay() == investedInOpenPot) return openPot.getToPlay(p);
            return Math.max(0, openPot.getInvestmentPP() - investedInOpenPot);
        } else {
            int cumToPlay = 0; // cumulative to play
            for (Pot pot : pots) {
                cumToPlay += pot.getToPlay(p);
            }
            if (cumToPlay == p.getTotalInvestment()) return cumToPlay;
            return Math.max((cumToPlay - p.getTotalInvestment()), 0);
        }
    }

    public static ArrayList<Pot> handlePlayerBet(ArrayList<Pot> pots, Player p) {
        ArrayList<Pot> paidPots = tryPayMultiplePots(pots,p);
        return paidPots != null ? paidPots : payOpenPot(pots,p);
    }

    private static ArrayList<Pot> tryPayMultiplePots(ArrayList<Pot> pots, Player p) {
        ArrayList<Pot> openPots = new ArrayList<>();
        ArrayList<Pot> closedPots = new ArrayList<>();
        int numOpenPots = 0;
        for (Pot pot : pots) {
            if (pot.getIsOpen()) {
                openPots.add(pot);
                numOpenPots++;
            } else {
                closedPots.add(pot);
            }
        }
        if (numOpenPots>1) {
            int o = 0;
            int c = 0;
            openPots = payMultipleSidePots(openPots,p);
            ArrayList<Pot> paidPots = new ArrayList<>();
            for (Pot pot : pots) {
                if (pot.getIsOpen()) {
                    paidPots.add(openPots.get(o));
                    o++;
                } else {
                    paidPots.add(closedPots.get(c));
                    c++;
                }
            }
            return paidPots;
        }
        return null;
    }

    private static ArrayList<Pot> payMultipleSidePots(ArrayList<Pot> pots, Player p) {
        Integer activeBet = p.getActiveBet();
        int bet = activeBet != null ? activeBet : 0;
        pots.sort(Comparator.comparingInt(Pot::getPotPriority));

        for (Pot pot : pots) {
            if (pot.equals(pots.getLast())) {
                pot.addBet(p, bet);
                break;
            } else {
                pot.addBet(p, pot.getToPlay());
                bet -= pot.getToPlay();
            }
        }
        return pots;
    }

    public static Pot findHighestPriorityPot(ArrayList<Pot> pots) {
        if (pots.isEmpty()) throw new IllegalStateException("Pots should never be empty, Always at least one pot.");
        int priority = 0;
        for (Pot pot : pots) {
            if (pot.getPotPriority() < priority && pot.getIsOpen()) priority = pot.getPotPriority();
        }
        for (Pot pot : pots) {
            if (pot.getPotPriority() == priority) return pot;
        }
        return pots.getLast();
    }

    public static Pot findBestAvailablePot(ArrayList<Pot> pots, Player player) {
        if (pots.isEmpty()) throw new IllegalStateException("Pots should never be empty, Always at least one pot.");
        int priority = 0;
        for (Pot pot : pots) {
            if (pot.getPotPriority() < priority && pot.getIsOpen() && pot.getPlayers().contains(player)) {
                priority = pot.getPotPriority();
            }
        }
        for (Pot pot : pots) {
            if (pot.getPotPriority() == priority) return pot;
        }
        return pots.getLast();
    }

    private static ArrayList<Pot> payOpenPot(ArrayList<Pot> pots, Player p) {
        Integer activeBet = p.getActiveBet();
        int bet = activeBet != null ? activeBet : 0;
        Integer i = getOpenPotIndex(pots);
        if (i == null) throw new IllegalStateException("There multiple open pots, error in pot payment.");
        pots.get(i).addBet(p,bet);
        return pots;
    }

    public static Integer getOpenPotIndex(ArrayList<Pot> pots) {
        if (pots.size() == 1) {
            if (pots.getFirst().getIsOpen()) return 0;
            else {
                throw new IllegalStateException("There is One pot and it is closed.");
            }
        }
        ArrayList<Integer> potIndex = new ArrayList<>();
        for (int i = 0; i < pots.size(); i++) {
            if (pots.get(i).getIsOpen())
                potIndex.add(i);
        }
        if (potIndex.isEmpty()) {
            throw new IllegalStateException("There is multiple pots and they are all closed.");
        }
        if (potIndex.size() > 1) return null; // returns null if there are multiple open pots
        return potIndex.getFirst();
    }
}
