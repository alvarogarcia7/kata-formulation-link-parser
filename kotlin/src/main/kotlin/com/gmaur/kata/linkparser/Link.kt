package com.gmaur.kata.linkparser

import arrow.core.Either
import arrow.core.Id

data class Link(val href: String, val rel: String, val method: String) {
    companion object {
        fun self(first: Pair<String, Id<String>>, vararg resources: Pair<String, Id<String>>): Link {
            return Link(buildHref(arrayOf(first, *resources)), "self", "GET")
        }

        private fun buildHref(resources: Array<out Pair<String, Id<String>>>): String {
            return "/" + resources
                    .map { "${it.first}/${it.second.value}" }
                    .joinToString("/")
        }

        fun parse(value: String): Either<List<Exception>, Link> {
            fun inPairs(it: String): Either<List<Exception>, List<Pair<String, String>>> {
                val values = it.split("/")
                if (values.size == 1) { // a single chunk
                    return Either.left(listOf(Exception("no resources")))
                }
                var i = 1 // need to skip the first slash
                val result: MutableList<Pair<String, String>> = mutableListOf()
                val exceptions = mutableListOf<Exception>()
                while (i < values.size - 1) {
                    val resource = values[i]
                    if (resource.isNullOrBlank()) {
                        exceptions.add(Exception("no resource"))
                    }
                    if (i + 1 < values.size) {
                        val id = values[i + 1]
                        i += 2
                        if (id.isNullOrBlank()) {
                            exceptions.add(Exception("no resource id"))
                            break
                        }
                        result.add(Pair(resource, id))
                    } else {
                        break
                    }
                }
                if (i == 1) {
                    exceptions.add(Exception("no resource"))
                }
                return if (result.isEmpty()) {
                    Either.left(exceptions)
                } else {
                    Either.right(result.toList())
                }
            }

            val pairs = inPairs(value)
                    .map { it -> it.map { (resource, idValue) -> Pair(resource, Id(idValue)) } }
                    .map { pairs -> self(pairs.first(), *pairs.subList(1, pairs.size).toTypedArray()) }
            return pairs
        }
    }

}