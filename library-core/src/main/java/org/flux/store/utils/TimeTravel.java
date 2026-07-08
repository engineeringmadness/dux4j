package org.flux.store.utils;

import lombok.Getter;
import org.flux.store.api.exceptions.TimeTravelExceededException;
import org.flux.store.api.v1.Action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("rawtypes")
@Getter
public class TimeTravel<T> {
    public static final Integer snapshotNumberLimit = 100;
    public static final Integer snapshotThreshold = 10;
    private final List<Action> actions;
    private final List<T> checkpointStates = new ArrayList<>();
    private Integer index;
    // Number of oldest checkpoints evicted by pruning; offsets checkpointStates
    // indexing so getSnapshot still maps an action index to the right checkpoint.
    private int evictedCheckpoints = 0;

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
            // Prune oldest snapshots when the limit is breached to bound memory.
            if(checkpointStates.size() > snapshotNumberLimit) {
                checkpointStates.subList(0, snapshotThreshold).clear();
                evictedCheckpoints += snapshotThreshold;
            }
        }
    }

    public void goForward() {
        if(index < actions.size() - 1)
            index ++;
    }

    public void goBack() {
        if (index > 0) {
            int targetIndex = index - 1;
            int targetCheckpoint = targetIndex / snapshotThreshold;
            if (targetCheckpoint - evictedCheckpoints < 0) {
                throw new TimeTravelExceededException(
                    "Cannot go back: snapshot for index " + targetIndex + " has been evicted. " +
                    "Time travel history beyond the oldest retained checkpoint is unavailable.");
            }
            index = targetIndex;
        }
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
        int listIndex = checkpointNum - evictedCheckpoints;
        if (listIndex >= 0 && listIndex < checkpointStates.size()) {
            return checkpointStates.get(listIndex);
        }
        if (listIndex < 0) {
            throw new TimeTravelExceededException(
                "Snapshot for checkpoint " + checkpointNum + " has been evicted. " +
                "Time travel history prior to this point is unavailable.");
        }
        return getInitialState();
    }
}
