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

package io.rover.sdk.core.assets

import io.rover.sdk.core.data.http.HttpClientResponse
import io.rover.sdk.core.logging.log
import io.rover.sdk.core.streams.blockForResult
import java.io.BufferedInputStream
import java.lang.Exception
import java.net.URL

/**
 * Stream the asset from a remote HTTP API.
 * Be mindful that the stream downstream of this one must close the input streams once they are finished reading from them.
 * This never faults to anything further down in the pipeline; it always retrieves from the API.
 */
class AssetRetrievalStage(
    private val imageDownloader: ImageDownloader
) : SynchronousPipelineStage<URL, BufferedInputStream> {
    /**
     * Be sure to call [BufferedInputStream.close] once complete reading the stream.
     */
    override fun request(input: URL): PipelineStageResult<BufferedInputStream> {
        // so now I am going to just *block* while waiting for the callback, since this is all being
        // run on a background executor.
        val streamResult = try {
            imageDownloader
                .downloadStreamFromUrl(input)
                .blockForResult(timeoutSeconds = 300)
                .first()
        } catch (exception: Exception) {
            log.w("Unable to download asset because: ${exception.message}")
            return PipelineStageResult.Failed<BufferedInputStream>(exception)
        }

        // My use of blockForResult() here has an unfortunate side-effect: because downloadStreamFromUrl()
        // itself dispatches work to the ioExecutor, and does not yield the Publisher until that
        // work is complete, then blockForResult() can hang until its timeout elapses if the
        // Executor's thread pool is exhausted.  Thus, rather than work stacking up in the pool's
        // queue like you would expect, the whole chain will give the appearance of hanging, because
        // in fact because the pool's max size is exhausted by the SynchronousOperationNetworkTask
        // object that kicked off the whole process in the first place, meaning that the ioExecutor
        // cannot complete the work delegated to it in downloadStreamForUrl.

        // However, the workaround for now for this is reasonable enough: it is sufficiently safe to
        // ensure a very high max bound of threads in the ioExecutor pool, because their job is
        // primarily to multiplex I/O (well, with a bit of image decoding as well.).  In practice,
        // even with large experiences, it seems to work well.  The timeout given by
        // Publisher.blockForResult is a sufficient fail-safe against the pool overflow case.
        return when (streamResult) {
            is HttpClientResponse.ConnectionFailure -> {
                PipelineStageResult.Failed(
                    RuntimeException("Network or HTTP error downloading asset", streamResult.reason)
                )
            }
            is HttpClientResponse.ApplicationError -> {
                PipelineStageResult.Failed(
                    RuntimeException("Remote HTTP API error downloading asset (code ${streamResult.responseCode}): ${streamResult.reportedReason}")
                )
            }
            is HttpClientResponse.Success -> {
                // we have the stream! pass it downstream for decoding.
                PipelineStageResult.Successful(
                    streamResult.bufferedInputStream
                )
            }
        }
    }
}
