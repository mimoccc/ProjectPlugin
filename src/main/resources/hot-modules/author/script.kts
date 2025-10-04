package `hot-modules`.author

class Welcome {
    fun onRun(
        context: Map<String, Any?>
    ): Any? {
        val value = context["input"]?.toString().orEmpty()
        return "Got text: $value"
    }
}
Welcome()
