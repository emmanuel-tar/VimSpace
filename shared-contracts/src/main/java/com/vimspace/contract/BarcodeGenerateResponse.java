package com.vimspace.contract;

public class BarcodeGenerateResponse {
    private String barcode;
    private String format;

    public BarcodeGenerateResponse() {}

    public BarcodeGenerateResponse(String barcode, String format) {
        this.barcode = barcode;
        this.format = format;
    }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
}

