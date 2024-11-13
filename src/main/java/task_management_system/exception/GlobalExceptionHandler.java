package task_management_system.exception;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import task_management_system.dto.CustomResponse;
import task_management_system.dto.ValidationException;

import java.nio.file.AccessDeniedException;
import java.security.SignatureException;
import java.util.HashMap;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public CustomResponse notFoundHandler(NotFoundException ex) {
        return setResponse(HttpStatus.NOT_FOUND.toString(), ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CustomResponse badRequestHandler(BadRequestException ex) {
        return setResponse(HttpStatus.BAD_REQUEST.toString(), ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CustomResponse handleIllegalArgument(IllegalArgumentException ex) {
        return setResponse(HttpStatus.BAD_REQUEST.toString(), ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public CustomResponse handleUnauthorized(UnauthorizedException ex) {
        return setResponse(HttpStatus.UNAUTHORIZED.toString(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationException InvalidArgumentHandler(MethodArgumentNotValidException ex) {
        HashMap<String, String> errors = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        return new ValidationException(errors);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public CustomResponse handleNoHandlerFound(NoHandlerFoundException ex) {
        return setResponse(HttpStatus.NOT_FOUND.toString(), "Resource not found for route");
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public CustomResponse handleConflict(ConflictException ex) {
        return setResponse(HttpStatus.CONFLICT.toString(), ex.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public CustomResponse handleUnauthenticated(AuthenticationException ex) {
        return setResponse(HttpStatus.UNAUTHORIZED.toString(), ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public CustomResponse handleBadCredentials(BadCredentialsException ex) {
        return setResponse(HttpStatus.UNAUTHORIZED.toString(),
                "Invalid authentication credentials");
    }

    @ExceptionHandler(AccountStatusException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public CustomResponse handleAccountStatus(AccountStatusException ex) {
        return setResponse(HttpStatus.FORBIDDEN.toString(), "This account is locked");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public CustomResponse handleAccessDenied(AccessDeniedException ex) {
        return setResponse(HttpStatus.FORBIDDEN.toString(),
                "You are not authorized to access this resource");
    }

    @ExceptionHandler(SignatureException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public CustomResponse handleSignatureException(AccessDeniedException ex) {
        return setResponse(HttpStatus.FORBIDDEN.toString(), "Invalid JWT token provided");
    }

    @ExceptionHandler(ExpiredJwtException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public CustomResponse handleExpiredJwt(ExpiredJwtException ex) {
        return setResponse(HttpStatus.FORBIDDEN.toString(), "JWT token has expired");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CustomResponse handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        return setResponse(HttpStatus.BAD_REQUEST.toString(), ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CustomResponse handleHttpNotReadable(HttpMessageNotReadableException ex) {
        return setResponse(HttpStatus.BAD_REQUEST.toString(), ex.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CustomResponse handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return setResponse(HttpStatus.BAD_REQUEST.toString(), ex.getMessage());
    }

    @ExceptionHandler(TooManyRequest.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public CustomResponse handleTooManyRequest(TooManyRequest ex) {
        return setResponse(HttpStatus.TOO_MANY_REQUESTS.toString(),
                ex.getMessage());
    }

//    @ExceptionHandler(Exception.class)
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    public CustomResponse handleSecurityException(Exception ex) {
//        log.info(ex.getMessage());
//        return setResponse(HttpStatus.INTERNAL_SERVER_ERROR.toString(),
//                "Unknown internal server error");
//    }

    private CustomResponse setResponse(String status, String message) {
        return CustomResponse.builder()
                .status(status)
                .message(message)
                .build();
    }
}
