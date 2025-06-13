package com.glazarou;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.glazarou.LimitOrderbook.Limit;
import com.glazarou.LimitOrderbook.Orderbook;
import com.glazarou.LimitOrderbook.Side;

public class OrderbookTest {
    private Orderbook orderBook;

    @BeforeEach
    public void setup() {
        orderBook = new Orderbook();
    }

    // order insertion tests
    @Test
    public void testInsertSingleBuyOrder() {
        orderBook.addLimitOrder(0, Side.BUY, 100, 100);
        assertEquals(100, orderBook.getLimitBuyTree().getTotalVolume());
        assertEquals(100, orderBook.getHighestBuy().getLimitPrice());
    }

    @Test
    public void testInsertMultipleBuyOrder() {
        orderBook.addLimitOrder(0, Side.BUY, 100, 80);
        orderBook.addLimitOrder(1, Side.BUY, 100, 90);
        orderBook.addLimitOrder(2, Side.BUY, 100, 100);
        assertEquals(100, orderBook.getLimitBuyTree().getTotalVolume());
        assertEquals(100, orderBook.getHighestBuy().getLimitPrice());
    }

    @Test
    public void testInsertSingleSellOrder() {
        orderBook.addLimitOrder(0, Side.SELL, 100, 100);
        assertEquals(100, orderBook.getLimitSellTree().getTotalVolume());
        assertEquals(100, orderBook.getLowestSell().getLimitPrice());
    }

    @Test
    public void testInsertMultipleSellOrder() {
        orderBook.addLimitOrder(2, Side.SELL, 100, 100);
        orderBook.addLimitOrder(0, Side.SELL, 100, 80);
        orderBook.addLimitOrder(1, Side.SELL, 100, 90);
        assertEquals(100, orderBook.getLimitSellTree().getTotalVolume());
        assertEquals(80, orderBook.getLowestSell().getLimitPrice());
    }

    // cancel orders
    @Test
    public void testCancelOrderLeavingNonEmptyLimit() {
        orderBook.addLimitOrder(5, Side.BUY, 80, 20);
        orderBook.addLimitOrder(6, Side.BUY, 32, 20);
        orderBook.addLimitOrder(7, Side.BUY, 111, 20);
        Limit limit = orderBook.searchLimitMaps(20, Side.BUY);

        assertEquals(3, limit.getSize());
        assertEquals(223, limit.getTotalVolume());

        orderBook.cancelLimitOrder(6);

        assertEquals(2, limit.getSize());
        assertEquals(191, limit.getTotalVolume());

        orderBook.cancelLimitOrder(7);

        assertEquals(1, limit.getSize());
        assertEquals(80, limit.getTotalVolume());
    }

    @Test
    public void testLimitHeadOrderChangeOnOrderCancel() {
        orderBook.addLimitOrder(5, Side.BUY, 80, 20);
        orderBook.addLimitOrder(6, Side.BUY, 32, 20);
        orderBook.addLimitOrder(7, Side.BUY, 111, 20);
        Limit limit = orderBook.searchLimitMaps(20, Side.BUY);

        assertEquals(5, limit.getHeadOrder().getId());

        orderBook.cancelLimitOrder(5);

        assertEquals(6, limit.getHeadOrder().getId());
    }

    @Test
    public void testLimitHeadOrderChangeOnOrderCancelLeavingEmptyLimit() {
        orderBook.addLimitOrder(5, Side.BUY, 80, 20);
        Limit limit = orderBook.searchLimitMaps(20, Side.BUY);

        assertEquals(5, limit.getHeadOrder().getId());

        orderBook.cancelLimitOrder(5);

        assertNull(limit.getHeadOrder());
    }

    @Test
    public void testCancelOrderLeavingEmptyLimit() {
        orderBook.addLimitOrder(5, Side.BUY, 80, 20);
        orderBook.addLimitOrder(6, Side.BUY, 80, 15);
        Limit limit1 = orderBook.searchLimitMaps(20, Side.BUY);
        Limit limit2 = orderBook.searchLimitMaps(15, Side.BUY);

        assertEquals(6, limit2.getHeadOrder().getId());
        assertEquals(15, limit1.getLeftChild().getLimitPrice());

        orderBook.cancelLimitOrder(6);

        assertNull(orderBook.searchLimitMaps(15, Side.BUY));
        assertNull(limit1.getLeftChild());
    }

    // updating orderBook edges
    @Test
    public void testUpdateBookEdgeOnInsertLowestSell() {
        orderBook.addLimitOrder(111, Side.SELL, 43, 80);
        orderBook.addLimitOrder(112, Side.SELL, 46, 78);
        assertEquals(78, orderBook.getLowestSell().getLimitPrice());

        orderBook.addLimitOrder(113, Side.SELL, 46, 77);
        assertEquals(77, orderBook.getLowestSell().getLimitPrice());

        orderBook.addLimitOrder(114, Side.SELL, 46, 85);
        assertEquals(77, orderBook.getLowestSell().getLimitPrice());
    }

