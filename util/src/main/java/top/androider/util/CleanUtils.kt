package top.androider.util

import android.os.Environment
import java.io.File

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2016/09/27
 * desc  : utils about clean
</pre> *
 */

/**
 * Clean the internal cache.
 *
 * directory: /data/data/package/cache
 *
 * @return `true`: success<br></br>`false`: fail
 */
fun cleanInternalCache(): Boolean {
    return FileUtils.deleteAllInDir(Utils.app!!.cacheDir)
}

/**
 * Clean the internal files.
 *
 * directory: /data/data/package/files
 *
 * @return `true`: success<br></br>`false`: fail
 */
fun cleanInternalFiles(): Boolean {
    return FileUtils.deleteAllInDir(Utils.app!!.filesDir)
}

/**
 * Clean the internal databases.
 *
 * directory: /data/data/package/databases
 *
 * @return `true`: success<br></br>`false`: fail
 */
fun cleanInternalDbs(): Boolean {
    return FileUtils.deleteAllInDir(File(Utils.app!!.filesDir.parent, "databases"))
}

/**
 * Clean the internal database by name.
 *
 * directory: /data/data/package/databases/dbName
 *
 * @param dbName The name of database.
 * @return `true`: success<br></br>`false`: fail
 */
fun cleanInternalDbByName(dbName: String?): Boolean {
    return Utils.app!!.deleteDatabase(dbName)
}

/**
 * Clean the internal shared preferences.
 *
 * directory: /data/data/package/shared_prefs
 *
 * @return `true`: success<br></br>`false`: fail
 */
fun cleanInternalSp(): Boolean {
    return FileUtils.deleteAllInDir(File(Utils.app!!.filesDir.parent, "shared_prefs"))
}

/**
 * Clean the external cache.
 *
 * directory: /storage/emulated/0/android/data/package/cache
 *
 * @return `true`: success<br></br>`false`: fail
 */
fun cleanExternalCache(): Boolean {
    return Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() && FileUtils.deleteAllInDir(
        Utils.app!!.externalCacheDir
    )
}

/**
 * Clean the custom directory.
 *
 * @param dirPath The path of directory.
 * @return `true`: success<br></br>`false`: fail
 */
fun cleanCustomDir(dirPath: String?): Boolean {
    return FileUtils.deleteAllInDir(FileUtils.getFileByPath(dirPath))
}