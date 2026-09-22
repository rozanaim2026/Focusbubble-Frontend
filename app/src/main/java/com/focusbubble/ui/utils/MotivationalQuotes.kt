package com.focusbubble.ui.utils

object MotivationalQuotes {

    val allCategories = listOf(
        "Motivational", "Focus", "Productivity", "Mindfulness",
        "Inspirational", "Breakup Motivational", "Mother Love"
    )

    private data class Quote(val category: String, val text: String)

    private val quotes = listOf(
        // Motivational
        Quote("Motivational", "Discipline is choosing between what you want now and what you want most."),
        Quote("Motivational", "Small steps every day lead to big results."),
        Quote("Motivational", "You don't have to be great to start, but you have to start to be great."),
        Quote("Motivational", "Push yourself, because no one else is going to do it for you."),

        // Focus
        Quote("Focus", "One distraction at a time avoided is one step closer to your goal."),
        Quote("Focus", "Concentrate all your thoughts on the task at hand."),
        Quote("Focus", "What you focus on grows."),
        Quote("Focus", "Every minute you focus now is a minute you won't regret later."),

        // Productivity
        Quote("Productivity", "Focus on being productive instead of busy."),
        Quote("Productivity", "Deep work is the superpower of the 21st century."),
        Quote("Productivity", "Success is the sum of small efforts repeated daily."),
        Quote("Productivity", "The secret of getting ahead is getting started."),

        // Mindfulness
        Quote("Mindfulness", "Wherever you are, be there fully."),
        Quote("Mindfulness", "One calm breath can reset an anxious mind."),
        Quote("Mindfulness", "Slow down. The present moment is the only one you truly have."),
        Quote("Mindfulness", "Stillness is where clarity begins."),

        // Inspirational
        Quote("Inspirational", "Great things never came from comfort zones."),
        Quote("Inspirational", "Your future is created by what you do today, not tomorrow."),
        Quote("Inspirational", "The way to get started is to quit talking and begin doing."),
        Quote("Inspirational", "Do something today that your future self will thank you for."),

        // Breakup Motivational
        Quote("Breakup Motivational", "Every ending makes room for a better beginning."),
        Quote("Breakup Motivational", "You are not losing them — you are finding yourself."),
        Quote("Breakup Motivational", "Healing isn't linear, and that's okay. Keep going."),
        Quote("Breakup Motivational", "The right person will never require you to lose yourself."),

        // Mother Love
        Quote("Mother Love", "A mother's love is the quiet strength behind every step forward."),
        Quote("Mother Love", "Make her proud — every focused hour is a gift back to her sacrifices."),
        Quote("Mother Love", "The people who love us most just want to see us try."),
        Quote("Mother Love", "Someone out there believes in you more than you believe in yourself.")
    )

    /** Returns a random quote, avoiding an exact repeat of [previous] when possible. */
    fun random(previous: String? = null): String {
        return randomFrom(quotes.map { it.text }, previous)
    }

    /**
     * Returns a random quote from only the given [categories]. Falls back to the
     * full unfiltered list if [categories] is empty or matches nothing, so the
     * user is never shown a blank state just because they haven't picked yet.
     */
    fun random(categories: Set<String>, previous: String? = null): String {
        val pool = if (categories.isEmpty()) {
            quotes
        } else {
            quotes.filter { it.category in categories }.ifEmpty { quotes }
        }
        return randomFrom(pool.map { it.text }, previous)
    }

    private fun randomFrom(pool: List<String>, previous: String?): String {
        if (pool.isEmpty()) return ""
        if (pool.size <= 1) return pool.first()
        var next: String
        do {
            next = pool.random()
        } while (next == previous)
        return next
    }
}
