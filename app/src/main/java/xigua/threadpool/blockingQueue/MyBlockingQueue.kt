package xigua.threadpool.blockingQueue

import java.util.concurrent.TimeUnit

/**
 * description ： TODO:类的作用
 * author : Watermelon02
 * email : 1446157077@qq.com
 * date : 2022/3/3 17:35
 */
interface MyBlockingQueue<T> {
    fun offer(data: T): Boolean
    fun size(): Int
    fun poll(keepAliveTime: Long, timeUnit: TimeUnit): T?
    fun take(): T?
    fun put(data: T)
}