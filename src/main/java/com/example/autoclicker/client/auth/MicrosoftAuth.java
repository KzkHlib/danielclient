package com.example.autoclicker.client.auth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;

/**
 * Authentification Microsoft légitime (device code flow) pour les comptes de
 * l'utilisateur. Aucun crack : l'utilisateur valide sur microsoft.com/link.
 *
 * IMPORTANT : renseigne {@link #AZURE_CLIENT_ID} avec l'ID d'une application
 * Azure (compte personnel, "public client", device code activé). Création
 * gratuite sur https://portal.azure.com (Azure AD > App registrations).
 */
public final class MicrosoftAuth {

    private MicrosoftAuth() {}

    /** À REMPLACER par ton ID d'application Azure. */
    public static final String AZURE_CLIENT_ID = "YOUR_AZURE_CLIENT_ID";

    private static final String DEVICE_CODE_URL =
            "https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode";
    private static final String TOKEN_URL =
            "https://login.microsoftonline.com/consumers/oauth2/v2.0/token";
    private static final String SCOPE = "XboxLive.signin offline_access";

    public static boolean configured() {
        return AZURE_CLIENT_ID != null && !AZURE_CLIENT_ID.equals("YOUR_AZURE_CLIENT_ID");
    }

    /** Retour d'étape vers la GUI. */
    public interface Listener {
        void onCode(String userCode, String verificationUri);
        void onStatus(String message);
    }

    /** Réponse initiale device code. */
    public static final class DeviceCode {
        public String deviceCode, userCode, verificationUri;
        public int interval, expiresIn;
    }

    // ====================== étapes ======================

    /** Étape 1 : demande un device code à Microsoft. */
    public static DeviceCode requestDeviceCode() throws Exception {
        String body = "client_id=" + enc(AZURE_CLIENT_ID) + "&scope=" + enc(SCOPE);
        JsonObject o = postForm(DEVICE_CODE_URL, body);
        DeviceCode dc = new DeviceCode();
        dc.deviceCode = o.get("device_code").getAsString();
        dc.userCode = o.get("user_code").getAsString();
        dc.verificationUri = o.get("verification_uri").getAsString();
        dc.interval = o.has("interval") ? o.get("interval").getAsInt() : 5;
        dc.expiresIn = o.has("expires_in") ? o.get("expires_in").getAsInt() : 900;
        return dc;
    }

    /**
     * Flux complet : poll du token, puis Xbox Live -> XSTS -> Minecraft -> profil.
     * Bloquant : à lancer dans un thread. Retourne le compte ou jette une exception.
     */
    public static Account authenticate(DeviceCode dc, Listener l) throws Exception {
        l.onStatus("En attente de validation...");
        JsonObject token = pollToken(dc, l);
        String msAccess = token.get("access_token").getAsString();
        String refresh = token.has("refresh_token") ? token.get("refresh_token").getAsString() : null;

        l.onStatus("Xbox Live...");
        JsonObject xbl = xblAuth(msAccess);
        String xblToken = xbl.get("Token").getAsString();
        String uhs = xbl.getAsJsonObject("DisplayClaims")
                .getAsJsonArray("xui").get(0).getAsJsonObject().get("uhs").getAsString();

        l.onStatus("XSTS...");
        JsonObject xsts = xstsAuth(xblToken);
        String xstsToken = xsts.get("Token").getAsString();

        l.onStatus("Minecraft...");
        String mcToken = minecraftLogin(uhs, xstsToken);

        l.onStatus("Profil...");
        JsonObject profile = minecraftProfile(mcToken);
        String name = profile.get("name").getAsString();
        String uuid = profile.get("id").getAsString();

        l.onStatus("Connecté : " + name);
        return new Account(name, uuid, mcToken, refresh);
    }

    // ====================== sous-étapes ======================

