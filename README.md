## 도메인 모델 패턴
- 표현 (Controller) => 응용(Service) => 도메인(domain) => 인프라스트럭쳐 (DB, 외부 API)

## 도메인 영역의 주요 구성요소
- 엔티티(Entitiy) : 고유의 식별자를 갖는 객체 
- 밸류(Value) : 고유의 식별자를 갖지 않는 객체로 주로 개념적인 하나인 도메인 객체의 속성을 표현. 엔티티의 속성이나 다른 밸류 타입의 속성으로 사용된다.
- 애그리거트(Aggregate) : 관련된 엔티티와 밸류 객체를 개념적으로 하나로 묶은 것
- 리포지터리(Repository) : 도메인 모델의 영속성을 처리한다.
- 도메인 서비스(Domain Service) : 특정 엔티티에 속하지 않은 도메인 로직을 제공한다. 여러 엔티티와 밸류를 필요할 경우 도메인 서비스에서 로직을 구현

## 도메인 모델 엔티티 vs DB 모델 엔티티
- 도메인 모델의 엔티티는 데이터와 함께 도메인 기능을 함께 제공
- 도메인 모델의 엔티티는 두 개 이상의 데이터가 개념적으로 하나인 경우 밸류 타입을 이용해서 표현
  - 예를 들어 RDMS에 주문자를 표현하려면 Order(주문) 테이블에 orderer_name, orderer_email을 같이 넣거나 테이블을 분리해서
  order (no, orderer_name, orderer_email), order_orderer (order_no, orderer_name, orderer_email) 중복되게 저장 해야 된다.

## JPA에서 Repository Interface 사용 이유
- Interface 자체는 고수준 이기 때문에 Service(고수준)에서 Repositroy(고수준)을 통해 RepositoryImpl(저수준)에 접근한다.
  - 이렇게 하면 인프라스트럭처에 의존하면 생기는 2가지 문제 (테스트의 어려움과 기능 확장의 어려움)이 해소된다.
  
## 요청 처리 흐름
![spring](https://user-images.githubusercontent.com/7076334/55076338-7f469180-50d8-11e9-8cad-7e051a12d0d0.jpg)

## 생각해볼점
- 실제 프로젝트 내에서 Service에서 외부 API통신을 하거나 인프라스트럭처 용도를 사용할 때는 인터페이스(고수준)을 두어서 유연하게 만들자.
- 하지만 예외도 있다. 인프라스트럭처에 대한 의존을 일부 도메인에 넣은 코드 (보통 JPA에서 많이 사용하는)
  - ex) @Entity @Table(name = "TBL_ORDER") public class Order {...}

## 애그리거트(Aggregate)
- 경계를 설정할 때 기본이 되는 것은 도메인 규칙과 요구사항이다.
- 도메인 규칙에 따라 함께 생성되는 구성요소는 한 애그리거트에 속할 가능성이 높다.
- 하나의 애그리거트 예) 주문할 상품 개수, 배송지 정보, 주문자 정보 (주문 시점에 함께 생성됨)
- 하나의 애그리거트 아닌경우 예) 상품 - 리뷰 (Product 주체는 상품 담당자, Review 주체는 고객)

## 애그리거트 루트
- 애그리거트 전체를 관리할 주체
- 루트의 핵심 역할은 애그리거트의 일관성이 깨지지 않도록 하는 것이다.
- 애그리거트 루트가 아닌 다른 객체가 애그리거트에 속한 객체를 직접 변경하면 안된다! (주의)

## 애그리거트 루트를 통해서만 도메인 로직을 구현하게 만들기 위한 두가지 습관
- 단순히 필드를 변경하는 set 메서드를 공개(pubic) 으로 만들지 않는다.
- 밸류 타입은 불변으로 구현한다.

## 트랜잭션 범위
- 트랜잭션 범위는 작을수록 좋다. 범위가 많아질 수록 잠금 대상이 많아져서 전체적인 성능을 떨어뜨린다.
- 한 트랜잭션에서는 한 개의 애그리거트만 수정해야 한다. (책임 범위를 넘어서 결합도가 높아짐)
- 만약 부득이하게 수정해야 한다면 애그리거트에서 다른 애그리거트를 직접 수정하지 말고 응용 서비스에서 수정하도록 해야된다.

