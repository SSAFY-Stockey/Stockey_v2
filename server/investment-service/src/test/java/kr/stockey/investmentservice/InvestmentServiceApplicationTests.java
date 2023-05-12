package kr.stockey.investmentservice;

import kr.stockey.investmentservice.dto.OrderListDto;
import kr.stockey.investmentservice.dto.OrderProducerDto;
import kr.stockey.investmentservice.enums.ContractType;
import kr.stockey.investmentservice.mapper.InvestmentDtoMapper;
import kr.stockey.investmentservice.redis.Order;
import kr.stockey.investmentservice.redis.OrderRedisRepository;
import kr.stockey.investmentservice.service.InvestmentService;

import kr.stockey.investmentservice.service.InvestmentServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@SpringBootTest
@ExtendWith(MockitoExtension.class)
class InvestmentServiceApplicationTests {

    @Autowired
    InvestmentService investmentService;

    @Autowired
    InvestmentServiceImpl investmentServiceImpl;

    @Autowired
    OrderRedisRepository orderRedisRepository;

    @Autowired
    InvestmentDtoMapper investmentDtoMapper;

    @Test
    void 주문적재_테스트() throws Exception {
//        OrderProducerDto orderProducerDto
//                = new OrderProducerDto(100L, 1L, 10,
//                ContractType.BUY, InvCategory.ORDER, LocalDateTime.now());
//
//        orderRedisRepository.deleteById(String.valueOf(orderProducerDto.getMemberId()));
//
//        investmentService.takeStockOrder(orderProducerDto);
//        Thread.sleep(1000); // 1초 대기
//
//        Order order = orderRedisRepository.findById(String.valueOf(orderProducerDto.getMemberId())).get();
//        System.out.println("order = " + order);
//
//        assertThat(order.getMemberId()).isEqualTo(orderProducerDto.getMemberId());
//
////        assertThrows(Exception.class, () -> {
////            // 동일한 주문이 들어오면 에러발생
////            investmentService.takeStockOrder(orderProducerDto);
////
////        });
//
//        orderRedisRepository.deleteById(String.valueOf(orderProducerDto.getMemberId()));
    }

    @Test
    void 주문체결_테스트() throws Exception {
        // 레디스에 데이터 적재
        // 1. 첫 모의투자 상황 (잔액 history x, 보유주식 x)
        Long memberId = -99L;
        List<OrderListDto> orderListDtoList = getOrderList();
        OrderProducerDto orderProducerDto = new OrderProducerDto(memberId, orderListDtoList, LocalDateTime.now());
        Order redisOrderDto = investmentDtoMapper.toRedisOrderDto(orderProducerDto);
        Order savedOrder = orderRedisRepository.save(redisOrderDto);
        System.out.println("savedOrder = " + savedOrder);

        investmentServiceImpl.orderExecute();

        // 적재했던 데이터 삭제
        orderRedisRepository.deleteById(String.valueOf(savedOrder.getMemberId()));
    }

    private List<OrderListDto> getOrderList() {
        List<OrderListDto> orderListDtoList = new ArrayList<>();

        OrderListDto orderListDto1 = new OrderListDto();
        orderListDto1.setStockId(1L);
        orderListDto1.setCount(10);
        orderListDto1.setOrderType(ContractType.BUY);
        orderListDtoList.add(orderListDto1);

        OrderListDto orderListDto2 = new OrderListDto();
        orderListDto2.setStockId(2L);
        orderListDto2.setCount(5);
        orderListDto2.setOrderType(ContractType.SELL);
        orderListDtoList.add(orderListDto2);

        OrderListDto orderListDto3 = new OrderListDto();
        orderListDto3.setStockId(3L);
        orderListDto3.setCount(8);
        orderListDto3.setOrderType(ContractType.BUY);
        orderListDtoList.add(orderListDto3);

        OrderListDto orderListDto4 = new OrderListDto();
        orderListDto4.setStockId(4L);
        orderListDto4.setCount(3);
        orderListDto4.setOrderType(ContractType.SELL);
        orderListDtoList.add(orderListDto4);

        OrderListDto orderListDto5 = new OrderListDto();
        orderListDto5.setStockId(5L);
        orderListDto5.setCount(15);
        orderListDto5.setOrderType(ContractType.BUY);
        orderListDtoList.add(orderListDto5);

        return orderListDtoList;
    }

    @Test
    public void testFilterOrders() {
        // create some test Order objects
        Order order1 = new Order(1L, null, LocalDateTime.parse("2022-05-11T09:00:01"));
        Order order2 = new Order(2L, null, LocalDateTime.parse("2022-05-11T09:30:00"));
        Order order3 = new Order(3L, null, LocalDateTime.parse("2022-05-11T10:00:00"));
        Order order4 = new Order(4L, null, LocalDateTime.parse("2022-05-11T10:05:00"));

        // create a list of the test Order objects
        List<Order> orderList = new ArrayList<>();
        orderList.add(order1);
        orderList.add(order2);
        orderList.add(order3);
        orderList.add(order4);

        // call the filterOrders() method with the test Order list
        List<Order> filteredOrderList = filterOrders(orderList, LocalDateTime.parse("2022-05-11T10:02:00"));

        // check that the filtered list contains only orders within the desired time range
        Assertions.assertEquals(2, filteredOrderList.size());
        Assertions.assertTrue(filteredOrderList.contains(order1));
        Assertions.assertTrue(filteredOrderList.contains(order2));
    }

    private List<Order> filterOrders(List<Order> rawOrderList, LocalDateTime currentDateTime) {
        LocalDateTime oneHourAgo = currentDateTime.minusHours(1);
        LocalDateTime desiredStartTime = LocalDateTime.of(oneHourAgo.getYear(), oneHourAgo.getMonth(), oneHourAgo.getDayOfMonth(), oneHourAgo.getHour(), 0, 0);
        LocalDateTime desiredEndTime = LocalDateTime.of(oneHourAgo.getYear(), oneHourAgo.getMonth(), oneHourAgo.getDayOfMonth(), currentDateTime.getHour(), 0, 0);

        List<Order> filteredOrderList = new ArrayList<>();
        for (Order order : rawOrderList) {
            LocalDateTime orderTime = order.getOrderTime();
            if (orderTime.isAfter(desiredStartTime) && orderTime.isBefore(desiredEndTime)) {
                filteredOrderList.add(order);
            }
        }

        return filteredOrderList;
    }

}