package kr.stockey.stockservice.api.request;


import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CreateFavoriteStockRequest {
    private Long memberId;
    private Long stockId;
}
