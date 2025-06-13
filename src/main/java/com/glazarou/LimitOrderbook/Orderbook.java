package com.glazarou.LimitOrderbook;

import java.util.Map;

public class Orderbook extends OrderbookAbstract{

    @Override
    public void addLimitOrder(int orderId, Side side, int shares, int limitPrice){
        int newShares = limitOrderAsMarket(orderId, side, shares, limitPrice);
        if (newShares != 0){
            Order newOrder = new Order(orderId, side, newShares, limitPrice);
            orderMap.put(orderId, newOrder);

            Map<Integer, Limit> limitMap = (side == Side.BUY) ? limitBuyMap : limitSellMap;
            if (!limitMap.containsKey(limitPrice)){
                addLimit(limitPrice, side);
            }
            limitMap.get(limitPrice).append(newOrder);
        }
    }


    public int limitOrderAsMarket(int orderId, Side side, int shares, int limitPrice) {
        if (side == Side.BUY) {
            while (lowestSell != null && shares > 0 && lowestSell.getLimitPrice() <= limitPrice) {
                if (shares <= lowestSell.getTotalVolume()) {
                    marketOrderHelper(orderId, side, shares);
                    return 0;
                } else {
                    shares -= lowestSell.getTotalVolume();
                    marketOrderHelper(orderId, side, lowestSell.getTotalVolume());
                }
            }
        } else {
            while (highestBuy != null && shares > 0 && highestBuy.getLimitPrice() >= limitPrice) {
                if (shares <= highestBuy.getTotalVolume()) {
                    marketOrderHelper(orderId, side, shares);
                    return 0;
                } else {
                    shares -= highestBuy.getTotalVolume();
                    marketOrderHelper(orderId, side, highestBuy.getTotalVolume());
                }
            }
        }
        return shares;
    }

    public void marketOrderHelper(int orderId, Side side, int shares) {
        Limit bookEdge = (side == Side.BUY) ? lowestSell : highestBuy;
    
        while (bookEdge != null && bookEdge.getHeadOrder().getShares() <= shares) {
            Order headOrder = bookEdge.getHeadOrder();
            shares -= headOrder.getShares();
            headOrder.execute();
    
            if (bookEdge.getSize() == 0) {
                deleteLimit(bookEdge);
            }

            removeFromOrderMap(headOrder.getId());
            executedOrdersCount++;
            bookEdge = (side == Side.BUY) ? lowestSell : highestBuy;
        }
    
        // if there are remaining shares, partially fill next order
        if (bookEdge != null && shares > 0) {
            bookEdge.getHeadOrder().partiallyFillOrder(shares);
            executedOrdersCount++;
        }
    }

    @Override
    public void deleteLimit(Limit limit) {
        updateBookEdgeRemove(limit);
        removeFromLimitMap(limit.getLimitPrice(), limit.getSide());
        removeNode(limit);
        changeBookRoots(limit);

        Limit parent = limit.getParent();
        int limitPrice = limit.getLimitPrice();
        
        // rebalance tree
        while (parent != null) {
            parent = balance(parent);
            if (parent.getParent() != null) {
                if (parent.getParent().getLimitPrice() > limitPrice) {
                    parent.getParent().setLeftChild(parent);
                } else {
                    parent.getParent().setRightChild(parent);
                }
            }
            parent = parent.getParent();
        }
    }

    // https://en.wikipedia.org/wiki/Binary_search_tree#Deletion
    private void removeNode(Limit limit) {    
        // Case 1: Node has no children
        if (limit.getLeftChild() == null && limit.getRightChild() == null) {
            shiftNodes(limit, null);
        }
        // Case 2: Node has one child
        else if (limit.getLeftChild() == null || limit.getRightChild() == null) {
            //System.out.println("Tree has one child");
            Limit child = (limit.getLeftChild() != null) ? limit.getLeftChild() : limit.getRightChild();
            shiftNodes(limit, child);
        }
        // Case 3: Node has two children
        else {
            //System.out.println("Tree has two children");
            Limit successor = limit.getRightChild();
            while (successor.getLeftChild() != null) {
                successor = successor.getLeftChild();
            }
    
            if (successor.getParent() != limit) {
                shiftNodes(successor, successor.getRightChild());
                successor.setRightChild(limit.getRightChild());
                if (successor.getRightChild() != null) {
                    successor.getRightChild().setParent(successor);
                }
            }

            shiftNodes(limit, successor);
            successor.setLeftChild(limit.getLeftChild());
            if (successor.getLeftChild() != null) {
                successor.getLeftChild().setParent(successor);
            }
        }
    }

