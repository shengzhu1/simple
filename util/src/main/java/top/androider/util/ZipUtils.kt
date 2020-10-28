package top.androider.util

import android.util.Log
import java.io.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2016/08/27
 * desc  : utils about zip
</pre> *
 */
object ZipUtils {
    private const val BUFFER_LEN = 8192

    /**
     * Zip the files.
     *
     * @param srcFilePaths The paths of source files.
     * @param zipFilePath  The path of ZIP file.
     * @param comment      The comment.
     * @return `true`: success<br></br>`false`: fail
     * @throws IOException if an I/O error has occurred
     */
    @Throws(IOException::class)
    fun zipFiles(
        srcFilePaths: Collection<String?>?,
        zipFilePath: String?,
        comment: String? = null
    ): Boolean {
        if (srcFilePaths == null || zipFilePath == null) return false
        var zos: ZipOutputStream? = null
        return try {
            zos = ZipOutputStream(FileOutputStream(zipFilePath))
            for (srcFile in srcFilePaths) {
                if (!zipFile(FileUtils.getFileByPath(srcFile), "", zos, comment)) return false
            }
            true
        } finally {
            zos?.apply{
                finish()
                close()
            }
        }
    }
    /**
     * Zip the files.
     *
     * @param srcFiles The source of files.
     * @param zipFile  The ZIP file.
     * @param comment  The comment.
     * @return `true`: success<br></br>`false`: fail
     * @throws IOException if an I/O error has occurred
     */
    @JvmOverloads
    @Throws(IOException::class)
    fun zipFiles(
        srcFiles: Collection<File>?,
        zipFile: File?,
        comment: String? = null
    ): Boolean {
        if (srcFiles == null || zipFile == null) return false
        var zos: ZipOutputStream? = null
        return try {
            zos = ZipOutputStream(FileOutputStream(zipFile))
            for (srcFile in srcFiles) {
                if (!zipFile(srcFile, "", zos, comment)) return false
            }
            true
        } finally {
            if (zos != null) {
                zos.finish()
                zos.close()
            }
        }
    }

    /**
     * Zip the file.
     *
     * @param srcFilePath The path of source file.
     * @param zipFilePath The path of ZIP file.
     * @return `true`: success<br></br>`false`: fail
     * @throws IOException if an I/O error has occurred
     */
    @Throws(IOException::class)
    fun zipFile(
        srcFilePath: String?,
        zipFilePath: String?
    ): Boolean {
        return zipFile(
            FileUtils.getFileByPath(srcFilePath),
            FileUtils.getFileByPath(zipFilePath),
            null
        )
    }

    /**
     * Zip the file.
     *
     * @param srcFilePath The path of source file.
     * @param zipFilePath The path of ZIP file.
     * @param comment     The comment.
     * @return `true`: success<br></br>`false`: fail
     * @throws IOException if an I/O error has occurred
     */
    @Throws(IOException::class)
    fun zipFile(
        srcFilePath: String?,
        zipFilePath: String?,
        comment: String?
    ): Boolean {
        return zipFile(
            FileUtils.getFileByPath(srcFilePath),
            FileUtils.getFileByPath(zipFilePath),
            comment
        )
    }
    /**
     * Zip the file.
     *
     * @param srcFile The source of file.
     * @param zipFile The ZIP file.
     * @param comment The comment.
     * @return `true`: success<br></br>`false`: fail
     * @throws IOException if an I/O error has occurred
     */
    @JvmOverloads
    @Throws(IOException::class)
    fun zipFile(
        srcFile: File?,
        zipFile: File?,
        comment: String? = null
    ): Boolean {
        if (srcFile == null || zipFile == null) return false
        var zos: ZipOutputStream? = null
        return try {
            zos = ZipOutputStream(FileOutputStream(zipFile))
            zipFile(srcFile, "", zos, comment)
        } finally {
            zos?.close()
        }
    }

    @Throws(IOException::class)
    private fun zipFile(
        srcFile: File?,
        rootPath: String,
        zos: ZipOutputStream,
        comment: String?
    ): Boolean {
        if (null == srcFile) return true
        var rootPath = rootPath
        rootPath = rootPath + (if (isSpace(rootPath)) "" else File.separator) + srcFile.name
        if (srcFile.isDirectory) {
            val fileList = srcFile.listFiles()
            if (fileList == null || fileList.size <= 0) {
                val entry = ZipEntry("$rootPath/")
                entry.comment = comment
                zos.putNextEntry(entry)
                zos.closeEntry()
            } else {
                for (file in fileList) {
                    if (!zipFile(file, rootPath, zos, comment)) return false
                }
            }
        } else {
            var `is`: InputStream? = null
            try {
                `is` = BufferedInputStream(FileInputStream(srcFile))
                val entry = ZipEntry(rootPath)
                entry.comment = comment
                zos.putNextEntry(entry)
                val buffer = ByteArray(BUFFER_LEN)
                var len: Int
                while (`is`.read(buffer, 0, BUFFER_LEN).also { len = it } != -1) {
                    zos.write(buffer, 0, len)
                }
                zos.closeEntry()
            } finally {
                `is`?.close()
            }
        }
        return true
    }

    /**
     * Unzip the file.
     *
     * @param zipFilePath The path of ZIP file.
     * @param destDirPath The path of destination directory.
     * @return the unzipped files
     * @throws IOException if unzip unsuccessfully
     */
    @Throws(IOException::class)
    fun unzipFile(
        zipFilePath: String?,
        destDirPath: String?
    ): List<File>? {
        return unzipFileByKeyword(zipFilePath, destDirPath, null)
    }

