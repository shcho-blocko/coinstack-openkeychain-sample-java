import io.blocko.coinstack.CoinStackClient;
import io.blocko.coinstack.ECKey;
import io.blocko.coinstack.exception.MalformedInputException;
import io.blocko.coinstack.openkeychain.KeyManager;
import io.blocko.coinstack.openkeychain.client.LoginManager;
import io.blocko.coinstack.openkeychain.model.Challenge;
import io.blocko.coinstack.openkeychain.model.Response;
import io.blocko.json.JSONException;

public class OpenkeychainClient {
	
	private CoinStackClient coinstack = null;
	private KeyManager keyManager = null;
	private LoginManager loginManager = null;
	
	private String serverAuthAddress = null;
	
	
	public OpenkeychainClient(String serverAuthAddress, String privateKeyWIF) {
		this.coinstack = SampleMain.createNewClient();
		this.serverAuthAddress = serverAuthAddress;
		initKeyManager(privateKeyWIF);
	}
	
	private void initKeyManager(String privateKeyWIF) {
		if (privateKeyWIF != null && !privateKeyWIF.isEmpty()) {
			this.keyManager = SampleMain.createNewClientKeyManager(privateKeyWIF);
		}
		this.loginManager = new LoginManager(coinstack, keyManager);
	}
	
	public String challengeResponse(String challengeSerialized) throws JSONException {
		return challengeResponse(challengeSerialized, null);
	}
	public String challengeResponse(String challengeSerialized, String newPrivKey) throws JSONException {
		// check challenge
		Challenge challenge = Challenge.unmarshal(challengeSerialized);
		boolean check = this.loginManager.checkChallenge(challenge, this.serverAuthAddress);
		if (!check) {
			System.out.println("- [client] challenge failed: failed to check challenge");
			return null;
		}
		//System.out.println("- [client] challenge="+challenge.marshal());
		
		// create new key and initialize
		if (newPrivKey != null) {
			initKeyManager(newPrivKey);
			try {
				System.out.printf("- [client] create new privkey: addr=%s, pkey=%s\n",
						ECKey.deriveAddress(newPrivKey), newPrivKey);
			} catch (MalformedInputException e) {
				throw new RuntimeException(e);
			}
		}
		
		// create response and request registration
		Response response = this.loginManager.createResponse(challenge);
		String responseSerialized = response.marshal();
		System.out.println("- [client] response="+responseSerialized);
		return responseSerialized;
	}
	
}
