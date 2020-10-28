package top.androider.util

import android.os.Build
import android.os.Environment
import android.text.TextUtils
import top.androider.util.Utils.Companion.app
import java.io.File

private val SEP = File.separatorChar

/**
 * Join the path.
 *
 * @param parent The parent of path.
 * @param child  The child path.
 * @return the path
 */
fun String?.join(child: String): String? {
    if (TextUtils.isEmpty(child)) return this
    val parent = this?:""
    val len = parent.length
    val legalSegment = getLegalSegment(child)
    return if (len == 0) {
        SEP.toString() + legalSegment
    } else if (parent[len - 1] == SEP) {
        parent + legalSegment
    } else {
        parent + SEP + legalSegment
    }
}

private fun getLegalSegment(segment: String): String {
    var st = -1
    var end = -1
    segment.toCharArray().forEachIndexed{
        i, c ->
        if (c != SEP) {
            if (st == -1) {
                st = i
            }
            end = i
        }
    }
    if (st in 0..end) {
        return segment.substring(st, end + 1)
    }
    throw IllegalArgumentException("segment of <$segment> is illegal")
}

/**
 * Return the path of /system.
 *
 * @return the path of /system
 */
val rootPath = Environment.getRootDirectory().getAbsolutePathKt()
val dataPath = Environment.getDataDirectory().getAbsolutePathKt()
val downloadCachePath = Environment.getDownloadCacheDirectory().getAbsolutePathKt()

/**
 * Return the path of /data/data/package.
 *
 * @return the path of /data/data/package
 */
val internalAppDataPath: String
    get() = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
        app!!.applicationInfo.dataDir
    } else app?.dataDir.getAbsolutePathKt()


val internalAppCodeCacheDir = app?.codeCacheDir.getAbsolutePathKt()
val internalAppCachePath = app?.cacheDir.getAbsolutePathKt()
val internalAppDbsPath = app!!.applicationInfo.dataDir + "/databases"

fun getInternalAppDbPath(name: String?): String {
    return app?.getDatabasePath(name).getAbsolutePathKt()
}

val internalAppFilesPath = app?.filesDir.getAbsolutePathKt()

val internalAppSpPath = app!!.applicationInfo.dataDir + "/shared_prefs"

val internalAppNoBackupFilesPath = app?.noBackupFilesDir.getAbsolutePathKt()

fun getExternalPath(path:String) = if (!isSDCardEnableByEnvironment) "" else Environment.getExternalStoragePublicDirectory(path).getAbsolutePathKt()
val externalStoragePath = if (!isSDCardEnableByEnvironment) "" else Environment.getExternalStorageDirectory().getAbsolutePathKt()

val externalMusicPath = getExternalPath(Environment.DIRECTORY_MUSIC)

/**
 * Return the path of /storage/emulated/0/Podcasts.
 *
 * @return the path of /storage/emulated/0/Podcasts
 */
val externalPodcastsPath = getExternalPath(Environment.DIRECTORY_PODCASTS)

/**
 * Return the path of /storage/emulated/0/Ringtones.
 *
 * @return the path of /storage/emulated/0/Ringtones
 */
val externalRingtonesPath= getExternalPath(Environment.DIRECTORY_RINGTONES)

/**
 * Return the path of /storage/emulated/0/Alarms.
 *
 * @return the path of /storage/emulated/0/Alarms
 */
val externalAlarmsPath= getExternalPath(Environment.DIRECTORY_ALARMS)

/**
 * Return the path of /storage/emulated/0/Notifications.
 *
 * @return the path of /storage/emulated/0/Notifications
 */
val externalNotificationsPath= getExternalPath(Environment.DIRECTORY_NOTIFICATIONS)

/**
 * Return the path of /storage/emulated/0/Pictures.
 *
 * @return the path of /storage/emulated/0/Pictures
 */
val externalPicturesPath= getExternalPath(Environment.DIRECTORY_PICTURES)

/**
 * Return the path of /storage/emulated/0/Movies.
 *
 * @return the path of /storage/emulated/0/Movies
 */
val externalMoviesPath = getExternalPath(Environment.DIRECTORY_MOVIES)


val externalDownloadsPath = getExternalPath(Environment.DIRECTORY_DOWNLOADS)


val externalDcimPath= getExternalPath(Environment.DIRECTORY_DCIM)

val externalDocumentsPath= getExternalPath(Environment.DIRECTORY_DOCUMENTS)

