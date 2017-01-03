# coinstack-openkeychain-sample-java

CoinStack Openkeychain의 샘플 코드를 제공합니다.

## caution
일부 테스트의 경우에는 비트코인 잔고가 필요하며
샘플 코드에 공개된 개인키에는 비트코인 잔고가 없어서 테스트가 원활히 진행되지 않을 수 있습니다.
InstanceFactory 클래스에서 비트코인 잔고가 있는 개인키를 지정한 후 테스트를 진행하시기 바랍니다.

## io.blocko.coinstack
- InstanceFactory: 테스트에 사용할 CoinStackClient, KeyManager 인스턴스를 생성합니다.

## io.blocko.coinstack.openkeychain
- TestAuth: Openkeychain
