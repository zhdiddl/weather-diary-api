package zerobase.weather.error;

public class InvalidDateException extends RuntimeException {
    private static final String MESSAGE = "너무 과거 혹은 미래의 날짜입니다.";

    public InvalidDateException() {
        super(MESSAGE); // 부모 클래스의 생성자를 호출해서 MESSAGE를 전달
    }
}
