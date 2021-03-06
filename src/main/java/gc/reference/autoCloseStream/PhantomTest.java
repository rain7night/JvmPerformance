package gc.reference.autoCloseStream;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PhantomTest {

    public static void main(String[] args) throws Exception {
        //打开回收
        ResourceCloseDaemon daemon = new ResourceCloseDaemon();
        daemon.setDaemon(true);
        daemon.start();

        // touch a.txt b.txt
        // echo "hello" > a.txt

        //保留对象,防止gc把stream回收掉,其不到演示效果
        List<Closeable> all = new ArrayList<>();
        FileInputStream inputStream;
        FileOutputStream outputStream;

        for (int i = 0; i < 100000; i++) {
            inputStream = new FileInputStream("D:\\IdeaProject\\JvmPerformance\\io\\a.txt");
            outputStream = new FileOutputStream("D:\\IdeaProject\\JvmPerformance\\io\\b.txt");
            FileOperation operation = new FileOperation(inputStream, outputStream);
            operation.operate();
            TimeUnit.MILLISECONDS.sleep(100);

            List<Closeable> closeables = new ArrayList<>();
            closeables.add(inputStream);
            closeables.add(outputStream);
            all.addAll(closeables);
            ResourceCloseDaemon.register(operation, closeables);
            //用下面命令查看文件句柄,如果把上面register注释掉,就会发现句柄数量不断上升
            //jps | grep PhantomTest | awk '{print $1}' |head -1 | xargs lsof -p | grep /JvmPerformance/io
            System.gc();

        }
    }
}
