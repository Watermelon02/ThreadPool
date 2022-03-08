package xigua.threadpool.blockingQueue

import android.util.Log
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock

/**仿写的LinkedBlockingQueue*/
class MyLinkedBlockingQueue<T>(private val size: Int = Int.MAX_VALUE) : MyBlockingQueue<T> {
    private val list = LinkedList<T>()
    var count = AtomicInteger(0)//计数器，因为需要自增和自减并保持线程同步，所以用AtomicInteger
    private val producerLock = ReentrantLock()
    private val consumerLock = ReentrantLock()
    private val producerCondition = producerLock.newCondition()
    private val consumerCondition = consumerLock.newCondition()

    override fun offer(data: T): Boolean {
        var dataAdded = false
        producerLock.lock()
        try {
            if (count.get() < size) {
                list.add(data)
                count.getAndIncrement()
                if (count.get() + 1 < size) producerCondition.signal()
                Log.d("testTag", "offer:${list.size}")
                dataAdded = true
            }
        } finally {
            producerLock.unlock()
        }
        return dataAdded
    }

    override fun put(data: T) {
        producerLock.lock()
        try {
            while (count.get() == size) {
                producerCondition.await()
                Log.d("testTag", "full")
            }
            list.add(data)
            count.getAndIncrement()
            consumerCondition.signal()
            Log.d("testTag", "put:${list.size}")
        } finally {
            producerLock.unlock()
        }
    }

    override fun poll(timeOut: Long, timeUnit: TimeUnit): T? {
        var nanos = timeUnit.toNanos(timeOut)//换算等待时间为毫秒单位
        val data: T
        consumerLock.lockInterruptibly()
        try {
            while (count.get() == 0) {
                if (nanos <= 0) return null
                nanos = consumerCondition.awaitNanos(nanos)
            }
            data = list.first
            list.removeFirst()
            count.getAndDecrement()
        } finally {
            consumerLock.unlock()
        }
        return data
    }

    override fun take(): T {
        consumerLock.lock()
        try {
            while (count.get() == 0) {
                consumerCondition.await()
                Log.d("testTag", "empty")
            }
            return dequeue()
        } finally {
            consumerLock.unlock()
        }
    }

    private fun dequeue(): T {
        val data = list.last
        count.getAndDecrement()
        list.removeLast()
        Log.d("testTag", "take:${list.size}")
        if (count.get() > 0) {
            consumerCondition.signal()
        }
        return data
    }

    override fun size(): Int {
        return count.get()
    }
}