## 트랜잭션 제약
- 한 트랜잭션에서 두 개 이상의 애그리거트를 수정하는 대신 도메인 이벤트와 비동기를 사용하는 방식을 사용하는데, 기술적으로 도입할 수 없을 경우 한 트랜잭션에서 다수의 애그리거트를 수정해서 일관성을 유지해야 된다.

## 리포지터리
- 리포지터리는 애그리거트 단위로 존재한다.
- 어떤 기술을 이용해서 리포지터리를 구현하느냐에 따라 애그리거트의 구현도 영향을 받는다.
- Order 애그리거트를 저장할 때 루트와 매핑되는 테이블과 모든 속성을 테이블에 저장해야 된다. 데이터를 불러올 때도 동일하다. (일관성)

## 필드를 이용한 애그리거트 참조의 문제점
- 편한 탐색 오용 : 트랜잭션 제약을 어기게 된다. (하나의 애그리거트에서 다른 애그리거트 수정)
- 성능에 대한 고민 : 지연(lazy)로 할 것이냐, 즉시(eager)로 할 것이냐 성능에 따른 고민
- 확장 어려움 : 도메인마다 다른 DBMS를 사용할 경우 애그리거트 루트를 참조하기 위해 JPA와 같은 단일 기술을 사용할 수 없다.
  - 해결책 : 아이디를 이용한 간접 참조 (리포지터리마다 다른 저장소를 사용할 수 있어서 확장에 용이)
    - 하지만 성능 이슈 생길 수 있다. (여러번 따로 조회 해야 하기 때문에)
      - 아이디 간접 해결책 : DAO를 만들고 조인을 이용해서 한 번의 쿼리로 필요한 데이터 조회 (쿼리가 복잡할 경우에만 사용 ex)JPQL)
        - 이 방법도 다른 저장소 사용하면 사용 못함 ㅠㅠ. 캐시 처리하거나 조회 전용 저장소를 따로 구성해야 됨
        
## 애그리거트 팩토리
- 애그리거트가 갖고 있는 데이터를 이용해서 다른 애그리거트를 생성해야 한다면 팩토리 메서드를 구현하는 것을 고려해 보자.

## 리포지터리와 모델 구현(JPA)
- 인터페이스는 애그리거트 루트를 기준으로 작성한다.
- JPA의 @Entity와 @Embeddable로 클래스를 매핑하려면 기본 생성자를 제공해야 한다.
- @AttributeConverter를 이용하면 convertToDatabaseColumn, convertToEntitiyAttribute 로 변환할 수 있다.
- @Embeddable 타입의 클래스 상속 매핑을 지원하지 않는다. 그래서 @Entity를 이용해야 된다.

## 엔티티와 밸류 기본 매핑 구현
- 애그리거트 루트는 엔티티이므로 @Entity로 매핑 설정한다.
- 한 테이블에 엔티티와 밸류 데이터가 같이 있다면 밸류는 @Embeddable로 매핑 설정한다. 밸류 타입 프로퍼티는 @Embedded로 매핑 설정한다.

## 애그리거트 로딩 전략
- FetchType.EAGER로 설정하면 루트 구할 때, 연관구성요소를 같이 불러온다.
  - 조회 성능이 나빠질 수도 있다.
- FetchType.Lazy로 설정하면 실제 호출될 때 구성요소를 불러온다.
  - 즉시 로딩보다 쿼리 실행 횟수가 많아질 수 있다.
  
## 애그리거트의 영속성 전파
- @Embeddable 매핑 타입의 경우 함께 저장되고 삭제되므로 cascade 속성 추가 설정 필요 없다.
- @Entity 타입은 cascade 속성을 사용해서 저장과 삭제 시에 함께 처리되도록 설정해야 된다.

## JPA를 위한 조회 스펙
- CriteriaBuilder와 Predicate를 이용

## 응용 서비스의 역할 (service)
- 주요 역할은 도메인 객체를 사용해서 사용자의 요청을 처리하는 것
- 표현(사용자) 영역 입장에서 보았을 때 응용 서비스는 도메인 영역과 표현 영역을 연결해 주는 창구인 퍼사드(facade) 역할을 한다.
- 도메인 객체 간의 실행 흐름을 제어하는 것과 더불어 주된 역할 중 하나는 트랜잭션 처리이다.
- 도메인 로직을 넣지 않도록 주의해야 된다.

## 도메인 로직을 도메인 영역과 응용 서비스에 분산해서 구현할 경우
- 코드의 응집성이 떨어진다. (로직을 파악하기 위해 여러 영역 분석 필요)
- 여러 응용 서비스에서 동일한 도메인 로직을 구현할 가능성이 높아진다. (이부분이 중요한듯)

