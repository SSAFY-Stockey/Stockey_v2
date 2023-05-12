package kr.stockey.investmentservice.service;

import kr.stockey.investmentservice.dto.AccountDto;
import kr.stockey.investmentservice.dto.ContractDto;
import kr.stockey.investmentservice.dto.OrderListDto;
import kr.stockey.investmentservice.dto.OrderProducerDto;
import kr.stockey.investmentservice.entity.Contract;
import kr.stockey.investmentservice.entity.DailyStock;
import kr.stockey.investmentservice.entity.Deposit;
import kr.stockey.investmentservice.entity.MyStock;
import kr.stockey.investmentservice.enums.ContractType;
import kr.stockey.investmentservice.enums.InvCategory;
import kr.stockey.investmentservice.kafka.producer.StockOrderProducer;
import kr.stockey.investmentservice.mapper.InvestmentMapper;
import kr.stockey.investmentservice.redis.Order;
import kr.stockey.investmentservice.redis.OrderRedisRepository;
import kr.stockey.investmentservice.repository.ContractRepository;
import kr.stockey.investmentservice.repository.DailyStockRepository;
import kr.stockey.investmentservice.repository.DepositRepository;
import kr.stockey.investmentservice.repository.MyStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class InvestmentServiceImpl implements InvestmentService{

    private final long DEFAULT_CREDIT = 10000000;
    private Map<Long, Long> stockPriceMap; // 데이터 캐싱

    private final StockOrderProducer stockOrderProducer;
    private final OrderRedisRepository orderRedisRepository;
    private final DepositRepository depositRepository;
    private final MyStockRepository myStockRepository;
    private final ContractRepository contractRepository;
    private final DailyStockRepository dailyStockRepository;
    private final InvestmentMapper investmentMapper;


    /**
     * 주문을 받아서 카프카 topic에 적재
     * 주문 가능시간: 9시 ~ 15시 && 매 05분 ~ 다음 시간 00분까지
     */
    @Override
    public void takeStockOrder(OrderProducerDto orderProducerDto) throws Exception {
        // 주문 가능 시간일 때만 진행
        if (checkOrderAvailableTime()) {
            stockOrderProducer.send(orderProducerDto);
        }
    }

    private boolean checkOrderAvailableTime() throws Exception {
        LocalDateTime now = LocalDateTime.now(); // 한국 시간 기준으로 현재 시간을 가져옴

        int hour = now.getHour();

        // 주문 가능시간: 9시 ~ 15시
        if (!(hour >= 9 && hour < 15)) {
            throw new Exception("주문 가능한 시간이 아닙니다.");
        }
        return true;
    }

    /**
     * 매 02분마다 체결 진행
     */

    @Scheduled(cron = "0 2 * * * *", zone = "Asia/Seoul")
    public void orderExecuteScheduler() throws Exception {
        // 실행할 메소드 내용 작성
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        if (now.getHour() >= 9 && now.getHour() <= 15 && now.getMinute() == 2) {
            // 오전 9시 ~ 오후 3시 사이, 2분일 때 실행
            // 실행할 코드 작성
            orderExecute();
        }
    }

    @Override
    public Boolean checkOrderSubmit(Long memberId) {
        Optional<Order> orderOptional = orderRedisRepository.findById(String.valueOf(memberId));
        if (orderOptional.isEmpty()) {
            return false; // 레디스에 없다면 주문 제출 안된것
        }
        return true; // 레디스에 있다면 주문 제출 된것 (체결은 안된것)
    }

    @Override
    public List<ContractDto> getOrderHistory(Long memberId) {
        List<Contract> orderHistory = contractRepository.findByMemberId(memberId).stream()
                .filter(contract -> contract.getCategory() == InvCategory.ORDER)
                .toList();
        return investmentMapper.toContractDtoList(orderHistory);
    }

    @Override
    public AccountDto getMyAccount(Long memberId) {
        // 총 자산 = 주식 평가금액 + 예수금
        Long curDeposit = 0L; // 예수금
        Long curStockValuation = 0L; // 주식 평가금액
        Long totalAsset = 0L; // 총 자산

        // 예수금 -> history에서 최신 가져오기
        Optional<Deposit> depositOptional = depositRepository.findLatestDepositByMemberId(memberId);
        if (depositOptional.isEmpty()) {
            // 첫거래면 보유 주식이 없음. 돈은 기본 크레딧
            curDeposit = DEFAULT_CREDIT;
            return new AccountDto(curDeposit, curStockValuation, curDeposit);
        } else {
            Deposit deposit = depositOptional.get();
            curDeposit = deposit.getMoney();
        }

        // 주식 평가금액 -> myStock 데이터 가져와서 특정 stockId에 해당하는 주식 현재가 가져와서 count랑 곱하기
        List<MyStock> myStocks = myStockRepository.findByMemberId(memberId);
        for (MyStock myStock : myStocks) {
            Long curStockId = myStock.getStockId();
            Long curStockPrice = stockPriceMap.get(curStockId);
            curStockValuation += curStockPrice * myStock.getCount();
        }

        // 총 자산 = 주식 평가금액 + 예수금
        totalAsset = curDeposit + curStockValuation;
        return new AccountDto(totalAsset, curStockValuation, curDeposit);
    }


    @Transactional
    public void orderExecute() throws Exception {
        // 주식 현재가 테이블 가져오기 (매 2분마다 갱신된 최신 주가정보 가져오기)
//        Map<Long, Long> stockPriceMap = getStockPrice();

        // 레디스에 쌓인 모든 order 데이터 가져와 list로 만들기
        Iterable<Order> orderIterable = orderRedisRepository.findAll();
        List<Order> rawOrderList = new ArrayList<>();
        for (Order order : orderIterable) {
            rawOrderList.add(order);
        }
        // 시간순 정렬
        Collections.sort(rawOrderList);

        // 이전 라운드에 대한 주문만 남기기
        List<Order> wholeOrderList = filterOrders(rawOrderList);

        // wholeOrderList 내용 DB에 적재
        loadOrderIntoDB(wholeOrderList);

        // 앞에서 부터 주문 체결 진행 (각 멤버 id는 고유)
        for (Order memberOrder : wholeOrderList) {
            // member id로 가장 최신 deposit history 체크 -> 없다면 최초거래이므로 기본크레딧에서 시작
            Long curMemberId = memberOrder.getMemberId();
            Optional<Deposit> depositOptional = depositRepository.findLatestDepositByMemberId(curMemberId);
            List<OrderListDto> stockOrders = memberOrder.getOrders();
            
            Long curMoney;
            if (depositOptional.isEmpty()) {
                // 잔액 히스토리가 없다면 디폴트 크레딧으로 시작 & 첫 거래이므로 보유 주식도 없음
                curMoney = DEFAULT_CREDIT;
            } else {
                // 최신 잔액부터 시작
                Deposit deposit = depositOptional.get();
                curMoney = deposit.getMoney();
            }

            // 현재 멤버의 보유주식 전체 get
            List<MyStock> myStocks = myStockRepository.findByMemberId(curMemberId);

            // 현재 멤버의 주문 목록 탐색
            for (OrderListDto stockOrder : stockOrders) {
                Long curStockId = stockOrder.getStockId(); // 현재 주문 주식 id
                Long curStockPrice = stockPriceMap.get(curStockId); // 주식 현재가
                int orderStockCount = stockOrder.getCount(); // 주문한 주식 수

                // 현재 주문에 대해 기존 보유종목 가져오기
                Optional<MyStock> myCurStockOptional = myStocks.stream()
                        .filter(myStock -> myStock.getStockId().equals(curStockId))
                        .findFirst();

                switch (stockOrder.getOrderType()) {
                    case BUY -> {
                        // 소지금보다 결제할 수량이 더 크다면 돈 되는 만큼만 구매
                        // [0]: 지불금액 [1]: 구매수량
                        Long[] buyInfo = calcBuyPayInfo(curMoney, curStockPrice, orderStockCount);
                        Long actualPayMoney = buyInfo[0];
                        Long actualBuyNum = buyInfo[1];

                        // 금액 지불
                        curMoney -= actualPayMoney;

                        if (myCurStockOptional.isPresent()) {
                            // 기존 보유 종목이 있다면 구매 시 평단 재계산, 보유주식 증가 필요
                            MyStock myStock = myCurStockOptional.get();
                            // 평단 재계산
                            double newMyAvgPrice = calculateNewAvgUnitPrice(myStock.getAvgPrice(), myStock.getCount(),
                                    curStockPrice, actualBuyNum);
                            // 보유주식 증가
                            myStock.setAvgPrice(newMyAvgPrice);
                            myStock.setCount(myStock.getCount() + actualBuyNum);

                        } else {
                            // 기존 보유종목이 없다면 보유 주식에 구매한 데이터 save
                            MyStock myStock = MyStock.builder()
                                    .memberId(curMemberId)
                                    .stockId(curStockId)
                                    .avgPrice(Double.valueOf(curStockPrice))
                                    .count(actualBuyNum).build();
                            myStockRepository.save(myStock);
                        }

                        // 체결 엔티티 생성 save
                        Contract contract = Contract.builder()
                                .memberId(curMemberId)
                                .stockId(curStockId)
                                .count(actualBuyNum)
                                .contractPrice(curStockPrice)
                                .contractType(ContractType.BUY)
                                .createdAt(LocalDateTime.now())
                                .category(InvCategory.CONTRACT)
                                .build();
                        contractRepository.save(contract);
                    }
                    case SELL -> {
                        myCurStockOptional.orElseThrow(() -> new Exception("보유 종목이 없으면 판매 하지 못함"));
                        MyStock myStock = myCurStockOptional.get();
                        Long myStockCount = myStock.getCount();
                        Long actualSellQuantity = Math.min(myStockCount, orderStockCount);

                        // 보유주식 감소, 만약 모든 보유 주식을 팔았다면 mystock 엔티티 삭제 진행
                        if (myStockCount.equals(actualSellQuantity)) {
                            myStockRepository.deleteById(myStock.getId());
                        } else {
                            myStock.setCount(myStockCount - actualSellQuantity);
                        }

                        // 예수금 증가
                        curMoney += actualSellQuantity * curStockPrice;

                        // 체결 엔티티 생성 save
                        Contract contract = Contract.builder()
                                .memberId(curMemberId)
                                .stockId(curStockId)
                                .count(actualSellQuantity)
                                .contractPrice(curStockPrice)
                                .contractType(ContractType.SELL)
                                .createdAt(LocalDateTime.now())
                                .category(InvCategory.CONTRACT)
                                .build();
                        contractRepository.save(contract);
                    }
                }
            }

            orderRedisRepository.delete(memberOrder); // 주문에 사용된 데이터는 레디스에서 삭제

            // 정산 완료된 금액을 deposit history에 넣기
            Deposit resultDeposit = Deposit.builder()
                    .memberId(curMemberId)
                    .createdAt(LocalDateTime.now())
                    .money(curMoney).build();
            depositRepository.save(resultDeposit);
        }
    }

    private void loadOrderIntoDB(List<Order> wholeOrderList) {
        for (Order order : wholeOrderList) {
            for (OrderListDto orderListDto : order.getOrders()) {
                // DB에 넣을 엔티티 생성
                Contract contract = Contract.builder()
                        .memberId(order.getMemberId())
                        .stockId(orderListDto.getStockId())
                        .count((long)orderListDto.getCount())
                        .contractType(orderListDto.getOrderType())
                        .createdAt(order.getOrderTime())
                        .category(InvCategory.ORDER)
                        .build();
                contractRepository.save(contract);
            }
        }
    }


    // 구매 가격과 수량을 리턴
    private Long[] calcBuyPayInfo(Long curMoney, Long curStockPrice, int orderStockCount) {
        Long maxBuyableQuantity = curMoney / curStockPrice; // 최대 구매 가능 주식 수
        Long actualQuantity = Math.min(maxBuyableQuantity, orderStockCount); // 돈이 부족하면 왼쪽, 충분하면 오른쪽
        Long payMoney = curStockPrice * actualQuantity;

        Long[] result = new Long[2];
        result[0] = payMoney;
        result[1] = actualQuantity;
        return result;
    }

    @Scheduled(cron = "0 0/5 * * * *", zone = "Asia/Seoul")  // 0분부터 55분까지 5분 간격으로 실행
    protected void updateStockPriceMap() {
        LocalDate today = LocalDate.now();
        List<DailyStock> dailyStockList = dailyStockRepository.findByStockDate(today);
        for (DailyStock dailyStock : dailyStockList) {
            stockPriceMap.put(dailyStock.getStockId(), Long.valueOf(dailyStock.getClosePrice()));
        }
    }

    private List<Order> filterOrders(List<Order> rawOrderList) {
        LocalDateTime currentDateTime = LocalDateTime.now();
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


    private double calculateNewAvgUnitPrice(double currentAvgUnitPrice, long numStocksOwned,
                                            double newPurchasePrice, long numNewStocks) {
        // 평균 가격 = 총 구매 가격 / 총 수량
        return ((currentAvgUnitPrice * numStocksOwned) + (newPurchasePrice * numNewStocks)) / (numStocksOwned + numNewStocks);
    }
}