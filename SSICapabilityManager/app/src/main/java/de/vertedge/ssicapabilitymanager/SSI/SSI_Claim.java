package de.vertedge.ssicapabilitymanager.SSI;

public class SSI_Claim{
    private final String _subject;
    private final String _property;
    private final String _value;

    public SSI_Claim(String _subject, String _property, String _value) {
        this._subject = _subject;
        this._property = _property;
        this._value = _value;
    }

    public String get_subject() {
        return _subject;
    }

    public String get_property() {
        return _property;
    }

    public String get_value() {
        return _value;
    }

    @Override
    public String toString(){
        return _subject + " " + _property + " " + _value;
    }
}
