package demo;

import org.springframework.hateoas.VndErrors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * this is an alternative to specifying an
 * error value on the {@link CustomerNotFoundException}
 * itself. Error handling is centralized in this class and
 * the Spring HATEOAS {@link VndErrors} representation of errors
 * can be used easily.
 */
@ControllerAdvice(annotations = RestController.class)
public class CustomerControllerAdvice {

    private final MediaType vndErrorMediaType = MediaType.parseMediaType(
            "application/vnd.error");

    @ExceptionHandler(CustomerNotFoundException.class)
    ResponseEntity<VndErrors> notFoundException(CustomerNotFoundException e) {
        return this.error(e, HttpStatus.NOT_FOUND, e.getCustomerId() + "");
    }

    @ExceptionHandler( IllegalArgumentException.class )
    ResponseEntity<VndErrors> assertionException( IllegalArgumentException ex) {
        return this.error(ex, HttpStatus.NOT_FOUND, ex.getLocalizedMessage());
    }

    private <E extends Exception> ResponseEntity<VndErrors> error(E e, HttpStatus httpStatus, String logref) {
        String msg = Optional.of(e.getMessage()).orElse(e.getClass().getSimpleName());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(this.vndErrorMediaType);
        return new ResponseEntity<>(new VndErrors(logref, msg), httpHeaders, httpStatus);
    }
}