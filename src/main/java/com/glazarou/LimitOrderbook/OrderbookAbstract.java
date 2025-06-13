package com.glazarou.LimitOrderbook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
    //functions for testing
    public Limit searchLimitMaps(int limitPrice, Side side){
        Map<Integer, Limit> limitMap = (side == Side.BUY) ? limitBuyMap : limitSellMap;
        if (limitMap.containsKey(limitPrice)){
            return limitMap.get(limitPrice);
        } else {
            return null;
        }
    }

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

    // in-order traversal: Left, Root, Right
    public List<Integer> inOrderTreeTraversal(Limit root) {
        List<Integer> result = new ArrayList<>();
        if (root == null)
            return result;

        result.addAll(inOrderTreeTraversal(root.getLeftChild()));
        result.add(root.getLimitPrice());
        result.addAll(inOrderTreeTraversal(root.getRightChild()));

        return result;
    }

    // pre-order traversal: Root, Left, Right
    public List<Integer> preOrderTreeTraversal(Limit root) {
        List<Integer> result = new ArrayList<>();
        if (root == null)
            return result;

        result.add(root.getLimitPrice());
        result.addAll(preOrderTreeTraversal(root.getLeftChild()));
        result.addAll(preOrderTreeTraversal(root.getRightChild()));

        return result;
    }

    // post-order traversal: Left, Right, Root
    public List<Integer> postOrderTreeTraversal(Limit root) {
        List<Integer> result = new ArrayList<>();
        if (root == null)
            return result;

        result.addAll(postOrderTreeTraversal(root.getLeftChild()));
        result.addAll(postOrderTreeTraversal(root.getRightChild()));
        result.add(root.getLimitPrice());

        return result;
    }
}
