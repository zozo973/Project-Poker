package com.example.projectpoker.model.game;

import com.example.projectpoker.model.game.enums.Action;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Utility class for pot management during a poker round.
 * Handles side-pot creation, bet routing, and pot selection logic.
 */
public final class PotUtil {

    /**
     * Creates a new side pot for the given player at the appropriate priority level,
     * redistributes existing pot chips, and adds the side pot to the list.
     *
     * @param pots       the current list of all {@link Pot} objects in play
     * @param p          the {@link Player} who triggered the side-pot creation (typically by going all-in)
     * @param newPotSize the chip amount that seeds the new side pot
     * @return the updated list of pots including the newly created side pot
     */
    public static ArrayList<Pot> addNewSidePot(ArrayList<Pot> pots, Player p, int newPotSize)   {
        int potPriority = -1;
        boolean adjustPot = true;
        Pot bestPot = findBestAvailablePot(pots,p);

        if ((bestPot.getInvestmentPP()+newPotSize)==p.getActiveBet()) {
            potPriority = bestPot.getPotPriority() + 1;
        } else {
            potPriority = bestPot.getPotPriority();
            bestPot.stepPotPriority(1);
        }

        if (bestPot.equals(findHighestPriorityPot(pots)) && p.getAction().equals(Action.RAISE) && bestPot.getInvestmentPP() <=
                (p.getTotalPotInvestment(bestPot)+p.getActiveBet())) {
            adjustPot = false;
        }

        Pot newSidePot = new Pot(p,potPriority);

        if (adjustPot) {
            newSidePot.addBet(p,newPotSize);
            newSidePot.setIsOpen(true);
            return adjustMultiplePots(pots, newSidePot);
        }
        else {
            bestPot.addBet(p,p.getActiveBet()-newPotSize);
            newSidePot.addBet(p,newPotSize);

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

    /**
     * Sets the amount-to-play on the correct open pot(s) so every player knows
     * how much they must contribute in the current betting round.
     *
     * @param pots   the current list of {@link Pot} objects
     * @param toPlay the total amount a player with no prior investment must pay
     */
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

    /**
     * Computes the exact chip amount a player must put in to remain in the hand.
     * Accounts for multiple open pots and the player's existing contributions.
     *
     * @param pots the current list of {@link Pot} objects
     * @param p    the {@link Player} whose call amount is calculated
     * @return the positive integer number of chips the player still needs to contribute,
     *         or {@code 0} if they are already matched or all amounts are settled
     */
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

    /**
     * Routes a player's active bet into the appropriate pot(s).
     * Tries to distribute across multiple open pots first; falls back to the single open pot.
     *
     * @param pots the current list of {@link Pot} objects
     * @param p    the {@link Player} whose {@code activeBet} amount should be placed
     * @return the updated list of pots after the bet has been recorded
     */
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

    /**
     * Returns the pot with the lowest (highest-priority) {@code potPriority} value that is still open.
     * This is usually the main pot or the most recently created side pot that controls betting.
     *
     * @param pots the current list of {@link Pot} objects; must not be empty
     * @return the highest-priority open {@link Pot}
     * @throws IllegalStateException if {@code pots} is empty
     */
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

    /**
     * Returns the most suitable open pot for a specific player to bet into,
     * preferring pots the player is already registered in.
     *
     * @param pots   the current list of {@link Pot} objects; must not be empty
     * @param player the {@link Player} looking for a pot to bet into
     * @return the best available {@link Pot} for this player
     * @throws IllegalStateException if {@code pots} is empty
     */
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

    /**
     * Returns the index of the single open pot in the list, or {@code null} if there are multiple open pots.
     *
     * @param pots the current list of {@link Pot} objects
     * @return the zero-based index of the sole open pot, or {@code null} if more than one pot is open
     * @throws IllegalStateException if all pots are closed or if there is only one pot and it is closed
     */
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
