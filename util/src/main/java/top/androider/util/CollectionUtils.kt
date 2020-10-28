package top.androider.util

import java.util.*

/**
 * <pre>
 * author: blankj
 * blog  : http://blankj.com
 * time  : 2019/07/26
 * desc  : utils about collection
</pre> *
 */
object CollectionUtils {
    /**
     * Returns a [Collection] containing the union
     * of the given [Collection]s.
     *
     *
     * The cardinality of each element in the returned [Collection]
     * will be equal to the maximum of the cardinality of that element
     * in the two given [Collection]s.
     *
     * @param a the first collection
     * @param b the second collection
     * @return the union of the two collections
     * @see Collection.addAll
     */
    fun union(a: Collection<Any>?, b: Collection<Any>?): Collection<Any> {
        if (a == null && b == null) return ArrayList<Any>()
        if (a == null) return ArrayList<Any>(b!!)
        if (b == null) return ArrayList<Any>(a)
        val list = ArrayList<Any>()
        val mapA = getCardinalityMap(a)
        val mapB = getCardinalityMap(b)
        val elts: MutableSet<Any> = HashSet<Any>(a)
        elts.addAll(b)
        for (obj in elts) {
            var i = 0
            val m = Math.max(getFreq(obj, mapA), getFreq(obj, mapB))
            while (i < m) {
                list.add(obj)
                i++
            }
        }
        return list
    }

    /**
     * Returns a [Collection] containing the intersection
     * of the given [Collection]s.
     *
     *
     * The cardinality of each element in the returned [Collection]
     * will be equal to the minimum of the cardinality of that element
     * in the two given [Collection]s.
     *
     * @param a the first collection
     * @param b the second collection
     * @return the intersection of the two collections
     * @see Collection.retainAll
     */
    fun intersection(a: Collection<Any>?, b: Collection<Any>?): Collection<Any> {
        if (a == null || b == null) return ArrayList<Any>()
        val list = ArrayList<Any>()
        val mapA: Map<*, *> = getCardinalityMap(a)
        val mapB: Map<*, *> = getCardinalityMap(b)
        val elts: MutableSet<Any> = HashSet<Any>(a)
        elts.addAll(b)
        for (obj in elts) {
            var i = 0
            val m = Math.min(getFreq(obj, mapA), getFreq(obj, mapB))
            while (i < m) {
                list.add(obj)
                i++
            }
        }
        return list
    }

    private fun getFreq(obj: Any?, freqMap: Map<*, *>): Int {
        val count = freqMap[obj] as Int?
        return count ?: 0
    }

    /**
     * Returns a [Collection] containing the exclusive disjunction
     * (symmetric difference) of the given [Collection]s.
     *
     *
     * The cardinality of each element *e* in the returned [Collection]
     * will be equal to
     * <tt>max(cardinality(*e*,*a*),cardinality(*e*,*b*)) - min(cardinality(*e*,*a*),cardinality(*e*,*b*))</tt>.
     *
     *
     * This is equivalent to
     * <tt>[subtract][.subtract]([union(a,b)][.union],[intersection(a,b)][.intersection])</tt>
     * or
     * <tt>[union][.union]([subtract(a,b)][.subtract],[subtract(b,a)][.subtract])</tt>.
     *
     * @param a the first collection
     * @param b the second collection
     * @return the symmetric difference of the two collections
     */
    fun disjunction(a: Collection<Any>?, b: Collection<Any>?): Collection<Any> {
        if (a == null && b == null) return ArrayList<Any>()
        if (a == null) return ArrayList<Any>(b!!)
        if (b == null) return ArrayList<Any>(a)
        val list = ArrayList<Any>()
        val mapA: Map<*, *> = getCardinalityMap(a)
        val mapB: Map<*, *> = getCardinalityMap(b)
        val elts: MutableSet<Any> = HashSet<Any>(a)
        elts.addAll(b)
        elts.forEach {
            obj ->
            var i = 0
            val m = (Math.max(getFreq(obj, mapA), getFreq(obj, mapB))
                    - Math.min(getFreq(obj, mapA), getFreq(obj, mapB)))
            while (i < m) {
                list.add(obj)
                i++
            }
        }
        return list
    }

