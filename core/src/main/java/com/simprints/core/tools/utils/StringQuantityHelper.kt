package com.simprints.core.tools.utils

import android.content.Context
import android.content.res.Resources

private enum class Quantity constructor(private val title: String) {
    ZERO("zero"),
    ONE("one"),
    TWO("two"),
    FEW("few"),
    MANY("many"),
    OTHER("other");

    override fun toString(): String {
        return title
    }
}

object QuantityHelper {

    /**
     * Converts an integer to the grammatical concept of quantity. This is an incomplete conversion
     * but works for most languages. More advanced processing is required for languages that treat
     * numbers that end in zero, one, and two differently (e.g. 22 in Russian).
     *
     * @param quantity The integer number that describes the quantity
     * @return The heuristic description of the number
     */
    private fun intToQuantity(quantity: Int): Quantity {
        when (quantity) {
            0 -> return Quantity.ZERO
            1 -> return Quantity.ONE
            2 -> return Quantity.TWO
            3, 4 -> return Quantity.FEW
        }

        return if (quantity > 4) Quantity.MANY else Quantity.OTHER
    }

    /**
     * A translation-friendly way to access pluralised string resources. Some assumptions are made
     * about the names of the child string resources. Always make sure there is an "other" version
     * of the plural as this will be used as the default. Besides "other", only include plurals in
     * strings.xml that are required by the language.
     *
     * @param context The parent context
     * @param stringQuantityKey The R.string id of the key of the target resource
     * @param quantity The number to use in deciding which plural to use
     * @param values The values used to format the string
     * @return The formatted final string
     */
    @JvmStatic
    internal fun getStringPlural(context: Context, stringQuantityKey: Int, quantity: Int, params: Array<Any>): String {
        val res = context.resources

        return try {
            val targetStringResourceName = res.getString(stringQuantityKey) + "_" + intToQuantity(quantity).toString()
            val targetStringResourceId = res.getIdentifier(targetStringResourceName, "string", context.packageName)
            res.getString(targetStringResourceId, *params)
        } catch (e: Resources.NotFoundException) {
            // If we can't find the resource, try instead to find the "other" version
            val targetStringResourceName = res.getString(stringQuantityKey) + "_" + Quantity.OTHER.toString()
            val targetStringResourceId = res.getIdentifier(targetStringResourceName, "string", context.packageName)
            res.getString(targetStringResourceId, *params)
        }
    }
}