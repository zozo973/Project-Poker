package com.example.projectpoker.model.game;

import java.util.ArrayList;

public final class PotUtil {

    public static ArrayList<Pot> tryPayMultiplePots(ArrayList<Pot> pots, Player p, int bet) {
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
            openPots = payMultipleSidePots(openPots,p,bet);
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

    public static ArrayList<Pot> payMultipleSidePots(ArrayList<Pot> pots, Player p, int bet) {
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

    public static int getOpenPotIndex(ArrayList<Pot> pots) {
        if (pots.size() == 1) return 0;
        for (int i = 0; i < pots.size(); i++) {
            if (pots.get(i).getIsOpen())
                return i;
        }
        return -1;
    }
}
