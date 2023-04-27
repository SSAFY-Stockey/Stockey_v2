## 왜 React Testing Library인가?

React Testing Library는 React 컴포넌트를 테스트하기 위해 설계된 라이브러리다. 과거에는 React 컴포넌트를 테스트하기 위해 Enzyme을 사용했을 수 있다. React Testing Library가 Enzyme과 다른 점은 테스트를 렌더링 할 때 React 컴포넌트의 인스턴스가 아닌 실제 DOM 노드를 사용한다는 점이다.

이건 사용자가 웹 브라우저에서 애플리케이션을 실행하는 실제 환경과 유사한 환경에서 테스트 케이스가 실행된다는 것을 의미한다. 테스트 환경이 사용자가 애플리케이션을 사용하는 환경과 비슷할수록 테스트를 더욱 신뢰할 수 있다.

필자가 React Testing Library를 선호하는 또 다른 큰 이유는 테스트가 사용자가 앱과 상호작용하는 방식과 유사해야 한다는 기본적인 라이브러리의 철학 때문이다. 사용자는 애플리케이션을 사용할 때 state와 props와 상호작용한다는 사실을 알지 못한다. 함수 컴포넌트에서 훅을 사용하는지, 클래스 컴포넌트와 함께 고차 컴포넌트를 사용하는지 신경 쓰지 않는다. 사용자는 그저 인터페이스(버튼, 입력, 모달 등)를 보고 상호작용할 뿐이다.

그래서 올바른 props나 state가 컴포넌트에서 변경되었는지 테스트하는 대신 React Testing Library는 사용자가 *보고* *수행*하는 작업을 테스트하도록 설계되었다. 따라서 접근 가능한 사용자 인터페이스를 구축하고 HTML을 구성할 때 모범 사례를 준수할 수 있다.





## 🤔 왜 테스트를 작성해야 하나요?

- 좋은 테스트 커버리지를 보장합니다.
- 유지보수 비용과 소프트웨어 지원 비용 절감시킵니다.
- 테스트 케이스를 재사용할 수 있습니다.
- 소프트웨어가 최종 사용자의 요구사항을 충족시키는지 확인할 수 있습니다.
- 소프트웨어의 품질과 사용자 경험을 향상합니다.
- 더 높은 품질의 제품은 더 많은 고객 만족으로 이어집니다.
- 더 많은 고객 만족은 기업의 이익을 증가시킵니다.



## 🖥 테스팅의 종류

### 1) 단위 테스트(Unit Testing)

단위 테스트는 매우 기본적이며 애플리케이션의 소스 코드에서 수행됩니다. 이러한 테스트는 소프트웨어의 메서드와 함수에서 사용하는 각 클래스, 컴포넌트 또는 모듈을 테스트하는 작업이 수반됩니다. 지속적 통합 서버는 비교적 저렴한 비용으로 매우 빠르게 단위 테스트를 수행할 수 있습니다.

### 2) 통합 테스트(Integration Testing)

통합 테스트를 통해 애플리케이션의 다양한 컴포넌트나 서비스가 제대로 작동하는지 확인할 수 있습니다. 예를 들어, 데이터베이스 상호 작용을 테스트하거나 마이크로 서비스가 의도한 대로 상호 작용하도록 제공하는 것을 포함할 수 있습니다. 애플리케이션의 여러 다른 컴포넌트들이 작동해야 하기 때문에 이러한 테스트를 수행하는 데 추가적인 비용이 발생합니다.

### 3) 기능 테스트(Functional Testing)

애플리케이션의 비즈니스 요구 사항은 기능 테스트의 주요 초점입니다. 기능 테스트에서는 수행되는 동안 시스템의 중간 상태를 체크하지 않고 수행의 결과만 확인합니다.

둘 다 상호 작용하기 위해 여러 컴포넌트가 필요하기 때문에 통합 테스트와 기능 테스트는 종종 혼동되곤 합니다. 기능 테스트에서는 제품 요구 사항에 따라 데이터베이스에서 특정 값을 얻을 것으로 예상하지만, 통합 테스트는 사용자가 데이터베이스를 쿼리할 수 있는지만 확인합니다.

### 4) 종단 간 테스트(End-to-end Testing - E2E)

종단 간 테스트는 애플리케이션의 전체 맥락에서 소프트웨어를 사용하여 사용자의 행동을 시뮬레이션합니다. 웹 페이지를 로드하거나 로그인하는 것처럼 간단한 시나리오나 이메일 알림, 온라인 결제처럼 다소 복잡한 시나리오에 대해서 다양한 사용자 흐름이 의도한 대로 작동하는지 확인합니다.

종단 간 테스트는 매우 유용하지만 실행하는 데 비용이 많이 들고, 자동화된 경우에는 관리하기 어려울 수 있습니다. 주요 변경 사항을 빠르게 발견하려면, 제한된 수의 중요한 종단 간 테스트를 갖고 하위 수준의 테스트(단위 및 통합 테스트)에 더 의존하는 것이 좋습니다.

