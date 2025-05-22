package com.glazarou.LimitOrderbook;

public class Limit extends LimitAbstract {

    public Limit(int limitPrice, Side side, int size, int totalVolume){
        super(limitPrice, side, size, totalVolume);
    }

    public Limit(int limitPrice, Side side) {
        super(limitPrice, side, 0, 0);
    }

    @Override
    public void print(){
        System.out.println(".()");
    }

    @Override
    public void partiallyFillTotalVolume(int shares){
        totalVolume -= shares;
    }

    // append an order at the end of this limit level
    @Override
    public void append(Order order){
        if (headOrder == null){
            headOrder = order;
            tailOrder = order;
        } else {
            order.setNext(null);
            tailOrder.setNext(order);
            order.setPrev(tailOrder);
            tailOrder = order;
        }
        size += 1;
        totalVolume += order.getShares();
        order.setParentLimit(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Limit limit = (Limit) obj;
        return this.limitPrice == limit.limitPrice && this.side == limit.side;
    }

}