## 응용 서비스(service)를 구현할 때 인터페이스가 필요 할까?
- 구현 클래스가 다수 존재할 때 인터페이스를 사용하는데, 응용 서비스는 보통 런타임에 이를 교체하는 경우가 거의 없을 뿐더러 구현 클래스가 여러개인 경우도 매우 드물다.
- 인터페이스가 명확하게 필요하기 전까지는 응용 서비스에 대한 인터페이스를 작성하는 것이 좋은 설계라고는 볼 수 없다.

## 응용 서비스에서 리턴할 때 필요한 데이터만 전달
- 응용 서비스에서 애그리거트 자체를 리턴하면 코딩을 편하게 할 수 있지만, 도메인의 로직 실행을 표현, 응용 두곳에서 할 수 있게 된다.
  - 로직을 응용 서비스와 표현 영역에 분산시켜서 코드의 응집도를 낮추는 원인이 된다.

## 표현 영역에 의존하지 않기
- 응용 서비스의 파라미터 타입을 결정할 때 주의점은 표현 영역과 관련된 타입을 사용하면 안 된다. ex) HttpServletRequest, HttpSession 등
- 의존이 발생하면 응용 서비스만 단독으로 테스트하기 어려워진다.
- 표현 영역의 구현이 변경되면 응용 서비스의 구현도 함께 변경해야 된다.

## 도메인 이벤트 처리
- 응용 서비스의 역할 중 하나는 도메인 영역에서 발생시킨 이벤트를 처리하는 것이다.
- 이벤트를 사용하면 코드가 다소 복잡해지는 대신 도메인 간의 의존성이나 외부 시스템에 대한 의존을 낮춰주는 장점을 얻을 수 있다.
- 시스템을 확장하는 데에 이벤트가 핵심 역할을 수행하게 된다.

## 표현 영역(controller)
- 사용자가 시스템을 사용할 수 있는 흐름을 제공하고 제어
- 사용자의 요청을 알맞은 응용 서비스에 전달하고 결과를 사용자에게 제공
- 사용자의 세션을 관리한다.
- 표현 영역에서 필수 값과 값의 형식을 검사하면 실질적으로 응용 서비스는 아이디 중복 여부와 같은 논리적 오류만 검사하면 된다.
  - 표현 영역 : 필수 값, 값의 형식, 범위 등을 검증
  - 응용 서비스 : 데이터의 존재 유무와 같은 논리적 오류를 검증

## 조회 전용 기능과 응용 서비스
- 서비스에서 수행하는 추가적인 로직이 없을뿐더러 조회 전용 기능이어서 트랜잭션이 필요 없는 경우, 표현 영역에서 바로 조회 전용 기능을 사용해도 된다.

## 도메인 서비스
- 응용 영역의 서비스가 응용 로직을 다룬다면 도메인 서비스는 도메인 로직을 다룬다.
- 도메인 영역의 애그리거트나 밸류와 차이점은 상태 없이 로직만 구현한다는 점이다.
- 도메인 서비스는 도메인 로직을 수행하지 응용 로직을 수행하지 않는다.
- 도메인 서비스의 구현이 특정 구현 기술에 의존적이거나 외부 시스템의 API를 실행한다면 도메인 영역의 도메인 서비스는 인터페이스로 추상화해야 한다.

## 애그리거트 트랜잭션 관리
- 선점 잠금(Pessimistic Lock)
  - 선점 잠근은 먼저 애그리거트를 구한 스레드가 애그리거트 사용이 끝날 때까지 다른 스레드가 해당 애그리거트를 수정하는 것을 막는 방식이다.
  - ex) 오라클의 select for update, JPA LockModeType.PESSIMISTIC_WRITE
  
- 비선점 잠금(Optimistic Lock)
  - 비선점 잠금 방식은 잠금을 해서 동시에 접근하는 것을 막는 대신 변경한 데이터를 실제 DBMS에 반영하는 시점에 변경 가능 여부를 확인하는 방식이다.
  - Update 쿼리를 실행할 때 Version을 체크해서 처리하는 방식
  - ex) 테이블 컬럼의 버전, JPA @Version
  
