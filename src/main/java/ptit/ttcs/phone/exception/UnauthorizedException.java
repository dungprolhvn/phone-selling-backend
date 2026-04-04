package ptit.ttcs.phone.exception;

public class UnauthorizedException extends RuntimeException {
  public UnauthorizedException(String invalidCredentials) {
    super(invalidCredentials);
  }
}
