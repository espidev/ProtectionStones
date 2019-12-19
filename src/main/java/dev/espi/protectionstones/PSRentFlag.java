package dev.espi.protectionstones;

public enum PSRentFlag {
    TENANT_IS_OWNER("false"),
    LANDLORD_IS_OWNER("false");

    String val;

    PSRentFlag(String val) {
        this.val = val;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }


}
