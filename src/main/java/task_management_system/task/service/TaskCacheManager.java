package task_management_system.task.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import task_management_system.exception.NotFoundException;
import task_management_system.task.entity.Task;
import task_management_system.task.repository.TaskRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskCacheManager {

    private final CacheManager cacheManager;
    private final TaskRepository taskRepository;

    private static final String TASK_CACHE_NAME = "tasks";

    /**
     *  Retrieves a task by ID from the cache, loads it from the database if not cached
     */

    public Task getTaskFromCache(UUID taskID) {
        Cache cache = cacheManager.getCache(TASK_CACHE_NAME);
        if (cache != null) {
            Task task = cache.get(taskID, Task.class);
            if (task != null) {
                return task;
            }
        }

        Task task = taskRepository.findById(taskID)
                .orElseThrow(() -> new NotFoundException("Task not found with id: " + taskID));

        addTaskToCache(task);
        return task;
    }

    /**
     * Adds a task to the cache
     */
    public void addTaskToCache(Task task) {
        Cache cache = cacheManager.getCache(TASK_CACHE_NAME);
        if (cache != null) {
            cache.put(task.getId(), task);
        }
    }

    /**
     * Removes a task from cache
     */
    public void evictTaskFromCache(UUID taskID) {
        Cache cache = cacheManager.getCache(TASK_CACHE_NAME);
        if (cache != null) {
            cache.evict(taskID);
        }
    }
}
