package remonone.nftilation.utils.tasks;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TaskCache {

    private final List<TaskContainer> containers;

    public TaskCache() {
        containers = new ArrayList<>();
    }

    public void add(TaskContainer tc) {
        containers.add(tc);
    }

    public void clear() {
        containers.clear();
    }

    public TaskContainer getTaskById(int id) {
        for (TaskContainer tc : containers) {
            if(tc.getRunnable().getTaskId() == id) return tc;
        }
        return null;
    }

    public void removeTask(TaskContainer tc) {
        if(tc == null) return;
        containers.remove(tc);
    }
}
