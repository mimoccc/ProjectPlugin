package org.mjdev.plugins.projectplugin.modules

data class HotModule(
    val name: String,
    val version: String?,
    val layoutJson: String?,
    val script: String?
) {
    companion object {
        fun error(
            message: String = "Unknown module or module error.",
            version: String = "1.0"
        ) = HotModule(
            "Error",
            version,
            """
            {
                "type"     : "Column",
                "padding"  : 16,
                "children" : [
                {
                    "type" : "Text",
                    "text" : "Hot module layout error. $message"
                },
                {
                    "type" : "Text",
                    "text" : "Check files existence or syntax."
                },
                {
                    "type" : "Text",
                    "text" : "Version $version"
                }
              ]
            }
            """.trimIndent(),
            null
        )
    }
}
