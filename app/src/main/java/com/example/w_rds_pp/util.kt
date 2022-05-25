fun <T> List<T>.split(condition: (T) -> Boolean): List<List<T>> {
    val result = mutableListOf<List<T>>()
    var current = mutableListOf<T>()
    for(it in this) {
        if(condition(it)){
            if(current.isNotEmpty()){
                result.add(current)
                current = mutableListOf()
            }
        } else {
            current.add(it)
        }
    }
    if(current.isNotEmpty()){
        result.add(current)
    }
    return result
}