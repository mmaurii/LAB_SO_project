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

            public String GetName() {
                String temp = name;
                name = "ciao";
                name=temp;
                return name;
            }
            public String op() {
                String temp = name;
                name = "null";
                name=temp;
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

        class Sync extends Thread {
            Person p;
            String n;
            boolean running = true;

            public Sync(Person p, String n) {
                this.p = p;
                this.n = n;
            }

            private void aumenta() {
                n = "";
            }

            private void decresci() {
                n += "ciao";
            }

            @Override
            public void run() {
                while (running) {
//                    aumenta();
//                    decresci();
                    synchronized (p) {
                        String name = p.GetName()+n;
                        p.op();
                        System.out.println(name);
                    }
                }
            }

            public void end() {
                running = false;
            }
        }

        ExecutorService service = Executors.newFixedThreadPool(3);
//        PrintWriter pw = new PrintWriter(System.out, true);
        Person p = new Person("pippo");
        Person p1 = new Person("pluto");
        Person p2 = new Person("paperino");
        Person p3 = new Person("titti");
        Person p4 = new Person("minni");
        Person p5 = new Person("pluto");
        Person p6 = new Person("paperino");
        Person p7 = new Person("pluto");
        Person p8 = new Person("paperino");

//        LinkedList<Person> ps = new LinkedList<>(Arrays.asList(p, p1, p2));
        Integer i = 1;
        String ciao = "ciao";
//        SynchronizedMethods t1 = new SynchronizedMethods(ps, i);
//        SynchronizedMethods t2 = new SynchronizedMethods(ps, i);
//        SynchronizedMethods t3 = new SynchronizedMethods(ps, i);
//        Set<SynchronizedMethods> set = Set.of(t1, t2, t3);

        Sync thread1 = new Sync(p, "1");
        Sync thread2 = new Sync(p, "2");
        Sync thread3 = new Sync(p, "3");
        Sync thread4 = new Sync(p, "4");
        Sync thread5 = new Sync(p, "5");
        Sync thread6 = new Sync(p, "6");
        Sync thread7 = new Sync(p, "7");
        Sync thread8 = new Sync(p, "8");
        Sync thread9 = new Sync(p, "9");

//            IntStream.range(0, 1000).forEach(count -> service.submit(t1::print));

//        service.awaitTermination(2000, TimeUnit.MILLISECONDS);
//        try {
//            service.invokeAll(set);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();
        thread5.start();
        thread6.start();
        thread7.start();
        thread8.start();

        long start = System.currentTimeMillis();
        while (start + 10000 > System.currentTimeMillis()) {

        }

        thread2.end();
        thread1.end();
        thread3.end();
        thread4.end();
        thread5.end();
        thread6.end();
        thread7.end();
        thread8.end();
        thread9.end();

//        pw.close();
    }

}
