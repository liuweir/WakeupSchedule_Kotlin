package com.suda.yzune.wakeupschedule.settings

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.suda.yzune.wakeupschedule.BaseTitleActivity
import com.suda.yzune.wakeupschedule.R
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_time_settings.*
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.textColorResource

class TimeSettingsActivity : BaseTitleActivity() {
    override val layoutId: Int
        get() = R.layout.activity_time_settings

    override fun onSetupSubButton(tvButton: TextView): TextView? {
        tvButton.text = "保存"
        tvButton.typeface = Typeface.DEFAULT_BOLD
        tvButton.textColorResource = R.color.colorAccent
        tvButton.setOnClickListener {
            when (navController.currentDestination?.id) {
                R.id.timeTableFragment -> {
                    setResult(Activity.RESULT_OK, Intent().putExtra("selectedId", viewModel.selectedId))
                    finish()
                }
                R.id.timeSettingsFragment -> {
                    viewModel.saveDetailData(viewModel.entryPosition)
                }
            }
        }
        return tvButton
    }

    private lateinit var viewModel: TimeSettingsViewModel
    private lateinit var navController: NavController
    private var isExit: Boolean = false
    private val tExit = object : CountDownTimer(2000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
        }

        override fun onFinish() {
            isExit = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(TimeSettingsViewModel::class.java)

        initView()

        viewModel.saveInfo.observe(this, Observer { s ->
            if (s == null) return@Observer
            when (s) {
                "detail_ok" -> {
                    navController.navigateUp()
                    Toasty.success(this.applicationContext, "保存成功").show()
                }
                "error" -> {
                    Toasty.error(this.applicationContext, "出现错误>_<", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun initView() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_fragment) as NavHostFragment
        val navGraph = navHostFragment.navController.navInflater.inflate(R.navigation.nav_time_settings)
        val fragDestination = navGraph.findNode(R.id.timeTableFragment)!!
        fragDestination.setDefaultArguments(Bundle().apply {
            this.putInt("selectedId", intent.extras!!.getInt("selectedId"))
        })
        navHostFragment.navController.graph = navGraph
        navController = Navigation.findNavController(this, R.id.nav_fragment)
        navController.addOnNavigatedListener { _, destination ->
            mainTitle.text = destination.label
        }
    }

    private fun exitBy2Click() {
        if (!isExit) {
            isExit = true // 准备退出
            ll_root.longSnackbar("真的不保存吗？那再按一次退出编辑哦，就不保存啦。")
            tExit.start() // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务
        } else {
            when (navController.currentDestination?.id) {
                R.id.timeTableFragment -> {
                    finish()
                }
                R.id.timeSettingsFragment -> {
                    navController.navigateUp()
                }
            }
        }
    }

    override fun onBackPressed() {
        exitBy2Click()
    }

    override fun onDestroy() {
        super.onDestroy()
        tExit.cancel()
    }
}