    private void shiftNodes(Limit nodeToRemove, Limit newNode) {
        if (nodeToRemove.getParent() == null) {
            updateBookEdgeRemove(nodeToRemove);
        } else if (nodeToRemove == nodeToRemove.getParent().getLeftChild()) {
            nodeToRemove.getParent().setLeftChild(newNode);
        } else {
            nodeToRemove.getParent().setRightChild(newNode);
        }
    
        if (newNode != null) {
            newNode.setParent(nodeToRemove.getParent());
        }
    }    

    public void updateBookEdgeRemove(Limit limit) {
        Limit bookEdge = (limit.getSide() == Side.BUY) ? highestBuy : lowestSell;
        Limit tree = (limit.getSide() == Side.BUY) ? limitBuyTree : limitSellTree;
        if (limit.equals(bookEdge)) {
            if (!bookEdge.equals(tree)) {
                if (limit.getSide() == Side.BUY && bookEdge.getLeftChild() != null) {
                    bookEdge = bookEdge.getLeftChild();
                } else if (limit.getSide() == Side.SELL && bookEdge.getRightChild() != null) {
                    bookEdge = bookEdge.getRightChild();
                } else {
                    bookEdge = bookEdge.getParent();
                }
            } else {
                if (limit.getSide() == Side.BUY && bookEdge.getLeftChild() != null) {
                    bookEdge = bookEdge.getLeftChild();
                } else if (limit.getSide() == Side.SELL && bookEdge.getRightChild() != null) {
                    bookEdge = bookEdge.getRightChild();
                } else {
                    bookEdge = null;
                }
            }
    
            if (limit.getSide() == Side.BUY) {
                highestBuy = bookEdge;
            } else {
                lowestSell = bookEdge;
            }
        }
    }

    public void changeBookRoots(Limit limit) {
        Limit tree = (limit.getSide() == Side.BUY) ? limitBuyTree : limitSellTree;
        if (limit.equals(tree)) {
            if (limit.getRightChild() != null) {
                tree = limit.getRightChild();
                while (tree.getLeftChild() != null) {
                    tree = tree.getLeftChild();
                }
            } else {
                tree = limit.getLeftChild();
            }
            
            if (limit.getSide() == Side.BUY) {
                limitBuyTree = tree;
            } else {
                limitSellTree = tree;
            }
        }
    }

    @Override
    public void addLimit(int price, Side side){
        Map<Integer, Limit> limitMap = (side == Side.BUY) ? limitBuyMap : limitSellMap;
        Limit newLimit = new Limit(price, side);
        limitMap.put(price, newLimit);
        if (side == Side.BUY) {
            if (limitBuyTree == null) {
                limitBuyTree = newLimit;
                highestBuy = newLimit;
            } else {
                limitBuyTree = insert(limitBuyTree, newLimit);
                updateBookEdgeInsert(newLimit);
            }
        } else {
            if (limitSellTree == null) {
                limitSellTree = newLimit;
                lowestSell = newLimit;
            } else {
                limitSellTree = insert(limitSellTree, newLimit);
                updateBookEdgeInsert(newLimit);
            }
        }
    }

    public void updateBookEdgeInsert(Limit newLimit) {
        if (newLimit.getSide() == Side.BUY) {
            if (newLimit.getLimitPrice() > highestBuy.getLimitPrice()) {
                highestBuy = newLimit;
            }
        } else {
            if (newLimit.getLimitPrice() < lowestSell.getLimitPrice()) {
                lowestSell = newLimit;
            }
        }
    }

