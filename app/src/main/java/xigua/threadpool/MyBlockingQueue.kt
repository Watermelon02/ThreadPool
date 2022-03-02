package xigua.threadpool

import android.util.Log
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
/**仿写的LinkedBlockingQueue,只实现了最基础的放进和拿出*/
class MyBlockingQueue<T>(private val size: Int = Int.MAX_VALUE) {
    private val list = LinkedList<T>()
    var count = AtomicInteger(0)//计数器，因为需要自增和自减并保持线程同步，所以用AtomicInteger
    private val producerLock = ReentrantLock()
    private val consumerLock = ReentrantLock()
    private val producerCondition = producerLock.newCondition()
    private val consumerCondition = consumerLock.newCondition()
    fun offer(data: T):Boolean{
        var dataAdded = false
        producerLock.lock()
        try {
            if (count.get()<size){
                list.add(data)
                count.getAndIncrement()
                Log.d("testTag", "offer:${list.size}")
                dataAdded = true
            }
        }finally {
            producerLock.unlock()
        }
        return dataAdded
    }

    fun put(data: T) {
        producerLock.lock()
        try {
            if (list.size == size) {
                producerCondition.await()
                Log.d("testTag", "full")
            }
            list.add(data)
            count.getAndIncrement()
            if (list.size==0){
                consumerCondition.signal()
            }
            Log.d("testTag", "put:${list.size}")
        } finally {
            producerLock.unlock()
        }
    }

    fun take():T {
        val data:T
        consumerLock.lock()
        try {
            if (list.size == 0) {
                consumerCondition.await()
                Log.d("testTag", "empty")
            }
            data = list.last
            list.removeLast()
            count.getAndDecrement()
            Log.d("testTag", "take:${list.size}")
            if (list.size==size){
                producerCondition.signal()
            }
        }finally {
            consumerLock.unlock()
        }
        return data
    }

    fun size(): Int {
        return count.get()
    }
}