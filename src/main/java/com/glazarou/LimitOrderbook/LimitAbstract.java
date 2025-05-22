package com.glazarou.LimitOrderbook;

public abstract class LimitAbstract {
    
    protected int limitPrice;
    protected Side side;
    protected int size;
    protected int totalVolume;

    // reference to parent to get next best bid/ask in O(1)
    protected Limit parent;
    protected Limit leftChild;
    protected Limit rightChild;

    protected Order headOrder;
    protected Order tailOrder;

    public LimitAbstract(int limitPrice, Side side, int size, int totalVolume) {
        this.limitPrice = limitPrice;
        this.side = side;
        this.size = size;
        this.totalVolume = totalVolume;
    }

    public abstract void append(Order order);
    public abstract void partiallyFillTotalVolume(int orderedShares);
    public abstract void print();

    // getters and setters
    public int getLimitPrice() { return limitPrice; }
    public Side getSide(){ return side; }
    public int getSize() { return size; }
    public int getTotalVolume() { return totalVolume; }
    public void setTotalVolume(int volume) { this.totalVolume = volume; }
    public Order getHeadOrder() { return headOrder; }
    public Order getTaiOrder() { return headOrder; }
    public void setHeadOrder(Order order) { this.headOrder = order; }
    public void setTailOrder(Order order) { this.tailOrder = order; }

    public Limit getParent() { return parent; }
    public void setParent(Limit parent) { this.parent = parent; }
    public Limit getLeftChild() { return leftChild; }
    public void setLeftChild(Limit leftChild) { this.leftChild = leftChild; }
    public Limit getRightChild() { return rightChild; }
    public void setRightChild(Limit rightChild) { this.rightChild = rightChild; }
}