    /**
     * Returns a new [Collection] containing <tt>*a* - *b*</tt>.
     * The cardinality of each element *e* in the returned [Collection]
     * will be the cardinality of *e* in *a* minus the cardinality
     * of *e* in *b*, or zero, whichever is greater.
     *
     * @param a the collection to subtract from
     * @param b the collection to subtract
     * @return a new collection with the results
     * @see Collection.removeAll
     */
    fun subtract(a: Collection<Any>?, b: Collection<Any>?): Collection<Any> {
        if (a == null) return ArrayList<Any>()
        if (b == null) return ArrayList<Any>(a)
        val list = ArrayList<Any>(a)
        for (o in b) {
            list.remove(o)
        }
        return list
    }

    /**
     * Returns `true` iff at least one element is in both collections.
     *
     *
     * In other words, this method returns `true` iff the
     * [.intersection] of *coll1* and *coll2* is not empty.
     *
     * @param coll1 the first collection
     * @param coll2 the first collection
     * @return `true` iff the intersection of the collections is non-empty
     * @see .intersection
     */
    fun containsAny(coll1: Collection<Any>?, coll2: Collection<Any>?): Boolean {
        if (coll1 == null || coll2 == null) return false
        if (coll1.size < coll2.size) {
            for (o in coll1) {
                if (coll2.contains(o)) {
                    return true
                }
            }
        } else {
            for (o in coll2) {
                if (coll1.contains(o)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Returns a [Map] mapping each unique element in the given
     * [Collection] to an [Integer] representing the number
     * of occurrences of that element in the [Collection].
     *
     *
     * Only those elements present in the collection will appear as
     * keys in the map.
     *
     * @param coll the collection to get the cardinality map for, must not be null
     * @return the populated cardinality map
     */
    fun getCardinalityMap(coll: Collection<Any>?): Map<Any, Int> {
        val count: MutableMap<Any, Int> = HashMap()
        if (coll == null) return count
        for (obj in coll) {
            val c = count[obj]
            if (c == null) {
                count[obj] = 1
            } else {
                count[obj] = c + 1
            }
        }
        return count
    }

    /**
     * Returns <tt>true</tt> iff *a* is a sub-collection of *b*,
     * that is, iff the cardinality of *e* in *a* is less
     * than or equal to the cardinality of *e* in *b*,
     * for each element *e* in *a*.
     *
     * @param a the first (sub?) collection
     * @param b the second (super?) collection
     * @return `true` iff *a* is a sub-collection of *b*
     * @see .isProperSubCollection
     *
     * @see Collection.containsAll
     */
    fun isSubCollection(a: Collection<Any>?, b: Collection<Any>?): Boolean {
        if (a == null || b == null) return false
        val mapA: Map<*, *> = getCardinalityMap(a)
        val mapB: Map<*, *> = getCardinalityMap(b)
        for (obj in a) {
            if (getFreq(obj, mapA) > getFreq(obj, mapB)) {
                return false
            }
        }
        return true
    }

    /**
     * Returns <tt>true</tt> iff *a* is a *proper* sub-collection of *b*,
     * that is, iff the cardinality of *e* in *a* is less
     * than or equal to the cardinality of *e* in *b*,
     * for each element *e* in *a*, and there is at least one
     * element *f* such that the cardinality of *f* in *b*
     * is strictly greater than the cardinality of *f* in *a*.
     *
     *
     * The implementation assumes
     *
     *  * `a.size()` and `b.size()` represent the
     * total cardinality of *a* and *b*, resp.
     *  * `a.size() < Integer.MAXVALUE`
     *
     *
     * @param a the first (sub?) collection
     * @param b the second (super?) collection
     * @return `true` iff *a* is a *proper* sub-collection of *b*
     * @see .isSubCollection
     *
     * @see Collection.containsAll
     */
    fun isProperSubCollection(a: Collection<Any>?, b: Collection<Any>?): Boolean {
        return if (a == null || b == null) false else a.size < b.size && isSubCollection(
            a,
            b
        )
    }

    /**
     * Returns <tt>true</tt> iff the given [Collection]s contain
     * exactly the same elements with exactly the same cardinalities.
     *
     *
     * That is, iff the cardinality of *e* in *a* is
     * equal to the cardinality of *e* in *b*,
     * for each element *e* in *a* or *b*.
     *
     * @param a the first collection
     * @param b the second collection
     * @return `true` iff the collections contain the same elements with the same cardinalities.
     */
    fun isEqualCollection(a: Collection<Any>?, b: Collection<Any>?): Boolean {
        if (a == null || b == null) return false
        return if (a.size != b.size) {
            false
        } else {
            val mapA: Map<*, *> = getCardinalityMap(a)
            val mapB: Map<*, *> = getCardinalityMap(b)
            if (mapA.size != mapB.size) {
                false
            } else {

                mapA.keys.forEach {
                    obj ->
                    if (null != obj){
                        if (getFreq(obj, mapA) != getFreq(obj, mapB)) {
                            return false
                        }
                    }
                }
                true
            }
        }
    }

    /**
     * Returns the number of occurrences of *obj* in *coll*.
     *
     * @param obj  the object to find the cardinality of
     * @param coll the collection to search
     * @return the the number of occurrences of obj in coll
     */
    fun <E> cardinality(obj: E?, coll: Collection<E?>?): Int {
        if (coll == null) return 0
        if (coll is Set<*>) {
            return if (coll.contains(obj)) 1 else 0
        }
        var count = 0
        if (obj == null) {
            for (e in coll) {
                if (e == null) {
                    count++
                }
            }
        } else {
            for (e in coll) {
                if (obj == e) {
                    count++
                }
            }
        }
        return count
    }

    /**
     * Finds the first element in the given collection which matches the given predicate.
     *
     *
     * If the input collection or predicate is null, or no element of the collection
     * matches the predicate, null is returned.
     *
     * @param collection the collection to search, may be null
     * @param predicate  the predicate to use, may be null
     * @return the first element of the collection which matches the predicate or null if none could be found
     */
    fun <E> find(collection: Collection<E>?, predicate: Predicate<E>?): E? {
        if (collection == null || predicate == null) return null
        for (item in collection) {
            if (predicate.evaluate(item)) {
                return item
            }
        }
        return null
    }

    /**
     * Executes the given closure on each element in the collection.
     *
     *
     * If the input collection or closure is null, there is no change made.
     *
     * @param collection the collection to get the input from, may be null
     * @param closure    the closure to perform, may be null
     */
    fun <E> forAllDo(collection: Collection<E>?, closure: Closure<E>?) {
        if (collection == null || closure == null) return
        var index = 0
        for (e in collection) {
            closure.execute(index++, e)
        }
    }

    /**
     * Filter the collection by applying a Predicate to each element. If the
     * predicate returns false, remove the element.
     *
     *
     * If the input collection or predicate is null, there is no change made.
     *
     * @param collection the collection to get the input from, may be null
     * @param predicate  the predicate to use as a filter, may be null
     */
    fun <E> filter(collection: MutableCollection<E>?, predicate: Predicate<E>?) {
        if (collection == null || predicate == null) return
        val it: MutableIterator<*> = collection.iterator()
        while (it.hasNext()) {
            if (!predicate.evaluate(it.next() as E)) {
                it.remove()
            }
        }
    }

    /**
     * Selects all elements from input collection which match the given predicate
     * into an output collection.
     *
     *
     * A `null` predicate matches no elements.
     *
     * @param inputCollection the collection to get the input from, may not be null
     * @param predicate       the predicate to use, may be null
     * @return the elements matching the predicate (new list)
     * @throws NullPointerException if the input collection is null
     */
    fun <E> select(inputCollection: Collection<E>?, predicate: Predicate<E>?): Collection<E> {
        if (inputCollection == null || predicate == null) return ArrayList()
        val answer = ArrayList<E>(inputCollection.size)
        for (o in inputCollection) {
            if (predicate.evaluate(o)) {
                answer.add(o)
            }
        }
        return answer
    }

    /**
     * Selects all elements from inputCollection which don't match the given predicate
     * into an output collection.
     *
     *
     * If the input predicate is `null`, the result is an empty list.
     *
     * @param inputCollection the collection to get the input from, may not be null
     * @param predicate       the predicate to use, may be null
     * @return the elements **not** matching the predicate (new list)
     * @throws NullPointerException if the input collection is null
     */
    fun <E> selectRejected(
        inputCollection: Collection<E>?,
        predicate: Predicate<E>?
    ): Collection<E> {
        if (inputCollection == null || predicate == null) return ArrayList()
        val answer = ArrayList<E>(inputCollection.size)
        for (o in inputCollection) {
            if (!predicate.evaluate(o)) {
                answer.add(o)
            }
        }
        return answer
    }

    /**
     * Transform the collection by applying a Transformer to each element.
     *
     *
     * If the input collection or transformer is null, there is no change made.
     *
     *
     * This routine is best for Lists, for which set() is used to do the
     * transformations "in place."  For other Collections, clear() and addAll()
     * are used to replace elements.
     *
     *
     * If the input collection controls its input, such as a Set, and the
     * Transformer creates duplicates (or are otherwise invalid), the
     * collection may reduce in size due to calling this method.
     *
     * @param collection  the collection to get the input from, may be null
     * @param transformer the transformer to perform, may be null
     */
    fun <E1 : Any, E2 : Any> transform(collection: MutableCollection<E1>?, transformer: Transformer<E1, E2>?):MutableCollection<E2>? {
        if (collection == null || transformer == null) return null
        val res :MutableCollection<E2> = mutableListOf()
        collection.forEach { res.add(transformer.transform(it)) }
        return res
    }

    /**
     * Returns a new Collection consisting of the elements of inputCollection transformed
     * by the given transformer.
     *
     *
     * If the input transformer is null, the result is an empty list.
     *
     * @param inputCollection the collection to get the input from, may be null
     * @param transformer     the transformer to use, may be null
     * @return the transformed result (new list)
     */
    fun <E1, E2> collect(
        inputCollection: Collection<E1>?,
        transformer: Transformer<E1, E2>?
    ): Collection<E2> {
        val answer: MutableList<E2> = ArrayList()
        if (inputCollection == null || transformer == null) return answer
        for (e1 in inputCollection) {
            answer.add(transformer.transform(e1))
        }
        return answer
    }

    /**
     * Counts the number of elements in the input collection that match the predicate.
     *
     *
     * A `null` collection or predicate matches no elements.
     *
     * @param collection the collection to get the input from, may be null
     * @param predicate  the predicate to use, may be null
     * @return the number of matches for the predicate in the collection
     */
    fun <E> countMatches(collection: Collection<E>?, predicate: Predicate<E>?): Int {
        if (collection == null || predicate == null) return 0
        var count = 0
        for (o in collection) {
            if (predicate.evaluate(o)) {
                count++
            }
        }
        return count
    }

    /**
     * Answers true if a predicate is true for at least one element of a collection.
     *
     *
     * A `null` collection or predicate returns false.
     *
     * @param collection the collection to get the input from, may be null
     * @param predicate  the predicate to use, may be null
     * @return true if at least one element of the collection matches the predicate
     */
    fun <E> exists(collection: Collection<E>?, predicate: Predicate<E>?): Boolean {
        if (collection == null || predicate == null) return false
        for (o in collection) {
            if (predicate.evaluate(o)) {
                return true
            }
        }
        return false
    }

    /**
     * Adds an element to the collection unless the element is null.
     *
     * @param collection the collection to add to, may be null
     * @param object     the object to add, if null it will not be added
     * @return true if the collection changed
     */
    fun <E> addIgnoreNull(collection: MutableCollection<E>?, `object`: E?): Boolean {
        return if (collection == null) false else `object` != null && collection.add(`object`)
    }

    /**
     * Adds all elements in the iteration to the given collection.
     *
     * @param collection the collection to add to, may be null
     * @param iterator   the iterator of elements to add, may be null
     */
    fun <E> addAll(collection: MutableCollection<E>?, iterator: Iterator<E>?) {
        if (collection == null || iterator == null) return
        while (iterator.hasNext()) {
            collection.add(iterator.next())
        }
    }

    /**
     * Adds all elements in the enumeration to the given collection.
     *
     * @param collection  the collection to add to, may be null
     * @param enumeration the enumeration of elements to add, may be null
     */
    fun <E> addAll(collection: MutableCollection<E>?, enumeration: Enumeration<E>?) {
        if (collection == null || enumeration == null) return
        while (enumeration.hasMoreElements()) {
            collection.add(enumeration.nextElement())
        }
    }

    /**
     * Adds all elements in the array to the given collection.
     *
     * @param collection the collection to add to, may be null
     * @param elements   the array of elements to add, may be null
     */
    fun <E> addAll(collection: MutableCollection<E>?, elements: Array<E>?) {
        if (collection == null || elements == null || elements.size == 0) return
        collection.addAll(Arrays.asList(*elements))
    }

    /**
     * Returns the `index`-th value in `object`, throwing
     * `IndexOutOfBoundsException` if there is no such element or
     * `IllegalArgumentException` if `object` is not an
     * instance of one of the supported types.
     *
     *
     * The supported types, and associated semantics are:
     *
     *  *  Map -- the value returned is the `Map.Entry` in position
     * `index` in the map's `entrySet` iterator,
     * if there is such an entry.
     *  *  List -- this method is equivalent to the list's get method.
     *  *  Array -- the `index`-th array entry is returned,
     * if there is such an entry; otherwise an `IndexOutOfBoundsException`
     * is thrown.
     *  *  Collection -- the value returned is the `index`-th object
     * returned by the collection's default iterator, if there is such an element.
     *  *  Iterator or Enumeration -- the value returned is the
     * `index`-th object in the Iterator/Enumeration, if there
     * is such an element.  The Iterator/Enumeration is advanced to
     * `index` (or to the end, if `index` exceeds the
     * number of entries) as a side effect of this method.
     *
     *
     * @param object the object to get a value from
     * @param index  the index to get
     * @return the object at the specified index
     * @throws IndexOutOfBoundsException if the index is invalid
     * @throws IllegalArgumentException  if the object type is invalid
     */
    operator fun get(`object`: Any?, index: Int): Any? {
        var index = index
        if (`object` == null) return null
        if (index < 0) {
            throw IndexOutOfBoundsException("Index cannot be negative: $index")
        }
        return if (`object` is Map<*, *>) {
            val iterator: Iterator<*> = `object`.entries.iterator()
            CollectionUtils[iterator, index]
        } else if (`object` is List<*>) {
            `object`[index]
        } else if (`object` is Array<*>) {
            (`object` as Array<Any?>)[index]
        } else if (`object` is Iterator<*>) {
            val it = `object`
            while (it.hasNext()) {
                index--
                if (index == -1) {
                    return it.next()
                } else {
                    it.next()
                }
            }
            throw IndexOutOfBoundsException("Entry does not exist: $index")
        } else if (`object` is Collection<*>) {
            val iterator = `object`.iterator()
            CollectionUtils[iterator, index]
        } else if (`object` is Enumeration<*>) {
            val it = `object`
            while (it.hasMoreElements()) {
                index--
                if (index == -1) {
                    return it.nextElement()
                } else {
                    it.nextElement()
                }
            }
            throw IndexOutOfBoundsException("Entry does not exist: $index")
        } else {
            try {
                java.lang.reflect.Array.get(`object`, index)
            } catch (ex: IllegalArgumentException) {
                throw IllegalArgumentException("Unsupported object type: " + `object`.javaClass.name)
            }
        }
    }

    /**
     * Gets the size of the collection/iterator specified.
     *
     *
     * This method can handles objects as follows
     *
     *  * Collection - the collection size
     *  * Map - the map size
     *  * Array - the array size
     *  * Iterator - the number of elements remaining in the iterator
     *  * Enumeration - the number of elements remaining in the enumeration
     *
     *
     * @param object the object to get the size of
     * @return the size of the specified collection
     * @throws IllegalArgumentException thrown if object is not recognised or null
     */
    fun size(`object`: Any?): Int {
        if (`object` == null) return 0
        var total = 0
        if (`object` is Map<*, *>) {
            total = `object`.size
        } else if (`object` is Collection<*>) {
            total = `object`.size
        } else if (`object` is Array<*>) {
            total = (`object` as Array<Any?>).size
        } else if (`object` is Iterator<*>) {
            val it = `object`
            while (it.hasNext()) {
                total++
                it.next()
            }
        } else if (`object` is Enumeration<*>) {
            val it = `object`
            while (it.hasMoreElements()) {
                total++
                it.nextElement()
            }
        } else {
            total = try {
                java.lang.reflect.Array.getLength(`object`)
            } catch (ex: IllegalArgumentException) {
                throw IllegalArgumentException("Unsupported object type: " + `object`.javaClass.name)
            }
        }
        return total
    }

    /**
     * Checks if the specified collection/array/iterator is empty.
     *
     *
     * This method can handles objects as follows
     *
     *  * Collection - via collection isEmpty
     *  * Map - via map isEmpty
     *  * Array - using array size
     *  * Iterator - via hasNext
     *  * Enumeration - via hasMoreElements
     *
     *
     *
     * Note: This method is named to avoid clashing with
     * [.isEmpty].
     *
     * @param object the object to get the size of, not null
     * @return true if empty
     * @throws IllegalArgumentException thrown if object is not recognised or null
     */
    fun sizeIsEmpty(`object`: Any?): Boolean {
        if (`object` == null) return true
        return if (`object` is Collection<*>) {
            `object`.isEmpty()
        } else if (`object` is Map<*, *>) {
            `object`.isEmpty()
        } else if (`object` is Array<*>) {
            (`object` as Array<Any?>).size == 0
        } else if (`object` is Iterator<*>) {
            !`object`.hasNext()
        } else if (`object` is Enumeration<*>) {
            !`object`.hasMoreElements()
        } else {
            try {
                java.lang.reflect.Array.getLength(`object`) == 0
            } catch (ex: IllegalArgumentException) {
                throw IllegalArgumentException("Unsupported object type: " + `object`.javaClass.name)
            }
        }
    }

    /**
     * Null-safe check if the specified collection is empty.
     *
     *
     * Null returns true.
     *
     * @param coll the collection to check, may be null
     * @return true if empty or null
     */
    fun isEmpty(coll: Collection<Any>?): Boolean {
        return coll == null || coll.size == 0
    }

    /**
     * Null-safe check if the specified collection is not empty.
     *
     *
     * Null returns false.
     *
     * @param coll the collection to check, may be null
     * @return true if non-null and non-empty
     */
    fun isNotEmpty(coll: Collection<Any>?): Boolean {
        return !isEmpty(coll)
    }

    /**
     * Returns a collection containing all the elements in `collection`
     * that are also in `retain`. The cardinality of an element `e`
     * in the returned collection is the same as the cardinality of `e`
     * in `collection` unless `retain` does not contain `e`, in which
     * case the cardinality is zero. This method is useful if you do not wish to modify
     * the collection `c` and thus cannot call `c.retainAll(retain);`.
     *
     * @param collection the collection whose contents are the target of the #retailAll operation
     * @param retain     the collection containing the elements to be retained in the returned collection
     * @return a `Collection` containing all the elements of `collection`
     * that occur at least once in `retain`.
     */
    fun <E> retainAll(collection: Collection<E>?, retain: Collection<E>?): Collection<E> {
        if (collection == null || retain == null) return ArrayList()
        val list: MutableList<E> = ArrayList()
        for (item in collection) {
            if (retain.contains(item)) {
                list.add(item)
            }
        }
        return list
    }

    /**
     * Removes the elements in `remove` from `collection`. That is, this
     * method returns a collection containing all the elements in `c`
     * that are not in `remove`. The cardinality of an element `e`
     * in the returned collection is the same as the cardinality of `e`
     * in `collection` unless `remove` contains `e`, in which
     * case the cardinality is zero. This method is useful if you do not wish to modify
     * the collection `c` and thus cannot call `collection.removeAll(remove);`.
     *
     * @param collection the collection from which items are removed (in the returned collection)
     * @param remove     the items to be removed from the returned `collection`
     * @return a `Collection` containing all the elements of `collection` except
     * any elements that also occur in `remove`.
     */
    fun <E> removeAll(collection: Collection<E>?, remove: Collection<E>?): Collection<E> {
        if (collection == null) return ArrayList()
        if (remove == null) return ArrayList(collection)
        val list: MutableList<E> = ArrayList()
        for (obj in collection) {
            if (!remove.contains(obj)) {
                list.add(obj)
            }
        }
        return list
    }

    /**
     * Randomly permutes the specified list using a default source of randomness.
     *
     * @param list the list to be shuffled.
     * @throws UnsupportedOperationException if the specified list or
     * its list-iterator does not support the <tt>set</tt> operation.
     */
    fun <T> shuffle(list: List<T?>?) {
        Collections.shuffle(list)
    }

    /**
     * Return the string of collection.
     *
     * @param collection The collection.
     * @return the string of collection
     */
    fun toString(collection: Collection<Any>?): String {
        return collection?.toString() ?: "null"
    }

    interface Closure<E> {
        fun execute(index: Int, item: E)
    }

    interface Transformer<E1, E2> {
        fun transform(input: E1): E2
    }

    interface Predicate<E> {
        fun evaluate(item: E): Boolean
    }
}