    @Test
    public void testUpdateBookEdgeOnInsertHighestBuy() {
        orderBook.addLimitOrder(111, Side.BUY, 43, 80);
        orderBook.addLimitOrder(112, Side.BUY, 46, 78);
        assertEquals(80, orderBook.getHighestBuy().getLimitPrice());

        orderBook.addLimitOrder(113, Side.BUY, 46, 82);
        assertEquals(82, orderBook.getHighestBuy().getLimitPrice());

        orderBook.addLimitOrder(114, Side.BUY, 46, 70);
        assertEquals(82, orderBook.getHighestBuy().getLimitPrice());
    }

    @Test
    public void testUpdateBookEdgeOnDeleteLowestSell() {
        orderBook.addLimitOrder(111, Side.SELL, 43, 80);
        orderBook.addLimitOrder(112, Side.SELL, 46, 78);
        orderBook.addLimitOrder(113, Side.SELL, 46, 77);
        assertEquals(77, orderBook.getLowestSell().getLimitPrice());

        orderBook.cancelLimitOrder(113);
        assertEquals(78, orderBook.getLowestSell().getLimitPrice());
    }

    @Test
    public void testUpdateBookEdgeOnDeleteHighestBuy() {
        orderBook.addLimitOrder(111, Side.BUY, 43, 80);
        orderBook.addLimitOrder(112, Side.BUY, 46, 78);
        orderBook.addLimitOrder(113, Side.BUY, 46, 82);
        assertEquals(82, orderBook.getHighestBuy().getLimitPrice());

        orderBook.cancelLimitOrder(113);
        assertEquals(80, orderBook.getHighestBuy().getLimitPrice());
    }

    @Test
    public void testUpdateBookEdgeOnDeleteHighestBuyEmptyTree() {
        orderBook.addLimitOrder(111, Side.BUY, 43, 80);
        assertEquals(80, orderBook.getHighestBuy().getLimitPrice());

        orderBook.cancelLimitOrder(111);
        assertNull(orderBook.getHighestBuy());
    }

    @Test
    public void testUpdateBookEdgeOnDeleteLowestSellEmptyTree() {
        orderBook.addLimitOrder(111, Side.SELL, 43, 80);
        assertEquals(80, orderBook.getLowestSell().getLimitPrice());

        orderBook.cancelLimitOrder(111);
        assertNull(orderBook.getLowestSell());
    }

    @Test
    public void testUpdateBookEdgeOnDeleteHighestBuyRootLimit() {
        orderBook.addLimitOrder(111, Side.BUY, 43, 80);
        orderBook.addLimitOrder(112, Side.BUY, 43, 75);
        assertEquals(80, orderBook.getHighestBuy().getLimitPrice());

        orderBook.cancelLimitOrder(111);
        assertEquals(75, orderBook.getHighestBuy().getLimitPrice());
    }

    @Test
    public void testUpdateBookEdgeOnDeleteLowestSellRootLimit() {
        orderBook.addLimitOrder(111, Side.SELL, 10, 80);
        orderBook.addLimitOrder(112, Side.SELL, 20, 85);
        assertEquals(80, orderBook.getLowestSell().getLimitPrice());

        orderBook.cancelLimitOrder(111);
        assertEquals(85, orderBook.getLowestSell().getLimitPrice());
    }

    @Test
    public void testUpdateBookEdgeOnDeleteHighestBuyNotParent() {
        orderBook.addLimitOrder(111, Side.BUY, 43, 80);
        orderBook.addLimitOrder(112, Side.BUY, 43, 75);
        orderBook.addLimitOrder(113, Side.BUY, 43, 85);
        orderBook.addLimitOrder(114, Side.BUY, 43, 82);
        assertEquals(85, orderBook.getHighestBuy().getLimitPrice());

        orderBook.cancelLimitOrder(113);
        assertEquals(82, orderBook.getHighestBuy().getLimitPrice());
    }

    @Test
    public void testUpdateBookEdgeOnDeleteLowestSellNotParent() {
        orderBook.addLimitOrder(111, Side.SELL, 43, 80);
        orderBook.addLimitOrder(112, Side.SELL, 43, 75);
        orderBook.addLimitOrder(113, Side.SELL, 43, 85);
        orderBook.addLimitOrder(114, Side.SELL, 43, 76);
        assertEquals(75, orderBook.getLowestSell().getLimitPrice());

        orderBook.cancelLimitOrder(112);
        assertEquals(76, orderBook.getLowestSell().getLimitPrice());
    }