    // execute a market order
    @Override
    public void marketOrder(int orderId, Side side, int shares) {
        executedOrdersCount = 0;
        AVLBalanceCount = 0;
        marketOrderHelper(orderId, side, shares);
    }

    @Override
    public void cancelLimitOrder(int orderId) {
        executedOrdersCount = 0;
        AVLBalanceCount = 0;
        Order order = orderMap.get(orderId);
        if (order != null){
            order.cancel();
            if (order.getParentLimit().getSize() == 0){
                deleteLimit(order.getParentLimit());
            }
            orderMap.remove(orderId);
        }
    }
    
    @Override
    public Limit insert(Limit root, Limit limit, Limit parent){
        if (root == null) {
            limit.setParent(parent);
            return limit;
        }
        if (limit.getLimitPrice() < root.getLimitPrice()) {
            root.setLeftChild(insert(root.getLeftChild(), limit, root));
        } else{
            root.setRightChild(insert(root.getRightChild(), limit, root));
        }
        root = balance(root);
        return root;
    }

    public Limit insert(Limit root, Limit newLimit){
        return insert(root, newLimit, null);
    }

    // AVL Tree operations
    public int getLimitHeight(Limit limit){
        if (limit == null){
            return 0;
        } else{
            int leftHeight = getLimitHeight(limit.getLeftChild());
            int rightHeight = getLimitHeight(limit.getRightChild());
            return Math.max(leftHeight, rightHeight)+1;
        }
    }

    public int limitHeightDifference(Limit limit){
        int leftHeight = getLimitHeight(limit.getLeftChild());
        int rightHeight = getLimitHeight(limit.getRightChild());
        return leftHeight-rightHeight;
    }

    public Limit balance(Limit limit){
        int balanceFactor = limitHeightDifference(limit);
        if (balanceFactor>1) {
            if (limitHeightDifference(limit.getLeftChild())>=0){
                limit = ll_rotate(limit);
            } else{
                limit = lr_rotate(limit);
            }
            AVLBalanceCount += 1;
        } else if (balanceFactor < -1){
            if (limitHeightDifference(limit.getRightChild())>0){
                limit = rl_rotate(limit);
            } else{
                limit = rr_rotate(limit);    
            }
            AVLBalanceCount += 1;
        }
        return limit;
    }

    public Limit ll_rotate(Limit parent){
        Limit newParent = parent.getLeftChild();
        parent.setLeftChild(newParent.getRightChild());

        if (newParent.getRightChild() != null){
            newParent.getRightChild().setParent(parent);
        }
        newParent.setRightChild(parent);
        if (parent.getParent() != null) {
            newParent.setParent(parent.getParent());
        } else {
            newParent.setParent(null);
            if (parent.getSide() == Side.BUY) {
                limitBuyTree = newParent;
            } else {
                limitSellTree = newParent;
            }
        }
        parent.setParent(newParent);
        return newParent;
    }

    public Limit rr_rotate(Limit parent){
        Limit newParent = parent.getRightChild();
        parent.setRightChild(newParent.getLeftChild());

        if (newParent.getLeftChild() != null){
            newParent.getLeftChild().setParent(parent);
        }
        newParent.setLeftChild(parent);
        if (parent.getParent() != null) {
            newParent.setParent(parent.getParent());
        } else {
            newParent.setParent(null);
            if (parent.getSide() == Side.BUY) {
                limitBuyTree = newParent;
            } else {
                limitSellTree = newParent;
            }
        }
        parent.setParent(newParent);
        return newParent;
    }

    public Limit lr_rotate(Limit parent){
        Limit newParent = parent.getLeftChild();
        parent.setLeftChild(rr_rotate(newParent));
        return ll_rotate(parent);
    }

    public Limit rl_rotate(Limit parent){
        Limit newParent = parent.getRightChild();
        parent.setRightChild(ll_rotate(newParent));
        return rr_rotate(parent);
    }

    

}