### 5) 검증 테스트(Validation Testing)

인수 테스트는 시스템이 비즈니스 요구사항을 충족하는지 여부를 공식적으로 결정하는 테스트입니다. 테스트 동안 전체 애플리케이션을 실행하면서 사용자 동작을 재현하는 데 집중합니다. 한 단계 더 나아가 시스템의 효율성을 평가하고 특정한 목표가 달성되지 않으면 개선을 거부할 수 있습니다.

### 6) 성능 테스트(Performance Testing)

성능 테스트는 특정 워크로드를 처리하는 시스템의 능력을 평가합니다. 이러한 평가는 애플리케이션의 신뢰성, 속도, 확장성 및 응답성을 결정하는 데 도움이 됩니다. 예를 들어, 성능 테스트는 많은 수의 요청이 처리될 때 응답 시간을 모니터링하거나 시스템이 상당한 양의 데이터에 어떻게 응답하는지 평가할 수 있습니다. 프로그램이 성능 표준을 충족하는지 평가하고, 병목 현상을 찾고, 트래픽이 많을 때 안정성을 측정하고, 훨씬 더 많은 작업을 수행할 수 있습니다.

### 7) 스모크 테스트(Smoke Test)

스모크 테스트는 애플리케이션의 기본 작동을 검사하는 간단한 테스트입니다. 이 테스트는 신속하게 수행되도록 설계되었으며, 주요 시스템 컴포넌트가 계획대로 작동하고 있다는 확신을 제공하는 것에 목적이 있습니다.

스모크 테스트는 더 비용이 많이 드는 테스트를 실행할 수 있는지를 결정하기 위해 새 빌드를 만든 직후에, 또는 새로 배포된 환경에서 애플리케이션이 제대로 작동하는지 확인하기 위해 배포 직후에 도움이 될 수 있습니다.



## 🧪 테스트 주도 개발(TDD)

테스트 주도 개발에서는 테스트 케이스를 검증하는 코드보다 테스트 케이스를 먼저 작성해야 합니다. 그리고 테스트 주도 개발은 빠른 개발 주기를 반복하는 데 의존합니다. 자동화된 단위 테스트를 사용하여 설계 및 종속성의 무제한 분리를 지시합니다.

테스트 주도 개발의 모토는 아래와 같습니다.

- 빨강(Red) : 테스트 케이스를 만들고 실패시킵니다.
- 녹색(Green) : 어떤 방법으로든 테스트 케이스를 통과시킵니다.
- 리팩터링(Refactor) : 코드를 변경하여 중복을 제거합니다.

> 테스트 주도 개발을 사용할 때, 함수 자체를 작성하기 전에 테스트를 작성하는 것이 좋습니다.





# 예시

로그인 컴포넌트 테스트

```typescript
export function Login() {
  return (
    <div>
      <div>
        <input type="email" name="email" placeholder="email" />
      </div>
      <div>
        <input type="password" name="password" placeholder="password" />
      </div>

      <div>
        <button type="button">Sign In</button>
        <button type="button">Sign Up</button>
      </div>
    </div>
  );
}
```

```typescript
describe('Login component tests', () => {
  let container: HTMLDivElement;

  // beforeEach: 이 파일의 각 테스트가 실행되기 전에 이 함수를 실행합니다. 함수가 promise를 반환하거나 생성자인 경우, jest는 테스트를 실행하기 전에 해당 promise가 해결될 때까지 기다립니다.
  beforeEach(() => {
    container = document.createElement('div');
    document.body.appendChild(container);
    ReactDOM.render(<Login />, container);
  });

  // 테스트가 서로 방해되지 않도록 마지막에 모든 것을 초기화합니다.
  afterEach(() => {
    document.body.removeChild(container);
    container.remove();
  });

  // 각 입력 필드를 렌더링하기 위한 테스트를 실행합니다.
  it('Renders all input fields correctly', () => {
    // 입력 필드를 선택합니다.
    const inputs = container.querySelectorAll('input');
    // 입력 필드가 올바르게 렌더링되었는지 확인합니다.
    expect(inputs).toHaveLength(2);

    // 첫 번째 입력 필드 및 두 번째 입력 필드를 각각 "이메일" 및 "암호"인지 확인합니다.
    expect(inputs[0].name).toBe('email');
    expect(inputs[1].name).toBe('password');
  });

  // 각 버튼을 렌더링하기 위한 테스트를 실행합니다.
  it('Renders all buttons correctly', () => {
    const buttons = container.querySelectorAll('button');
    expect(buttons).toHaveLength(2);

    expect(buttons[0].type).toBe('button');
    expect(buttons[1].type).toBe('button');
  });
});
```