val externalAppDataPath = if (!isSDCardEnableByEnvironment) "" else app?.externalCacheDir?.parentFile.getAbsolutePathKt()

val externalAppCachePath = if (!isSDCardEnableByEnvironment) "" else app!!.externalCacheDir.getAbsolutePathKt()


fun  getExternalAppFilesPath(path: String?) = if (!isSDCardEnableByEnvironment) "" else app?.getExternalFilesDir(path).getAbsolutePathKt()
/**
 * Return the path of /storage/emulated/0/Android/data/package/files.
 *
 * @return the path of /storage/emulated/0/Android/data/package/files
 */
val externalAppFilesPath = getExternalAppFilesPath(null)

/**
 * Return the path of /storage/emulated/0/Android/data/package/files/Music.
 *
 * @return the path of /storage/emulated/0/Android/data/package/files/Music
 */
val externalAppMusicPath = getExternalAppFilesPath(Environment.DIRECTORY_MUSIC)
/**
 * Return the path of /storage/emulated/0/Android/data/package/files/Podcasts.
 *
 * @return the path of /storage/emulated/0/Android/data/package/files/Podcasts
 */
val externalAppPodcastsPath = getExternalAppFilesPath(Environment.DIRECTORY_PODCASTS)
/**
 * Return the path of /storage/emulated/0/Android/data/package/files/Ringtones.
 *
 * @return the path of /storage/emulated/0/Android/data/package/files/Ringtones
 */
val externalAppRingtonesPath = getExternalAppFilesPath(Environment.DIRECTORY_RINGTONES)

/**
 * Return the path of /storage/emulated/0/Android/data/package/files/Alarms.
 *
 * @return the path of /storage/emulated/0/Android/data/package/files/Alarms
 */
val externalAppAlarmsPath = getExternalAppFilesPath(Environment.DIRECTORY_ALARMS)

/**
 * Return the path of /storage/emulated/0/Android/data/package/files/Notifications.
 *
 * @return the path of /storage/emulated/0/Android/data/package/files/Notifications
 */
val externalAppNotificationsPath= getExternalAppFilesPath(Environment.DIRECTORY_NOTIFICATIONS)

/**
 * Return the path of /storage/emulated/0/Android/data/package/files/Pictures.
 *
 * @return path of /storage/emulated/0/Android/data/package/files/Pictures
 */
val externalAppPicturesPath= getExternalAppFilesPath(Environment.DIRECTORY_PICTURES)

/**
 * Return the path of /storage/emulated/0/Android/data/package/files/Movies.
 *
 * @return the path of /storage/emulated/0/Android/data/package/files/Movies
 */
val externalAppMoviesPath= getExternalAppFilesPath(Environment.DIRECTORY_MOVIES)

/**
 * Return the path of /storage/emulated/0/Android/data/package/files/Download.
 *
 * @return the path of /storage/emulated/0/Android/data/package/files/Download
 */
val externalAppDownloadPath= getExternalAppFilesPath(Environment.DIRECTORY_DOWNLOADS)

/**
 * Return the path of /storage/emulated/0/Android/data/package/files/DCIM.
 *
 * @return the path of /storage/emulated/0/Android/data/package/files/DCIM
 */
val externalAppDcimPath= getExternalAppFilesPath(Environment.DIRECTORY_DCIM)

/**
 * Return the path of /storage/emulated/0/Android/data/package/files/Documents.
 *
 * @return the path of /storage/emulated/0/Android/data/package/files/Documents
 */
val externalAppDocumentsPath= getExternalAppFilesPath(Environment.DIRECTORY_DOCUMENTS)
/**
 * Return the path of /storage/emulated/0/Android/obb/package.
 *
 * @return the path of /storage/emulated/0/Android/obb/package
 */
val externalAppObbPath = if (!isSDCardEnableByEnvironment) "" else app?.obbDir.getAbsolutePathKt()

val rootPathExternalFirst= externalStoragePath.let { if (it.isEmpty()) rootPath else it }
val appDataPathExternalFirst = externalAppDataPath.let { if (it.isEmpty()) internalAppCachePath else it }
val filesPathExternalFirst = externalAppFilesPath.let { if (it.isEmpty()) internalAppFilesPath else it }
val cachePathExternalFirst = externalAppCachePath.let { if (it.isEmpty()) internalAppCachePath else it }

private fun File?.getAbsolutePathKt() = this?.absolutePath?:""