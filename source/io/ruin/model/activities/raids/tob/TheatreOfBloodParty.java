package io.ruin.model.activities.raids.tob;

import io.ruin.cache.Color;
import io.ruin.model.activities.raids.xeric.ChambersOfXeric;
import io.ruin.model.entity.player.Player;

import java.util.LinkedList;
import java.util.List;

public final class TheatreOfBloodParty {

    private static int NEXT_PARTY_ID = 0;

    private Player leader;
    private LinkedList<Player> members;
    private TheatreOfBlood theatre;
    private final int partyId;

    public TheatreOfBloodParty(Player leader) {
        this.leader = leader;
        partyId = NEXT_PARTY_ID++;
        members = new LinkedList<>();
        members.add(leader);
    }

    public Player getLeader() {
        return leader;
    }

    public void setLeader(Player leader) {
        this.leader = leader;
    }

    public List<Player> getMembers() {
        return members;
    }

    public int getSize() {
        return members.size();
    }

    public boolean addMember(Player player) {
        return members.add(player);
    }

    public void removeMember(Player player) {
        members.remove(player);
        if (members.size() > 0 && player == leader) {
            leader = members.getFirst();
            leader.sendMessage(Color.RAID_PURPLE.wrap("You are now the party leader."));
        }
    }

    public TheatreOfBlood getTheatre() {
        return theatre;
    }

    public void setTheatre(TheatreOfBlood theatre) {
        this.theatre = theatre;
    }


}
