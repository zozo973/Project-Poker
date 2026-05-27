package com.example.projectpoker.model.game;

import com.example.projectpoker.model.game.enums.Roles;

import java.util.ArrayList;

public final class RoleUtil {

    /**
     * Assigns DEALER, SMALLBLIND, and BIGBLIND roles to the players at the given indices.
     * All other players are reset to the base PLAYER role.
     * For a two-player game the roles are always SMALLBLIND and BIGBLIND regardless of the indices.
     *
     * @param players     the full list of {@link Player} objects in the game
     * @param roleIndices a three-element array: index 0 = dealer seat, index 1 = small blind seat,
     *                    index 2 = big blind seat
     * @return the same player list with updated roles
     */
    public static ArrayList<Player> delegateRoles(ArrayList<Player> players, int[] roleIndices) {
        for (Player p : players) {
            p.setRole(Roles.PLAYER);
        }

        if (players.size() == 2) {
            players.getFirst().setRole(Roles.SMALLBLIND);
            players.getLast().setRole(Roles.BIGBLIND);
        } else {
            players.get(roleIndices[0]).setRole(Roles.DEALER);
            players.get(roleIndices[1]).setRole(Roles.SMALLBLIND);
            players.get(roleIndices[2]).setRole(Roles.BIGBLIND);
        }

        return players;
    }

    /**
     * Scans the player list and returns the seat indices currently holding DEALER,
     * SMALLBLIND, and BIGBLIND roles.
     *
     * @param players the full list of {@link Player} objects
     * @return a three-element array: [dealerIndex, smallBlindIndex, bigBlindIndex]
     */
    public static int[] findRoleIndices(ArrayList<Player> players) {
        int[] roleIndices = {0, 0, 0};
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getRole() == Roles.DEALER) {
                roleIndices[0] = i;
            } else if (players.get(i).getRole() == Roles.SMALLBLIND) {
                roleIndices[1] = i;
            } else if (players.get(i).getRole() == Roles.BIGBLIND) {
                roleIndices[2] = i;
            }
        }
        return roleIndices;
    }

    /**
     * Advances the role indices by one seat so the next round starts with the
     * correct rotation of dealer and blinds.
     * Wraps around when indices reach the end of the player list.
     *
     * @param players the full list of {@link Player} objects (used to determine wrapping boundaries)
     * @return a new three-element array with the rotated indices for the next round
     */
    public static int[] stepRoleIndices(ArrayList<Player> players) {
        int[] roleIndices = findRoleIndices(players);
        if (roleIndices[0] == players.size() - 3) {
            roleIndices[0] += 1;
            roleIndices[1] += 1;
            roleIndices[2] = 0;
        } else if (roleIndices[0] == players.size() - 2) {
            roleIndices[0] += 1;
            roleIndices[1] = 0;
            roleIndices[2] = 1;
        } else if (roleIndices[0] == players.size() - 1) {
            roleIndices = new int[]{0, 1, 2};
        } else {
            roleIndices[0] += 1;
            roleIndices[1] += 1;
            roleIndices[2] += 1;
        }
        return roleIndices;
    }
}
