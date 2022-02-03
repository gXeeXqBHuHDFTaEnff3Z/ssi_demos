package de.vertedge.ssicapabilitymanager.SSI;

import org.apache.commons.codec.digest.DigestUtils;

public class SSI_Proof {

    public static final String DEFAULT_PROOF_TYPE = "sha256";

    private final String _type;
    private final String _signature;
    private final String _authority;

    public SSI_Proof(String _type, String _signature, String _authority) {
        this._type = _type;
        this._signature = _signature;
        this._authority = _authority;
    }

    public String get_type() {
        return _type;
    }

    public String get_signature() {
        return _signature;
    }

    public String get_authority() {
        return _authority;
    }

    /** DEMO we are just checking predetermined SHA256s
     *  TRUE IFF the given claim is valid according to this proof
     *  it is valid if the signature matches what the authority tells us it should
     *
     * @param claim the claim to validate
     * @return
     */
    public boolean validates(String claim){
        String verification = claim + "\n----SHA256 SIGNATURE----\n" + _authority;
        verification = DigestUtils.sha256Hex(verification);
        return (_signature.equals(verification));
    }

    @Override
    public String toString(){
        return _signature + "," + _authority;
    }
}
