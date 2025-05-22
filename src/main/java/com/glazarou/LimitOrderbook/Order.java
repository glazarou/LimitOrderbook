package com.glazarou.LimitOrderbook;

public class Order {
    int id;
    int shares;
    int limit;
    Limit parentLimit;

    Order nextOrder;
    Order prevOrder;
    Side side;

    public Order(int id, Side side, int shares, int limit){
        this.id = id;
        this.shares = shares;
        this.limit = limit;
        this.side = side;
    }

    //getters and setters
    public int getId(){ return this.id; }
    public int getShares(){ return this.shares; }
    public void setShares(int shares){this.shares= shares; }
    public void setParentLimit(Limit parentLimit){ this.parentLimit = parentLimit; }
    public Limit getParentLimit(){ return this.parentLimit; }
    public void setNext(Order nextOrder){ this.nextOrder = nextOrder; }
    public Order getNext(){return this.nextOrder;}
    public void setPrev(Order prevOrder){this.prevOrder = prevOrder; }
    public Order getPrev(){ return this.prevOrder;}

    public void cancel(){
        if (prevOrder == null) {
            parentLimit.setHeadOrder(nextOrder);
        } else{
            prevOrder.setNext(nextOrder);
        }

        if (nextOrder == null){
            parentLimit.setTailOrder(nextOrder);
        } else {
            nextOrder.setPrev(prevOrder);
        }
        parentLimit.size -= 1;
        parentLimit.setTotalVolume(parentLimit.getTotalVolume() - this.shares);
    }

    // execute head order
    public void execute(){
        parentLimit.setHeadOrder(nextOrder);
        if (nextOrder == null){
            parentLimit.setTailOrder(null);
        } else{
            nextOrder.setPrev(null);
        }
        nextOrder = null;
        prevOrder = null;
        parentLimit.totalVolume -= shares;
        parentLimit.size -= 1;
    }

    public void partiallyFillOrder(int orderedShares){
        shares -= orderedShares;
        parentLimit.partiallyFillTotalVolume(orderedShares);
    }
}
