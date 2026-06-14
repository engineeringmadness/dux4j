package org.flux.store.utils;

import lombok.Getter;
import org.flux.store.api.v1.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("rawtypes")
@Getter
public class TimeTravel<T> {
    private static final Logger log = LoggerFactory.getLogger(TimeTravel.class);
    private final List<Action> actions;
    private T snapshot;
    private static final Integer snapshotThreshold = 10;
    private Integer index;

    public TimeTravel() {
        this.actions = Collections.synchronizedList(new ArrayList<>());
        this.index = -1;
    }

    public TimeTravel(T snapshot, List<Action> actions, Integer index) {
        this.actions = Collections.synchronizedList(new ArrayList<>(actions));
        this.index = index;
        this.snapshot = snapshot;
    }

    public void recordChange(Action action, T newState) {
        actions.add(action);
        index ++;
        // If snapshot threshold is reached then record new state in snapshot container
        if(index % snapshotThreshold == 0) {
            this.setSnapshot(newState);
        }
    }

    public void goForward() {
        log.info("Going forward...");
        if(index < actions.size() - 1)
            index ++;
    }

    public boolean goBack() {
        log.info("Going back...");
        boolean snapshotInvalidated = false;
        if(index > 0)
            index --;
        // If index has fallen below checkpoint then new snapshot from previous checkpoint has to be created
        if(index % snapshotThreshold == (snapshotThreshold - 1)) {
            log.info("Snapshot is invalid");
            snapshotInvalidated = true;
        }
        return snapshotInvalidated;
    }

    private int getPreviousCheckpoint() {
        // Let's say threshold is 10 and index is 45 then index from snapshot = 45 / 10 = 4 * 10 = 40
        return (index / snapshotThreshold) * snapshotThreshold;
    }

    public List<String> getFullActionHistory() {
        return this.actions.stream()
                .map(x -> x.getType())
                .collect(Collectors.toList())
                .subList(0,index+1);
    }

    public List<Action> getActionHistory() {
        return this.actions.subList(0, getPreviousCheckpoint());
    }

    public List<Action> getActionToRecreate() {
        int indexOfSnapshot = (index / snapshotThreshold) * snapshotThreshold;
        return this.actions.subList(indexOfSnapshot,index+1);
    }

    public Action getLatestAction() {
        return index > 0 ? this.actions.get(index) : null;
    }

    public T getInitialState() {
        return (T) this.actions.get(0).getPayload();
    }

    public T getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(T snapshot) {
        log.info("Snapshot updated");
        this.snapshot = snapshot;
    }
}
