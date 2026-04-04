package ptit.ttcs.phone.exception;

public class ForbiddenException extends RuntimeException {
  public ForbiddenException(String accountIsBanned) {
    super(accountIsBanned);
  }
}
