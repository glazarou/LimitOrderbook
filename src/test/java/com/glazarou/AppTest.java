package com.glazarou;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
