import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class TestConcurrency {
    public static void main(String[] Args) {
        class SynchronizedMethods extends Thread {
            PrintWriter pw;
            final int s;
            boolean running = true;

            public SynchronizedMethods(PrintWriter writer, int s) {
                this.pw = writer;
                this.s = s;
            }

            public PrintWriter getPW() {
                return pw;
            }

            public void setPW(PrintWriter pw) {
                this.pw = pw;
            }

            public void print() {
                synchronized (pw) {
                    pw.print(s);
                    pw.println(s+1);
                }
            }

            @Override
            public void run() {
                while (running) {
                    print();
                }
            }

            public void end(){
                running = false;
            }
        }

        ExecutorService service = Executors.newFixedThreadPool(3);
        PrintWriter pw = new PrintWriter(System.out, true);
        SynchronizedMethods t1 = new SynchronizedMethods(pw, 1);
        SynchronizedMethods t2 = new SynchronizedMethods(pw, 3);
        SynchronizedMethods t3 = new SynchronizedMethods(pw, 5);
        Set<SynchronizedMethods> set = Set.of(t1, t2, t3);

//            IntStream.range(0, 1000).forEach(count -> service.submit(t1::print));

//        service.awaitTermination(2000, TimeUnit.MILLISECONDS);
//        try {
//            service.invokeAll(set);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        t1.start();
        t2.start();
        t3.start();

        long start = System.currentTimeMillis();
        while (start+5000>System.currentTimeMillis()) {

        }

        t1.end();
        t2.end();
        t3.end();

        pw.close();
    }

}
