package miotaxi.aidemo.naturalLanguage

/**
 * Операция отправки команды на естественном языке.
 *
 * Принимает текст команды от пользователя и возвращает результат её обработки.
 */
interface SendNaturalLanguageCommandOperation {
    suspend fun execute(request: Request): Response

    /** Запрос с текстом команды на естественном языке. */
    interface Request {
        /** Текст команды. */
        val text: String
    }

    /** Результат выполнения команды. */
    interface Response {
        /** Признак успешного выполнения команды. */
        val success: Boolean

        /** Текст ответа при успехе или текст ошибки при неудаче. */
        val text: String
    }
}
