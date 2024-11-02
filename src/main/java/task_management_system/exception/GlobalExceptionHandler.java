package task_management_system.exception;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import task_management_system.dto.CustomResponse;
import task_management_system.dto.ValidationException;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public CustomResponse notFoundHandler(NotFoundException ex) {
        return CustomResponse.builder()
                .status("failure")
                .message(ex.getMessage())
                .build();
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CustomResponse badRequestHandler(BadRequestException ex) {
        return CustomResponse.builder()
                .status("failure")
                .message(ex.getMessage())
                .build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CustomResponse handleIllegalArg(IllegalArgumentException ex) {
        return CustomResponse.builder()
                .status("failure")
                .message(ex.getMessage())
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationException InvalidArgumentHandler(MethodArgumentNotValidException ex) {
        List<ValidationException.ValidationField> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            ValidationException.ValidationField field = ValidationException.ValidationField
                    .builder()
                    .field(error.getField())
                    .message(error.getDefaultMessage())
                    .build();

            errors.add(field);
        }
        return new ValidationException(errors);
    }
}
