import java.security.PublicKey;

import io.blocko.coinstack.AbstractEndpoint;
import io.blocko.coinstack.CoinStackClient;
import io.blocko.coinstack.ECKey;
import io.blocko.coinstack.exception.MalformedInputException;
import io.blocko.coinstack.model.CredentialsProvider;
import io.blocko.coinstack.openkeychain.InMemoryKeyManager;
import io.blocko.coinstack.openkeychain.KeyManager;

public class SampleMain {
	
	public static final String TESTCHAIN_ENDPOINT_URL = "http://testchain.blocko.io";
	
	public static final String SERVER_PRIVATE_KEY = "L3mTMTnrvBS5S4Fn7PFWWSFrDipQA1NNEyHLdTQcEpv4Lo1m5zHA";
	public static final String SERVER_AUTH_ADDRESS = "1DT3Hah1qimqcQwqXSf5QXZp1Uk8fZsSwt";
	public static final String[] SERVER_ALL_AUTH_ADDRESSES = new String[] {
			SERVER_AUTH_ADDRESS
	};
	
	public static final String CLIENT_REGISTED_PKEY = "L5WfxoVoA8s3Lgw424TRwqESwYNPgoxQDSpmeqArXMRYUZ8es22i";
	public static final String CLIENT_REGISTED_ADDR = "1LMkFgxudNGxHnvTDb6hz3bzH1jAhrMUHX";
	public static final String CLIENT_REVOKED_PKEY = "L4RHR4ThbZnpUMzt7t3b7StoxijzhBoccfeBrUPSgqeMGUGDaDLG";
	public static final String CLIENT_REVOKED_ADDR = "1KqnJmYGv3mLmwyS6wxuVEkwXwgnDPMrTP";
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("# SampleOpenkeychain");
		String newPrivKey = SampleRegistration.sampleRegistration();
		
		Thread.sleep(500);
		SampleAuthorization.sampleAuthorization(newPrivKey);
		
		Thread.sleep(500);
		SampleRevocation.sampleRevocation(newPrivKey);
	}
	
	public static OpenkeychainServer newServer() {
		OpenkeychainServer server = new OpenkeychainServer(
				SampleMain.SERVER_PRIVATE_KEY,
				SampleMain.SERVER_AUTH_ADDRESS,
				SampleMain.SERVER_ALL_AUTH_ADDRESSES);
		return server;
	}
	public static OpenkeychainClient newClient(String privateKeyWIF) {
		OpenkeychainClient client = new OpenkeychainClient(
				SampleMain.SERVER_AUTH_ADDRESS,
				privateKeyWIF);
		return client;
	}
	
	public static CoinStackClient createNewClient() {
		CredentialsProvider credentials = null;
		AbstractEndpoint endpoint = new AbstractEndpoint() {
			@Override
			public String endpoint() {
				return SampleMain.TESTCHAIN_ENDPOINT_URL;
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
		return new CoinStackClient(credentials, endpoint);
	}
	
	public static KeyManager createNewClientKeyManager(String privateKeyWIF) {
		String certAddress = null;
		try {
			certAddress = ECKey.deriveAddress(privateKeyWIF);
		} catch (MalformedInputException e) {
			throw new RuntimeException(e);
		}
		KeyManager keyManager = new InMemoryKeyManager();
		keyManager.registerKey(certAddress, privateKeyWIF.toCharArray());
		return keyManager;
	}
	public static KeyManager createNewServerKeyManager(
			String privateKeyWIF, String authAddress, String[] allAuthAddresses) {
		try {
			String derivedAddress = ECKey.deriveAddress(privateKeyWIF);
			if (!derivedAddress.equals(authAddress)) {
				throw new RuntimeException("mismatched key and address: derivedAddress="+derivedAddress);
			}
		} catch (MalformedInputException e) {
			throw new RuntimeException(e);
		}
		KeyManager keyManager = new InMemoryKeyManager();
		keyManager.registerKey(authAddress, privateKeyWIF.toCharArray());
		keyManager.registerAllAddress(allAuthAddresses);
		return keyManager;
	}
	
}
