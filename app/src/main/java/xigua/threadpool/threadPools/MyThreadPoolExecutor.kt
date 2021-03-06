package xigua.threadpool.threadPools

import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.AbstractQueuedSynchronizer
import java.util.concurrent.locks.ReentrantLock

/**
 * 仿写的ThreadPoolExecutor,能够实现SingleThreadPoolExecutor,FixedPoolExecutor,CachedPoolExecutor
 * */

class MyThreadPoolExecutor(
    private val corePoolSize: Int,
    private val maxPoolSize: Int,
    private val keepAliveTime: Long = 0,
    private val timeUnit: TimeUnit,
    private val taskQueue: BlockingQueue<Runnable>
) : AbstractExecutorService() {
    companion object RunStatus {
        const val RUNNING = 0
        const val SHUTDOWN = 1
        const val STOP = 2
        const val TERMINATED = 3
    }

    //poolSize和runState在ThreadPoolExecutor中是一起以ctl存储的
    var poolSize = AtomicInteger(0)
    private var runState = AtomicInteger(RUNNING)
    private val lock = ReentrantLock()
    private val workers = HashSet<Worker>(corePoolSize)

    override fun execute(runnable: Runnable) {
        if (runState.get() == RUNNING) {
            if (poolSize.get() < corePoolSize) addNewWorker(
                runnable,
                true
            ) else if (poolSize.get() in corePoolSize..maxPoolSize) {
                if (taskQueue.offer(runnable)) {
                    if (poolSize.get() == 0) {
                        addNewWorker(null, false)
                    }
                } else {
                    addNewWorker(runnable, false)
                }
            }
        } else {
            shutdown()
        }
    }


    /**
     * @param core 新增的worker是否是核心线程，影响方法中对于线程数目的判断。
     * 如果不是核心线程，则将poolSize和MaximumPoolSizwe比较*/
    private fun addNewWorker(runnable: Runnable?, core: Boolean) {

        var worker: Worker?
        lock.lock()
        try {//execute中没有加锁后进行条件判断，因此可能存在线程同步导致的问题，所以这里加锁后再次进行判断(类似于DCL?)
            worker = Worker(runnable)
            if (runState.get() == RUNNING) {
                if ((core && poolSize.get() < corePoolSize) || (!core && poolSize.get() < corePoolSize))
                    workers.add(worker)
                poolSize.getAndIncrement()
            } else {
                shutdown()
            }
        } finally {
            lock.unlock()
        }
        worker?.thread?.start()
    }

    override fun shutdown() {
        runState.set(SHUTDOWN)
        interruptWorks()
    }

    override fun shutdownNow(): MutableList<Runnable> {
        runState.set(SHUTDOWN)
        interruptWorks()
        return ArrayList<Runnable>()//应该返回taskQueue中剩余的Runnable,但是因为这个阻塞队列没设计好，所以暂时这么处理
    }

    private fun interruptWorks() {
        for (worker in workers) {
            worker.interrupt()
        }
    }

    override fun isShutdown(): Boolean {
        return runState.get() >= SHUTDOWN
    }

    override fun isTerminated(): Boolean {
        return runState.get() == TERMINATED
    }

    override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean {
        TODO("Not yet implemented")
    }

    private fun getTask(): Runnable? {
        val timed = keepAliveTime > 0//是否设置了KeepAliveTime
        while (true) {
            return if (runState.get() == RUNNING) {
                if (timed) taskQueue.poll(keepAliveTime, timeUnit) else taskQueue.take()
            } else {
                null
            }
        }
    }

    fun scheduleAtFixedRate() {}
    fun scheduleWithFixedDelay() {}

    private inner class Worker(var task: Runnable?) : Runnable, AbstractQueuedSynchronizer() {

        /**
         * 继承ReentrantLock的父类AQS，目的:
         * 1.不让所有Worker共用一个锁，而是每个worker都有自己的锁,不然当一个worker在running的时候，其他worker获取不到锁只能干看着
         * 2.可以直接通过CAS来获取锁，避免阻塞。例如当多Worker的情况下执行shutDown。如果这个Worker此时正在Running无法获取到锁，那么执行shutDown()线程就会阻塞住了
         * */
        var thread: Thread = Thread(this)
        override fun run() {
            try {
                while (task != null || getTask().also { task = it } != null) {
                    lock()
                    try {
                        if (runState.get() == RUNNING) {
                            task!!.run()
                            task = null
                        } else interrupt()
                    } finally {
                        unLock()
                    }
                }
            } finally {//当task为null退出while时，中断线程(其实还应该销毁worker
                workers.remove(this)
                interrupt()
            }
        }

        //acquire和release的参数1是可重入性，0表示未锁定，大于0为锁定，想要完全解锁一个线程就要解锁到0
        fun lock() = acquire(1)
        fun tryLock() = tryAcquire(0)
        fun unLock() = release(0)

        /**
         *AQS中的tryAcquire是模板函数，需要子类自己覆写
         *原理是非公平锁，通过CAS尝试设置state,设置成功则独占锁
         * */
        override fun tryAcquire(arg: Int): Boolean {
            return if (compareAndSetState(0, 1)) {
                exclusiveOwnerThread = Thread.currentThread()
                true
            } else false
        }

        override fun tryRelease(arg: Int): Boolean {
            return if (compareAndSetState(1, 0)) {
                exclusiveOwnerThread = null
                state = 0
                true
            } else false
        }

        fun interrupt() {
            poolSize.getAndDecrement()
            thread.interrupt()
        }
    }
}

