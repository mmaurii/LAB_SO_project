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
        class TestPW extends Thread {
            PrintWriter pw;
            final int s;
            boolean running = true;

            public TestPW(PrintWriter writer, int s) {
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
                    pw.println(s + 1);
                }
            }

            @Override
            public void run() {
                while (running) {
                    print();
                }
            }

            public void end() {
                running = false;
            }
        }

        class Person {
            private String name;

            public Person(String name) {
                this.name = name;
            }

            public synchronized String GetName() {
                return name;
            }
        }

        class SynchronizedMethods extends Thread {
            LinkedList<Person> ps;
            Integer n;
            boolean running = true;

            public SynchronizedMethods(LinkedList<Person> ps, Integer n) {
                this.ps = ps;
                this.n = n;
            }

            public void print() {
                for (Person p : ps) {
                    String name = p.GetName();
                    System.out.println(n + " " + name + " " + n);
                }
            }

            @Override
            public void run() {
                while (running) {
//                    Person p =
//                            new Person("gianni");
////                            ps.removeFirst();
//                    print();
//                    ps.add(p);

//                    synchronized (n) {
                    aumenta();
                    decresci();
                    System.out.println(n);
//                    }
                }
            }

            private void decresci() {
                n--;
            }

            private void aumenta() {
                n++;
            }

            public void end() {
                running = false;
            }
        }

        ExecutorService service = Executors.newFixedThreadPool(3);
        PrintWriter pw = new PrintWriter(System.out, true);
        Person p = new Person("pippo");
        Person p1 = new Person("pluto");
        Person p2 = new Person("paperino");
        LinkedList<Person> ps = new LinkedList<>(Arrays.asList(p, p1, p2));
        Integer i = 1;
        SynchronizedMethods t1 = new SynchronizedMethods(ps, i);
        SynchronizedMethods t2 = new SynchronizedMethods(ps, i);
        SynchronizedMethods t3 = new SynchronizedMethods(ps, i);
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
        while (start + 20000 > System.currentTimeMillis()) {

        }

        t1.end();
        t2.end();
        t3.end();

        pw.close();
    }

}
