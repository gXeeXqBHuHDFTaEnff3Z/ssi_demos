package de.vertedge.ssicapabilitymanager.SSI;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.codec.digest.DigestUtils;

public class SSI_Authority {

    public static String DEFAULT_AUTHORITY = "Deutschland (DE)";

    private long _uid;
    private final String name;
    private final String uri;
    private final String publicKey;
    private final int picture;
    private boolean enabled;

    /** constructor used by dao
     *
     * @param name
     * @param uri
     * @param publicKey
     * @param picture
     */
    public SSI_Authority(String name, String uri, String publicKey, int picture, boolean enabled) {
        this.name = name;
        this.uri = uri;
        this.publicKey = publicKey;
        this.picture = picture;
        this.enabled = enabled;
    }

    public long get_uid() {
        return _uid;
    }

    public void set_uid(long _uid) {
        this._uid = _uid;
    }

    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public int getPicture() {
        return picture;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString(){
        return name;
    }

    /** DEMO STUB authorities don't sign anything on local devices (unless when Self-Signing)
     * and signing is not really done with public keys
     *
     * @param _document document that needs to be signed
     * @return signed document
     */
    public String sign(String _document) {
        String _signature = _document + "\n----SHA256 SIGNATURE----\n"+ publicKey;
        _signature = DigestUtils.sha256Hex(_signature);

        return _signature; // Outputs "SGVsbG8="
    }
}
