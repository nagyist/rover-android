/*
 * Copyright (c) 2023, Rover Labs, Inc. All rights reserved.
 * You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
 * copy, modify, and distribute this software in source code or binary form for use
 * in connection with the web services and APIs provided by Rover.
 *
 * This copyright notice shall be included in all copies or substantial portions of
 * the software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.rover.sdk.core.data.http

import org.reactivestreams.Publisher

interface NetworkClient {
    /**
     * Wen subscribed performs the given [HttpRequest] and then yields the result.
     *
     * Note that the subscriber is given an [HttpClientResponse], which includes readable streams.
     * Thus, it is called on the background worker thread to allow for client code to read those
     * streams, safely away from the Android main UI thread.
     *
     * @param gzip If true, the request's body will be gzipped.
     */
    fun request(
        request: HttpRequest,
        bodyData: String?,
        gzip: Boolean = false
    ): Publisher<HttpClientResponse>
}
