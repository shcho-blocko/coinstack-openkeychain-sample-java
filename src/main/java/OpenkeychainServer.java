import java.io.IOException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import io.blocko.coinstack.CoinStackClient;
import io.blocko.coinstack.exception.CoinStackException;
import io.blocko.coinstack.openkeychain.KeyManager;
import io.blocko.coinstack.openkeychain.model.Challenge;
import io.blocko.coinstack.openkeychain.model.RegistrationMetadata;
import io.blocko.coinstack.openkeychain.model.Response;
import io.blocko.coinstack.openkeychain.server.AuthorizationManager;
import io.blocko.coinstack.openkeychain.server.RegistrationManager;
import io.blocko.coinstack.util.Codecs;
import io.blocko.json.JSONException;

public class OpenkeychainServer {
	
	private CoinStackClient coinstack;
	private KeyManager keyManager;
	private RegistrationManager regManager;
	private AuthorizationManager authManager;
	
	private Map<String, String> session;
	
	public OpenkeychainServer(String privateKeyWIF, String authAddress, String[] allAuthAddresses) {
		this.coinstack = SampleMain.createNewClient();
		this.keyManager = SampleMain.createNewServerKeyManager(privateKeyWIF, authAddress, allAuthAddresses);
		this.regManager = new RegistrationManager(coinstack, keyManager);
		this.authManager = new AuthorizationManager(coinstack, keyManager);
		this.session = new HashMap<String, String>();
	}
	
	
	// challenge response
	public String requestChallenge() {
		String context = generateRandomContextString();
		Challenge challenge = this.regManager.createChallenge(context);
		String challengeSerialized = challenge.marshal();
		System.out.println("- [server] challenge="+challengeSerialized);
		this.session.put("SESSION_CONTEXT", context);
		return challengeSerialized;
	}
	private static Random rand = new SecureRandom();
	public static String generateRandomContextString() {
		byte[] buffer = new byte[32];
		rand.nextBytes(buffer);
		return Codecs.HEX.encode(buffer);
	}
	
	
	// record registration
	public String requestRegistration(String responseSerialized)
			throws JSONException, IOException, CoinStackException {
		// unmarshal and check response
		Response response = Response.unmarshal(responseSerialized);
		String savedContext = this.session.get("SESSION_CONTEXT");
		boolean check = regManager.checkResponse(savedContext, response);
		if (!check) {
			System.out.println("- [server] registration failed: failed to check response");
			return null;
		}
		// registration
		String certAddress = response.getCertificate();
		RegistrationMetadata metadata = createNewMetadata();
		String txId = this.regManager.recordRegistration(certAddress, metadata);
		if (txId == null) {
			System.out.println("- [server] registration failed: failed to record");
			return null;
		}
		System.out.println("- [server] registration succeed: txId="+txId);
		return txId;
	}
	private static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
	public static RegistrationMetadata createNewMetadata() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, 1);
		Date expiredDate = cal.getTime();
		final String dateStr = DATE_FORMAT.format(expiredDate);
		RegistrationMetadata metadata = new RegistrationMetadata() {
			@Override
			public byte[] marshal() {
				return dateStr.getBytes();
			}
		};
		return metadata;
	}
	
	
	// check authorization
	public String requestAuthrization(String responseSerialized)
			throws JSONException, IOException, CoinStackException {
		// unmarshal and check response
		Response response = Response.unmarshal(responseSerialized);
		String savedContext = this.session.get("SESSION_CONTEXT");
		boolean check = regManager.checkResponse(savedContext, response);
		if (!check) {
			System.out.println("- [server] authorization failed: failed to check response");
			return null;
		}
		
		// check registration
		String certAddress = response.getCertificate();
		byte[] metadataSerialized = this.authManager.checkRegistration(certAddress);
		if (metadataSerialized == null) {
			System.out.println("- [server] authorization failed: failed to check");
			return null;
		}
		String metadataStr = new String(metadataSerialized);
		System.out.println("- [server] authorization succeed: metadata="+metadataStr);
		return metadataStr;
	}
	
	
	// revoke registration
	public String requestRevocation(String responseSerialized)
			throws JSONException, IOException, CoinStackException {
		
		// unmarshal and check response
		Response response = Response.unmarshal(responseSerialized);
		String savedContext = this.session.get("SESSION_CONTEXT");
		boolean check = regManager.checkResponse(savedContext, response);
		if (!check) {
			System.out.println("- [server] revocation failed: failed to check response");
			return null;
		}
		
		// record revocation
		String certAddress = response.getCertificate();
		String txId = this.regManager.revokeRegistration(certAddress);
		if (txId == null) {
			System.out.println("- [server] revocation failed: failed to record");
			return null;
		}
		System.out.println("- [server] revocation succeed: txId="+txId);
		return txId;
	}
	
}
