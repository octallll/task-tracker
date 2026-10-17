package task.tracker.web;

import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import task.tracker.service.NotFoundException;

@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail notFound(NotFoundException e, HttpServletRequest request) {
        return problem(Level.WARN, request, HttpStatus.NOT_FOUND, e.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail invalid(MethodArgumentNotValidException e, HttpServletRequest request) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return problem(Level.WARN, request, HttpStatus.BAD_REQUEST, detail, null);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ProblemDetail unreadable(Exception e, HttpServletRequest request) {
        return problem(Level.WARN, request, HttpStatus.BAD_REQUEST, "Incorrect request", null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail conflict(DataIntegrityViolationException e, HttpServletRequest request) {
        log.atWarn()
                .addKeyValue("error.message", e.getMostSpecificCause().getMessage())
                .log("Data integrity violation");
        return problem(Level.WARN, request, HttpStatus.CONFLICT, "Data conflict", null);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail unexpected(Exception e, HttpServletRequest request) {
        if (e instanceof ErrorResponse error) {
            return problem(Level.WARN, request, error.getStatusCode(), error.getBody().getDetail(), null);
        }
        return problem(Level.ERROR, request, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", e);
    }

    private static ProblemDetail problem(
            Level level, HttpServletRequest request, HttpStatusCode status, String detail, Throwable cause) {
        log.atLevel(level)
                .addKeyValue("http.request.method", request.getMethod())
                .addKeyValue("url.path", request.getRequestURI())
                .addKeyValue("http.response.status_code", status.value())
                .setCause(cause)
                .log("{} {} -> {}: {}", request.getMethod(), request.getRequestURI(), status.value(), detail);
        return ProblemDetail.forStatusAndDetail(status, detail);
    }
}
