package xigua.threadpool.threadPools

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.TimeUnit

/**
 * description ： TODO:类的作用
 * author : Watermelon02
 * email : 1446157077@qq.com
 * date : 2022/3/3 17:42
 */
object CachedThreadPool {
    fun newCachedThreadPool():MyThreadPoolExecutor{
        return MyThreadPoolExecutor(0,Int.MAX_VALUE,60,TimeUnit.SECONDS, SynchronousQueue())
    }
}