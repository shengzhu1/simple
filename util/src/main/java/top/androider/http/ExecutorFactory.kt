package top.androider.http

import android.os.Handler
import android.os.Looper
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

object ExecutorFactory {
    private val CPU_COUNT = Runtime.getRuntime().availableProcessors()
    val defaultWorkExecutor: Executor = ThreadPoolExecutor(2 * CPU_COUNT + 1,
        2 * CPU_COUNT + 1,
        30, TimeUnit.SECONDS,
        LinkedBlockingQueue(128),
        object : ThreadFactory {
            private val mCount = AtomicInteger(1)
            override fun newThread(r: Runnable): Thread {
                return Thread(r, "http-pool-" + mCount.getAndIncrement())
            }
        }
    )
    val defaultMainExecutor: Executor = object : Executor {
        private val mHandler = Handler(Looper.getMainLooper())
        override fun execute(command: Runnable) {
            mHandler.post(command)
        }
    }
}