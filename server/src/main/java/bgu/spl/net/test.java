package bgu.spl.net;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;

public class test {
    public static void main(String[] args){
        Deque<Integer> d = new LinkedList<>();
        d.add(1);
        d.add(2);
        System.out.println(d.peek());
        d.addFirst(3);
        System.out.println(d.peek());
        System.out.println(d);
        d.remove();
        System.out.println(d);
        d.addFirst(4);
        System.out.println(d);
    }
}
