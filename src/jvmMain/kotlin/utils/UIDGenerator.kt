package utils

import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random
object UIDGenerator {
    private const val CUSTOM_EPOCH=1672531200000L//2023-1-1 Sun 00:00:00 (GMT)

    private const val INSTANCE_BITS=10
    private const val COUNTER_BITS=12
    private const val EPOCH_BITS=41

    @Volatile
    private var lastEpoch=-1L
    private val counter=AtomicInteger(0)

    fun nextUID(): Long {
        val timestamp= timestamp()
        if (timestamp> lastEpoch){
            lastEpoch=timestamp
            counter.set(0)
        }
        var id = timestamp shl (COUNTER_BITS+INSTANCE_BITS)
        id=id or ((counter.incrementAndGet().toLong() shl INSTANCE_BITS))
        id=id or Random.nextBits(INSTANCE_BITS).toLong()
        return id
    }
    private fun timestamp(): Long {
        return Instant.now().toEpochMilli() - CUSTOM_EPOCH
    }

    fun stringFromUID(uid:Long): String {
        val s=uid.toString(2)
        return s.dropLast(COUNTER_BITS+ INSTANCE_BITS)+" "+s.dropLast(INSTANCE_BITS).takeLast(COUNTER_BITS)+" "+s.takeLast(INSTANCE_BITS)
    }
}