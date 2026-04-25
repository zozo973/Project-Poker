package com.example.projectpoker.model.game;

import java.util.ArrayList;

public final class PotUtil {

    public static ArrayList<Pot> addNewSidePot(ArrayList<Pot> pots, Player p)   {
        int potPriority = -1;
        int totalInvestment = 0;
        boolean adjustPot = true;
        int initialBet = 0;
        for (Pot pot : pots) {
            totalInvestment += pot.getToPlay();
        } if (p.getTotalInvestment() > totalInvestment) {
            potPriority = pots.size();
            adjustPot = false;
        } else {
            totalInvestment = 0;
            int subInvestment = 0;
            for (Pot pot : pots) {
                totalInvestment += pot.getToPlay();
                if (p.getTotalInvestment() <= totalInvestment) {
                    if (potPriority == -1) potPriority = pot.getPotPriority();
                    pot.stepPotPriority(1);
                } else {
                    subInvestment += pot.getToPlay();
                }
            }
            totalInvestment = subInvestment;
        }
        initialBet = p.getTotalInvestment() - totalInvestment;
        Pot newSidePot = new Pot(p,potPriority);
        newSidePot.addBet(p,initialBet);
        if (adjustPot) return adjustMultiplePots(pots, newSidePot);
        else pots.add(newSidePot); return pots;
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

    public static ArrayList<Pot> tryPayMultiplePots(ArrayList<Pot> pots, Player p) {
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

    public static ArrayList<Pot> payMultipleSidePots(ArrayList<Pot> pots, Player p) {
        Integer activeBet = p.getActiveBet();
        int bet = activeBet != null ? activeBet : 0;
        ArrayList<Pot> paidPots = new ArrayList<>();
        int totalPotToPlay = 0;
        for (Pot pot : pots) {
            totalPotToPlay += pot.getToPlay();
        }
        while (!pots.isEmpty()) {
            Pot pot =  findHighestPriorityPot(pots);
            pot.addBet(p,pot.getToPlay());
            bet -= pot.getToPlay();
            paidPots.add(pot);
            pots.remove(pot);
        }
        return paidPots;
    }

    public static Pot findHighestPriorityPot(ArrayList<Pot> pots) {
        int priority = 0;
        for (Pot pot : pots) {
            if (pot.getPotPriority() < priority) priority = pot.getPotPriority();
        }
        for (Pot pot : pots) {
            if (pot.getPotPriority() == priority) return pot;
        }
        return pots.getLast();
    }

    public static ArrayList<Pot> payOpenPot(ArrayList<Pot> pots, Player p) {
        Integer activeBet = p.getActiveBet();
        int bet = activeBet != null ? activeBet : 0;
        int i = getOpenPotIndex(pots);
        if (i == -1) throw new IllegalStateException("There is no active Pot");
        pots.get(i).addBet(p,bet);
        return pots;
    }

    public static int getOpenPotIndex(ArrayList<Pot> pots) {
        if (pots.size() == 1) return 0;
        for (int i = 0; i < pots.size(); i++) {
            if (pots.get(i).getIsOpen())
                return i;
        }
        return -1;
    }
}
