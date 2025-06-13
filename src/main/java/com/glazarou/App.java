package com.glazarou;
import com.glazarou.LimitOrderbook.Orderbook;
import com.glazarou.LimitOrderbook.Side;

public class App {
    public static void main(String[] args) {

        //example use case
        Orderbook orderbook = new Orderbook();

        orderbook.addLimitOrder(1, Side.BUY, 100, 150);

        orderbook.cancelLimitOrder(1);

        orderbook.printOrderBook();
        orderbook.printOrderBookTree();
    }
}
