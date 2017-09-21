package io.blocko.coinstack;

import java.security.PublicKey;

import io.blocko.coinstack.CoinStackClient;
import io.blocko.coinstack.ECKey;
import io.blocko.coinstack.exception.CoinStackException;
import io.blocko.coinstack.model.CredentialsProvider;
import io.blocko.coinstack.openkeychain.InMemoryKeyManager;
import io.blocko.coinstack.openkeychain.KeyManager;

@Deprecated
public class InstanceFactory {
	
	public static CoinStackClient createNewCoinStackClient() {
		CredentialsProvider credential = null;
		AbstractEndpoint TESTCHAIN = new AbstractEndpoint() {
			@Override
			public String endpoint() {
				return "http://testchain.blocko.io";
			}
			@Override
			public boolean mainnet() {
				return true;
			}
			@Override
			public PublicKey getPublicKey() {
				return null;
			}
		};
		CoinStackClient coinstack = new CoinStackClient(credential, TESTCHAIN);
		return coinstack;
	}
	
	public static KeyManager createNewKeyManager(String privateKey) throws CoinStackException {
		String authAddress = ECKey.deriveAddress(privateKey);
		return createNewKeyManager(privateKey, authAddress);
	}
	public static KeyManager createNewKeyManager(String privateKey, String authAddress) throws CoinStackException {
		KeyManager keyManager = new InMemoryKeyManager();
		keyManager.registerKey(authAddress, privateKey.toCharArray());
		keyManager.registerAllAddress(new String[] { authAddress });
		return keyManager;
	}
}
