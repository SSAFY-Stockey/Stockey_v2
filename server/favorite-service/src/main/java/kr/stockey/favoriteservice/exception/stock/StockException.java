package kr.stockey.favoriteservice.exception.stock;


import kr.stockey.newsservice.exception.BaseException;
import kr.stockey.newsservice.exception.BaseExceptionType;

public class StockException extends BaseException {
    private final BaseExceptionType exceptionType;

    public StockException(BaseExceptionType exceptionType) {
        this.exceptionType = exceptionType;
    }

    @Override
    public BaseExceptionType getExceptionType() {
        return exceptionType;
    }
}
