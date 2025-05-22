package com.glazarou;
import com.glazarou.LimitOrderbook.Orderbook;
import com.glazarou.LimitOrderbook.Side;

public class App {
    public static void main(String[] args) {

        //example use case
        Orderbook orderbook = new Orderbook();

        orderbook.addLimitOrder(1, Side.BUY, 100, 150);
        orderbook.addLimitOrder(2, Side.BUY, 100, 100);
        orderbook.addLimitOrder(3, Side.BUY, 100, 80);        
        orderbook.addLimitOrder(4, Side.BUY, 100, 90);
        orderbook.addLimitOrder(5, Side.BUY, 100, 95);
        orderbook.addLimitOrder(6, Side.BUY, 100, 90);
        orderbook.addLimitOrder(7, Side.BUY, 100, 90);
        orderbook.addLimitOrder(8, Side.BUY, 100, 60);
        orderbook.addLimitOrder(9, Side.BUY, 100, 50);

        orderbook.cancelLimitOrder(2);

        orderbook.printOrderBook();
        orderbook.printOrderBookTree();
    }
}
