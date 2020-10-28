package top.androider.util

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2018/03/21
 * desc  : 坐标相关工具类
</pre> *
 */

private const val X_PI = 3.14159265358979324 * 3000.0 / 180.0
private const val A = 6378245.0
private const val EE = 0.00669342162296594323

/**
 * BD09 坐标转 GCJ02 坐标
 *
 * @param lng BD09 坐标纬度
 * @param lat BD09 坐标经度
 * @return GCJ02 坐标：[经度，纬度]
 */
fun bd09ToGcj02(lng: Double, lat: Double): DoubleArray {
    val x = lng - 0.0065
    val y = lat - 0.006
    val z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * X_PI)
    val theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * X_PI)
    val gg_lng = z * Math.cos(theta)
    val gg_lat = z * Math.sin(theta)
    return doubleArrayOf(gg_lng, gg_lat)
}

/**
 * GCJ02 坐标转 BD09 坐标
 *
 * @param lng GCJ02 坐标经度
 * @param lat GCJ02 坐标纬度
 * @return BD09 坐标：[经度，纬度]
 */
fun gcj02ToBd09(lng: Double, lat: Double): DoubleArray {
    val z = Math.sqrt(lng * lng + lat * lat) + 0.00002 * Math.sin(lat * X_PI)
    val theta = Math.atan2(lat, lng) + 0.000003 * Math.cos(lng * X_PI)
    val bd_lng = z * Math.cos(theta) + 0.0065
    val bd_lat = z * Math.sin(theta) + 0.006
    return doubleArrayOf(bd_lng, bd_lat)
}

/**
 * GCJ02 坐标转 WGS84 坐标
 *
 * @param lng GCJ02 坐标经度
 * @param lat GCJ02 坐标纬度
 * @return WGS84 坐标：[经度，纬度]
 */
fun gcj02ToWGS84(lng: Double, lat: Double): DoubleArray {
    if (outOfChina(lng, lat)) {
        return doubleArrayOf(lng, lat)
    }
    var dlat = transformLat(lng - 105.0, lat - 35.0)
    var dlng = transformLng(lng - 105.0, lat - 35.0)
    val radlat = lat / 180.0 * Math.PI
    var magic = Math.sin(radlat)
    magic = 1 - EE * magic * magic
    val sqrtmagic = Math.sqrt(magic)
    dlat = dlat * 180.0 / (A * (1 - EE) / (magic * sqrtmagic) * Math.PI)
    dlng = dlng * 180.0 / (A / sqrtmagic * Math.cos(radlat) * Math.PI)
    val mglat = lat + dlat
    val mglng = lng + dlng
    return doubleArrayOf(lng * 2 - mglng, lat * 2 - mglat)
}

/**
 * WGS84 坐标转 GCJ02 坐标
 *
 * @param lng WGS84 坐标经度
 * @param lat WGS84 坐标纬度
 * @return GCJ02 坐标：[经度，纬度]
 */
fun wgs84ToGcj02(lng: Double, lat: Double): DoubleArray {
    if (outOfChina(lng, lat)) {
        return doubleArrayOf(lng, lat)
    }
    var dlat = transformLat(lng - 105.0, lat - 35.0)
    var dlng = transformLng(lng - 105.0, lat - 35.0)
    val radlat = lat / 180.0 * Math.PI
    var magic = Math.sin(radlat)
    magic = 1 - EE * magic * magic
    val sqrtmagic = Math.sqrt(magic)
    dlat = dlat * 180.0 / (A * (1 - EE) / (magic * sqrtmagic) * Math.PI)
    dlng = dlng * 180.0 / (A / sqrtmagic * Math.cos(radlat) * Math.PI)
    val mglat = lat + dlat
    val mglng = lng + dlng
    return doubleArrayOf(mglng, mglat)
}

/**
 * BD09 坐标转 WGS84 坐标
 *
 * @param lng BD09 坐标经度
 * @param lat BD09 坐标纬度
 * @return WGS84 坐标：[经度，纬度]
 */
fun bd09ToWGS84(lng: Double, lat: Double): DoubleArray {
    val gcj = bd09ToGcj02(lng, lat)
    return gcj02ToWGS84(gcj[0], gcj[1])
}

/**
 * WGS84 坐标转 BD09 坐标
 *
 * @param lng WGS84 坐标经度
 * @param lat WGS84 坐标纬度
 * @return BD09 坐标：[经度，纬度]
 */
fun wgs84ToBd09(lng: Double, lat: Double): DoubleArray {
    val gcj = wgs84ToGcj02(lng, lat)
    return gcj02ToBd09(gcj[0], gcj[1])
}

private fun transformLat(lng: Double, lat: Double): Double {
    var ret =
        -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * Math.sqrt(
            Math.abs(lng)
        )
    ret += (20.0 * Math.sin(6.0 * lng * Math.PI) + 20.0 * Math.sin(2.0 * lng * Math.PI)) * 2.0 / 3.0
    ret += (20.0 * Math.sin(lat * Math.PI) + 40.0 * Math.sin(lat / 3.0 * Math.PI)) * 2.0 / 3.0
    ret += (160.0 * Math.sin(lat / 12.0 * Math.PI) + 320 * Math.sin(lat * Math.PI / 30.0)) * 2.0 / 3.0
    return ret
}

private fun transformLng(lng: Double, lat: Double): Double {
    var ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * Math.sqrt(
        Math.abs(lng)
    )
    ret += (20.0 * Math.sin(6.0 * lng * Math.PI) + 20.0 * Math.sin(2.0 * lng * Math.PI)) * 2.0 / 3.0
    ret += (20.0 * Math.sin(lng * Math.PI) + 40.0 * Math.sin(lng / 3.0 * Math.PI)) * 2.0 / 3.0
    ret += (150.0 * Math.sin(lng / 12.0 * Math.PI) + 300.0 * Math.sin(lng / 30.0 * Math.PI)) * 2.0 / 3.0
    return ret
}

private fun outOfChina(lng: Double, lat: Double): Boolean {
    return lng < 72.004 || lng > 137.8347 || lat < 0.8293 || lat > 55.8271
}