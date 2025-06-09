package com.glazarou;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.glazarou.LimitOrderbook.Limit;
import com.glazarou.LimitOrderbook.Orderbook;
import com.glazarou.LimitOrderbook.Side;

public class AppTest {
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

    //cancel orders
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

    //updating book edges
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

}