    private static JsonObject pollToken(DeviceCode dc, Listener l) throws Exception {
        long deadline = System.currentTimeMillis() + dc.expiresIn * 1000L;
        int interval = Math.max(2, dc.interval);
        while (System.currentTimeMillis() < deadline) {
            Thread.sleep(interval * 1000L);
            String body = "grant_type=urn:ietf:params:oauth:grant-type:device_code"
                    + "&client_id=" + enc(AZURE_CLIENT_ID)
                    + "&device_code=" + enc(dc.deviceCode);
            JsonObject o = postFormAllowError(TOKEN_URL, body);
            if (o.has("access_token")) return o;
            String err = o.has("error") ? o.get("error").getAsString() : "";
            if (err.equals("authorization_pending")) continue;
            if (err.equals("slow_down")) { interval += 2; continue; }
            if (err.equals("authorization_declined")) throw new Exception("Connexion refusée.");
            if (err.equals("expired_token")) throw new Exception("Code expiré, recommence.");
            // autre erreur
            throw new Exception("Erreur OAuth: " + err);
        }
        throw new Exception("Délai dépassé.");
    }

    private static JsonObject xblAuth(String msAccessToken) throws Exception {
        String json = "{\"Properties\":{\"AuthMethod\":\"RPS\",\"SiteName\":\"user.auth.xboxlive.com\","
                + "\"RpsTicket\":\"d=" + msAccessToken + "\"},"
                + "\"RelyingParty\":\"http://auth.xboxlive.com\",\"TokenType\":\"JWT\"}";
        return postJson("https://user.auth.xboxlive.com/user/authenticate", json);
    }

    private static JsonObject xstsAuth(String xblToken) throws Exception {
        String json = "{\"Properties\":{\"SandboxId\":\"RETAIL\",\"UserTokens\":[\"" + xblToken + "\"]},"
                + "\"RelyingParty\":\"rp://api.minecraftservices.com/\",\"TokenType\":\"JWT\"}";
        return postJson("https://xsts.auth.xboxlive.com/xsts/authorize", json);
    }

    private static String minecraftLogin(String uhs, String xstsToken) throws Exception {
        String json = "{\"identityToken\":\"XBL3.0 x=" + uhs + ";" + xstsToken + "\"}";
        JsonObject o = postJson("https://api.minecraftservices.com/authentication/login_with_xbox", json);
        return o.get("access_token").getAsString();
    }

    private static JsonObject minecraftProfile(String mcToken) throws Exception {
        HttpURLConnection c = open("https://api.minecraftservices.com/minecraft/profile", "GET");
        c.setRequestProperty("Authorization", "Bearer " + mcToken);
        JsonObject o = readJson(c);
        if (!o.has("id")) throw new Exception("Ce compte ne possède pas Minecraft.");
        return o;
    }

    // ====================== HTTP ======================

    private static HttpURLConnection open(String url, String method) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setRequestMethod(method);
        c.setConnectTimeout(15000);
        c.setReadTimeout(15000);
        c.setRequestProperty("Accept", "application/json");
        return c;
    }

    private static JsonObject postForm(String url, String body) throws Exception {
        return writeRead(open(url, "POST"), "application/x-www-form-urlencoded", body, false);
    }

    private static JsonObject postFormAllowError(String url, String body) throws Exception {
        return writeRead(open(url, "POST"), "application/x-www-form-urlencoded", body, true);
    }

    private static JsonObject postJson(String url, String body) throws Exception {
        return writeRead(open(url, "POST"), "application/json", body, false);
    }

    private static JsonObject writeRead(HttpURLConnection c, String contentType,
                                        String body, boolean allowError) throws Exception {
        c.setDoOutput(true);
        c.setRequestProperty("Content-Type", contentType);
        DataOutputStream out = new DataOutputStream(c.getOutputStream());
        out.write(body.getBytes(Charset.forName("UTF-8")));
        out.flush();
        out.close();
        return readJson(c, allowError);
    }

    private static JsonObject readJson(HttpURLConnection c) throws Exception {
        return readJson(c, false);
    }

    private static JsonObject readJson(HttpURLConnection c, boolean allowError) throws Exception {
        int code = c.getResponseCode();
        InputStream is = (code >= 200 && code < 300) ? c.getInputStream() : c.getErrorStream();
        if (is == null) throw new Exception("Réponse vide (HTTP " + code + ")");
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();
        if (code >= 400 && !allowError) {
            throw new Exception("HTTP " + code + ": " + sb);
        }
        return new JsonParser().parse(sb.toString()).getAsJsonObject();
    }

    private static String enc(String s) throws Exception {
        return URLEncoder.encode(s, "UTF-8");
    }
}
