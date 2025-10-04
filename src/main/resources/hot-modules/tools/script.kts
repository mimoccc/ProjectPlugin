class Tools {
    fun onRun(
        context: Map<String, Any?>
    ): Any? {
        val value = context["input"]?.toString().orEmpty()
        return "Got text: $value"
    }
}
Tools()
