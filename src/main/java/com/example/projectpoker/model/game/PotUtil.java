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

    // TODO: potentially remove or implement
    public static ArrayList<Pot> setPotsToPlay(ArrayList<Pot> pots, int toPlay) {
        int openPotIndex = getOpenPotIndex(pots);
        if (openPotIndex == -2) throw new IllegalStateException("There are no open pots, this is an illegal state.");
        if (openPotIndex != -1) {
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
        return pots;
    }

    // -- test --
    public static int getToCall(ArrayList<Pot> pots, Player p) {
        int openPotIndex = getOpenPotIndex(pots);
        if (openPotIndex == -2) throw new IllegalStateException("There are no open pots, this is an illegal state.");
        if (openPotIndex != -1) {

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
        ArrayList<Pot> paidPots = new ArrayList<>();
        int totalPotToPlay = 0;
        for (Pot pot : pots) {
            totalPotToPlay += pot.getToPlay();
        }
        while (!pots.isEmpty()) {
            Pot pot = findHighestPriorityPot(pots);
            if (pots.size() == 1) {
                pot.addBet(p,bet);
                bet = 0;
        } else {
                pot.addBet(p,pot.getToPlay());
                bet -= pot.getToPlay();
            }
            paidPots.add(pot);
            pots.remove(pot);
        }
        return paidPots;
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

    private static ArrayList<Pot> payOpenPot(ArrayList<Pot> pots, Player p) {
        Integer activeBet = p.getActiveBet();
        int bet = activeBet != null ? activeBet : 0;
        int i = getOpenPotIndex(pots);
        if (i == -1) throw new IllegalStateException("There is no active Pot");
        pots.get(i).addBet(p,bet);
        return pots;
    }

    public static int getOpenPotIndex(ArrayList<Pot> pots) {
        if (pots.size() == 1) {
            if (pots.getFirst().getIsOpen()) return 0;
            else return -2;
        }
        ArrayList<Integer> potIndex = new ArrayList<>();
        for (int i = 0; i < pots.size(); i++) {
            if (pots.get(i).getIsOpen())
                potIndex.add(i);
        }
        if (potIndex.size() > 1) return -1;
        return potIndex.getFirst();
    }
}
