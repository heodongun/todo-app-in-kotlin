package com.heodongun.ugoal.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.heodongun.ugoal.MainActivity
import com.heodongun.ugoal.R
import com.heodongun.ugoal.utils.HapticFeedback
import com.heodongun.ugoal.utils.NotificationHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PomodoroService : Service() {

    private val binder = PomodoroBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private val _timerState = MutableStateFlow<PomodoroTimerState>(PomodoroTimerState.Idle)
    val timerState: StateFlow<PomodoroTimerState> = _timerState
    
    private var timerJob: Job? = null
    private var currentTodoId: String? = null
    
    sealed class PomodoroTimerState {
        object Idle : PomodoroTimerState()
        data class Running(val remainingSeconds: Int, val totalSeconds: Int, val isBreak: Boolean = false) : PomodoroTimerState()
        data class Paused(val remainingSeconds: Int, val totalSeconds: Int, val isBreak: Boolean = false) : PomodoroTimerState()
        data class Completed(val wasBreak: Boolean = false) : PomodoroTimerState()
    }
    
    inner class PomodoroBinder : Binder() {
        fun getService(): PomodoroService = this@PomodoroService
    }
    
    override fun onBind(intent: Intent): IBinder = binder
    
    fun startPomodoro(todoId: String, durationMinutes: Int = 25) {
        currentTodoId = todoId
        startTimer(durationMinutes * 60, isBreak = false)
        startForeground(NOTIFICATION_ID, createNotification("포모도로 진행 중", "${durationMinutes}분 집중 타이머"))
    }
    
    fun startBreak(durationMinutes: Int = 5) {
        startTimer(durationMinutes * 60, isBreak = true)
        updateNotification("휴식 시간", "${durationMinutes}분 휴식")
    }
    
    fun pauseTimer() {
        timerJob?.cancel()
        val currentState = _timerState.value
        if (currentState is PomodoroTimerState.Running) {
            _timerState.value = PomodoroTimerState.Paused(
                currentState.remainingSeconds,
                currentState.totalSeconds,
                currentState.isBreak
            )
        }
    }
    
    fun resumeTimer() {
        val currentState = _timerState.value
        if (currentState is PomodoroTimerState.Paused) {
            startTimer(
                totalSeconds = currentState.totalSeconds,
                remainingSeconds = currentState.remainingSeconds,
                isBreak = currentState.isBreak
            )
        }
    }
    
    fun stopTimer() {
        timerJob?.cancel()
        _timerState.value = PomodoroTimerState.Idle
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    private fun startTimer(totalSeconds: Int, remainingSeconds: Int = totalSeconds, isBreak: Boolean = false) {
        timerJob?.cancel()
        
        timerJob = serviceScope.launch {
            var remaining = remainingSeconds
            
            while (remaining > 0) {
                _timerState.value = PomodoroTimerState.Running(remaining, totalSeconds, isBreak)
                
                val minutes = remaining / 60
                val seconds = remaining % 60
                updateNotification(
                    if (isBreak) "휴식 중" else "포모도로 진행 중",
                    String.format("%02d:%02d", minutes, seconds)
                )
                
                delay(1000)
                remaining--
            }
            
            // Timer completed
            _timerState.value = PomodoroTimerState.Completed(isBreak)
            onTimerComplete(isBreak)
        }
    }
    
    private fun onTimerComplete(wasBreak: Boolean) {
        HapticFeedback.performSuccess(this)
        
        if (wasBreak) {
            NotificationHelper.showPomodoroComplete(this, "휴식 완료! 다시 집중할 준비가 되셨나요?")
        } else {
            NotificationHelper.showPomodoroComplete(this, "포모도로 완료! 잠깐 휴식을 취하세요.")
        }
        
        stopTimer()
    }
    
    private fun createNotification(title: String, text: String): Notification {
        createNotificationChannel()
        
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }
    
    private fun updateNotification(title: String, text: String) {
        val notification = createNotification(title, text)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "포모도로 타이머",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "포모도로 타이머 진행 상황"
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        serviceScope.cancel()
    }
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "pomodoro_channel"
    }
}
