package com.example.projectpoker.model.game;

import java.util.ArrayList;

public final class RoleUtil {

    public static ArrayList<Player> delegateRoles(ArrayList<Player> players, int[] roleIndices) {
        for (Player p : players) {
            p.setRole(Roles.PLAYER);
        }
        players.get(roleIndices[0]).setRole(Roles.DEALER);
        players.get(roleIndices[1]).setRole(Roles.SMALLBLIND);
        players.get(roleIndices[2]).setRole(Roles.BIGBLIND);
        return players;
    }

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
