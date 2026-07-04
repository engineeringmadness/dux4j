package org.flux.store.utils;

import lombok.Getter;
import org.flux.store.api.v1.Action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("rawtypes")
@Getter
public class TimeTravel<T> {
    private final List<Action> actions;
    private final List<T> checkpointStates = new ArrayList<>();
    private static final Integer snapshotThreshold = 10;
    private Integer index;

    public TimeTravel() {
        this.actions = Collections.synchronizedList(new ArrayList<>());
        this.index = -1;
    }

    public void recordChange(Action action, T newState) {
        actions.add(action);
        index ++;
        // Capture state at each checkpoint so goBack can resume from the nearest
        // checkpoint instead of replaying the entire history from initial state.
        if(index % snapshotThreshold == 0) {
            checkpointStates.add(newState);
        }
    }

    public void goForward() {
        if(index < actions.size() - 1)
            index ++;
    }

    public void goBack() {
        if(index > 0)
            index --;
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

    public List<Action> getActionToRecreate() {
        int indexOfSnapshot = getPreviousCheckpoint();
        // Snapshot already holds state with the checkpoint action applied,
        // so replay only the actions after it.
        return this.actions.subList(indexOfSnapshot + 1, index + 1);
    }

    public Action getLatestAction() {
        return index > 0 ? this.actions.get(index) : null;
    }

    public T getInitialState() {
        return (T) this.actions.get(0).getPayload();
    }

    public T getSnapshot() {
        int checkpointNum = getPreviousCheckpoint() / snapshotThreshold;
        if (checkpointNum >= 0 && checkpointNum < checkpointStates.size()) {
            return checkpointStates.get(checkpointNum);
        }
        return getInitialState();
    }
}
