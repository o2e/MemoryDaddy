package moe.ore.memory.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import moe.ore.memory.ext.isDebugConsole
import moe.ore.memory.ext.toast
import kotlin.reflect.KClass

open class BaseActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isDebugConsole) {

        }


    }

    @JvmOverloads fun startActivity(target: KClass<*>, finishMe: Boolean = false) {
        val intent = Intent(this, target.java)
        startActivity(intent)
        if (finishMe) {
            finish()
        }
    }

    companion object {

    }
}