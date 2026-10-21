package com.vimspace.contract;

public class BarcodeGenerateRequest {
    private String format; // "EAN13", "CODE128", "UPC_A"
    private String prefix;

    public BarcodeGenerateRequest() {}

    public BarcodeGenerateRequest(String format, String prefix) {
        this.format = format;
        this.prefix = prefix;
    }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }
}

