package kr.stockey.investmentservice.mapper;

import kr.stockey.investmentservice.dto.ContractDto;
import kr.stockey.investmentservice.dto.OrderHistoryDto;
import kr.stockey.investmentservice.entity.Contract;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel= "spring")
public interface InvestmentMapper {
//    List<ContractDto> toContractDtoList(List<Contract> orderHistory);

    OrderHistoryDto toOrderHistoryDto(Contract orderHistory);
    List<OrderHistoryDto> toOrderHistoryDtoList(List<Contract> orderHistory);
}