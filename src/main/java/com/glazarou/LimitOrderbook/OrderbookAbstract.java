package com.glazarou.LimitOrderbook;

import java.util.HashMap;

public abstract class OrderbookAbstract {
    protected HashMap<Integer, Limit> limitBuyMap;
    protected HashMap<Integer, Limit> limitSellMap;
    protected HashMap<Integer, Order> orderMap;
    
    protected Limit limitBuyTree;
    protected Limit limitSellTree;
    protected Limit lowestSell;
    protected Limit highestBuy;
    protected int AVLBalanceCount;
    protected int executedOrdersCount;

    public OrderbookAbstract(){
        this.limitBuyMap = new HashMap<>();
        this.limitSellMap = new HashMap<>();
        this.orderMap = new HashMap<>();
        this.limitBuyTree = null;
        this.limitSellTree = null;
        this.lowestSell = null;
        this.highestBuy = null;
        this.AVLBalanceCount = 0;
        this.executedOrdersCount = 0;
    }

    public abstract void addLimit(int price, Side side);
    public abstract void deleteLimit(Limit limit);
    public abstract void marketOrder(int orderId, Side side, int shares);
    public abstract void addLimitOrder(int orderId, Side side, int shares, int limitPrice);
    public abstract void cancelLimitOrder(int orderId);
    public abstract Limit insert(Limit root, Limit limit, Limit parent);
    
    public void removeFromOrderMap(int orderId) { orderMap.remove(orderId); }
    public void removeFromLimitMap(int limitPrice, Side side) {
        if (side == Side.BUY){
            limitBuyMap.remove(limitPrice);
        } else{
            limitSellMap.remove(limitPrice);
        }
    }

    // Getters
    public HashMap<Integer, Limit> getLimitBuyMap() { return limitBuyMap; }
    public HashMap<Integer, Limit> getLimitSellMap() { return limitSellMap; }
    public HashMap<Integer, Order> getOrderMap() { return orderMap; }
    public Limit getLimitBuyTree() { return limitBuyTree; }
    public Limit getLimitSellTree() { return limitSellTree; }
    public Limit getLowestSell() { return lowestSell; }
    public Limit getHighestBuy() { return highestBuy; }


    //print methods for visualizing
    public void printOrderBook() {
        System.out.println("Buy Orders (Descending Order):");
        printBuyTree(limitBuyTree);
        
        System.out.println("Sell Orders (Ascending Order):");
        printSellTree(limitSellTree);
    }

    private void printBuyTree(Limit root) {
        if (root == null) return;
        printBuyTree(root.getRightChild());
        System.out.println("Limit Price: " + root.getLimitPrice() + ", Total Volume: " + root.getTotalVolume());
        printBuyTree(root.getLeftChild());
    }

    private void printSellTree(Limit root) {
        if (root == null) return;
        printSellTree(root.getLeftChild());
        System.out.println("Limit Price: " + root.getLimitPrice() + ", Total Volume: " + root.getTotalVolume());
        printSellTree(root.getRightChild());
    }

    public void printOrderBookTree() {
        System.out.println("Buy Orders Tree:");
        printTreeWithBranches(limitBuyTree, "", true);
    
        System.out.println("\nSell Orders Tree:");
        printTreeWithBranches(limitSellTree, "", true);
    }
    
    private void printTreeWithBranches(Limit node, String prefix, boolean isRight) {
        if (node == null) return;
        printTreeWithBranches(node.getRightChild(), prefix + (isRight ? "    " : "│   "), true);
        System.out.println(prefix + (isRight ? "┌── " : "└── ") + node.getLimitPrice());
        printTreeWithBranches(node.getLeftChild(), prefix + (isRight ? "│   " : "    "), false);
    }
}
