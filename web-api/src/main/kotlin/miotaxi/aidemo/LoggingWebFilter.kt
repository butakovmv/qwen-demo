package miotaxi.aidemo

import org.reactivestreams.Publisher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpRequestDecorator
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.http.server.reactive.ServerHttpResponseDecorator
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.ServerWebExchangeDecorator
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets
import java.util.UUID

@Component
internal class LoggingWebFilter : WebFilter {
    companion object {
        private const val NANOS_TO_MS = 1_000_000
    }

    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> {
        val method = exchange.request.method.toString()
        val path = exchange.request.path.pathWithinApplication().value()

        if (shouldSkip(path)) {
            return chain.filter(exchange)
        }

        val logger = resolveLogger(exchange)
        val startNanos = System.nanoTime()
        logBefore(method, path, exchange.request, logger)

        val responseLog =
            Mono.defer<Void> {
                logAfter(exchange, startNanos, logger)
                Mono.empty()
            }

        return if (!logger.isDebugEnabled) {
            chain.filter(exchange)
                .then(responseLog)
                .doFinally { cleanupMdc() }
        } else {
            val responseCaptor = CapturingResponseDecorator(exchange.response)
            logRequestBody(exchange.request.body, logger)
                .flatMap { requestBytes ->
                    val decoratedExchange = CachedBodyExchange(exchange, requestBytes, responseCaptor)
                    chain.filter(decoratedExchange)
                }
                .then(responseLog)
                .then(
                    Mono.defer<Void> {
                        logResponseBody(responseCaptor, logger)
                        Mono.empty()
                    },
                )
                .doFinally { cleanupMdc() }
        }
    }

    private fun readAndRelease(dataBuffer: DataBuffer): ByteArray {
        val bytes = ByteArray(dataBuffer.readableByteCount())
        dataBuffer.read(bytes)
        DataBufferUtils.release(dataBuffer)
        return bytes
    }

    private fun resolveLogger(exchange: ServerWebExchange): Logger {
        val handlerAttr: Any? =
            exchange.getAttribute(
                HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE,
            )
        val packageName =
            when (handlerAttr) {
                is HandlerMethod -> handlerAttr.beanType.packageName
                else ->
                    handlerAttr?.let { it::class.java.packageName }
                        ?: this::class.java.packageName
            }

        return LoggerFactory.getLogger(packageName)
    }

    private fun logRequestBody(
        body: Flux<DataBuffer>,
        logger: Logger,
    ): Mono<ByteArray> {
        return DataBufferUtils.join(body)
            .map { dataBuffer: DataBuffer -> readAndRelease(dataBuffer) }
            .defaultIfEmpty(ByteArray(0))
            .doOnNext { requestBytes ->
                val bodyStr = String(requestBytes, StandardCharsets.UTF_8)
                if (bodyStr.isNotEmpty()) {
                    logger.debug("--> body: {}", bodyStr)
                }
            }
    }

    private fun logResponseBody(
        responseCaptor: CapturingResponseDecorator,
        logger: Logger,
    ) {
        val responseBody = responseCaptor.capturedBody
        if (responseBody.isNotEmpty()) {
            logger.debug("<-- body: {}", responseBody)
        }
    }

    private fun logBefore(
        method: String,
        path: String,
        request: ServerHttpRequest,
        logger: Logger,
    ) {
        val requestId =
            request.headers.getFirst("X-Request-ID")
                ?: request.headers.getFirst("X-Trace-ID")
                ?: request.headers.getFirst("X-Correlation-ID")
                ?: UUID.randomUUID().toString()
        MDC.put("http.requestId", requestId)
        MDC.put("http.request", "$method $path")
        logger.info("--> {} {}", method, path)
    }

    private fun logAfter(
        exchange: ServerWebExchange,
        startNanos: Long,
        logger: Logger,
    ): Mono<Void> {
        val durationMs = (System.nanoTime() - startNanos) / NANOS_TO_MS
        MDC.put("http.status", exchange.response.statusCode?.toString() ?: "unknown")
        MDC.put("http.duration", "${durationMs}ms")
        val statusLabel =
            when {
                exchange.response.statusCode?.is2xxSuccessful == true -> "Успех"
                exchange.response.statusCode?.is4xxClientError == true ||
                    exchange.response.statusCode?.is5xxServerError == true -> "Ошибка"
                else -> "Служебное"
            }
        logger.info("<-- {} {}ms", statusLabel, durationMs)
        return Mono.empty()
    }

    private fun cleanupMdc() {
        MDC.remove("http.requestId")
        MDC.remove("http.request")
        MDC.remove("http.status")
        MDC.remove("http.duration")
    }

    private fun shouldSkip(path: String): Boolean =
        path.startsWith("/actuator") || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")

    private class CachedBodyExchange(
        delegate: ServerWebExchange,
        requestBytes: ByteArray,
        private val responseCaptor: CapturingResponseDecorator,
    ) : ServerWebExchangeDecorator(delegate) {
        private val cachedRequest = CachedBodyRequest(delegate.request, requestBytes)

        override fun getRequest(): ServerHttpRequest = cachedRequest

        override fun getResponse(): ServerHttpResponse = responseCaptor
    }

    private class CachedBodyRequest(
        delegate: ServerHttpRequest,
        private val bytes: ByteArray,
    ) : ServerHttpRequestDecorator(delegate) {
        override fun getBody(): Flux<DataBuffer> {
            if (bytes.isEmpty()) return Flux.empty()

            return Flux.just(DefaultDataBufferFactory().wrap(bytes))
        }
    }

    private class CapturingResponseDecorator(delegate: ServerHttpResponse) :
        ServerHttpResponseDecorator(delegate) {
        val capturedBody = StringBuilder()

        override fun writeWith(body: Publisher<out DataBuffer>): Mono<Void> {
            val bodyFlux = if (body is Flux) body else Flux.from(body)

            return super.writeWith(
                bodyFlux.map { buffer ->
                    val bytes = ByteArray(buffer.readableByteCount())
                    buffer.read(bytes)
                    capturedBody.append(String(bytes, StandardCharsets.UTF_8))
                    DataBufferUtils.release(buffer)
                    DefaultDataBufferFactory().wrap(bytes)
                },
            )
        }

        override fun writeAndFlushWith(body: Publisher<out Publisher<out DataBuffer>>): Mono<Void> {
            return writeWith(Flux.from(body).flatMap { Flux.from(it) })
        }
    }
}
