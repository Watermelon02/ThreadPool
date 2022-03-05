package xigua.threadpool

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import xigua.threadpool.threadPools.MyThreadPoolExecutor
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val threadPool =
            MyThreadPoolExecutor(1, Int.MAX_VALUE, 10, TimeUnit.SECONDS, SynchronousQueue<Runnable>())
        for (a in 0..100) {
            threadPool.execute {
                Log.d("testTag", "${Thread.currentThread()} :count = ${a}")
            }
        }
        /*val threadPool = ThreadPoolExecutor(2,2,0, TimeUnit.SECONDS, LinkedBlockingQueue<Runnable>(2))
        for (a in 0 .. 2){
            threadPool.execute {
                val thread = Thread.currentThread()
                Log.d("testTag", "${Thread.currentThread()} :count = ${a}")
            }
        }
        GlobalScope.launch {
            delay(2000)
            Log.d("testTag","${threadPool.poolSize}")
        }*/
    }
}