    //limit removals with the avl tree structure test
    @Test
    void testRemoveLimitWithTwoChildrenRightChildHasLeftChildWithRightChild() {
        orderBook.addLimitOrder(3, Side.BUY, 80, 224);
        orderBook.addLimitOrder(4, Side.BUY, 80, 220);
        orderBook.addLimitOrder(5, Side.BUY, 80, 228);
        orderBook.addLimitOrder(6, Side.BUY, 80, 218);
        orderBook.addLimitOrder(7, Side.BUY, 80, 221);
        orderBook.addLimitOrder(8, Side.BUY, 80, 226);
        orderBook.addLimitOrder(9, Side.BUY, 80, 231);
        orderBook.addLimitOrder(10, Side.BUY, 80, 217);
        orderBook.addLimitOrder(11, Side.BUY, 80, 225);
        orderBook.addLimitOrder(12, Side.BUY, 80, 229);
        orderBook.addLimitOrder(13, Side.BUY, 80, 233);
        orderBook.addLimitOrder(14, Side.BUY, 80, 230);

        List<Integer> expectedInOrder = Arrays.asList(217, 218, 220, 221, 224, 225, 226, 228, 229, 230, 231, 233);
        assertEquals(expectedInOrder, orderBook.inOrderTreeTraversal(orderBook.getLimitBuyTree()));

        List<Integer> expectedPreOrder = Arrays.asList(224, 220, 218, 217, 221, 228, 226, 225, 231, 229, 230, 233);
        assertEquals(expectedPreOrder, orderBook.preOrderTreeTraversal(orderBook.getLimitBuyTree()));

        List<Integer> expectedPostOrder = Arrays.asList(217, 218, 221, 220, 225, 226, 230, 229, 233, 231, 228, 224);
        assertEquals(expectedPostOrder, orderBook.postOrderTreeTraversal(orderBook.getLimitBuyTree()));

        orderBook.cancelLimitOrder(5);

        expectedInOrder = Arrays.asList(217, 218, 220, 221, 224, 225, 226, 229, 230, 231, 233);
        assertEquals(expectedInOrder, orderBook.inOrderTreeTraversal(orderBook.getLimitBuyTree()));

        expectedPreOrder = Arrays.asList(224, 220, 218, 217, 221, 229, 226, 225, 231, 230, 233);
        assertEquals(expectedPreOrder, orderBook.preOrderTreeTraversal(orderBook.getLimitBuyTree()));

        expectedPostOrder = Arrays.asList(217, 218, 221, 220, 225, 226, 230, 233, 231, 229, 224);
        assertEquals(expectedPostOrder, orderBook.postOrderTreeTraversal(orderBook.getLimitBuyTree()));
    }

    @Test
    void testRemoveLimitWithTwoChildrenRightChildHasLeftChildWithRightChild2() {
        orderBook.addLimitOrder(3, Side.BUY, 80, 250);
        orderBook.addLimitOrder(4, Side.BUY, 80, 255);
        orderBook.addLimitOrder(5, Side.BUY, 80, 228);
        orderBook.addLimitOrder(6, Side.BUY, 80, 251);
        orderBook.addLimitOrder(7, Side.BUY, 80, 260);
        orderBook.addLimitOrder(8, Side.BUY, 80, 226);
        orderBook.addLimitOrder(9, Side.BUY, 80, 231);
        orderBook.addLimitOrder(10, Side.BUY, 80, 265);
        orderBook.addLimitOrder(11, Side.BUY, 80, 225);
        orderBook.addLimitOrder(12, Side.BUY, 80, 229);
        orderBook.addLimitOrder(13, Side.BUY, 80, 233);
        orderBook.addLimitOrder(14, Side.BUY, 80, 230);

        List<Integer> expectedInOrder = Arrays.asList(225, 226, 228, 229, 230, 231, 233, 250, 251, 255, 260, 265);
        assertEquals(expectedInOrder, orderBook.inOrderTreeTraversal(orderBook.getLimitBuyTree()));

        List<Integer> expectedPreOrder = Arrays.asList(250, 228, 226, 225, 231, 229, 230, 233, 255, 251, 260, 265);
        assertEquals(expectedPreOrder, orderBook.preOrderTreeTraversal(orderBook.getLimitBuyTree()));

        List<Integer> expectedPostOrder = Arrays.asList(225, 226, 230, 229, 233, 231, 228, 251, 265, 260, 255, 250);
        assertEquals(expectedPostOrder, orderBook.postOrderTreeTraversal(orderBook.getLimitBuyTree()));

        orderBook.cancelLimitOrder(5);

        expectedInOrder = Arrays.asList(225, 226, 229, 230, 231, 233, 250, 251, 255, 260, 265);
        assertEquals(expectedInOrder, orderBook.inOrderTreeTraversal(orderBook.getLimitBuyTree()));

        expectedPreOrder = Arrays.asList(250, 229, 226, 225, 231, 230, 233, 255, 251, 260, 265);
        assertEquals(expectedPreOrder, orderBook.preOrderTreeTraversal(orderBook.getLimitBuyTree()));

        expectedPostOrder = Arrays.asList(225, 226, 230, 233, 231, 229, 251, 265, 260, 255, 250);
        assertEquals(expectedPostOrder, orderBook.postOrderTreeTraversal(orderBook.getLimitBuyTree()));
    }

    @Test
    void testEmptyingATree() {
        orderBook.addLimitOrder(5, Side.BUY, 80, 20);

        assertNotNull(orderBook.getLimitBuyTree());
        assertEquals(20, orderBook.getLimitBuyTree().getLimitPrice());

        orderBook.cancelLimitOrder(5);

        assertNull(orderBook.getLimitBuyTree());
    }

}
