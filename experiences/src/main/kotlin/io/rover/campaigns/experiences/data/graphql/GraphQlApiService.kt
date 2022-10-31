package io.rover.campaigns.experiences.data.graphql

import android.net.Uri
import io.rover.campaigns.experiences.data.http.HttpClient
import io.rover.campaigns.experiences.data.http.HttpRequest
import io.rover.campaigns.experiences.data.http.HttpVerb
import io.rover.campaigns.experiences.logging.log
import io.rover.campaigns.experiences.streams.map
import io.rover.campaigns.experiences.data.domain.Experience
import io.rover.campaigns.experiences.data.http.HttpResultMapper
import io.rover.campaigns.experiences.data.operations.FetchExperienceRequest
import org.reactivestreams.Publisher
import java.net.URL

/**
 * Responsible for providing access the Rover cloud API, powered by GraphQL.
 */
internal class GraphQlApiService(
    private val endpoint: URL,
    private val accountToken: String?,
    private val httpClient: HttpClient,
    private val httpResultMapper: HttpResultMapper = HttpResultMapper()
) {
    private fun urlRequest(mutation: Boolean, queryParams: Map<String, String>): HttpRequest {
        val uri = Uri.parse(endpoint.toString())
        val builder = uri.buildUpon()
        queryParams.forEach { (k, v) -> builder.appendQueryParameter(k, v) }

        return HttpRequest(
            URL(builder.toString()),
            hashMapOf<String, String>().apply {
                if (mutation) {
                    this["Content-Type"] = "application/json"
                }

                when {
                    accountToken != null -> this["x-rover-account-token"] = accountToken
                }

                this.entries.forEach { (key, value) ->
                    this[key] = value
                }
            },
            if (mutation) {
                HttpVerb.POST
            } else {
                HttpVerb.GET
            }
        )
    }

    /**
     * Performs the given [GraphQlRequest] when subscribed and yields the result to the subscriber.
     */
    fun <TEntity> operation(request: GraphQlRequest<TEntity>): Publisher<ApiResult<TEntity>> {
        // TODO: once we change urlRequest() to use query parameters and GET for non-mutation
        // requests, replace true `below` with `request.mutation`.
        val urlRequest = urlRequest(request.mutation, request.encodeQueryParameters())
        val bodyData = request.encodeBody()

        log.v("going to make network request $urlRequest")

        return httpClient.request(urlRequest, bodyData).map { httpClientResponse ->
            httpResultMapper.mapResultWithBody(httpClientResponse) {
                request.decode(it)
            }
        }
    }

    /**
     * Retrieves the experience when subscribed and yields it to the subscriber.
     */
    fun fetchExperience(
        query: FetchExperienceRequest.ExperienceQueryIdentifier
    ): Publisher<ApiResult<Experience>> {
        return this.operation(
            FetchExperienceRequest(query)
        )
    }
}
