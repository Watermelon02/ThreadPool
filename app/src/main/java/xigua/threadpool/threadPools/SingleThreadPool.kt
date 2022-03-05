package xigua.threadpool.threadPools

import xigua.threadpool.blockingQueue.MyLinkedBlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/**
 * description ： TODO:类的作用
 * author : Watermelon02
 * email : 1446157077@qq.com
 * date : 2022/3/3 17:42
 */
object SingleThreadPool {
    fun newSingleThreadPool(): MyThreadPoolExecutor {
        return MyThreadPoolExecutor(1,1,0,
            TimeUnit.SECONDS,
            LinkedBlockingQueue<Runnable>()
        )
    }
}