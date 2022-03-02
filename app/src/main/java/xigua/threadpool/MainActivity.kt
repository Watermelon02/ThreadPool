package xigua.threadpool

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val threadPool = MyThreadPoolExecutor(2,2,)
        for (a in 0 .. 2){
            threadPool.execute {
                val thread = Thread.currentThread()
                Log.d("testTag", "${Thread.currentThread()} :count = ${a}")
            }
        }
        GlobalScope.launch {
            delay(2000)
            Log.d("testTag","${threadPool.poolSize}")
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