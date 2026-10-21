package com.vimspace.contract;

import java.util.List;

public class ReceiveGoodsRequest {
    private Long poId;
    private List<ReceiveItem> items;

    public ReceiveGoodsRequest() {}

    public ReceiveGoodsRequest(Long poId, List<ReceiveItem> items) {
        this.poId = poId;
        this.items = items;
    }

    public Long getPoId() { return poId; }
    public void setPoId(Long poId) { this.poId = poId; }
    public List<ReceiveItem> getItems() { return items; }
    public void setItems(List<ReceiveItem> items) { this.items = items; }

    public static class ReceiveItem {
        private String sku;
        private Integer quantityReceived;

        public ReceiveItem() {}

        public ReceiveItem(String sku, Integer quantityReceived) {
            this.sku = sku;
            this.quantityReceived = quantityReceived;
        }

        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public Integer getQuantityReceived() { return quantityReceived; }
        public void setQuantityReceived(Integer quantityReceived) { this.quantityReceived = quantityReceived; }
    }
}