    /**
     * Unzip the file.
     *
     * @param zipFile The ZIP file.
     * @param destDir The destination directory.
     * @return the unzipped files
     * @throws IOException if unzip unsuccessfully
     */
    @Throws(IOException::class)
    fun unzipFile(
        zipFile: File?,
        destDir: File?
    ): List<File>? {
        return unzipFileByKeyword(zipFile, destDir, null)
    }

    /**
     * Unzip the file by keyword.
     *
     * @param zipFilePath The path of ZIP file.
     * @param destDirPath The path of destination directory.
     * @param keyword     The keyboard.
     * @return the unzipped files
     * @throws IOException if unzip unsuccessfully
     */
    @Throws(IOException::class)
    fun unzipFileByKeyword(
        zipFilePath: String?,
        destDirPath: String?,
        keyword: String?
    ): List<File>? {
        return unzipFileByKeyword(
            FileUtils.getFileByPath(zipFilePath),
            FileUtils.getFileByPath(destDirPath),
            keyword
        )
    }

    /**
     * Unzip the file by keyword.
     *
     * @param zipFile The ZIP file.
     * @param destDir The destination directory.
     * @param keyword The keyboard.
     * @return the unzipped files
     * @throws IOException if unzip unsuccessfully
     */
    @Throws(IOException::class)
    fun unzipFileByKeyword(
        zipFile: File?,
        destDir: File?,
        keyword: String?
    ): List<File>? {
        if (zipFile == null || destDir == null) return null
        val files: MutableList<File> = ArrayList()
        val zip = ZipFile(zipFile)
        val entries: Enumeration<*> = zip.entries()
        try {
            if (keyword.isEmpty()) {
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement() as ZipEntry
                    val entryName = entry.name.replace("\\", "/")
                    if (entryName.contains("../")) {
                        Log.e("ZipUtils", "entryName: $entryName is dangerous!")
                        continue
                    }
                    if (!unzipChildFile(destDir, files, zip, entry, entryName)) return files
                }
            } else {
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement() as ZipEntry
                    val entryName = entry.name.replace("\\", "/")
                    if (entryName.contains("../")) {
                        Log.e("ZipUtils", "entryName: $entryName is dangerous!")
                        continue
                    }
                    if (entryName.contains(keyword!!)) {
                        if (!unzipChildFile(destDir, files, zip, entry, entryName)) return files
                    }
                }
            }
        } finally {
            zip.close()
        }
        return files
    }

    @Throws(IOException::class)
    private fun unzipChildFile(
        destDir: File,
        files: MutableList<File>,
        zip: ZipFile,
        entry: ZipEntry,
        name: String
    ): Boolean {
        val file = File(destDir, name)
        files.add(file)
        if (entry.isDirectory) {
            return FileUtils.createOrExistsDir(file)
        } else {
            if (!FileUtils.createOrExistsFile(file)) return false
            var `in`: InputStream? = null
            var out: OutputStream? = null
            try {
                `in` = BufferedInputStream(zip.getInputStream(entry))
                out = BufferedOutputStream(FileOutputStream(file))
                val buffer = ByteArray(BUFFER_LEN)
                var len: Int
                while (`in`.read(buffer).also { len = it } != -1) {
                    out.write(buffer, 0, len)
                }
            } finally {
                `in`?.close()
                out?.close()
            }
        }
        return true
    }

    /**
     * Return the files' path in ZIP file.
     *
     * @param zipFilePath The path of ZIP file.
     * @return the files' path in ZIP file
     * @throws IOException if an I/O error has occurred
     */
    @Throws(IOException::class)
    fun getFilesPath(zipFilePath: String?): List<String>? {
        return getFilesPath(FileUtils.getFileByPath(zipFilePath))
    }

    /**
     * Return the files' path in ZIP file.
     *
     * @param zipFile The ZIP file.
     * @return the files' path in ZIP file
     * @throws IOException if an I/O error has occurred
     */
    @Throws(IOException::class)
    fun getFilesPath(zipFile: File?): List<String>? {
        if (zipFile == null) return null
        val paths: MutableList<String> = ArrayList()
        val zip = ZipFile(zipFile)
        val entries: Enumeration<*> = zip.entries()
        while (entries.hasMoreElements()) {
            val entryName = (entries.nextElement() as ZipEntry).name.replace("\\", "/")
            if (entryName.contains("../")) {
                Log.e("ZipUtils", "entryName: $entryName is dangerous!")
                paths.add(entryName)
            } else {
                paths.add(entryName)
            }
        }
        zip.close()
        return paths
    }

    /**
     * Return the files' comment in ZIP file.
     *
     * @param zipFilePath The path of ZIP file.
     * @return the files' comment in ZIP file
     * @throws IOException if an I/O error has occurred
     */
    @Throws(IOException::class)
    fun getComments(zipFilePath: String?): List<String>? {
        return getComments(FileUtils.getFileByPath(zipFilePath))
    }

    /**
     * Return the files' comment in ZIP file.
     *
     * @param zipFile The ZIP file.
     * @return the files' comment in ZIP file
     * @throws IOException if an I/O error has occurred
     */
    @Throws(IOException::class)
    fun getComments(zipFile: File?): List<String>? {
        if (zipFile == null) return null
        val comments: MutableList<String> = ArrayList()
        val zip = ZipFile(zipFile)
        val entries: Enumeration<*> = zip.entries()
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement() as ZipEntry
            comments.add(entry.comment)
        }
        zip.close()
        return comments
    }
}