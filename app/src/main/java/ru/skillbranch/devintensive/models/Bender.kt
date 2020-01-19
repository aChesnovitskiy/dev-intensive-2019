package ru.skillbranch.devintensive.models

class Bender(var status: Status = Status.NORMAL, var question: Question = Question.NAME) {

    var wrongAnswerCount = 0

    fun askQuestion(): String = when (question) {
        Question.NAME -> Question.NAME.question
        Question.PROFESSION -> Question.PROFESSION.question
        Question.MATERIAL -> Question.MATERIAL.question
        Question.BDAY -> Question.BDAY.question
        Question.SERIAL -> Question.SERIAL.question
        Question.IDLE -> Question.IDLE.question
    }

    private fun validation(answer: String): Pair<Boolean, String> = when (question) {
        Question.NAME -> {
            if (answer.length != 0 && answer[0].isUpperCase()) {
                true to ""
            } else {
                false to "Имя должно начинаться с заглавной буквы"
            }
        }
        Question.PROFESSION -> {
            if (answer.length != 0 && answer[0].isLowerCase()) {
                true to ""
            } else {
                false to "Профессия должна начинаться со строчной буквы"
            }
        }
        Question.MATERIAL -> {
            if (answer.matches("\\D+".toRegex())) {
                true to ""
            } else {
                false to "Материал не должен содержать цифр"
            }
        }
        Question.BDAY -> {
            if (answer.matches("\\d+".toRegex())) {
                true to ""
            } else {
                false to "Год моего рождения должен содержать только цифры"
            }
        }
        Question.SERIAL -> {
            if (answer.matches("\\d+".toRegex()) && answer.length == 7) {
                true to ""
            } else {
                false to "Серийный номер содержит только цифры, и их 7"
            }
        }
        Question.IDLE -> {
            false to ""
        }
    }

    fun listenAnswer(answer: String): Pair<String, Triple<Int, Int, Int>> {
        val (isValidated, validationFailText) = validation(answer)
        return if (isValidated) {
            if (question.answers.contains(answer.toLowerCase())) {
                question = question.nextQuestion()
                "Отлично - ты справился\n${question.question}" to status.color
            } else {
                wrongAnswerCount++
                when (wrongAnswerCount) {
                    in 1..3 -> {
                        status = status.nextStatus()
                        "Это неправильный ответ\n${question.question}" to status.color
                    }
                    else -> {
                        wrongAnswerCount = 0
                        status = Status.NORMAL
                        question = Question.NAME
                        "Это неправильный ответ. Давай все по новой\n${question.question}" to status.color
                    }
                }
            }
        } else {
            "${validationFailText}\n${question.question}" to status.color
        }
    }

    enum class Status(val color: Triple<Int, Int, Int>) {
        NORMAL(Triple(255, 255, 255)),
        WARNING(Triple(255, 120, 0)),
        DANGER(Triple(255, 60, 60)),
        CRITICAL(Triple(255, 0, 0));

        fun nextStatus(): Status {
            return if (this.ordinal < values().lastIndex) {
                values()[this.ordinal + 1]
            } else {
                values()[0]
            }
        }
    }

    enum class Question(val question: String, val answers: List<String>) {
        NAME("Как меня зовут?", listOf("bender", "бендер")) {
            override fun nextQuestion(): Question = PROFESSION
        },
        PROFESSION("Назови мою профессию?", listOf("bender", "сгибальщик")) {
            override fun nextQuestion(): Question = MATERIAL
        },
        MATERIAL("Из чего я сделан?", listOf("металл", "дерево", "metal", "wood", "iron")) {
            override fun nextQuestion(): Question = BDAY
        },
        BDAY("Когда меня создали?", listOf("2993")) {
            override fun nextQuestion(): Question = SERIAL
        },
        SERIAL("Мой серийный номер?", listOf("2716057")) {
            override fun nextQuestion(): Question = IDLE
        },
        IDLE("На этом все, вопросов больше нет", listOf()) {
            override fun nextQuestion(): Question = IDLE
        };

        abstract fun nextQuestion(): Question
    }
}