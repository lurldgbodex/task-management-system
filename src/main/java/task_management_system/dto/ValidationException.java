package task_management_system.dto;

import java.util.HashMap;

public record ValidationException(HashMap<String, String> errors) {}
