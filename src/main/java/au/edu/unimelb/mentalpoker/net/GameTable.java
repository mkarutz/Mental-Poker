package au.edu.unimelb.mentalpoker.net;

import com.google.common.collect.Iterables;

import java.util.HashMap;
import java.util.Set;

public class GameTable {
    private HashMap<Address, Boolean> table;

    public GameTable() {
        this.table = new HashMap<>();
    }

    public GameTable(HashMap<Address, Boolean> table) {
        this.table = table;
    }

    //default ready state is False
    public void addPlayerConnection(Address endPoint) {
        this.table.put(endPoint, Boolean.FALSE);
    }

    public void changeReadyState(Address endPoint, Boolean readyState) {
        this.table.replace(endPoint, readyState);
    }

    public void playerReady(Address endPoint) {
        this.table.replace(endPoint, Boolean.TRUE);
    }

    public void resetReadyState() {
        this.table.replaceAll((k, v) -> Boolean.FALSE);
    }

    public boolean allPlayersReady() {
        return Iterables.all(table.values(), (Boolean value) -> value);
    }

    //gets the list of Player IPs
    public Set<Address> getPlayers() {
        return (this.table.keySet());
    }

    public HashMap<Address, Boolean> getGameTable() {
        return (this.table);
    }

    public void RemovePlayer(Address endPoint) {
        this.table.remove(endPoint);
    }
}
