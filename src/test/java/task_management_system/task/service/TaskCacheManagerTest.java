package task_management_system.task.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import task_management_system.task.entity.Task;
import task_management_system.task.enums.TaskPriority;
import task_management_system.task.enums.TaskStatus;
import task_management_system.task.repository.TaskRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@ExtendWith(SpringExtension.class)
class TaskCacheManagerTest {

    @Autowired
    private TaskCacheManager taskCacheManager;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CacheManager cacheManager;

    private Task task;

    @BeforeEach
    void setup() {
        task = new Task();
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setDueDate(LocalDateTime.now().plusMonths(2));
        task.setStatus(TaskStatus.COMPLETED);
        task.setPriority(TaskPriority.HIGH);

        taskRepository.saveAndFlush(task);
    }

    @AfterEach
    void tearDown() {
        taskRepository.delete(task);
    }

    @Test
    void testGetTaskFromCache() {

        Optional<Task> getTask = taskRepository.findById(task.getId());

        assertTrue(getTask.isPresent());

        Task cachedTask = taskCacheManager.getTaskFromCache(task.getId());
        assertNotNull(cachedTask);
        assertEquals(task.getId(), cachedTask.getId());
        assertEquals(task.getTitle(), cachedTask.getTitle());
    }

    @Test
    void testEvictTaskFromCache() {
        taskCacheManager.evictTaskFromCache(task.getId());
        Cache cache = cacheManager.getCache("tasks");

        assert cache != null;
        assertNull(cache.get(task.getId(), Task.class));
    }

    @Test
    void testAddTaskToCache() {
        taskCacheManager.addTaskToCache(task);
        Cache cache = cacheManager.getCache("tasks");

        assertNotNull(cache);
        assertNotNull(cache.get(task.getId(), Task.class));

    }
}