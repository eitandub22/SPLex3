package bgu.spl.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;

public class test {
    public static void main(String[] args){
        System.out.println(Files.exists(Paths.get("Files" + "\\" + "A")));
    }
}
