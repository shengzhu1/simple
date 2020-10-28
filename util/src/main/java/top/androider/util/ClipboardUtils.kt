package top.androider.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2016/09/25
 * desc  : 剪贴板相关工具类
</pre> *
 */
/**
 * 复制文本到剪贴板
 *
 * @param text 文本
 */
fun copyText(text: CharSequence?) {
    val cm = Utils.app!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("text", text))
}

/**
 * 获取剪贴板的文本
 *
 * @return 剪贴板的文本
 */
fun getText(): CharSequence? {
    val cm =
        Utils.app!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    return cm.primaryClip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(Utils.app)
}

/**
 * 复制uri到剪贴板
 *
 * @param uri uri
 */
fun copyUri(uri: Uri?) {
    val cm = Utils.app!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newUri(Utils.app!!.contentResolver, "uri", uri))
}

/**
 * 获取剪贴板的uri
 *
 * @return 剪贴板的uri
 */
fun getUri(): Uri?{
    val cm =
        Utils.app!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    return cm.primaryClip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.uri
}

/**
 * 复制意图到剪贴板
 *
 * @param intent 意图
 */
fun copyIntent(intent: Intent?) {
    val cm = Utils.app!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newIntent("intent", intent))
}

/**
 * 获取剪贴板的意图
 *
 * @return 剪贴板的意图
 */
fun getIntent(): Intent? {
    val cm =
        Utils.app!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    return cm.primaryClip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.intent
}