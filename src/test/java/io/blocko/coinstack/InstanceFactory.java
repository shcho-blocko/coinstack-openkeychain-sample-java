package io.blocko.coinstack;

import io.blocko.coinstack.CoinStackClient;
import io.blocko.coinstack.ECKey;
import io.blocko.coinstack.Endpoint;
import io.blocko.coinstack.exception.CoinStackException;
import io.blocko.coinstack.model.CredentialsProvider;
import io.blocko.coinstack.openkeychain.InMemoryKeyManager;
import io.blocko.coinstack.openkeychain.KeyManager;

public class InstanceFactory {
	
	public static CoinStackClient createNewCoinStackClient() {
		CoinStackClient coinstack = new CoinStackClient(new CredentialsProvider() {
			@Override
			public String getAccessKey() {
				return "17155ccf15e603853c19a35559f3f5"; // coinstack api key: "coinstack-sample-java"
			}
			@Override
			public String getSecretKey() {
				return "4ffe022576916bf0d9c4c13718d582";
			}
		}, Endpoint.MAINNET);
		return coinstack;
	}
	
	public static KeyManager createNewKeyManager(String privateKey) throws CoinStackException {
		String authAddress = ECKey.deriveAddress(privateKey);
		return createNewKeyManager(privateKey, authAddress);
	}
	public static KeyManager createNewKeyManager(String privateKey, String authAddress) throws CoinStackException {
		KeyManager keyManager = new InMemoryKeyManager();
		keyManager.registerKey(authAddress, privateKey.toCharArray());
		return keyManager;
	}
}
