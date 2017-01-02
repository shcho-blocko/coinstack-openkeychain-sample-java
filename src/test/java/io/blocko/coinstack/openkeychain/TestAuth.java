package io.blocko.coinstack.openkeychain;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.*;

import io.blocko.apache.commons.codec.binary.Base64;
import io.blocko.coinstack.CoinStackClient;
import io.blocko.coinstack.InstanceManager;
import io.blocko.coinstack.ECKey;
import io.blocko.coinstack.exception.CoinStackException;
import io.blocko.coinstack.openkeychain.KeyManager;
import io.blocko.coinstack.openkeychain.client.LoginManager;
import io.blocko.coinstack.openkeychain.model.Challenge;
import io.blocko.coinstack.openkeychain.model.RegistrationMetadata;
import io.blocko.coinstack.openkeychain.model.Response;
import io.blocko.coinstack.openkeychain.server.AuthorizationManager;
import io.blocko.coinstack.openkeychain.server.ChallengeResponseManager;
import io.blocko.coinstack.openkeychain.server.RegistrationManager;

public class TestAuth {
	
	public static String generateRandomContextString() {
		return ECKey.createNewPrivateKey();
	}
	public static String loadClientKeyForTest() {
		//return ECKey.createNewPrivateKey();
		return "KwdANrBRPcX1DwDHK2M3FwZzJeS6o2CncDJar5Fby23es2f6TRoG"; // addr=1DXnc2fX9S1N7HmJgawGcWsF8WXxbspZnB (registed)
	}
	
    private static final String SERVER_PRIVATE_KEY = ""; // SECRET: private key for test
    private static final String SERVER_AUTHORITY_ADDRESS = "1LNerxk3A4KDtoXMtYXLfL3LRnhjvwkC55";
	
	
	private CoinStackClient coinstack = null;
	private KeyManager keyManager = null;
	
	private CoinStackClient clientCoinStack = null;
	private KeyManager clientKeyManager = null;
	
	
	@Before
	public void before() throws Exception {
		// [server]
		coinstack = InstanceManager.createNewCoinStackClient();
		keyManager = InstanceManager.createNewKeyManager(SERVER_PRIVATE_KEY, SERVER_AUTHORITY_ADDRESS);
		System.out.println("[server] authAddress="+keyManager.fetchAddress());
		
		// [client]
		String clientPrivateKey = loadClientKeyForTest();
		clientCoinStack = InstanceManager.createNewCoinStackClient();
		clientKeyManager = InstanceManager.createNewKeyManager(clientPrivateKey);
		System.out.println("[client] authAddress="+clientKeyManager.fetchAddress()+" / privateKey="+clientPrivateKey);
	}
	
	
	@Test
	public void testMain() throws Exception {
		// [server] prepare regManager
		String SERVER_AUTH_ADDRESS = keyManager.fetchAddress();
		if (coinstack.getBalance(SERVER_AUTH_ADDRESS) <= 0) {
			String errMsg = "not enough balance (BTC) for testing";
			System.out.println(errMsg); return;
			//throw new RuntimeException(errMsg);
		}
		
		// [server] prepare regManager
		ChallengeResponseManager regManager = new RegistrationManager(coinstack, keyManager);
		
		
		// [client] prepare loginManager
		String CLIENT_AUTH_ADDRESS = clientKeyManager.fetchAddress();
		LoginManager clientLoginManager = new LoginManager(clientCoinStack, clientKeyManager);
		
		// [client] send request to server
		System.out.println("[client] request");
		
		
		// [server] create challenge and send to client
		String CHALLENGE_CONTEXT = generateRandomContextString();
		Challenge challenge = regManager.createChallenge(CHALLENGE_CONTEXT);
		System.out.println("[server] create challenge context: "+CHALLENGE_CONTEXT);
		System.out.println("[server] send challenge: "+challenge.marshal());
		
		
		// [client] check challenge
		System.out.println("[client] received challenge context: "+challenge.getContext());
		assertTrue(clientLoginManager.checkChallenge(challenge, SERVER_AUTH_ADDRESS));
		System.out.println("[client] check challenge: success");
		
		// [client] create response and send to server
		Response response = clientLoginManager.createResponse(challenge);
		System.out.println("[client] send response: "+response.marshal());
		
		
		// [server] check response
		String buf = new String(Base64.decodeBase64(response.getChallenge()));
		Challenge clientReceived = Challenge.unmarshal(buf);
		System.out.println("[server] received response context: "+clientReceived.getContext());
		assertTrue(regManager.checkResponse(CHALLENGE_CONTEXT, response));
		System.out.println("[server] check response: success");
		
		
		// [server] record
		//testRegistration(response.getCertificate()); // use BTC
		
		// [server] check
		testSignin(CLIENT_AUTH_ADDRESS, response.getCertificate());
		
		// [server] revoke
		//testRevocation(CLIENT_AUTH_ADDRESS, response.getCertificate()); // use BTC
	}
	
	public void testRegistration(String certificateAddress) throws IOException {
		RegistrationManager regManager = new RegistrationManager(coinstack, keyManager);
		RegistrationMetadata metadata = new RegistrationMetadata() {
			@Override
			public byte[] marshal() {
				return "METADATA".getBytes();
			}
		};
		System.out.println("[server] registration metadata: "+new String(metadata.marshal()));
		
		String txId = regManager.recordRegistration(certificateAddress, metadata);
		System.out.println("[server] registration txId: "+txId);
	}
	
	public void testSignin(String clientAuthAddress, String certificateAddress) throws IOException, CoinStackException {
		if (clientAuthAddress == null || !clientAuthAddress.equals(certificateAddress)) {
			System.out.println("[server] signin: fail, invalid authAddress");
			return;
		}
		AuthorizationManager authManager = new AuthorizationManager(coinstack, keyManager);
		byte[] metadata = authManager.checkRegistration(certificateAddress);
		if (metadata == null) {
			System.out.println("[server] signin: fail.");
		}
		else {
			System.out.println("[server] signin: success.");
			System.out.println("[server] signin metadata: "+new String(metadata));
		}
	}
	
	public void testRevocation(String clientAuthAddress, String certificateAddress) throws IOException {
		if (clientAuthAddress == null || !clientAuthAddress.equals(certificateAddress)) {
			System.out.println("[server] revocation: fail, invalid authAddress");
			return;
		}
		RegistrationManager regManager = new RegistrationManager(coinstack, keyManager);
		String txId = regManager.revokeRegistration(certificateAddress);
		System.out.println("[server] revocation txId: "+txId);
	}
}