- 오프라인 선점 잠금
  - 선점이나 비선점 보다 더 엄격하게 데이터 충돌을 막고 싶다면 누군가가 수정 화면을 보고 있을 때 수정 화면 자체를 실행하지 못하도록 해야 된다.
  - 단일 트랜잭션에서 동시 변경을 막는 선점 잠금 방식과 달리 오프라인 선점 잠금은 여러 트랜잭션에 걸쳐 동시 변경을 막는다.
  - 오프라인 선점 잠금은 크게 잠금 선점 시도, 잠금 확인, 잠금 해제, 락 유효 시간 연장 등의 네 가지 기능을 제공해야 한다.
  
## BOUNDED CONTEXT
- 내가 이해한 부분은 업무 단위로 CONTEXT를 나눈다는 의미 인듯 (MSA)
- 물리적 BOUNDED CONTEXT가 하나더라도 하위 도메인마다 구분되는 패키지를 갖도록 구현해야 하위 도메인을 위한 모델이 서로 뒤섞이지 않아서 하위 도메인마다 BOUNDED CONTEXT를 갖는 효과를 낼 수 있다.

## 공개 호스트 서비스 (OPEN HOST SERVICE)
- 상류 팀의 고객인 하류 팀이 다수 존재하면 상류 팀은 여러 하류팀의 요구사항을 수용할 수 있는 API를 만들고 이를 서비스 형태로 공개해서 서비스의 일관성을 유지할 수 있다. ex) 검색

## 공유 커널 (SHARED KERNEL)
- 여러 팀이 공유하는 모델
  - 중복을 줄여준다.

## 컨텍스트 맵
- 전체 비즈니스를 조망할 수 있는 지도
- BOUNDED CONTEXT의 경계가 명확하게 드러나고 서로 어떤 관계를 맺고 있는지 알 수 있다.

## 이벤트
- 이벤트는 BOUNDED CONTEXT 끼리 강하게 결합을 없앨 수 있는 방법이다.
- 도메일 모델에서 이벤트 주체는 엔티티, 밸류, 도메인 서비스와 같은 도메인 객체이다.
- 이벤트 핸들러는 이벤트 생성 주체가 발생한 이벤트에 반응한다.
- 이벤트 디스패처는 이벤트 생성 주체와 이벤트 핸들러를 연결해 준다.
- 이벤트는 과거시제를 사용해야 된다.

## 이벤트 용도
- 트리거
- 서로 다른 시스템 간의 데이터 동기화

## 이벤트 장점
- 서로 다른 도메인 로직이 섞이는 것을 방지한다.
- 기능 확장도 용이하다. (필요할 때 등록만 하면 된다.)

## 비동기 이벤트 처리 방법
- 로컬 핸들러를 비동기로 실행 ex) 쓰레드풀 사용
- 메시지 큐를 사용 ex) RabbitMQ
- 이벤트 저장소와 이벤트 포워더 사용 ex) 배치
  - 이벤트를 어디까지 처리했는지 추적하는 역할이 포워더에 있다.
- 이벤트 저장소와 이벤트 제공 API 사용 ex) API 서버
  - 외부 핸들러가 자신이 어디까지 이벤트를 처리했는지 기억해야 한다.
  
## CQRS (Command Query Responsibility Segregation)
- 도메인 모델을 사용하면 여러 애그리거트에서 데이터를 가져와 출력하는 기능을 구현하기에 고려할 것들이 많아 진다.
  - 이런 구현 복잡도를 낮추는 간단한 방법이 상태 변경을 위한 모델과 조회를 위한 모델을 분리하는 것이다.
  - 상태를 변경하는 범위와 상태를 조회하는 범위가 정확하게 일치하지 않기 때문에 단일 모델로 두 종류의 기능을 구현하면 모델이 불필요하게 복잡해진다.

## CQRS 장단점
- 장점
  - 명령 모델을 구현할 때 도메인 자체에 집중할 수 있다. (복잡한건 조회 모델에서)
  - 명령 모델에서 조회 관련 로직이 사라져 복잡도를 낮춰준다.
  - 조회 성능을 향상시키는데 유리하다.
- 단점
  - 구현해야 할 코드가 더 많아진다. (명령, 조회)
  - 도메인이 단순하거나 트래픽이 적을 경우 조회 전용 모델을 만드는 것은 비효율적일 수 있다.
  - 더 많은 구현 기술이 필요하다.
    - 명령과 조회 모델 별로 다른 기술을 구현
    - 명령과 조회 사이의 데이터 동기화
- 결론
  - 트래픽이 높은 서비스인 경우 CQRS 도입을 고려해 보자.
